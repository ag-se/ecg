/*
 * Classname: ECGParser
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.misc.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.xerces.parsers.DOMParser;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.logging.LogHelper.ECGLevel;
import org.electrocodeogram.modulepackage.ModuleDescriptor;
import org.electrocodeogram.modulepackage.ModuleProperty;
import org.electrocodeogram.modulepackage.ModuleType;
import org.electrocodeogram.modulepackage.classloader.ModuleClassLoader;
import org.electrocodeogram.modulepackage.modulesetup.ModuleConfiguration;
import org.electrocodeogram.modulepackage.modulesetup.ModuleSetup;
import org.electrocodeogram.msdt.MicroSensorDataType;
import org.electrocodeogram.msdt.MicroSensorDataTypeException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;

/**
 * This is a static utility class providing methods to parse XML files
 * for different information. These methods are used widely in the ECG
 * subsystems and by ECG's modules.
 */
public final class ECGParser {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper.createLogger(ECGParser.class
        .getName());

    /**
     * The path to the XML schema file, which defines the structure of
     * "module.properties.xml" files.
     * 
     * TODO: No path here
     */
    private static final String MODULE_PROPERTIES_XSD = "lib/ecg/module.properties.xsd";

    /**
     * The path to the XML schema file, which defines the structure of
     * <em>ModuleSetup</em> files.
     * 
     * TODO: No path here
     */
    private static final String MODULE_SETUP_XSD = "lib/ecg/module.setup.xsd";

    /**
     * The constructor is hidden for this utility class.
     */
    private ECGParser() {
    // empty
    }

    /**
     * This parses a given file and validates it against the given XML
     * schema. If parsing is successfull a <code>Document</code>
     * object is returned. Parsing is done by the
     * <em>Apache Xerces2 DOMParser</em>.
     * @param file
     *            Is the file to parse
     * @param schemaLocation
     *            Is the path to a XML schema to be used during
     *            parsing
     * @return A XML <code>Document</code> object
     * @throws SAXException
     *             If the file does not contain a wellformed and valid
     *             XML document
     * @throws IOException
     *             If an error occurs while reading from the file
     */
     public static Document parseFile(final File file,
        final String schemaLocation) throws SAXException, IOException {

        logger.entering(ECGParser.class.getName(), "parseFile", new Object[] {
            file, schemaLocation});

        InputSource inputSource = null;

        // read the property file
        try {
            inputSource = new InputSource(new FileReader(file));
        } catch (FileNotFoundException e2) {

            // is checked before and should never happen

            logger.log(Level.SEVERE, "The file couls not be found.");

            logger.log(Level.SEVERE, e2.getMessage());

        }

        // create the XML parser
        DOMParser parser = new DOMParser();

        // set the parsing properties
        try {
            parser.setFeature("http://xml.org/sax/features/validation", true);
            parser.setFeature(
                "http://apache.org/xml/features/validation/schema", true);
            parser
                .setFeature(
                    "http://apache.org/xml/features/validation/schema-full-checking",
                    true);
            parser.setFeature(
                "http://apache.org/xml/features/validation/dynamic", true);

            // parse the property file against the
            // "module.properties.xsd
            // XML schema
            parser
                .setProperty(
                    "http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",
                    schemaLocation);

        } catch (SAXNotRecognizedException e1) {
            // is checked and schould never occure

            logger.log(Level.SEVERE,
                "A requested feature is not recognized by the parser.");

            logger.log(Level.SEVERE, e1.getMessage());

        } catch (SAXNotSupportedException e1) {

            // is checked and schould never occure

            logger.log(Level.SEVERE,
                "A requested feature is not supported by the parser.");

            logger.log(Level.SEVERE, e1.getMessage());

        }

        // parse

        parser.setErrorHandler(new ErrorHandler() {

            @SuppressWarnings("synthetic-access")
            public void warning(final SAXParseException exception)
                throws SAXException {

                logger.log(Level.SEVERE, exception.getMessage());

                logger.exiting(ECGParser.class.getName(), "parseFile");

                throw new SAXException(exception.getMessage());
            }

            @SuppressWarnings("synthetic-access")
            public void error(final SAXParseException exception)
                throws SAXException {
                logger.log(Level.SEVERE, exception.getMessage());

                logger.exiting(ECGParser.class.getName(), "parseFile");

                throw new SAXException(exception.getMessage());

            }

            @SuppressWarnings("synthetic-access")
            public void fatalError(final SAXParseException exception)
                throws SAXException {
                logger.log(Level.SEVERE, exception.getMessage());

                logger.exiting(ECGParser.class.getName(), "parseFile");

                throw new SAXException(exception.getMessage());

            }
        });

        parser.parse(inputSource);

        logger.exiting(ECGParser.class.getName(), "parseFile", parser
            .getDocument());

        // get the XML document
        return parser.getDocument();

    }

