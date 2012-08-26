# Copyright (c) 2002-2006 Infrae. All rights reserved.
# See also LICENSE.txt中文ok——
# $Revision: 1.101 $

from zope.interface import implements

# Zope
from OFS import SimpleItem
from AccessControl import ClassSecurityInfo
from Globals import InitializeClass
from Products.PageTemplates.PageTemplateFile import PageTemplateFile
from DateTime import DateTime

# Silva
from VersionedContent import CatalogedVersionedContent
from Version import CatalogedVersion
from Products.Silva import mangle
from Products.Silva.i18n import translate as _
import SilvaPermissions
from adapters.path import PathAdapter
# misc
from helpers import add_and_edit
import urlparse

from interfaces import \
    IVersionedContent, IContainer, IVersion, IContent, IGhost, \
    IGhostContent, IIcon

icon = "www/silvaghost.gif"

class GhostBase:
    """baseclas for Ghosts (or Ghost versions if it's versioned)
    """
    security = ClassSecurityInfo()

    # status codes as returned by get_link_status
    # NOTE: LINK_FOLDER (and alike) must *only* be returned if it is an error
    # for the link to point to a folder. If it is not an error LINK_OK must
    # be returned.
    LINK_OK = None   # link is ok
    LINK_EMPTY = 1   # no link entered (XXX this cannot happen)
    LINK_VOID = 2    # object pointed to does not exist
    LINK_FOLDER = 3  # link points to folder
    LINK_GHOST = 4   # link points to another ghost
    LINK_NO_CONTENT = 5 # link points to something which is not a content
    LINK_CONTENT = 6 # link points to content
    LINK_NO_FOLDER = 7 # link doesn't point to a folder
    LINK_CIRC = 8 # Link results in a ghost haunting itself

    # those should go away
    security.declareProtected(SilvaPermissions.ChangeSilvaContent,
                              'set_title')
    def set_title(self, title):
        """Don't do a thing.
        """
        pass

    security.declareProtected(SilvaPermissions.AccessContentsInformation,
                              'get_title')
    def get_title(self):
        """Get title.
        """
        content = self.get_haunted_unrestricted()
        if content is None:
            return ("Ghost target is broken")
        else:
            return content.get_title()

    def get_title_editable(self):
        """Get title.
        """
        content = self.get_haunted_unrestricted()
        if content is None:
            return ("Ghost target is broken")
        else:
            return content.get_title_editable()


    security.declareProtected(
        SilvaPermissions.AccessContentsInformation, 'get_short_title')
    def get_short_title(self):
        """Get short title.
        """
        content = self.get_haunted_unrestricted()
        if content is None:
            return ("Ghost target is broken")
        else:
            short_title = content.get_short_title()
        if not short_title:
            return self.get_title()
        return short_title
    # /those should go away



    security.declareProtected(
        SilvaPermissions.ChangeSilvaContent, 'set_haunted_url')
    def set_haunted_url(self, content_url):
        """Set content url.
        """
        pad = PathAdapter(self.REQUEST)
        path = pad.urlToPath(content_url)

        path_elements = path.split('/')

        # Cut off 'edit' and anything after it
        try:
            idx = path_elements.index('edit')
        except ValueError:
            pass
        else:
            path_elements = path_elements[:idx]

        if path_elements[0] == '':
            traversal_root = self.get_root()
        else:
            traversal_root = self.get_container()

        # Now resolve it...
        target = traversal_root.unrestrictedTraverse(path_elements, None)
        if target is None:

            (scheme, netloc, path, parameters, query, fragment) = \
                                            urlparse.urlparse(content_url)
            self._content_path = path.split('/')
        else:
            # ...and get physical path for it
            self._content_path = target.getPhysicalPath()

    security.declareProtected(SilvaPermissions.View, 'get_haunted_url')
    def get_haunted_url(self):
        """Get content url.
        """
        if self._content_path is None:
            return None

        object = self.get_root().unrestrictedTraverse(self._content_path, None)
        if object is None:
            return '/'.join(self._content_path)

        pad = PathAdapter(self.REQUEST)
        url = pad.pathToUrlPath('/'.join(object.getPhysicalPath()))
        return url

    security.declareProtected(
        SilvaPermissions.ChangeSilvaContent, 'haunted_path')
    def haunted_path(self):
        return self._content_path

    security.declareProtected(SilvaPermissions.View,'get_link_status')
    def get_link_status(self, content=None):
        """return an error code if this version of the ghost is broken.
        returning None means the ghost is Ok.
        """
        raise NotImplementedError, "implemented in subclasses"

    def _get_object_at(self, path, check=1):
        """Get content object for a url.
        """
        # temporary tuck away old skin & resourcebase, so we don't
        # screw up any subsequence view lookups with traversal
        request = self.REQUEST
        current_skin = request.getPresentationSkin()
        current_resourcebase = request.get('resourcebase')
        try:
            # XXX what if we're pointing to something that cannot be viewed
            # publically?
            if path is None:
                return None
            content = self.aq_inner.aq_parent.unrestrictedTraverse(path, None)
            if content is None:
                return None
            # check if it's valid
            valid = None
            if check:
                valid = self.get_link_status(content)
            if valid is None:
                return content
            return None
        finally:
            # put back the old skin & resourcebase...
            request.setPresentationSkin(current_skin)
            request.set('resourcebase', current_resourcebase)

    security.declarePrivate('get_haunted_unrestricted')
    def get_haunted_unrestricted(self, check=1):
        """Get the real content object.
        """
        return self._get_object_at(self._content_path, check)

    security.declareProtected(SilvaPermissions.View,'get_haunted')
    def get_haunted(self):
        """get the real content object; using restrictedTraverse

            returns content object, or None on traversal failure.
        """
        path = self._content_path
        return self.restrictedTraverse(path, None)

    def render_preview(self):
        """Render preview of this version (which is what we point at)
        """
        return self.render_view()

    def render_view(self):
        """Render view of this version (which is what we point at)
        """
        # FIXME what if content is None?
        # what if we get circular ghosts?
        self.REQUEST.set('ghost_model', self.aq_inner)
        content = self.get_haunted_unrestricted()
        if content is None:
            # public render code of ghost should give broken message
            return None
        if not content.get_viewable():
            return None
        user = self.REQUEST.AUTHENTICATED_USER
        permission = 'View'
        if user.has_permission(permission, content):
            return content.view()
        else:
            raise "Unauthorized"

