/**
 * 
 */
package org.electrocodeogram.module.target.implementation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.electrocodeogram.logging.LogHelper;

/**
 * The XmlSchemaProxy Class is a kind of Proxy for a XML Schema. i.e. it prvides
 * Methods for certain tasks and for better access to the XML Schema
 * 
 * @author jule
 * @version 1.0
 */
public class XMLSchemaProxy {

	/**
	 * This is the logger.
	 */
	private static Logger logger = LogHelper.createLogger(XMLSchemaProxy.class
			.getName());

	/**
	 * the vector which holds the information about all the tables which are
	 * involved in storing Xml Documents which correlate with the given schema
	 */
	private Vector<Table> tables = new Vector<Table>();

	// the complete path to the Schema File for which the Proxy is used
	private File xsdFile = null;

	// the schema Type of a searched and found Element -- needed for the
	// findElementDeklaration Method
	private SchemaType foundElement = null;

	// the Name of the Schema
	private String schemaName = null;

	private String coreSchemaName = "";

	private DBCommunicator dbCommunicator = null;

	/**
	 * returns the name of the schema File without any prefix or suffix
	 * 
	 */
	public void getCoreMsdtName() {
		StringTokenizer st = new StringTokenizer(this.schemaName, ".");
		while (st.hasMoreTokens()) {
			String temp = st.nextToken();
			if ((temp.equalsIgnoreCase("msdt"))
					|| (temp.equalsIgnoreCase("xsd"))) {
				continue;
			}
			logger.info("Core SchemaName: " + temp);
			this.coreSchemaName = temp;
		}
	}

	/**
	 * Constructor
	 * 
	 * @param schemaName
	 *            the Filename of the schema for which the proxy stands
	 */
	public XMLSchemaProxy(File schemaFile, DBCommunicator dbCommunicator) {
		this.dbCommunicator = dbCommunicator;

		this.schemaName = schemaFile.getName();

		this.xsdFile = schemaFile;

		getCoreMsdtName();
	}
	
	

	/**
	 * this method builds a SchemaTypeSystem from the schema file the
	 * SchemaTypeSystem makes up a kind of Meta-API to the Schema
	 * 
	 * @see org.apache.xmlbeans.SchemaTypeSystem
	 * 
	 * @return the SchemaTypeSystem of the given schema
	 */
	public SchemaTypeSystem generateSchemaTypeSystem() {
		logger.entering(this.getClass().getName(), "generateSchemaTypeSystem");

		/*
		 * Array which has to hold the XMLObject instance of the parsed schema
		 * for compiling it in the next step
		 */
		XmlObject[] schema = new XmlObject[1];

		/*
		 * the SchemaTypeSystem of the given Schema which is generated by
		 * compiling the Schema
		 */
		SchemaTypeSystem schemaTypeSystem = null;

		/*
		 * if there are compilation errors, the errors are written in a
		 * collection
		 */
		Collection compErrors = new ArrayList();

		// options for compiling the schema
		XmlOptions schemaOptions = new XmlOptions();
		// set options for compiling
		schemaOptions.setCompileDownloadUrls();
		schemaOptions.setErrorListener(compErrors);

		/*
		 * parsing the schema file with the given options returns an instance of
		 * XMLObject which then is stored in the prepared XMLObject Array
		 * 
		 * in fact there is only one XMLObject instance in the Array
		 * 
		 * @see org.apache.xmlbeans.XMLObject
		 */
		try {
			schema[0] = XmlObject.Factory.parse(xsdFile, schemaOptions);
		} catch (XmlException e) {
			System.out
					.println("[Validation:generateSchemaTypeSystem] XmlException: "
							+ e.getCause());
			e.printStackTrace();
			return null;
		}

		catch (IOException e) {
			System.out
					.println("[Validation:generateSchemaTypeSystem] IOException: "
							+ e.getCause());
			e.printStackTrace();
			return null;
		}

		try {
			/**
			 * compiling the Schema returns a SchemaTypeSystem, a kind of
			 * Meta-API or view on the Schema
			 * 
			 * @see org.apache.xmlbeans.SchemaTypeSystem
			 */
			schemaTypeSystem = XmlBeans.compileXsd(schema, XmlBeans
					.getBuiltinTypeSystem(), schemaOptions);
		}
		// if an Exception occured while compiling the the schema
		catch (XmlException e) {
			if (compErrors.isEmpty()) {
				System.out
						.println("[XMLSchemaProxy.generateSchemaTypeSystem] XmlException:"
								+ e.getCause());
				e.printStackTrace();
				return null;
			}

			System.out
					.println("[XMLSchemaProxy.generateSchemaTypeSystem] XmlException: "
							+ e.getCause() + "Schema invalid");
			for (Iterator i = compErrors.iterator(); i.hasNext();) {

				// if there are compile errors, print them out
				System.out
						.println("[XMLSchemaProxy.generateSchemaTypeSystem] XSD Error: "
								+ i.next());
			}
			e.printStackTrace();
			return null;
		}

		logger.exiting(this.getClass().getName(), "generateSchemaTypeSystem");

		// if nothing went wrong return the generated SchemaTypeSystem
		return schemaTypeSystem;
	}