    /**
     * This method parses the given string that usually
     * comes from a
     * {@link org.electrocodeogram.event.ValidEventPacket} beeing a
     * <em>MicroActivityEvent</em>.
     * @param string
     *            The <em>MicroActivityEvent's</em> content
     * @param schemaLocation
     *            The path to the XML schmema
     * @return A XML <code>Document</code> object
     * @throws SAXException
     *             If the string does not contain a wellformed and valid
     *             XML document
     * @throws IOException
     *             If an error occurs while reading from the file
     */
    public static Document parseAsMicroActivity(final String string,
        final String schemaLocation) throws SAXException, IOException {

        logger.entering(ECGParser.class.getName(), "parseAsMicroActivity",
            new Object[] {string, schemaLocation});

        InputSource inputSource = null;

        // read the property file

        inputSource = new InputSource(new StringReader(string));
        // create the XML parser
        DOMParser parser = new DOMParser();

        // set the parsing properties
        try {
            parser.setFeature("http://xml.org/sax/features/validation", true);
            parser.setFeature(
                "http://apache.org/xml/features/validation/schema", true);
            parser
                .setFeature(
                    "http://apache.org/xml/features/validation/schema-full-checking",
                    true);
            parser.setFeature(
                "http://apache.org/xml/features/validation/dynamic", true);

            // parse the property file against the
            // "module.properties.xsd
            // XML schema
            parser
                .setProperty(
                    "http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",
                    schemaLocation);

        } catch (SAXNotRecognizedException e1) {
            // is checked and schould never occure

            logger.log(Level.SEVERE,
                "A requested feature is not recognized by the parser.");

            logger.log(Level.SEVERE, e1.getMessage());

        } catch (SAXNotSupportedException e1) {
            // is checked and schould never occure

            logger.log(Level.SEVERE,
                "A requested feature is not supported by the parser.");

            logger.log(Level.SEVERE, e1.getMessage());

        }

        // parse

        parser.setErrorHandler(new ErrorHandler() {

            @SuppressWarnings({"synthetic-access", "synthetic-access" })
            public void warning(final SAXParseException exception)
                throws SAXException {

                logger.log(Level.SEVERE, exception.getMessage());

                logger.exiting(ECGParser.class.getName(),
                    "parseAsMicroActivity");

                throw new SAXException(exception.getMessage());
            }

            @SuppressWarnings("synthetic-access")
            public void error(final SAXParseException exception)
                throws SAXException {
                logger.log(Level.SEVERE, exception.getMessage());

                logger.exiting(ECGParser.class.getName(),
                    "parseAsMicroActivity");

                throw new SAXException(exception.getMessage());

            }

            @SuppressWarnings("synthetic-access")
            public void fatalError(final SAXParseException exception)
                throws SAXException {
                logger.log(Level.SEVERE, exception.getMessage());

                logger.exiting(ECGParser.class.getName(),
                    "parseAsMicroActivity");

                throw new SAXException(exception.getMessage());

            }
        });

        parser.parse(inputSource);

        logger.exiting(ECGParser.class.getName(), "parseAsMicroActivity",
            parser.getDocument());

        // get the XML document
        return parser.getDocument();

    }