class Ghost(CatalogedVersionedContent):
    __doc__ = _("""Ghosts are special documents that function as a
       placeholder for an item in another location (like an alias,
       symbolic link, shortcut). Unlike a hyperlink, which takes the
       Visitor to another location, a ghost object keeps the Visitor in the
       current publication, and presents the content of the ghosted item.
       The ghost inherits properties from its location (e.g. layout
       and stylesheets).
    """)

    security = ClassSecurityInfo()

    meta_type = "Silva Ghost"

    implements(IVersionedContent, IGhostContent)

    def __init__(self, id):
        Ghost.inheritedAttribute('__init__')(self, id)

    security.declareProtected(SilvaPermissions.ReadSilvaContent,
                              'to_xml')
    def to_xml(self, context):
        if context.last_version == 1:
            version_id = self.get_next_version()
            if version_id is None:
                version_id = self.get_public_version()
        else:
            version_id = self.get_public_version()
        if version_id is None:
            return
        version = getattr(self, version_id)
        content = version.get_haunted_unrestricted()
        if content is None:
            return
        content.to_xml(context)

    def get_title_editable(self):
        """Get title for editable or previewable use
        """
        # Ask for 'previewable', which will return either the 'next version'
        # (which may be under edit, or is approved), or the public version,
        # or, as a last resort, the closed version.
        # This to be able to show at least some title in the Silva edit
        # screens.
        previewable = self.get_previewable()
        if previewable is None:
            return "[No title available]"
        return previewable.get_title_editable()

    security.declarePrivate('getLastVersion')
    def getLastVersion(self):
        """returns `latest' version of ghost

            ghost: Silva Ghost intance
            returns GhostVersion
        """
        version_id = self.get_public_version()
        if version_id is None:
            version_id = self.get_next_version()
        if version_id is None:
            version_id = self.get_last_closed_version()
        version = getattr(self, version_id)
        return version

    security.declareProtected(SilvaPermissions.View, 'get_haunted_url')
    def get_haunted_url(self):
        """return content url of `last' version"""
        version = self.getLastVersion()
        return version.get_haunted_url()

    security.declareProtected(SilvaPermissions.AccessContentsInformation,
                                'is_version_published')
    def is_version_published(self):
        public_id = self.get_public_version()
        if not public_id:
            return False
        public = getattr(self, public_id)
        haunted = public.get_haunted_unrestricted()
        if haunted is None:
            return False
        return haunted.is_published()

    def _factory(self, container, id, content_url):
        return container.manage_addProduct['Silva'].manage_addGhost(id,
            content_url)