	/**
	 * With this Method you can find an arbitrary element or type deklaration in
	 * the Schema
	 * 
	 * the Method needs a 'helper' Method findElementInSubtree(...) because to
	 * find an element in a xml schema you have to search the complete xml tree
	 * 
	 * the xmlBeans API provides only search methos for a schema which only
	 * search for global elements or types. i.e. elements or types which are at
	 * the first level of the xml schema tree. but when elements are not at the
	 * to level in the schema, you have to search the subtrees of the xml schema
	 * tree recursively and so does the 'helper' method findElementInSubtree...
	 * 
	 * the Method returns the SchemaType of an Element or Type Daklaration.
	 * Every XML Bean class corresponds to a singleton SchemaType object
	 * obtainable by ClassName.type
	 * 
	 * All types in a schema (for example xs:string or xs:int) are represented
	 * by a SchemaType, this includes all types regardless of whether they are
	 * built-in or user-defined, compiled or uncompiled, simple or complex.
	 * 
	 * @see org.apache.xmlbeans.SchemaType
	 * 
	 * @param elementName
	 *            the Element to find
	 * @return the schemaType of the found element, null if the element does not
	 *         exist
	 */
	public SchemaType findElementDeklaration(String elementName) {

		logger.entering(this.getClass().getName(), "findElement");
		logger.info("Element to find:" + elementName);

		/*
		 * Get the SchemaTypeSystem for the Schema
		 */
		SchemaTypeSystem sts = generateSchemaTypeSystem();

		// if the searched Element is a global element in the schema return its
		// SchemaType
		if (sts.findElement(new QName(elementName)) != null) {

			this.foundElement = sts.findElement(new QName(elementName))
					.getType();
			logger.info("Found Element: "
					+ sts.findElement(new QName(elementName)).getName());

		}

		// if the searched Element is a global type in the schema return its
		// SchemaType
		if (sts.findType(new QName(elementName)) != null) {
			this.foundElement = sts.findType(new QName(elementName));
			logger.info("Found Type: "
					+ sts.findType(new QName(elementName)).getName());
		}

		/**
		 * if the searched Element or Type is not at the top level of the xml
		 * Schema get all elements and types at the top level and search in all
		 * their subtrees
		 */
		else {

			// get all global elements of the schema
			SchemaGlobalElement[] globElements = sts.globalElements();

			// get all global types of the schema
			SchemaType[] globTypes = sts.globalTypes();

			/**
			 * if the global element is a complex type search in the 'subtree'
			 * of each global element
			 */
			for (int i = 0; i < globElements.length; i++) {
				if ((globElements[i].getType() != null)
						&& (!(globElements[i].getType().isSimpleType()))) {
					SchemaType st = globElements[i].getType();
					findElementInSubtree(elementName, st);
				}
			}
			/**
			 * if the global type is a complex type search in the 'subtree' of
			 * each global type
			 */
			for (int i = 0; i < globTypes.length; i++) {
				if ((globTypes[i] != null) && (!(globTypes[i].isSimpleType()))) {
					findElementInSubtree(elementName, globTypes[i]);
				}
			}
		}
		logger.exiting(this.getClass().getName(), "findElementDeklaration");

		// return the found Element
		return this.foundElement;
	}