    /**
     * This method parses the given string ans returns a <code>Document</code> from it.
     * @param string
     *            The string that is containing the XML document.
     * @param schemaLocation
     *            The path to the XML schmema
     * @return A XML <code>Document</code> object
     * @throws SAXException
     *             If the file does not contain a wellformed and valid
     *             XML document
     * @throws IOException
     *             If an error occurs while reading from the file
     */
    public static Document parseAsDocument(final String string,
        final String schemaLocation) throws SAXException, IOException {

        logger.entering(ECGParser.class.getName(), "parseAsMicroActivity",
            new Object[] {string, schemaLocation});

        InputSource inputSource = null;

        // read the property file

        inputSource = new InputSource(new StringReader(string));
        // create the XML parser
        DOMParser parser = new DOMParser();

        parser.setErrorHandler(new ErrorHandler() {

            @SuppressWarnings({"synthetic-access", "synthetic-access" })
            public void warning(final SAXParseException exception)
                throws SAXException {

                logger.log(Level.SEVERE, exception.getMessage());

                logger.exiting(ECGParser.class.getName(),
                    "parseAsMicroActivity");

                throw new SAXException(exception.getMessage());
            }

            @SuppressWarnings("synthetic-access")
            public void error(final SAXParseException exception)
                throws SAXException {
                logger.log(Level.SEVERE, exception.getMessage());

                logger.exiting(ECGParser.class.getName(),
                    "parseAsMicroActivity");

                throw new SAXException(exception.getMessage());

            }

            @SuppressWarnings("synthetic-access")
            public void fatalError(final SAXParseException exception)
                throws SAXException {
                logger.log(Level.SEVERE, exception.getMessage());

                logger.exiting(ECGParser.class.getName(),
                    "parseAsMicroActivity");

                throw new SAXException(exception.getMessage());

            }
        });

        parser.parse(inputSource);

        logger.exiting(ECGParser.class.getName(), "parseAsMicroActivity",
            parser.getDocument());

        // get the XML document
        return parser.getDocument();

    }

    /**
     * This method parses the given "module.properties.xml" file. If
     * this file is wellformed and valid a <em>ModuleDescriptor</em>
     * is created, which contains the information form the properties
     * file.
     * @param file
     *            Is the "module.properties.xml" file
     * @return The <em>ModuleDescriptor</em> containing the
     *         information form the properties file.
     * @throws ClassLoadingException
     *             If an error occurs while loading the module class,
     *             which is defined in the properties file
     * @throws MicroSensorDataTypeException
     *             If an error occurs while loading the
     *             <em>MicroSensorDataTypes</em>, which are defined
     *             in the properties file
     * @throws NodeException
     *             If an expected XML node or its value
     *             can not be found
     * @throws SAXException
     *             If the file does not contain a wellformed and valid
     *             XML document
     * @throws IOException
     *             If an error occurs while reading from the file
     */
    public static ModuleDescriptor parseAsModuleDescriptor(final File file)
        throws ClassLoadingException, MicroSensorDataTypeException,
        NodeException, SAXException, IOException {

        logger.entering(ECGParser.class.getName(), "parseAsModuleDescriptor",
            new Object[] {file});

        Document document = parseFile(file, MODULE_PROPERTIES_XSD);

        String id = getSingleNodeValue("id", document);

        String name = getSingleNodeValue("name", document);

        String providerName = getSingleNodeValue("provider-name", document);

        String version = getSingleNodeValue("version", document);

        String moduleDescription = getSingleNodeValue("description", document);

        Class clazz = getClass(file.getParent(), getSingleNodeValue("class",
            document));

        ModuleType moduleType = getModuleType(document);

        ModuleProperty[] moduleProperties = getModuleProperties(document);

        MicroSensorDataType[] microSensorDataTypes = getMicroSensorDataTypes(
            document, file.getParent());

        ModuleDescriptor toReturn = new ModuleDescriptor(id, name, file.getParent(),
            providerName, version, clazz, moduleDescription, moduleType,
            moduleProperties, microSensorDataTypes);

        logger.exiting(ECGParser.class.getName(), "parseAsModuleDescriptor",
            toReturn);

        return toReturn;
    }

    /**
     * Returns the <em>MODULE_TYPE</em> from the given XML
     * <code>Document</code> that should be a parsed
     * "module.properties.xml" file.
     * @param document
     *            Is the XML document
     * @return The <em>MODULE_TYPE</em>
     * @throws NodeException
     *             If the given XML document does not
     *             contain a module description
     */
    private static ModuleType getModuleType(final Document document)
        throws NodeException {

        logger.entering(ECGParser.class.getName(), "getModuleType",
            new Object[] {document});

        String moduleTypeString = getSingleNodeValue("type", document);

        ModuleType moduleType;

        if (moduleTypeString.equals("SOURCE")) {
            moduleType = ModuleType.SOURCE_MODULE;
        } else if (moduleTypeString.equals("INTERMEDIATE")) {
            moduleType = ModuleType.INTERMEDIATE_MODULE;
        } else {
            moduleType = ModuleType.TARGET_MODULE;
        }

        logger.exiting(ECGParser.class.getName(), "getModuleType", moduleType);

        return moduleType;
    }

