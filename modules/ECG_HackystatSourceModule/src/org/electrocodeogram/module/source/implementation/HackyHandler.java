// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   HackyHandler.java

package org.electrocodeogram.module.source.implementation;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.logging.LogHelper.ECGLevel;
import org.electrocodeogram.misc.xml.ECGParser;
import org.electrocodeogram.misc.xml.NodeException;
import org.hackystat.kernel.util.StringListCodec;
import org.hackystat.kernel.util.StringListCodecException;
import org.mortbay.http.*;
import org.mortbay.http.handler.AbstractHttpHandler;
import org.mortbay.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

// Referenced classes of package org.electrocodeogram.module.source.implementation:
//            HackyEventReader

public class HackyHandler extends AbstractHttpHandler {

    private static Logger logger = LogHelper.createLogger(HackyHandler.class.getName());
    
    private HackyEventReader eventReader;

    private BufferedReader reader;

    private ByteArrayOutputStream buf;
    
    private Writer writer;
    
    private java.io.OutputStream out;
    
    /** The maximum length of any individual string to be encoded. */
    public static final int MAX_STRING_LENGTH = 99999;
    /** The DecimalFormat pattern for this max size. */
    private static final String STRING_LENGTH_PATTERN = "00000";
    /** The number of characters used to represent length field. */
    private static final int STRING_LENGTH_FIELD_LENGTH =
        HackyHandler.STRING_LENGTH_PATTERN.length();

    /** The maximum number of strings that can be encoded. */
    public static final int MAX_NUM_STRINGS = 9999;
    /** The DecimalFormat pattern for this max strings. */
    private static final String NUM_STRINGS_PATTERN = "0000";
    /** The number of characters used to represent the total number of encoded strings. */
    private static final int NUM_STRINGS_FIELD_LENGTH = HackyHandler.NUM_STRINGS_PATTERN.length();
    
    private static final String HACKY_OK = "<?xml version='1.0' encoding='UTF-8'?>\r\n<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/1999/XMLSchema\">\r\n<SOAP-ENV:Body>\r\n<ns1:receiveResponse xmlns:ns1=\"urn:hackystat.SoapNotification\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\r\n<return xsi:type=\"xsd:string\">OK</return>\r\n</ns1:receiveResponse>\r\n\r\n</SOAP-ENV:Body>\r\n</SOAP-ENV:Envelope>\r\n\r\n";

    
    public HackyHandler(HackyEventReader reader) {
        
        this.eventReader = reader;
        
        this.eventReader.startReader();
    }

    public void handle(String pathInContext, String pathParams,
        HttpRequest request, HttpResponse response) throws HttpException,
        IOException {

        this.out = response.getOutputStream();
        
        this.buf = new ByteArrayOutputStream(2048);
        
        this.writer = new OutputStreamWriter(this.buf, StringUtil.__ISO_8859_1);
        
        response.setField("Content-Type", "text/xml");

        StringBuffer buffer = null;
        
        try {

            
            
            if (!isStarted())
            {
                return;
            }
            
            if (!"GET".equals(request.getMethod())
                && !"HEAD".equals(request.getMethod())
                && !"POST".equals(request.getMethod()))
            {
                return;
            }
            
            
            buffer = readRequest(request);
            
            logger.log(Level.INFO,"Before parsing");
            
            Document document = ECGParser.parseAsDocument(buffer.toString(), "soap.xml");
            
            logger.log(Level.INFO,"After parsing");
            
            String type = getMessageType(document);
            
                     
            if (type.equals("Ping")) {
                
                sendOK();
                
                request.setHandled(true);
                
                return;
            }

            if (type.equals("Sensor")) {
                                
                
                getSensorData(document);
                
                sendOK();
                
                request.setHandled(true);
                
                return;
            }
            
        } catch (SAXException e) {
            
            logger.log(Level.FINE,"The incoming request from" + request.getRemoteAddr() + "was not a SOAP message.");
            
            if(buffer != null)
            {
                logger.log(ECGLevel.PACKET,buffer.toString());
            }
                        
            logger.log(Level.FINE,e.getMessage());
            
        } catch (IOException e) {
            
            logger.log(Level.WARNING,"Error while reading the request.");
            
            logger.log(Level.WARNING,e.getMessage());
            
        } catch (NodeException e) {

            logger.log(Level.WARNING,"Error while parsing the SOAP message.");
            
            logger.log(Level.WARNING,e.getMessage());
            
        } catch (StringListCodecException e) {
            
            logger.log(Level.WARNING,"Error while decoding the Hackystat data entries.");
            
            logger.log(Level.WARNING,e.getMessage());
            
        } catch (IllegalEventParameterException e) {
            logger.log(Level.WARNING,"Error while creating an ECG event from request.");
            
            logger.log(Level.WARNING,e.getMessage());
        }
    }

    private String getMessageType(Document document) throws SAXException, IOException, NodeException
    {
        
        
        Node receiveNode;
        
        String type;
        
        Node bodyNode = ECGParser.getChildNode(document
            .getDocumentElement(), "SOAP-ENV:Body");
        
        receiveNode = ECGParser.getChildNode(bodyNode, "ns1:receive");

        Node typeNode = ECGParser.getChildNode(receiveNode,
            "type");
        
        type = ECGParser.getNodeValue(typeNode);
        
        return type;
    }
    
