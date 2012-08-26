<%@ taglib uri="/WEB-INF/dbforms.tld" prefix="db" %>

<%@ include file="include.jsp" %>
<%
  String success_url = request.getRequestURI();
  String user = (String) session.getAttribute("CLANuserXYZ");
  String level = (String) session.getAttribute("CLANlevelXYZ");
  String id = (String) session.getAttribute("CLANidXYZ");
  int counter = 0;
  if (user == null )
  {
    response.sendRedirect("servlet/login?success_url=" + success_url);
  }
  String header = "User: " + user;
  String filter = "id=" + id;
%>
<jsp:include page="header.jsp">
  <jsp:param name="title" value="<%= header %>" />
  <jsp:param name="SECURITY" value="Guests" />
</jsp:include>
<br />
<db:dbform multipart="false" autoUpdate="false" followUp="/page_profile.jsp" maxRows="1" filter="<%= filter %>" tableName="users">
  <db:header>
    <db:errors/>
    <table>
  </db:header>
  <db:body>
    <tr>
      <td>Nick:</td>
      <td></td>
      <td><db:textField styleClass="formitems" size="24" maxlength="24" fieldName="membername"/></td>
    </tr>
    <tr>
      <td>Function:</td>
      <td></td>
      <td><db:textField styleClass="formitems" size="32" maxlength="48" fieldName="userfunction"/></td>
    </tr>
    <tr>
      <td>Mail:</td>
      <td></td>
      <td><db:textField styleClass="formitems" size="32" maxlength="64" fieldName="email"/>
      <db:select styleClass="formitems" fieldName="email_show">
       <db:staticData name="emailEnabled">
        <db:staticDataItem key="0" value="Hide"/>
        <db:staticDataItem key="1" value="Show"/>
       </db:staticData>
      </db:select></td>
    </tr>
    <tr>
      <td>IM:</td>
      <td>
      <db:select styleClass="formitems" fieldName="im_type">
        <db:tableData name="imValue" foreignTable="ims" visibleFields="im" storeField="id" />
      </db:select></td>
      <td><db:textField styleClass="formitems" size="32" maxlength="64" fieldName="im_name"/>
      <db:select styleClass="formitems" fieldName="im_show">
       <db:staticData name="imEnabled">
        <db:staticDataItem key="0" value="Hide"/>
        <db:staticDataItem key="1" value="Show"/>
       </db:staticData>
      </db:select></td>
    </tr>
    <tr>
      <td>Firstname:</td>
      <td></td>
      <td><db:textField styleClass="formitems" size="32" maxlength="32" fieldName="firstname"/></td>
    </tr>
    <tr>
      <td>Birthday:</td>
      <td></td>
      <td><db:dateField styleClass="formitems" size="10" fieldName="birthday" pattern="<%=DATE_FORMAT%>"/>&nbsp;Format: <%=DATE_FORMAT%></td>
    </tr>
    <tr>
      <td>City:</td>
      <td></td>
      <td><db:textField styleClass="formitems" size="32" maxlength="32" fieldName="city"/></td>
    </tr>
    <tr>
      <td>Country:</td>
      <td></td>
      <td>
      <db:select styleClass="formitems" fieldName="country">
        <db:tableData name="countryValue" foreignTable="countries" visibleFields="long_name" storeField="short_name" />
      </db:select></td>
    </tr>
    <tr>
      <td>Phone:</td>
      <td></td>
      <td><db:textField styleClass="formitems" size="10" maxlength="16" fieldName="telephone"/></td>
    </tr>
    <tr>
      <td>Homepage:</td>
      <td></td>
      <td><db:textField styleClass="formitems" size="32" maxlength="96" fieldName="homepage"/></td>
    </tr>
    <tr>
      <td>Quote:</td>
      <td></td>
      <td><db:textField styleClass="formitems" size="32" maxlength="192" fieldName="quote"/></td>
    </tr>
    <tr>
      <td>Map(s):</td>
      <td></td>
      <td><db:textField styleClass="formitems" size="32" maxlength="32" fieldName="favmap"/></td>
    </tr>
    <tr>
      <td>Weapon(s):</td>
      <td></td>
      <td><db:textField styleClass="formitems" size="32" maxlength="32" fieldName="favwep"/></td>
    </tr>
    <tr>
      <td>CPU:</td>
      <td></td>
      <td><db:textField styleClass="formitems" size="32" maxlength="32" fieldName="cpu"/></td>
    </tr>
    <tr>
      <td>GFX:</td>
      <td></td>
      <td><db:textField styleClass="formitems" size="32" maxlength="72" fieldName="gfx"/></td>
    </tr>
    <tr>
      <td>OS:</td>
      <td></td>
      <td><db:textField styleClass="formitems" size="32" maxlength="32" fieldName="os"/></td>
    </tr>
    <tr>
      <td>Mouse:</td>
      <td></td>
      <td><db:textField styleClass="formitems" size="32" maxlength="72" fieldName="mouse"/></td>
    </tr>
    <tr>
      <td>Network:</td>
      <td></td>
      <td><db:textField styleClass="formitems" size="32" maxlength="32" fieldName="network"/></td>
    </tr>
  </db:body>
  <db:footer>
    </table>
    <div align="center">
    <hr color="#586273" size="0">
    <db:updateButton styleClass="formitems" caption="Update" showAlways="true"/>
    </div>
    <br />
  </db:footer>
</db:dbform>
<jsp:include page="footer.jsp" />
end