    /**
     * This method parses the given file as a <em>ModuleSetup</em>
     * and if it is wellformed and valid the {@link ModuleSetup} is
     * returned.
     * @param file
     *            Is the file to parse
     * @return The <em>ModuleSetup</em> if it could be parsed from
     *         the file
     * @throws SAXException
     *             If the file does not contain a wellformed and valid
     *             <em>ModuleSetup</em>
     * @throws IOException
     *             If an error occurs while reading from the file
     * @throws NodeException
     *             If a expected XML node or its value
     *             can not be found
     * @throws ClassLoadingException
     *             If an error occurs while loading the module class
     *             from the <em>ModuleSetup</em>
     */
    public static ModuleSetup parseAsModuleSetup(final File file)
        throws SAXException, IOException, NodeException, ClassLoadingException {

        logger.entering(ECGParser.class.getName(), "parseAsModuleSetup",
            new Object[] {file});

        Document document = parseFile(file, MODULE_SETUP_XSD);

        logger.log(Level.INFO, "The module setup file seems to be valid.");

        ModuleSetup moduleSetup = new ModuleSetup();

        Node[] moduleNodes = getChildNodes(document.getDocumentElement(),
            "module");

        logger.log(Level.INFO, "Found " + moduleNodes.length
                               + " stored modules in module setup file.");

        if (moduleNodes.length == 0) {
            throw new NodeException(
                "The ModuleSetup is empty. No node was found.", "module", file
                    .getAbsolutePath());
        }

        for (Node moduleNode : moduleNodes) {

            ModuleConfiguration moduleConfiguration;

            ArrayList < ModuleProperty > modulePropertyList = null;

            ArrayList < Integer > connectedToList = null;

            String moduleName;

            String fromClassId;

            Integer storedModuleId = null;

            logger.log(Level.INFO, "Going to load next module.");

            String idValue = getAttributeValue(moduleNode, "id");

            String activeValue = getAttributeValue(moduleNode, "active");

            try {
                storedModuleId = new Integer(idValue);
            } catch (NumberFormatException e) {
                logger
                    .log(Level.SEVERE,
                        "Illegal value for attribute \"id\" for element \"module\".");

                logger.exiting(ECGParser.class.getName(), "parseAsModuleSetup");

                throw new NodeException(
                    "The value is not a positive integer number", moduleNode
                        .getNodeName(), file.getAbsolutePath());
            }

            boolean active = false;

            if (activeValue != null && activeValue.equalsIgnoreCase("true")) {
                active = true;
            }

            logger.log(Level.INFO,
                "Value for attribute \"id\" for element \"module\".");

            Node nameNode = getChildNode(moduleNode, "name");

            Node fromClassIdNode = getChildNode(moduleNode, "fromClassId");

            moduleName = getNodeValue(nameNode);

            fromClassId = getNodeValue(fromClassIdNode);

            connectedToList = new ArrayList < Integer >();

            Node connectedToNode = getChildNode(moduleNode, "connectedTo");

            if (isNode(connectedToNode, "connectedTo")) {
                Node[] idNodes = getChildNodes(connectedToNode, "id");

                for (Node idNode : idNodes) {

                    String connectedToIdValue = getNodeValue(idNode);

                    try {
                        Integer id = new Integer(connectedToIdValue);

                        connectedToList.add(id);

                        logger
                            .log(
                                Level.INFO,
                                "Module number "
                                                + storedModuleId
                                                + " is connected to module with id "
                                                + id);

                    } catch (NumberFormatException e) {
                        logger
                            .log(
                                Level.SEVERE,
                                "Illegal value for element \"id\" for element \"connectedTo\" for module number "
                                                + storedModuleId);

                        logger.exiting(ECGParser.class.getName(),
                            "parseAsModuleSetup");

                        throw new NodeException(
                            "The value is not a positive integer number",
                            connectedToNode.getNodeName(), file
                                .getAbsolutePath());
                    }

                }

            }

            modulePropertyList = new ArrayList < ModuleProperty >();

            Node propertiesNode = getChildNode(moduleNode, "properties");

            if (isNode(propertiesNode, "properties")) {
                Node[] propertyNodes = getChildNodes(propertiesNode, "property");

                for (Node propertyNode : propertyNodes) {
                    String propertyName = null;

                    String propertyValue = null;

                    Class propertyType = null;

                    Node propertyNameNode = getChildNode(propertyNode, "name");

                    propertyName = getNodeValue(propertyNameNode);

                    Node propertyValueNode = getChildNode(propertyNode, "value");

                    propertyValue = getNodeValue(propertyValueNode);

                    Node propertyTypeNode = getChildNode(propertyNode, "type");

                    propertyType = getClass(null,
                        getNodeValue(propertyTypeNode));

                    ModuleProperty moduleProperty = new ModuleProperty(
                        propertyName, propertyValue, propertyType);

                    modulePropertyList.add(moduleProperty);
                }
            }

            moduleConfiguration = new ModuleConfiguration(connectedToList
                .toArray(new Integer[0]), storedModuleId.intValue(),
                moduleName, active, modulePropertyList
                    .toArray(new ModuleProperty[modulePropertyList.size()]),
                fromClassId);

            moduleSetup.addModuleConfiguration(moduleConfiguration);
        }

        logger.exiting(ECGParser.class.getName(), "parseAsModuleSetup",
            moduleSetup);

        return moduleSetup;

    }