InitializeClass(Ghost)

class GhostVersion(GhostBase, CatalogedVersion):
    """Ghost version.
    """
    meta_type = 'Silva Ghost Version'

    security = ClassSecurityInfo()

    def __init__(self, id):
        GhostVersion.inheritedAttribute('__init__')(
            self, id, '[Ghost title bug]')
        self._content_path = None

    security.declareProtected(
        SilvaPermissions.AccessContentsInformation, 'fulltext')
    def fulltext(self):
       target = self.get_haunted_unrestricted()
       if target:
           public_version = target.get_viewable()
           if public_version and hasattr(public_version.aq_inner, 'fulltext'):
               return public_version.fulltext()
       return ""

    security.declareProtected(SilvaPermissions.View, 'get_link_status')
    def get_link_status(self, content=None):
        """return an error code if this version of the ghost is broken.
        returning None means the ghost is Ok.
        """
        if content is None:
            content = self.get_haunted_unrestricted(check=0)
        if self._content_path is None:
            return self.LINK_EMPTY
        if content is None:
            return self.LINK_VOID
        if IContainer.providedBy(content):
            return self.LINK_FOLDER
        if not IContent.providedBy(content):
            return self.LINK_NO_CONTENT
        if IGhost.providedBy(content):
            return self.LINK_GHOST
        return self.LINK_OK

manage_addGhostForm = PageTemplateFile("www/ghostAdd", globals(),
                                       __name__='manage_addGhostForm')

def manage_addGhost(self, id, content_url, REQUEST=None):
    """Add a Ghost."""
    if not mangle.Id(self, id).isValid():
        return
    object = Ghost(id)
    self._setObject(id, object)
    object = getattr(self, id)
    # add first version
    object._setObject('0', GhostVersion('0'))
    # we need to set content url after we created version, not
    # in constructor, as getPhysicalRoot() won't work there
    getattr(object, '0').set_haunted_url(content_url)
    object.create_version('0', None, None)
    add_and_edit(self, id, REQUEST)
    return ''

manage_addGhostVersionForm = PageTemplateFile("www/ghostversionAdd", globals(),
                                              __name__='manage_addGhostVersionForm')

def manage_addGhostVersion(self, id,REQUEST=None):
    """Add a Ghost version."""
    object = GhostVersion(id)
    self._setObject(id, object)
    object.set_haunted_url(content_url)
    add_and_edit(self, id, REQUEST)
    return ''


def ghostFactory(container, id, haunted_object):
    """add new ghost to container

        container: container to add ghost to (must be acquisition wrapped)
        id: (str) id for new ghost in container
        haunted_object: object to be haunted (ghosted), acquisition wrapped
        returns created ghost

        actual ghost created depends on haunted object
        on IContainer a GhostFolder is created
        on IVersionedContent a Ghost is created

        willem suggested to call this function electricChair, but well...
    """
    addProduct = container.manage_addProduct['Silva']
    content_url = '/'.join(haunted_object.getPhysicalPath())
    if IContainer.providedBy(haunted_object):
        factory = addProduct.manage_addGhostFolder
    elif IContent.providedBy(haunted_object):
        if haunted_object.meta_type == 'Silva Ghost':
            version = getLastVersionFromGhost(haunted_object)
            content_url = version.get_haunted_url()
        factory = addProduct.manage_addGhost
    factory(id, content_url)
    ghost = getattr(container, id)
    return ghost


def canBeHaunted(to_be_haunted):
    if IGhost.providedBy(to_be_haunted):
        return 0
    if (IContainer.providedBy(to_be_haunted) or
            IContent.providedBy(to_be_haunted)):
        return 1
    return 0

#END