package org.electrocodeogram.module.source.implementation;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.target.implementation.DBCommunicator;
import org.electrocodeogram.module.target.implementation.XMLSchemaProxy;
import org.electrocodeogram.msdt.MicroSensorDataType;
import org.electrocodeogram.system.ModuleSystem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The SchemaTree class is used to recreate a xml Document from a Event Object
 * and its corresponding xml schema.
 * 
 * @author jule
 */
public class SchemaTree {
    /**
     * the XML document
     */
    private Document myDocument = null;

    /**
     * the Event Object from which to fetch the data
     */
    private Event event = null;

    /**
     * the File for the xml schema from which to build up the xml document
     */
    private File defFile;

    /**
     * the DBCommunicator for direct access to the database which is not
     * directly used here
     */
    private DBCommunicator dbCom;

    /**
     * the array of the registered msdts in the ECG_Lab
     */
    MicroSensorDataType[] msdts;

    /**
     * this is the logger.
     */
    private static Logger logger = LogHelper.createLogger(SchemaTree.class
            .getName());

    public SchemaTree(Event event, DBCommunicator dbCommunicator) {
        // get the registered msdts
        msdts = ModuleSystem.getInstance().getMicroSensorDataTypes();
        this.event = event;
    }

    /**
     * With this method the recreation of a xml document beginns. It
     * 
     * @param schemaName
     * @param result
     * @return
     */
    public Document getSchemaElements() {
        for (int i = 0; i < msdts.length; i++) {
            MicroSensorDataType msdt = msdts[i];
            if (msdt.getName().equalsIgnoreCase(event.getMSDT())) {
                defFile = msdt.getDefFile();
            }
        }
        try {
            myDocument = ((DocumentBuilderFactory.newInstance())
                    .newDocumentBuilder()).newDocument();
        }
        catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        }
        XMLSchemaProxy proxy = new XMLSchemaProxy(defFile, dbCom);
        SchemaTypeSystem typeSystem = proxy.generateSchemaTypeSystem();
        // GlobalElement microActivity
        SchemaGlobalElement[] globElements = typeSystem.globalElements();
        Element root = myDocument.createElement(globElements[0].getName()
                .toString());
        myDocument.appendChild(root);
        if (globElements[0].getType().getElementProperties().length > 0) {
            getSubNodes(globElements[0].getType(), root);
        }
        removeEmptyNodes(root);
        return this.myDocument;
    }

    /**
     * This Method traverses the complete tree of a given node (element or type
     * deklaration) in a xml schema. For each found node it tries to find an
     * element with the same name in the Event Object. If such an element is
     * found, then a new element node with the value from the element in the
     * Event object is created in the xml document. If there are any attributes
     * for an element then same procedure is done and a attribute node is
     * appended to to element node.
     * 
     * @param schemaType
     *            the schemaType of the currently examined node
     * @param parent
     *            the parent node of the examined node
     */
    private void getSubNodes(SchemaType schemaType, Element parent) {
        // commonData and msdt-special Part
        SchemaProperty[] subNodes = schemaType.getElementProperties();
        for (int i = 0; i < subNodes.length; i++) {
            SchemaProperty currentNode = subNodes[i];
            if (currentNode.getName() == null) {
                continue;
            }
            String elementName = subNodes[i].getName().toString();
            logger.info("Element Name: " + elementName);
            LinkedList columnValues = new LinkedList();
            if (event.containsElement(elementName)) {
                columnValues = event.getEntryValues(elementName);
                if (columnValues.size() >= 1) {
                    // for elements that can occur more than once in a xml
                    // document create as much elements in the xml document as
                    // there are values for this element in the event object
                    Iterator iter = columnValues.iterator();
                    while (iter.hasNext()) {
                        // create element node
                        Element element = myDocument.createElement(elementName);
                        // fetch the value for this element
                        String currentValue = (String) iter.next();
                        // fill element node with the value
                        try {
                            element.appendChild(myDocument
                                    .createTextNode(currentValue));
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (subNodes[i].getType().getAttributeProperties() != null) {
                            setAttributes(subNodes[i].getType(), element);
                        }
                        if (subNodes[i].getType().getElementProperties().length > 0) {
                            getSubNodes(subNodes[i].getType(), element);
                        }
                        parent.appendChild(element);
                    }// END while
                }
            }
            else {
                Element element = myDocument.createElement(elementName);
                // in the case there are some attributes
                if (subNodes[i].getType().getAttributeProperties() != null) {
                    setAttributes(subNodes[i].getType(), element);
                }
                if (subNodes[i].getType().getElementProperties().length > 0) {
                    getSubNodes(subNodes[i].getType(), element);
                }
                parent.appendChild(element);
            }
        }
    }

    /**
     * If a complex element has some attributes, then the values for the
     * attributes are fetched and the attribute nodes with the values are
     * appended to the element node
     * 
     * @param st
     *            the schema type of the element
     * @param element
     *            the Element to which the attribute nodes are appended
     */
    private void setAttributes(SchemaType st, Element element) {
        SchemaProperty[] attributes = st.getAttributeProperties();
        for (int i = 0; i < attributes.length; i++) {
            String attributeName = attributes[i].getName().toString();
            String attributeValue = event.getAttributeValue(attributeName);
            element.setAttribute(attributeName, attributeValue);
        }
    }

    /**
     * clear the xml document from nodes which does not contain a value, child
     * nodes or attributes.
     * 
     * @param doc
     *            the node to examine
     */
    private void removeEmptyNodes(Node doc) {
        NodeList childNodes = doc.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node.hasChildNodes()) {
                removeEmptyNodes(node);
            }
            if ((!node.hasChildNodes())
                    && (!node.hasAttributes())
                    && ((node.getNodeValue() == null) || node.getNodeValue()
                            .equals(""))) {
                doc.removeChild(node);
            }
            else {
            }
        }
    }
}