    /**
     * Returns the <em>ModuleProperties</em> form the given XML
     * document, wich is expected to be a parsed
     * "module.properties.xml" file.
     * @param document
     *            The XML document
     * @return The <em>ModuleProperties</em>
     * @throws NodeException
     *             If an error occurs while accessing the document's
     *             nodes.
     */
    private static ModuleProperty[] getModuleProperties(final Document document)
        throws NodeException {

        logger.entering(ECGParser.class.getName(), "getModuleProperties",
            new Object[] {document});

        Node properties;

        ArrayList < ModuleProperty > moduleProperties = null;

        Node[] propertyNodes;

        try {

            properties = getChildNode(document.getDocumentElement(),
                "properties");

            propertyNodes = getChildNodes(properties, "property");
        } catch (NodeException e) {

            logger.exiting(ECGParser.class.getName(), "getModuleProperties",
                null);

            return null;
        }

        moduleProperties = new ArrayList < ModuleProperty >();

        for (Node propertyNode : propertyNodes) {

            Node modulePropertyNameNode = getChildNode(propertyNode,
                "propertyName");

            Node modulePropertyTypeNode = getChildNode(propertyNode,
                "propertyType");

            Node modulePropertyValueNode = getChildNode(propertyNode,
                "propertyValue");

            String modulePropertyType = null;

            String modulePropertyName = null;

            modulePropertyName = getNodeValue(modulePropertyNameNode);

            modulePropertyType = getNodeValue(modulePropertyTypeNode);

            Class type = null;

            try {
                type = Class.forName(modulePropertyType);
            } catch (ClassNotFoundException e) {
                try {
                    type = ModuleClassLoader.getInstance().loadClass(
                        modulePropertyType);
                } catch (ClassNotFoundException e1) {
                    logger.log(Level.WARNING,
                        "Error while reading the property type: "
                                        + modulePropertyType);

                    logger.exiting(ECGParser.class.getName(),
                        "getModuleProperties");

                    throw new NodeException(
                        "The property type is not a Java class.",
                        modulePropertyTypeNode.getNodeName(),
                        modulePropertyValueNode.getNodeName());
                }
            }

            if (type == null) {
                logger.log(Level.WARNING,
                    "Error while reading the property type: "
                                    + modulePropertyType);

                logger
                    .exiting(ECGParser.class.getName(), "getModuleProperties");

                throw new NodeException(
                    "The property type is not a Java class.",
                    modulePropertyTypeNode.getNodeName(),
                    modulePropertyValueNode.getNodeName());
            }

            String modulePropertyValue = null;

            modulePropertyValue = getNodeValue(modulePropertyValueNode);

            moduleProperties.add(new ModuleProperty(modulePropertyName,
                modulePropertyValue, type));

        }

        logger.exiting(ECGParser.class.getName(), "getModuleProperties",
            moduleProperties
                .toArray(new ModuleProperty[moduleProperties.size()]));

        return moduleProperties.toArray(new ModuleProperty[moduleProperties
            .size()]);

    }