    /**
     * @param receiveNode
     * @throws NodeException
     * @throws StringListCodecException
     * @throws IllegalEventParameterException 
     */
    private void getSensorData(Document document) throws NodeException, StringListCodecException, IllegalEventParameterException {
        
        logger.log(Level.INFO,"Getting Sensor Data");
        
        Node receiveNode;
        
        Node bodyNode = ECGParser.getChildNode(document
            .getDocumentElement(), "SOAP-ENV:Body");
        
        receiveNode = ECGParser.getChildNode(bodyNode, "ns1:receive");
        
        
        Node dataNode = ECGParser.getChildNode(receiveNode,
            "data");
        
        String data = ECGParser.getNodeValue(dataNode);
        
        logger.log(Level.INFO,"Got Data Node");
        
        ArrayList decodedData = HackyHandler.decode(data);
        
        logger.log(Level.INFO,"Decoded");
        
        Date timestamp = null;
        
        String sdtName = "";
        
        List argList;
        
        List attributeList = null;
               
        for (Iterator i = decodedData.iterator(); i.hasNext();) {
        
            String entryString = (String) i.next();
            
            List notificationStringList = HackyHandler
                .decode(entryString);
            
            logger.log(Level.INFO,"Got notificationStringList");
            
            sdtName = (String) notificationStringList.get(0);
            
            logger.log(Level.INFO,"SDT: " + sdtName);
            
            attributeList = notificationStringList.subList(1,
                notificationStringList.size());
     
            logger.log(Level.INFO,"Got attributeList");
            
            long ts = Long.parseLong((String) attributeList.get(0));
            
            timestamp = new Date(ts);
            
            logger.log(Level.INFO,"Got timestamp");
            
            argList = attributeList.subList(2,attributeList.size());
                                            
            logger.log(Level.INFO,"Got argList");
            
            argList.add(0,"add");
            
            WellFormedEventPacket event = new WellFormedEventPacket(timestamp, sdtName, argList);
                        
            logger.log(ECGLevel.INFO,event.toString());
            
            this.eventReader.add(event);
            
        }
        
     
        
     
    }

    
    public static ArrayList decode(String encodedString) throws StringListCodecException {
        
        int size = encodedString.length();
        
        //replace all occurences of "\r", "\r\n" with "\n"
        encodedString = encodedString.replaceAll("\r\n", "\n").replace('\r', '\n');
        
        size = encodedString.length();
        
        // Get the number of fields to be decoded.
        int numFields;
        try {
          numFields = Integer.parseInt(encodedString.substring(0, NUM_STRINGS_FIELD_LENGTH));
        }
        catch (Exception e) {
          throw new StringListCodecException("Error decoding numFields: " + encodedString);
        }
        // Make an array list to hold this number of elements.
        ArrayList stringList = new ArrayList(numFields);
        // Cursor always holds the index of next character to be processed in string.
        int cursor = NUM_STRINGS_FIELD_LENGTH;
        // Loop through the specified number of fields, extracting the field length and string,
        // and incrementing cursor.
        for (int i = 0; i < numFields; i++) {
          // First, get the field length.
          int fieldLength;
          String field;
          try {
            fieldLength = Integer.parseInt(
                encodedString.substring(cursor, cursor + STRING_LENGTH_FIELD_LENGTH));
          }
          catch (Exception e) {
            throw new StringListCodecException("Parse failed for field " + i
                                               + " and string " + encodedString, e);
          }

          // Second, extract that substring
          cursor += STRING_LENGTH_FIELD_LENGTH;
          try {
            field = encodedString.substring(cursor, cursor + fieldLength);
          }
          catch (Exception e) {
            throw new StringListCodecException("Could not extract field " + i + "from string "
                                               + encodedString, e);
          }

          // Third, add the field to the list, and increment the cursor.
          stringList.add(field);
          cursor += fieldLength;
        }

        // Make sure we've consumed the entire string.
        if (cursor != encodedString.length()) {
          throw new StringListCodecException("Encoded string too long: " + encodedString);
        }

        // We've extracted all of the fields, so now return the list.
        return stringList;
      }
    
    
    /**
     * @param buf
     * @param writer
     * @param out
     * @throws IOException
     */
    private void sendOK() throws IOException {
        //this.writer.write("<?xml version='1.0' encoding='UTF-8'?>\r\n<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/1999/XMLSchema\">\r\n<SOAP-ENV:Body>\r\n<ns1:receiveResponse xmlns:ns1=\"urn:hackystat.SoapNotification\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\r\n<return xsi:type=\"xsd:string\">OK</return>\r\n</ns1:receiveResponse>\r\n\r\n</SOAP-ENV:Body>\r\n</SOAP-ENV:Envelope>\r\n\r\n");
        
        this.writer.write(HACKY_OK);
        
        this.writer.flush();
        
        this.buf.writeTo(this.out);
        
        this.writer.close();
        
    }

    /**
     * @param request
     * @return
     * @throws IOException
     */
    private StringBuffer readRequest(HttpRequest request) throws IOException {
        StringBuffer buffer;
        this.reader = new BufferedReader(new InputStreamReader(request
            .getInputStream()));
        
        buffer = new StringBuffer();
        
        String line;
        
        while ((line = this.reader.readLine()) != null)
        {
            buffer.append(line + "\n");
        }
        
        this.reader.close();
        
        logger.log(Level.INFO,buffer.toString());
        
        return buffer;
    }

    public static void main(String args1[]) {}

}