	/**
	 * the 'helper' Method for findElementDeklaration which searches in the
	 * subtree of any complexType
	 * 
	 * @param element
	 * @param sType
	 */
	private void findElementInSubtree(String element, SchemaType sType) {
		logger.entering(this.getClass().getName(), "findElementInSubtree");

		// a schemaProperty represents a summary of similar SchemaFields in a
		// complex type, get all the subelements of the given SchemaType
		// i.e. get all the direct child nodes of the given node
		SchemaProperty[] properties = sType.getElementProperties();
		for (int i = 0; i < properties.length; i++) {
			// if the searched element is a child node of the given node return
			// its SchemaType
			if (properties[i].getName().toString().equals(element)) {
				this.foundElement = properties[i].getType();
				logger.info("Found Element: "
						+ properties[i].getName().toString());
			}
			// if the searched element was not a child of the given Node
			// then again for each of these child nodes search recursively in
			// their child nodes, in the case they are a complex type, because
			// only complex types have child nodes
			if ((properties[i].getType() != null)
					&& (!(properties[i].getType().isSimpleType()))) {
				SchemaType st = properties[i].getType();
				findElementInSubtree(element, st);
			}
		}
	}

	/**
	 * This Method returns all the Tables which are necessary to store instances
	 * (xml Documents) of the given xml Schema in the database. for each
	 * complexType in the schema a table is created. the columns of the table
	 * are the simple type child elements of this complex type. In the case that
	 * an element can occure more than once in a xml document (element
	 * deklaration with maxOccurs>1) there is also an own table for this element
	 * created
	 * 
	 * @return the vector which holds the elements of the Table datatype
	 */
	public Vector<Table> getSchemaProperties() {
		/**
		 * the logger entering the method
		 */
		logger.entering(this.getClass().getName(), "getSchemaProperties");
		// get the global microActivity Element which surrounds every msdt
		// definition in a schema
		SchemaType microActivity = findElementDeklaration("microActivity");
		Table microActivityTable = new Table("microActivity",
				new Vector<ColumnElement>());
		getElementProperties(microActivity, microActivityTable, "");

		// Information about the tables which are linked to store a msdt with
		// the given schema
		TableInformation inf = TableInformation.Instance();
		inf.addTableInformation(this.schemaName, tables);
		return this.tables;
	}

	public Table getCommonProperties() {

		/**
		 * the logger entering the method
		 */
		logger.entering(this.getClass().getName(), "getCommonProperties");
		SchemaType commonDataType = findElementDeklaration("commonDataType");

		Table commondata = new Table("commondata", new Vector<ColumnElement>());
		getElementProperties(commonDataType, commondata, "");

		return commondata;

	}