    /**
     * Returns the <code>Class</code> object to a given class name.
     * This method uses the {@link ModuleClassLoader}.
     * @param classPath
     *            Is an additional classpath for the
     *            {@link ModuleClassLoader}
     * @param className
     *            Is the name of the class to load
     * @return The named class
     * @throws ClassLoadingException
     *             If loading of the named class failed
     */
    private static Class getClass(final String classPath, final String className)
        throws ClassLoadingException {

        logger.entering(ECGParser.class.getName(), "getClass", new Object[] {
            classPath, className});

        Class moduleClass;

        if (classPath != null) {
            ModuleClassLoader.addPath(classPath);
            ModuleClassLoader.addPath(classPath + File.separator + "bin");
            ModuleClassLoader.addPath(classPath + File.separator + "classes");
            ModuleClassLoader.addPath(classPath + File.separator + "lib");
        }

        try {
            moduleClass = ModuleClassLoader.getInstance().loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new ClassLoadingException();
        }

        if (moduleClass == null) {
            throw new ClassLoadingException();
        }

        logger.exiting(ECGParser.class.getName(), "getClass", moduleClass);

        return moduleClass;
    }

    /**
     * This method returns the value of a given XML node
     * in a given XML document. There is only one
     * node of the given name expected to be in the
     * document. Otherwiese the value of the first
     * occurance of the given node is returned.
     * @param nodeName
     *            Is the name of the XML node to return
     *            the value from
     * @param document
     *            Is the value from the given XML node
     * @return The value of the node
     * @throws NodeException
     *             If an error occurs while accessing the
     *             node
     */
     public static String getSingleNodeValue(final String nodeName,
        final Document document) throws NodeException {

        logger.entering(ECGParser.class.getName(), "getSingleNodeValue",
            new Object[] {nodeName, document});

        NodeList nodeList = document.getElementsByTagName(nodeName);

        if (nodeList == null || nodeList.getLength() == 0) {
            throw new NodeException(
                "The requested element could not be found.", nodeName, document
                    .getNodeName());
        }

        Node node = nodeList.item(0);

        logger.exiting(ECGParser.class.getName(), "getSingleNodeValue",
            getNodeValue(node));

        return getNodeValue(node);
    }

    public static String getSingleNodeValueIfAvailable(final String nodeName, 
    		final Document document) {
		String value = "";
		try {
			value = getSingleNodeValue(nodeName, document);
		} catch (NodeException e) {
			// ignore, value remains ""
		}
		return value;
	}

	/**
     * Returns the <em>MicroSensorDataTypes</em> form the given XML
     * <code>Document</code>, wich is expected to be a parsed
     * "module.properties.xml" file. This method uses
     * {@link #parseMicroSensorDataType(File)}
     * to actually parse the XML schemata into
     * {@link javax.xml.validation.Schema} objects.
     * @param document
     *            The XML document
     * @param modulePath
     *            Is the path to the module whose
     *            <em>ModuleDescription</em> is currently parsed.
     * @return The <em>MicroSensorDataTypes</em>
     * @throws NodeException
     *             If an error occurs while accessing the document's
     *             nodes
     * @throws MicroSensorDataTypeException
     *             If an error occures while a
     *             <em>MicroSensorDataType</em> is created from the
     *             <em>ModuleDescription</em>
     */
     private static MicroSensorDataType[] getMicroSensorDataTypes(
        final Document document, final String modulePath)
        throws MicroSensorDataTypeException, NodeException {

        logger.entering(ECGParser.class.getName(), "getMicroSensorDataTypes");

        Node microSensorDataTypesNode = null;

        try {
            microSensorDataTypesNode = getChildNode(document
                .getDocumentElement(), "microsensordatatypes");
        } catch (NodeException e1) {

            logger.exiting(ECGParser.class.getName(),
                "getMicroSensorDataTypes", null);

            return null;
        }

        ArrayList < MicroSensorDataType > microSensorDataTypes = null;

        Node[] msdtNodes = getChildNodes(microSensorDataTypesNode,
            "microsensordatatype");

        microSensorDataTypes = new ArrayList < MicroSensorDataType >();

        for (Node msdtNode : msdtNodes) {

            Node msdtFileNode = getChildNode(msdtNode, "msdtFile");

            String msdtFileString = getNodeValue(msdtFileNode);

            File msdtFile = new File(modulePath + File.separator + "msdt"
                                     + File.separator + msdtFileString);


            microSensorDataTypes.add(parseMicroSensorDataType(msdtFile));

        }

        logger.exiting(ECGParser.class.getName(), "getMicroSensorDataTypes",
            microSensorDataTypes
                .toArray(new MicroSensorDataType[microSensorDataTypes.size()]));

        return microSensorDataTypes
            .toArray(new MicroSensorDataType[microSensorDataTypes.size()]);
    }

