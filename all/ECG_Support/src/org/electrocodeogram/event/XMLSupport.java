package org.electrocodeogram.event;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.electrocodeogram.logging.LogHelper;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class XMLSupport {
    
    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(XMLSupport.class.getName());

    /**
     * A general XML Document Builder
     */
    private static DocumentBuilder docBuilder = null;        
    
    /**
     * A serializer to output XML DOM structures 
     */
    private static LSSerializer xmlSerializer = null; 

    /**
     * TODO Document
     * TODO Make Exception Handling
     * @return the Document Builder
     */
    static public DocumentBuilder getDocumentBuilder() {
        if (docBuilder == null) {
            try {
                docBuilder= DocumentBuilderFactory.newInstance().newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                return null;
            }
        }
        return docBuilder;
    }
    
    static private void init() {
        try {
            // get DOM Implementation using DOM Registry
            // TODO Use Xerxes instead? This one is only available in JDK 5
            System.setProperty(DOMImplementationRegistry.PROPERTY, "com.sun.org.apache.xerces.internal.dom.DOMXSImplementationSourceImpl");
            DOMImplementationRegistry registry;
                registry = DOMImplementationRegistry.newInstance();
            // Retrieve load/save features
            DOMImplementationLS impl = 
                (DOMImplementationLS)registry.getDOMImplementation("LS");
            // create DOMWriter
            xmlSerializer = impl.createLSSerializer();   
            xmlSerializer.getDomConfig().setParameter("xml-declaration", Boolean.FALSE);
        } catch (ClassCastException e) {
            logger.log(Level.SEVERE, "Unable to instantiate Xerxes XML parser.");
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Unable to instantiate Xerxes XML parser.");
        } catch (InstantiationException e) {
            logger.log(Level.SEVERE, "Unable to instantiate Xerxes XML parser.");
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, "Unable to instantiate Xerxes XML parser.");
        }
    }
    
    static private LSSerializer getXmlSerializer() {
        if (xmlSerializer == null)
            init();
        return xmlSerializer;
    }
    
    /**
     * TODO Document
     * TODO Make Exception Handling
     * @return a string serialization of the element
     */
    static public String serialize(Document doc) {
        LSSerializer sl = XMLSupport.getXmlSerializer();
        if (sl != null)
            return sl.writeToString(doc);
        else
            return "";
    }

}