	/**
	 * This Method traverses the complete tree of a given node (element or type
	 * deklaration) in a xml schema. for each found node it explores the
	 * properties of the node and then decides how to map the properties to the
	 * database
	 * 
	 * complex type nodes --> new table
	 * 
	 * attributes of a complex type node --> new table
	 * 
	 * simple type nodes --> column in the table of the parent complex type node
	 * 
	 * for a simple type node the name and the type are extracted. the name of
	 * the corresponding column in a database table is the same as the node name
	 * and node type is mapped to a corresponding xml type.
	 * 
	 * @param schemaType
	 *            the schema type of a complex type element
	 * @param table
	 *            the Table instance to which the simple elements belong to
	 * @param father
	 *            the name of the complex father element of the current explored
	 *            element
	 */
	private void getElementProperties(SchemaType schemaType, Table table,
			String father) {

		/*
		 * the name of the father element of the current schemaType
		 */
		String fatherElementName = father;

		/*
		 * the logger entering the method
		 */
		logger.entering(this.getClass().getName(), "getElementProperties");

		/*
		 * get all child Elements of the given SchemaType
		 */
		SchemaProperty[] properties = null;
		if (schemaType.getElementProperties() != null) {
			properties = schemaType.getElementProperties();
		}

		/**
		 * for each global element of the given schemaType it has to be disposed
		 * whether it is a simple or a complex datatype in the case of a complex
		 * datatype a new Table instance is created and the method is called
		 * recursively for determining the global elements of this complex type
		 * and so on until all atomic elements of the schema are found
		 */
		for (int i = 0; i < properties.length; i++) {

			ColumnElement tableColumn = null;

			/*
			 * exploring each of the child elements of the schema Type
			 */
			SchemaProperty property = properties[i];

			/*
			 * SchemaType of the SchemaProperty Element which has to be explored
			 */
			SchemaType currentSchemaType = property.getType();

			String currentElementName = "";

			Table newTable;

			/*
			 * if the element has a name then set this name for
			 * currenElementName
			 */

			if (property.getName().toString() != null) {
				currentElementName = property.getName().toString();
				if (currentElementName.equalsIgnoreCase("commonData")) {
					continue;
				}
			}

			/*
			 * if the current element is a simple type
			 */
			if (currentSchemaType.isSimpleType()) {

				switch (currentSchemaType.getSimpleVariety()) {

				case org.apache.xmlbeans.SchemaType.ATOMIC:

					tableColumn = createElement(currentElementName,
							currentSchemaType);

					/*
					 * if the maxOccurs value of the element is > 1 a new table
					 * for only this element is created the name of the table
					 * consists of the name of the father element, then an
					 * underline followed by the name of the current explored
					 * element
					 */
					if (property.getMaxOccurs().compareTo((BigInteger.ONE)) == 1) {

						newTable = new Table(this.coreSchemaName + "_"
								+ fatherElementName + "_maxOccurs_"
								+ currentElementName,
								new Vector<ColumnElement>());
						newTable.addVectorElement(tableColumn);
						this.tables.add(newTable);
						break;

					}

					else
						table.addVectorElement(tableColumn);
					break;

				/*
				 * a list datatype contains a sequence of atomic datatypes the
				 * atomic datatype which is involved in the definition of the
				 * list is labeled as itemType. Every list element is stored in
				 * the database as a single column with the item data in the
				 * rows as BLOBs
				 */
				case org.apache.xmlbeans.SchemaType.LIST:

					tableColumn = createColumnElementWithGivenType(
							currentElementName, "BLOB");

					table.addVectorElement(tableColumn);
					break;

				case org.apache.xmlbeans.SchemaType.UNION:
					logger
							.warning("Union Datatypes are not supported by this SchemaProxy!");
					break;

				default:
					logger.warning("no default case in SchemaProxy");
					break;
				}
			}

			/**
			 * if the element is a complexType
			 */
			else {

				/*
				 * the Element is a complex Type complex Types are divided into
				 * four content types: Emtpy content, Simple Content, Element
				 * Content or Mixed Content
				 */

				/*
				 * all kinds of complex types may have attributes
				 * 
				 * if there are any attributes for a complex element then get
				 * all the attributes with their datatypes
				 */
				switch (currentSchemaType.getContentType()) {
				/**
				 * if the Content Type of the Element is empty there may be
				 * Attributes
				 * 
				 * An empty complex element cannot have contents, only
				 * attributes.
				 */
				case org.apache.xmlbeans.SchemaType.EMPTY_CONTENT:
					logger.info("EMPTY_CONTENT " + currentElementName);
					Table emptyTable = new Table(this.coreSchemaName + "_"
							+ currentElementName+"_attributes", new Vector<ColumnElement>());
					checkForAttributes(currentSchemaType, currentElementName,
							emptyTable);
					this.tables.add(emptyTable);
					break;

				/**
				 * if the content is a simple content
				 * 
				 * A complex text-only element can contain text and attributes.
				 */
				case org.apache.xmlbeans.SchemaType.SIMPLE_CONTENT:
					logger.info("SIMPLE_CONTENT " + currentElementName);
					Table simpleTable = new Table(this.coreSchemaName + "_"
							+ currentElementName, new Vector<ColumnElement>());
					// getElementProperties(currentSchemaType, simpleTable,
					// currentElementName);
					String xmlType = currentSchemaType.getContentBasedOnType()
							.getShortJavaName();
					// the column for the content
					ColumnElement content = createColumnElementWithGivenType(
							currentElementName, xmlType);
					// add this column to the table and then check for
					// attributes
					simpleTable.addVectorElement(content);
					checkForAttributes(currentSchemaType, currentElementName,
							simpleTable);
					this.tables.add(simpleTable);
					break;

				/**
				 * if the Content is an Element Content
				 * 
				 * A complex element contains other elements and/or attributes.
				 */
				case org.apache.xmlbeans.SchemaType.ELEMENT_CONTENT:
					logger.info("ELEMENT_CONTENT " + currentElementName);
					// if the elements are in a choice model, then there
					// must be only one column in the database table for all
					// the choice elements
					Table complexTable = new Table(this.coreSchemaName + "_"
							+ currentElementName, new Vector<ColumnElement>());
					// recursive call with the currentSchemaType with its
					// subelements and a new table
					getElementProperties(currentSchemaType, complexTable,
							currentElementName);
					// check the currentSchemaType for attributes
					checkForAttributes(currentSchemaType, currentElementName,
							complexTable);
					this.tables.add(complexTable);
					break;

				/**
				 * if the Content is a Mixed Content
				 * 
				 * A mixed complex type element can contain attributes,
				 * elements, and text.
				 */
				case org.apache.xmlbeans.SchemaType.MIXED_CONTENT:
					logger.info("MIXED_CONTENT " + currentElementName);

					Table mixedTable = new Table(this.coreSchemaName + "_"
							+ currentElementName, new Vector<ColumnElement>());
					// Column for the text content
					ColumnElement mixedContent = createColumnElementWithGivenType(
							currentElementName, "mixedContent");

					mixedTable.addVectorElement(mixedContent);
					// if there are any Sub-Elements
					if (currentSchemaType.getElementProperties().length > 0) {
						getElementProperties(currentSchemaType, mixedTable,
								currentElementName);
					}
					// check if there are any Attributes
					checkForAttributes(currentSchemaType, currentElementName,
							mixedTable);
					this.tables.add(mixedTable);
					break;
				default:
					break;
				}
			}

		}
		logger.exiting(this.getClass().getName(), "getElementProperties");
	}