    /**
     * This method is used to parse a given XML schema file and create
     * a {@link MicroSensorDataType} from it.
     * @param file
     *            Is the file containing the XML schema
     * @return The <em>MicroSensorDataType</em> defined by the file
     * @throws MicroSensorDataTypeException
     *             If the file does not contain a valid XML schema or
     *             if any other error occures during parsing
     */
    private static MicroSensorDataType parseMicroSensorDataType(final File file)
        throws MicroSensorDataTypeException {
        logger.entering(ECGParser.class.getName(), "parseMicroSensorDataType",
            new Object[] {file});

        if (!file.exists()) {

            logger.exiting(ECGParser.class.getName(),
                "parseMicroSensorDataType");

            throw new MicroSensorDataTypeException(
                "Error while loading MSDT:\nThe schema file "
                                + file.getAbsolutePath() + " does not exist.");
        }

        if (!file.isFile()) {

            logger.exiting(ECGParser.class.getName(),
                "parseMicroSensorDataType");

            throw new MicroSensorDataTypeException(
                "Error while loading MSDT:\nThe schema file "
                                + file.getAbsolutePath()
                                + " is not a plain file.");
        }

        SchemaFactory schemaFactory = SchemaFactory
            .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        Schema schema = null;

        try {

            schema = schemaFactory.newSchema(file);

            MicroSensorDataType microSensorDataType = new MicroSensorDataType(
                file.getName(), schema, file);

            logger.log(Level.INFO, "Loaded additional MicroSensorDatyType "
                                   + file.getName());

            logger.exiting(ECGParser.class.getName(),
                "parseMicroSensorDataType");

            return microSensorDataType;

        } catch (SAXException e) {

            logger.exiting(ECGParser.class.getName(),
                "parseMicroSensorDataType");

            throw new MicroSensorDataTypeException(
                "Error while reading the XML schema file " + file.getName()
                                + "\n" + e.getMessage());
        }

    }

    /**
     * This helper method is returning an array of XML
     * nodes with the given name and of the given
     * parent node.
     * @param parentNode
     *            Is the parent node
     * @param nodeName
     *            Is the name of the child nodes to
     *            return
     * @return An array of child nodes
     *         with the given name
     * @throws NodeException
     *             If an error occurs while accessing the document's
     *             nodes.
     */
    public static Node[] getChildNodes(final Node parentNode,
        final String nodeName) throws NodeException {

        logger.entering(ECGParser.class.getName(), "getChildNodes",
            new Object[] {parentNode, nodeName});

        if (parentNode == null || nodeName == null) {

            logger.exiting(ECGParser.class.getName(), "getChildNodes", null);

            return new Node[0];
        }

        ArrayList < Node > nodeList = new ArrayList < Node >();

        NodeList childNodes = parentNode.getChildNodes();

        if (childNodes == null || childNodes.getLength() == 0) {
            logger.log(Level.WARNING, "Node " + parentNode.getNodeName()
                                      + " does not have any childNodes.");

            logger.exiting(ECGParser.class.getName(), "getChildNodes");

            throw new NodeException("The requested child node " + nodeName
                                    + " is not found.", parentNode
                .getNodeName(), parentNode.getOwnerDocument().getNodeName());
        }

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);

