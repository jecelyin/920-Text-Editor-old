<%@ page import="java.util.List" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.io.File" %>
<%@ page import="java.lang.String" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="org.apache.commons.codec.binary.Base64" %>
<%@ page import="org.apache.commons.fileupload.servlet.ServletFileUpload"%>
<%@ page import="org.apache.commons.fileupload.disk.DiskFileItemFactory"%>
<%@ page import="org.apache.commons.fileupload.*"%>
<%@ page import="org.apache.axis2.client.Options" %>
<%@ page import="org.apache.axis2.client.ServiceClient" %>
<%@ page import="org.apache.axis2.addressing.EndpointReference" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.axiom.om.OMFactory" %>
<%@ page import="org.apache.axiom.om.OMAbstractFactory" %>
<%@ page import="org.apache.axiom.om.OMText" %>
<%@ page import="javax.xml.stream.XMLStreamReader"%>
<%@ page import="javax.xml.stream.XMLInputFactory"%>
<%@ page import="org.apache.axiom.om.impl.builder.StAXOMBuilder"%>
<%@ page import="java.io.StringReader"%>
<%@ page import="org.apache.commons.httpclient.protocol.ProtocolSocketFactory"%>
<%@ page import="org.apache.commons.httpclient.protocol.Protocol"%>
<%@ page import="org.apache.axiom.soap.SOAP11Constants"%>
<%@ page import="org.apache.axiom.soap.SOAP12Constants"%>
<%@ page import="org.apache.axis2.Constants"%>
<%@ page import="org.apache.axis2.description.WSDL2Constants"%>
<%@ page import="org.apache.axis2.util.JavaUtils"%>
<%@ page import="org.apache.axis2.addressing.AddressingConstants"%>
<%@ page import="org.apache.axis2.transport.http.HttpTransportProperties"%>
<%@ page import="org.apache.axis2.transport.http.HTTPConstants"%>
<%@ page import="org.apache.axis2.AxisFault"%>
<%@ page import="org.apache.axis2.context.OperationContext"%>
<%@ page import="org.apache.axis2.context.MessageContext"%>
<%@ page import="org.apache.axiom.soap.SOAPEnvelope"%>
<%@ page import="org.apache.axiom.om.OMNamespace"%>
<%@ page contentType="text/plain" language="java" %>

<%
boolean isMultipart = ServletFileUpload.isMultipartContent(request);
if (!isMultipart) {
} else {
    FileItemFactory factory = new DiskFileItemFactory();
    ServletFileUpload upload = new ServletFileUpload(factory);
    List items = null;
    String packageName = null;
    try {
        items = upload.parseRequest(request);
    } catch (FileUploadException e) {
        e.printStackTrace();
    }
    Iterator itr = items.iterator();
    while (itr.hasNext()) {
        FileItem item = (FileItem) itr.next();
        if (item.isFormField()) {
            if(item.getFieldName().equals("fileName")){
                packageName = item.getString();
            }
        } else {
            try {
                String itemName = item.getName();
                String extension = itemName.toLowerCase();
                if(extension.endsWith(".zip")){
                    long  size = item.getSize();
                    InputStream is = item.getInputStream();
                    byte[] bytes = new byte[(int)size];
                    int offset = 0;
                    int numRead = 0;
                    while (offset < bytes.length
                        && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
                        offset += numRead;
                    }
                    if (offset < bytes.length) {
                        out.println("Overflow Error Occurred!");
                    }else{
                        if(!Base64.isArrayByteBase64(bytes)){
                            byte[] encodedBytes  = Base64.encodeBase64(bytes);
                            String encodedString = new String(encodedBytes);
                            Options opts = new Options();
                            opts.setAction("http://www.apache.org/ode/deployapi/DeploymentPortType/deployRequest");
                            opts.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
                            opts.setProperty(Constants.Configuration.HTTP_METHOD,
                                Constants.Configuration.HTTP_METHOD_POST);
                            opts.setTo(new EndpointReference("http://localhost:8080/ode/processes/DeploymentService"));

                            OMElement payload = null;
                            OMFactory omFactory = OMAbstractFactory.getOMFactory();
                            OMNamespace ns = omFactory.createOMNamespace("http://www.apache.org/ode/pmapi","p");
                            payload = omFactory.createOMElement("deploy", ns);
                            OMElement name = omFactory.createOMElement("name", ns);
                            OMElement packageCont = omFactory.createOMElement("package", ns);
                            OMElement zipEle = omFactory.createOMElement("zip", ns);
                            if(packageName != null && encodedString != null){
                                OMText nameText = omFactory.createOMText(name, packageName);
                                OMText packageText = omFactory.createOMText(zipEle, encodedString);
                                packageCont.addChild(zipEle);
                                payload.addChild(name);
                                payload.addChild(packageCont);

                                //creating service client
                                ServiceClient sc = new ServiceClient();
                                sc.setOptions(opts);

                                try {
                                    //invoke service
                                    OMElement responseMsg = sc.sendReceive(payload);
                                    String body = responseMsg.toString();
                                    if(body.indexOf("name") > 0){
                                        out.println("Package deployed successfully!");
                                    }else{
                                        out.println("Package deployement failed!");
                                    }
                                } catch (AxisFault axisFault) {
                                    out.println("Axis2 Fault Occurred while Sending the request!");
                                }
                            }else{
                                out.println("No package Name specified!");
                                break;
                            }
                        }else{
                            out.println("TODO: Implement Base64 encoded string support!");
                        }
                    }

                }else{
                    out.write("Wrong input format. Inout file must be zip archive!");
                }
            } catch (Exception e) {
                out.println(e);
                out.println("Exception occuured while processing the file upload request!");
            }
        }
    }
}
%>
//END