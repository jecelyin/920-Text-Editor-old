/*
 * Copyright 1994-2009 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.中文ok——
 */

package java.io;

import java.beans.ConstructorProperties;
import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.SecureRandom;
import sun.security.action.GetPropertyAction;


/**
 * An abstract representation of file and directory pathnames.
 *
 * <p> User interfaces and operating systems use system-dependent <em>pathname
 * strings</em> to name files and directories.  This class presents an
 * abstract, system-independent view of hierarchical pathnames.  An
 * <em>abstract pathname</em> has two components:
 *
 * <ol>
 * <li> An optional system-dependent <em>prefix</em> string,
 *      such as a disk-drive specifier, <code>"/"</code>&nbsp;for the UNIX root
 *      directory, or <code>"\\\\"</code>&nbsp;for a Microsoft Windows UNC pathname, and
 * <li> A sequence of zero or more string <em>names</em>.
 * </ol>
 *
 * The first name in an abstract pathname may be a directory name or, in the
 * case of Microsoft Windows UNC pathnames, a hostname.  Each subsequent name
 * in an abstract pathname denotes a directory; the last name may denote
 * either a directory or a file.  The <em>empty</em> abstract pathname has no
 * prefix and an empty name sequence.
 *
 * <p> The conversion of a pathname string to or from an abstract pathname is
 * inherently system-dependent.  When an abstract pathname is converted into a
 * pathname string, each name is separated from the next by a single copy of
 * the default <em>separator character</em>.  The default name-separator
 * character is defined by the system property <code>file.separator</code>, and
 * is made available in the public static fields <code>{@link
 * #separator}</code> and <code>{@link #separatorChar}</code> of this class.
 * When a pathname string is converted into an abstract pathname, the names
 * within it may be separated by the default name-separator character or by any
 * other name-separator character that is supported by the underlying system.
 *
 * <p> A pathname, whether abstract or in string form, may be either
 * <em>absolute</em> or <em>relative</em>.  An absolute pathname is complete in
 * that no other information is required in order to locate the file that it
 * denotes.  A relative pathname, in contrast, must be interpreted in terms of
 * information taken from some other pathname.  By default the classes in the
 * <code>java.io</code> package always resolve relative pathnames against the
 * current user directory.  This directory is named by the system property
 * <code>user.dir</code>, and is typically the directory in which the Java
 * virtual machine was invoked.
 *
 * <p> The <em>parent</em> of an abstract pathname may be obtained by invoking
 * the {@link #getParent} method of this class and consists of the pathname's
 * prefix and each name in the pathname's name sequence except for the last.
 * Each directory's absolute pathname is an ancestor of any <tt>File</tt>
 * object with an absolute abstract pathname which begins with the directory's
 * absolute pathname.  For example, the directory denoted by the abstract
 * pathname <tt>"/usr"</tt> is an ancestor of the directory denoted by the
 * pathname <tt>"/usr/local/bin"</tt>.
 *
 * <p> The prefix concept is used to handle root directories on UNIX platforms,
 * and drive specifiers, root directories and UNC pathnames on Microsoft Windows platforms,
 * as follows:
 *
 * <ul>
 *
 * <li> For UNIX platforms, the prefix of an absolute pathname is always
 * <code>"/"</code>.  Relative pathnames have no prefix.  The abstract pathname
 * denoting the root directory has the prefix <code>"/"</code> and an empty
 * name sequence.
 *
 * <li> For Microsoft Windows platforms, the prefix of a pathname that contains a drive
 * specifier consists of the drive letter followed by <code>":"</code> and
 * possibly followed by <code>"\\"</code> if the pathname is absolute.  The
 * prefix of a UNC pathname is <code>"\\\\"</code>; the hostname and the share
 * name are the first two names in the name sequence.  A relative pathname that
 * does not specify a drive has no prefix.
 *
 * </ul>
 *
 * <p> Instances of this class may or may not denote an actual file-system
 * object such as a file or a directory.  If it does denote such an object
 * then that object resides in a <i>partition</i>.  A partition is an
 * operating system-specific portion of storage for a file system.  A single
 * storage device (e.g. a physical disk-drive, flash memory, CD-ROM) may
 * contain multiple partitions.  The object, if any, will reside on the
 * partition <a name="partName">named</a> by some ancestor of the absolute
 * form of this pathname.
 *
 * <p> A file system may implement restrictions to certain operations on the
 * actual file-system object, such as reading, writing, and executing.  These
 * restrictions are collectively known as <i>access permissions</i>.  The file
 * system may have multiple sets of access permissions on a single object.
 * For example, one set may apply to the object's <i>owner</i>, and another
 * may apply to all other users.  The access permissions on an object may
 * cause some methods in this class to fail.
 *
 * <p> Instances of the <code>File</code> class are immutable; that is, once
 * created, the abstract pathname represented by a <code>File</code> object
 * will never change.
 *
 * <h4>Interoperability with {@code java.nio.file} package</h4>
 *
 * <p> The <a href="../../java/nio/file/package-summary.html">{@code java.nio.file}</a>
 * package defines interfaces and classes for the Java virtual machine to access
 * files, file attributes, and file systems. This API may be used to overcome
 * many of the limitations of the {@code java.io.File} class.
 * The {@link #toPath toPath} method may be used to obtain a {@link
 * Path} that uses the abstract path represented by a {@code File} object to
 * locate a file. The resulting {@code Path} provides more efficient and
 * extensive access to file attributes, additional file operations, and I/O
 * exceptions to help diagnose errors when an operation on a file fails.
 *
 * @author  unascribed
 * @since   JDK1.0
 */

