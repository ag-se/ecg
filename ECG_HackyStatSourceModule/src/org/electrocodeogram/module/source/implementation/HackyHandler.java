// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   HackyHandler.java

package org.electrocodeogram.module.source.implementation;

import java.io.*;
import java.util.*;
import org.electrocodeogram.misc.xml.ECGParser;
import org.electrocodeogram.misc.xml.NodeException;
import org.hackystat.kernel.util.StringListCodec;
import org.hackystat.kernel.util.StringListCodecException;
import org.mortbay.http.*;
import org.mortbay.http.handler.AbstractHttpHandler;
import org.mortbay.util.StringUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

// Referenced classes of package org.electrocodeogram.module.source.implementation:
//            HackyEventReader

public class HackyHandler extends AbstractHttpHandler
{

    public HackyHandler(HackyEventReader reader)
    {
        eventReader = reader;
    }

    public static void main(String args1[])
    {
    }

    public void handle(String pathInContext, String pathParams, HttpRequest request, HttpResponse response)
        throws HttpException, IOException
    {
        
        ByteArrayOutputStream buf = null;
        Writer writer = null;
        java.io.OutputStream out = null;
        
        try {
            
        
        
        StringBuffer buffer;
        if(!isStarted())
            return;
        if(!"GET".equals(request.getMethod()) && !"HEAD".equals(request.getMethod()) && !"POST".equals(request.getMethod()))
            return;
        reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        response.setField("Content-Type", "text/xml");
        out = response.getOutputStream();
        buf = new ByteArrayOutputStream(2048);
        writer = new OutputStreamWriter(buf, StringUtil.__ISO_8859_1);
        buffer = new StringBuffer();
        String line;
        while((line = reader.readLine()) != null) 
            buffer.append(line);
        org.w3c.dom.Node receiveNode;
        String type;
        Document document;
        
            document = ECGParser.parseAsDocument(buffer.toString(), "soap.xml");
       
        org.w3c.dom.Node bodyNode = ECGParser.getChildNode(document.getDocumentElement(), "SOAP-ENV:Body");
        receiveNode = ECGParser.getChildNode(bodyNode, "ns1:receive");
        org.w3c.dom.Node keyNode = ECGParser.getChildNode(receiveNode, "key");
        String key = ECGParser.getNodeValue(keyNode);
        org.w3c.dom.Node typeNode = ECGParser.getChildNode(receiveNode, "type");
        type = ECGParser.getNodeValue(typeNode);
        if(type.equals("Ping"))
        {
            writer.write("<?xml version='1.0' encoding='UTF-8'?>\r\n<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/1999/XMLSchema\">\r\n<SOAP-ENV:Body>\r\n<ns1:receiveResponse xmlns:ns1=\"urn:hackystat.SoapNotification\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\r\n<return xsi:type=\"xsd:string\">OK</return>\r\n</ns1:receiveResponse>\r\n\r\n</SOAP-ENV:Body>\r\n</SOAP-ENV:Envelope>\r\n\r\n");
            writer.flush();
            buf.writeTo(out);
            writer.close();
            reader.close();
            request.setHandled(true);
            return;
        }
       
            if(type.equals("Sensor"))
            {
                org.w3c.dom.Node dataNode = ECGParser.getChildNode(receiveNode, "data");
                String data = ECGParser.getNodeValue(dataNode);
                ArrayList decodedData = StringListCodec.decode(data);
                for(Iterator i = decodedData.iterator(); i.hasNext();)
                {
                    String entryString = (String)i.next();
                    List notificationStringList = StringListCodec.decode(entryString);
                    String sdtName = (String)notificationStringList.get(0);
                    List attributeList = notificationStringList.subList(1, notificationStringList.size());
                    System.out.println((new StringBuilder()).append("SDT: ").append(sdtName).toString());
                    Iterator i$ = attributeList.iterator();
                    while(i$.hasNext()) 
                    {
                        Object entry = i$.next();
                        System.out.println((String)entry);
                    }
                }

            }
        }
        catch(SAXException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(NodeException e)
        {
            e.printStackTrace();
        }
        catch(StringListCodecException e)
        {
            e.printStackTrace();
        }
       
        writer.write("<?xml version='1.0' encoding='UTF-8'?>\r\n<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/1999/XMLSchema\">\r\n<SOAP-ENV:Body>\r\n<ns1:receiveResponse xmlns:ns1=\"urn:hackystat.SoapNotification\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\r\n<return xsi:type=\"xsd:string\">OK</return>\r\n</ns1:receiveResponse>\r\n\r\n</SOAP-ENV:Body>\r\n</SOAP-ENV:Envelope>\r\n\r\n");
        writer.flush();
        buf.writeTo(out);
        writer.close();
        reader.close();
        request.setHandled(true);
        return;
    }

    private HackyEventReader eventReader;
    BufferedReader reader;
    private static final String HACKY_OK = "<?xml version='1.0' encoding='UTF-8'?>\r\n<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/1999/XMLSchema\">\r\n<SOAP-ENV:Body>\r\n<ns1:receiveResponse xmlns:ns1=\"urn:hackystat.SoapNotification\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\r\n<return xsi:type=\"xsd:string\">OK</return>\r\n</ns1:receiveResponse>\r\n\r\n</SOAP-ENV:Body>\r\n</SOAP-ENV:Envelope>\r\n\r\n";
}