            if (node.getNodeName().equals(nodeName)) {
                nodeList.add(node);
            }
        }

        logger.exiting(ECGParser.class.getName(), "getChildNodes", nodeList
            .toArray(new Node[nodeList.size()]));

        return nodeList.toArray(new Node[nodeList.size()]);
    }

    /**
     * This helper method is returning the first XML node
     * with the given name and of the given parent node.
     * @param parentNode
     *            Is the parent node
     * @param nodeName
     *            Is the name of the child node to
     *            return
     * @return The first child node with the given name
     * @throws NodeException
     *             If an error occurs while accessing the document's
     *             nodes.
     */
    public static Node getChildNode(final Node parentNode, final String nodeName)
        throws NodeException {

        logger.entering(ECGParser.class.getName(), "getChildNode",
            new Object[] {parentNode, nodeName});

        Node[] propertyNameNodes = getChildNodes(parentNode, nodeName);

        if (propertyNameNodes == null || propertyNameNodes.length != 1) {

            logger.exiting(ECGParser.class.getName(), "getChildNodes");

            throw new NodeException("The requested child node " + nodeName
                                    + " is not found.", parentNode
                .getNodeName(), parentNode.getOwnerDocument().getNodeName());
        }

        logger.exiting(ECGParser.class.getName(), "getChildNodes",
            propertyNameNodes[0]);

        return propertyNameNodes[0];
    }

    /**
     * This helper methos returns the value of a given XML
     * node.
     * @param node
     *            Is the XML node
     * @return The value of the node
     */
    public static String getNodeValue(final Node node) {

        logger.entering(ECGParser.class.getName(), "getNodeValue",
            new Object[] {node});

        String value = null;

        if (node.getFirstChild() == null) {
            logger.log(ECGLevel.PACKET, "Missing value for element "
                                        + node.getNodeName());

            logger.exiting(ECGParser.class.getName(), "getNodeValue", null);

            return null;

        }

        value = node.getFirstChild().getNodeValue();

        if (value == null || value.equals("")) {
            logger.log(ECGLevel.PACKET, "Missing value for element "
                                        + node.getNodeName());

            logger.exiting(ECGParser.class.getName(), "getNodeValue", null);

            return null;

        }

        logger.log(ECGLevel.PACKET, "Found value " + value + " for element "
                                    + node.getNodeName());

        logger.exiting(ECGParser.class.getName(), "getNodeValue", value);

        return value;
    }

    /**
     * This helper method is checking if an given node
     * has the given name.
     * @param node
     *            Is the node to check
     * @param nodeName
     *            Is the name to check
     * @return <code>true</code> if the name is right and
     *         <code>false</code> otherwise. If the
     *         node is null, <code>false</code> is
     *         returned.
     */
    public static boolean isNode(final Node node, final String nodeName) {

        logger.entering(ECGParser.class.getName(), "isNode", new Object[] {
            node, nodeName});

        if (node == null) {

            logger.exiting(ECGParser.class.getName(), "isNode", Boolean
                .valueOf(false));

            return false;
        }

        if (!node.getNodeName().equals(nodeName)) {
            logger.log(Level.SEVERE, "Misspelled element " + node.getNodeName()
                                     + ". Should be " + nodeName);

            logger.exiting(ECGParser.class.getName(), "isNode", Boolean
                .valueOf(false));

            return false;
        }

        logger.exiting(ECGParser.class.getName(), "isNode", Boolean
            .valueOf(true));

        return true;
    }

    /**
     * This helper method is used to return the value of an XML
     * attribute.
     * @param node
     *            Is the XML node containing the
     *            attribute
     * @param attributeName
     *            Is the name of the attribute from which the value is
     *            returned
     * @return Is the value of the attribute
     * @throws NodeException
     *             If an error occurs while accessing the document's
     *             nodes.
     */
    public static String getAttributeValue(final Node node,
        final String attributeName) throws NodeException {

        logger.entering(ECGParser.class.getName(), "getAttributeValue",
            new Object[] {node, attributeName});

        if (node == null) {

            logger
                .exiting(ECGParser.class.getName(), "getAttributeValue", null);

            return null;
        }

        if (node.getAttributes() == null
            || node.getAttributes().getLength() == 0) {
            logger.log(Level.SEVERE, "Attribute missing for element "
                                     + node.getNodeName());

            logger.exiting(ECGParser.class.getName(), "getAttributeValue");

            throw new NodeException(
                "The node does not provide the requested attribute "
                                + attributeName, node.getNodeName(), node
                    .getOwnerDocument().getNodeName());
        }

        Node attributeNode = node.getAttributes().getNamedItem(attributeName);

        if (attributeNode == null || attributeNode.getFirstChild() == null) {
            logger.log(Level.SEVERE, "Attribute value missing for element "
                                     + node.getNodeName());

            logger.exiting(ECGParser.class.getName(), "getAttributeValue");

            throw new NodeException(
                "The node does not provide the requested attribute "
                                + attributeName, node.getNodeName(), node
                    .getOwnerDocument().getNodeName());
        }

        logger.exiting(ECGParser.class.getName(), "getAttributeValue",
            attributeNode.getFirstChild().getNodeValue());

        return attributeNode.getFirstChild().getNodeValue();
    }
}