	/**
	 * this method checks weather a complex type has any attributes and in the
	 * case there are some attributes they are appended as columns to the table
	 * of the complex type element
	 * 
	 * @param currentSchemaType
	 * @param schemaTypeName
	 * @param table
	 *            the table which represents the complex type element in the
	 *            database
	 */
	private void checkForAttributes(SchemaType currentSchemaType,
			String schemaTypeName, Table table) {
		// Vector<ColumnElement> attributeColumns = new Vector<ColumnElement>();
		if (currentSchemaType.getAttributeProperties().length > 0) {
			SchemaProperty[] attributes = currentSchemaType
					.getAttributeProperties();

			for (int j = 0; j < attributes.length; j++) {
				SchemaProperty attribute = attributes[j];
				String attributeName = attribute.getName().toString();
				String xsDatatype = attribute.getType().getShortJavaName();
				ColumnElement e = new ColumnElement(attributeName, null,
						xsDatatype);
				table.addVectorElement(e);
			}
		}
	}

	/**
	 * create a Column Element with a fixed given xml Datatype which is not
	 * extracted from the SchemaType of the node
	 * 
	 * @param elementName
	 * @param givenSchemaDatatype
	 * @return the instance of a ColumnElement with the given nam and xml type
	 */
	private ColumnElement createColumnElementWithGivenType(String elementName,
			String givenSchemaDatatype) {
		String xmlDatatypeName = givenSchemaDatatype;
		ColumnElement element = new ColumnElement(elementName, null,
				xmlDatatypeName);

		return element;
	}