public class File
    implements Serializable, Comparable<File>
{

    /**
     * The FileSystem object representing the platform's local file system.
     */
    static private FileSystem fs = FileSystem.getFileSystem();

    /**
     * This abstract pathname's normalized pathname string.  A normalized
     * pathname string uses the default name-separator character and does not
     * contain any duplicate or redundant separators.
     *
     * @serial
     */
    private String path;

    /**
     * The length of this abstract pathname's prefix, or zero if it has no
     * prefix.
     */
    private transient int prefixLength;

    /**
     * Returns the length of this abstract pathname's prefix.
     * For use by FileSystem classes.
     */
    int getPrefixLength() {
        return prefixLength;
    }

    /**
     * The system-dependent default name-separator character.  This field is
     * initialized to contain the first character of the value of the system
     * property <code>file.separator</code>.  On UNIX systems the value of this
     * field is <code>'/'</code>; on Microsoft Windows systems it is <code>'\\'</code>.
     *
     * @see     java.lang.System#getProperty(java.lang.String)
     */
    public static final char separatorChar = fs.getSeparator();

    /**
     * The system-dependent default name-separator character, represented as a
     * string for convenience.  This string contains a single character, namely
     * <code>{@link #separatorChar}</code>.
     */
    public static final String separator = "" + separatorChar;

    /**
     * The system-dependent path-separator character.  This field is
     * initialized to contain the first character of the value of the system
     * property <code>path.separator</code>.  This character is used to
     * separate filenames in a sequence of files given as a <em>path list</em>.
     * On UNIX systems, this character is <code>':'</code>; on Microsoft Windows systems it
     * is <code>';'</code>.
     *
     * @see     java.lang.System#getProperty(java.lang.String)
     */
    public static final char pathSeparatorChar = fs.getPathSeparator();

    /**
     * The system-dependent path-separator character, represented as a string
     * for convenience.  This string contains a single character, namely
     * <code>{@link #pathSeparatorChar}</code>.
     */
    public static final String pathSeparator = "" + pathSeparatorChar;


    /* -- Constructors -- */

    /**
     * Internal constructor for already-normalized pathname strings.
     */
    private File(String pathname, int prefixLength) {
        this.path = pathname;
        this.prefixLength = prefixLength;
    }

    /**
     * Internal constructor for already-normalized pathname strings.
     * The parameter order is used to disambiguate this method from the
     * public(File, String) constructor.
     */
    private File(String child, File parent) {
        assert parent.path != null;
        assert (!parent.path.equals(""));
        this.path = fs.resolve(parent.path, child);
        this.prefixLength = parent.prefixLength;
    }

    /**
     * Creates a new <code>File</code> instance by converting the given
     * pathname string into an abstract pathname.  If the given string is
     * the empty string, then the result is the empty abstract pathname.
     *
     * @param   pathname  A pathname string
     * @throws  NullPointerException
     *          If the <code>pathname</code> argument is <code>null</code>
     */
    @ConstructorProperties("path")
    public File(String pathname) {
        if (pathname == null) {
            throw new NullPointerException();
        }
        this.path = fs.normalize(pathname);
        this.prefixLength = fs.prefixLength(this.path);
    }

    /* Note: The two-argument File constructors do not interpret an empty
       parent abstract pathname as the current user directory.  An empty parent
       instead causes the child to be resolved against the system-dependent
       directory defined by the FileSystem.getDefaultParent method.  On Unix
       this default is "/", while on Microsoft Windows it is "\\".  This is required for
       compatibility with the original behavior of this class. */

    /**
     * Creates a new <code>File</code> instance from a parent pathname string
     * and a child pathname string.
     *
     * <p> If <code>parent</code> is <code>null</code> then the new
     * <code>File</code> instance is created as if by invoking the
     * single-argument <code>File</code> constructor on the given
     * <code>child</code> pathname string.
     *
     * <p> Otherwise the <code>parent</code> pathname string is taken to denote
     * a directory, and the <code>child</code> pathname string is taken to
     * denote either a directory or a file.  If the <code>child</code> pathname
     * string is absolute then it is converted into a relative pathname in a
     * system-dependent way.  If <code>parent</code> is the empty string then
     * the new <code>File</code> instance is created by converting
     * <code>child</code> into an abstract pathname and resolving the result
     * against a system-dependent default directory.  Otherwise each pathname
     * string is converted into an abstract pathname and the child abstract
     * pathname is resolved against the parent.
     *
     * @param   parent  The parent pathname string
     * @param   child   The child pathname string
     * @throws  NullPointerException
     *          If <code>child</code> is <code>null</code>
     */
    public File(String parent, String child) {
        if (child == null) {
            throw new NullPointerException();
        }
        if (parent != null) {
            if (parent.equals("")) {
                this.path = fs.resolve(fs.getDefaultParent(),
                                       fs.normalize(child));
            } else {
                this.path = fs.resolve(fs.normalize(parent),
                                       fs.normalize(child));
            }
        } else {
            this.path = fs.normalize(child);
        }
        this.prefixLength = fs.prefixLength(this.path);
    }

    /**
     * Creates a new <code>File</code> instance from a parent abstract
     * pathname and a child pathname string.
     *
     * <p> If <code>parent</code> is <code>null</code> then the new
     * <code>File</code> instance is created as if by invoking the
     * single-argument <code>File</code> constructor on the given
     * <code>child</code> pathname string.
     *
     * <p> Otherwise the <code>parent</code> abstract pathname is taken to
     * denote a directory, and the <code>child</code> pathname string is taken
     * to denote either a directory or a file.  If the <code>child</code>
     * pathname string is absolute then it is converted into a relative
     * pathname in a system-dependent way.  If <code>parent</code> is the empty
     * abstract pathname then the new <code>File</code> instance is created by
     * converting <code>child</code> into an abstract pathname and resolving
     * the result against a system-dependent default directory.  Otherwise each
     * pathname string is converted into an abstract pathname and the child
     * abstract pathname is resolved against the parent.
     *
     * @param   parent  The parent abstract pathname
     * @param   child   The child pathname string
     * @throws  NullPointerException
     *          If <code>child</code> is <code>null</code>
     */
    public File(File parent, String child) {
        if (child == null) {
            throw new NullPointerException();
        }
        if (parent != null) {
            if (parent.path.equals("")) {
                this.path = fs.resolve(fs.getDefaultParent(),
                                       fs.normalize(child));
            } else {
                this.path = fs.resolve(parent.path,
                                       fs.normalize(child));
            }
        } else {
            this.path = fs.normalize(child);
        }
        this.prefixLength = fs.prefixLength(this.path);
    }

    /**
     * Creates a new <tt>File</tt> instance by converting the given
     * <tt>file:</tt> URI into an abstract pathname.
     *
     * <p> The exact form of a <tt>file:</tt> URI is system-dependent, hence
     * the transformation performed by this constructor is also
     * system-dependent.
     *
     * <p> For a given abstract pathname <i>f</i> it is guaranteed that
     *
     * <blockquote><tt>
     * new File(</tt><i>&nbsp;f</i><tt>.{@link #toURI() toURI}()).equals(</tt><i>&nbsp;f</i><tt>.{@link #getAbsoluteFile() getAbsoluteFile}())
     * </tt></blockquote>
     *
     * so long as the original abstract pathname, the URI, and the new abstract
     * pathname are all created in (possibly different invocations of) the same
     * Java virtual machine.  This relationship typically does not hold,
     * however, when a <tt>file:</tt> URI that is created in a virtual machine
     * on one operating system is converted into an abstract pathname in a
     * virtual machine on a different operating system.
     *
     * @param  uri
     *         An absolute, hierarchical URI with a scheme equal to
     *         <tt>"file"</tt>, a non-empty path component, and undefined
     *         authority, query, and fragment components
     *
     * @throws  NullPointerException
     *          If <tt>uri</tt> is <tt>null</tt>
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameter do not hold
     *
     * @see #toURI()
     * @see java.net.URI
     * @since 1.4
     */
    public File(URI uri) {

        // Check our many preconditions
        if (!uri.isAbsolute())
            throw new IllegalArgumentException("URI is not absolute");
        if (uri.isOpaque())
            throw new IllegalArgumentException("URI is not hierarchical");
        String scheme = uri.getScheme();
        if ((scheme == null) || !scheme.equalsIgnoreCase("file"))
            throw new IllegalArgumentException("URI scheme is not \"file\"");
        if (uri.getAuthority() != null)
            throw new IllegalArgumentException("URI has an authority component");
        if (uri.getFragment() != null)
            throw new IllegalArgumentException("URI has a fragment component");
        if (uri.getQuery() != null)
            throw new IllegalArgumentException("URI has a query component");
        String p = uri.getPath();
        if (p.equals(""))
            throw new IllegalArgumentException("URI path component is empty");

        // Okay, now initialize
        p = fs.fromURIPath(p);
        if (File.separatorChar != '/')
            p = p.replace('/', File.separatorChar);
        this.path = fs.normalize(p);
        this.prefixLength = fs.prefixLength(this.path);
    }


    /* -- Path-component accessors -- */

    /**
     * Returns the name of the file or directory denoted by this abstract
     * pathname.  This is just the last name in the pathname's name
     * sequence.  If the pathname's name sequence is empty, then the empty
     * string is returned.
     *
     * @return  The name of the file or directory denoted by this abstract
     *          pathname, or the empty string if this pathname's name sequence
     *          is empty
     */
    public String getName() {
        int index = path.lastIndexOf(separatorChar);
        if (index < prefixLength) return path.substring(prefixLength);
        return path.substring(index + 1);
    }

    /**
     * Returns the pathname string of this abstract pathname's parent, or
     * <code>null</code> if this pathname does not name a parent directory.
     *
     * <p> The <em>parent</em> of an abstract pathname consists of the
     * pathname's prefix, if any, and each name in the pathname's name
     * sequence except for the last.  If the name sequence is empty then
     * the pathname does not name a parent directory.
     *
     * @return  The pathname string of the parent directory named by this
     *          abstract pathname, or <code>null</code> if this pathname
     *          does not name a parent
     */
    public String getParent() {
        int index = path.lastIndexOf(separatorChar);
        if (index < prefixLength) {
            if ((prefixLength > 0) && (path.length() > prefixLength))
                return path.substring(0, prefixLength);
            return null;
        }
        return path.substring(0, index);
    }

    /**
     * Returns the abstract pathname of this abstract pathname's parent,
     * or <code>null</code> if this pathname does not name a parent
     * directory.
     *
     * <p> The <em>parent</em> of an abstract pathname consists of the
     * pathname's prefix, if any, and each name in the pathname's name
     * sequence except for the last.  If the name sequence is empty then
     * the pathname does not name a parent directory.
     *
     * @return  The abstract pathname of the parent directory named by this
     *          abstract pathname, or <code>null</code> if this pathname
     *          does not name a parent
     *
     * @since 1.2
     */
    public File getParentFile() {
        String p = this.getParent();
        if (p == null) return null;
        return new File(p, this.prefixLength);
    }

    /**
     * Converts this abstract pathname into a pathname string.  The resulting
     * string uses the {@link #separator default name-separator character} to
     * separate the names in the name sequence.
     *
     * @return  The string form of this abstract pathname
     */
    public String getPath() {
        return path;
    }


    /* -- Path operations -- */

    /**
     * Tests whether this abstract pathname is absolute.  The definition of
     * absolute pathname is system dependent.  On UNIX systems, a pathname is
     * absolute if its prefix is <code>"/"</code>.  On Microsoft Windows systems, a
     * pathname is absolute if its prefix is a drive specifier followed by
     * <code>"\\"</code>, or if its prefix is <code>"\\\\"</code>.
     *
     * @return  <code>true</code> if this abstract pathname is absolute,
     *          <code>false</code> otherwise
     */
    public boolean isAbsolute() {
        return fs.isAbsolute(this);
    }

    /**
     * Returns the absolute pathname string of this abstract pathname.
     *
     * <p> If this abstract pathname is already absolute, then the pathname
     * string is simply returned as if by the <code>{@link #getPath}</code>
     * method.  If this abstract pathname is the empty abstract pathname then
     * the pathname string of the current user directory, which is named by the
     * system property <code>user.dir</code>, is returned.  Otherwise this
     * pathname is resolved in a system-dependent way.  On UNIX systems, a
     * relative pathname is made absolute by resolving it against the current
     * user directory.  On Microsoft Windows systems, a relative pathname is made absolute
     * by resolving it against the current directory of the drive named by the
     * pathname, if any; if not, it is resolved against the current user
     * directory.
     *
     * @return  The absolute pathname string denoting the same file or
     *          directory as this abstract pathname
     *
     * @throws  SecurityException
     *          If a required system property value cannot be accessed.
     *
     * @see     java.io.File#isAbsolute()
     */
    public String getAbsolutePath() {
        return fs.resolve(this);
    }

    /**
     * Returns the absolute form of this abstract pathname.  Equivalent to
     * <code>new&nbsp;File(this.{@link #getAbsolutePath})</code>.
     *
     * @return  The absolute abstract pathname denoting the same file or
     *          directory as this abstract pathname
     *
     * @throws  SecurityException
     *          If a required system property value cannot be accessed.
     *
     * @since 1.2
     */
    public File getAbsoluteFile() {
        String absPath = getAbsolutePath();
        return new File(absPath, fs.prefixLength(absPath));
    }

    /**
     * Returns the canonical pathname string of this abstract pathname.
     *
     * <p> A canonical pathname is both absolute and unique.  The precise
     * definition of canonical form is system-dependent.  This method first
     * converts this pathname to absolute form if necessary, as if by invoking the
     * {@link #getAbsolutePath} method, and then maps it to its unique form in a
     * system-dependent way.  This typically involves removing redundant names
     * such as <tt>"."</tt> and <tt>".."</tt> from the pathname, resolving
     * symbolic links (on UNIX platforms), and converting drive letters to a
     * standard case (on Microsoft Windows platforms).
     *
     * <p> Every pathname that denotes an existing file or directory has a
     * unique canonical form.  Every pathname that denotes a nonexistent file
     * or directory also has a unique canonical form.  The canonical form of
     * the pathname of a nonexistent file or directory may be different from
     * the canonical form of the same pathname after the file or directory is
     * created.  Similarly, the canonical form of the pathname of an existing
     * file or directory may be different from the canonical form of the same
     * pathname after the file or directory is deleted.
     *
     * @return  The canonical pathname string denoting the same file or
     *          directory as this abstract pathname
     *
     * @throws  IOException
     *          If an I/O error occurs, which is possible because the
     *          construction of the canonical pathname may require
     *          filesystem queries
     *
     * @throws  SecurityException
     *          If a required system property value cannot be accessed, or
     *          if a security manager exists and its <code>{@link
     *          java.lang.SecurityManager#checkRead}</code> method denies
     *          read access to the file
     *
     * @since   JDK1.1
     * @see     Path#toRealPath
     */
    public String getCanonicalPath() throws IOException {
        return fs.canonicalize(fs.resolve(this));
    }

    /**
     * Returns the canonical form of this abstract pathname.  Equivalent to
     * <code>new&nbsp;File(this.{@link #getCanonicalPath})</code>.
     *
     * @return  The canonical pathname string denoting the same file or
     *          directory as this abstract pathname
     *
     * @throws  IOException
     *          If an I/O error occurs, which is possible because the
     *          construction of the canonical pathname may require
     *          filesystem queries
     *
     * @throws  SecurityException
     *          If a required system property value cannot be accessed, or
     *          if a security manager exists and its <code>{@link
     *          java.lang.SecurityManager#checkRead}</code> method denies
     *          read access to the file
     *
     * @since 1.2
     * @see     Path#toRealPath
     */
    public File getCanonicalFile() throws IOException {
        String canonPath = getCanonicalPath();
        return new File(canonPath, fs.prefixLength(canonPath));
    }

    private static String slashify(String path, boolean isDirectory) {
        String p = path;
        if (File.separatorChar != '/')
            p = p.replace(File.separatorChar, '/');
        if (!p.startsWith("/"))
            p = "/" + p;
        if (!p.endsWith("/") && isDirectory)
            p = p + "/";
        return p;
    }
}

//END