	/**
	 * creat a ColumnElement instance from the schemaType of a simple type Node
	 * (Element or Type Deklaration)
	 * 
	 * @param elementName
	 * @param schemaType
	 * @return the instance of a ColumnElement with the extracted name and xml
	 *         type
	 */
	private ColumnElement createElement(String elementName,
			SchemaType schemaType) {
		String xmlDatatypeName;
		if (schemaType.getEnumerationValues() != null) {
			xmlDatatypeName = schemaType.getBaseType().getShortJavaName();

		} else {
			xmlDatatypeName = schemaType.getShortJavaName();
		}
		ColumnElement element = new ColumnElement(elementName, null,
				xmlDatatypeName);
		return element;
	}

	/**
	 * check for all tables which represent complex type elements in a schema
	 * whether the tables in the database are correct
	 */
	public void synchronizeSchemaToDatabase() {
		Vector<Table> schemaTables = new Vector<Table>();
		// getAll the Tables (complexType Elements) from the schema
		schemaTables = getSchemaProperties();
		for (Iterator iter = schemaTables.iterator(); iter.hasNext();) {
			Table table = (Table) iter.next();
			synchronizeTable(table);
		}
	}

	public void synchronizeCommonSchemaToDatabase() {
		Table commonTable = getCommonProperties();
		synchronizeTable(commonTable);
	}

	/**
	 * Check for each element and attribute deklaration in a schema whether they
	 * are represented in the corrensponding database table
	 * 
	 * @param schemaTable
	 *            the table information for a complex type element
	 */
	private void synchronizeTable(Table schemaTable) {
		logger.info("tablename: " + schemaTable.getTableName());
		if (!(dbCommunicator.tableExists(schemaTable.getTableName()))) {
			logger.info("Table " + schemaTable.getTableName()
					+ " does not exist in the database");
			dbCommunicator.executeStmt(CreateSQL.createTableStmt(schemaTable));
			return;
		}
		Vector schemaColumns = new Vector();
		schemaColumns = schemaTable.getElements();
		SqlDatatypes sqlTypes = new SqlDatatypes();
		sqlTypes.setSqlTypes4Elements(schemaColumns);

		Vector dbTableMetada = dbCommunicator
				.getMetadataInColumnOrder(schemaTable.getTableName());

		// complex type in schema = Table in Database
		// element deklaration in the complex type = column in the table

		// for each schema element in a complex type
		for (int i = 0; i < schemaColumns.size(); i++) {
			boolean correspondingColumnFound = false;
			// the current schema element
			ColumnElement schemaElement = (ColumnElement) schemaColumns.get(i);

			// search for the corresponding column in the database-table
			for (int j = 0; j < dbTableMetada.size(); j++) {
				// current column in the table
				ColumnElement dbColumn = (ColumnElement) dbTableMetada.get(j);
				// compare current schema element with current table column
				switch (ColumnElement.compareWithDbColumn(schemaElement,
						dbColumn)) {
				case ColumnElement.nameEqual:
					correspondingColumnFound = true;
					break;

				default:
					break;
				}
			}
			if (correspondingColumnFound)
				continue;
			else {
				dbCommunicator.executeStmt(CreateSQL.alterTableNewColumn(
						schemaTable, schemaElement));
			}
		}
	}
}
