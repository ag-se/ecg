/**
 * 
 */
package org.electrocodeogram.module.target.implementation;

import java.util.HashMap;
import java.util.Vector;

/**
 * This class is responsible for mapping the xml schema types to sql types
 * @author jule
 * @version 1.0
 */
public class SqlDatatypes {

    private HashMap <String,String>xml_sql_dt = new HashMap <String,String>();

    /**
     * empty constructor
     */
    public SqlDatatypes() {
        addDatatypes();
    }

    private void addDatatypes(){

        xml_sql_dt.put("XmlID", "VARCHAR(255)");
        xml_sql_dt.put("SimpleValue", "VARCHAR(255)");
        xml_sql_dt.put("XmlAnySimpleType", "TEXT");
        xml_sql_dt.put("XmlAnyURI", "VARCHAR(255)");
        xml_sql_dt.put("XmlBase64Binary", "BLOB(255)");
        xml_sql_dt.put("XmlBoolean", "TINYINT(1)");
        xml_sql_dt.put("XmlByte", "TINYINT");
        xml_sql_dt.put("XmlDate", "DATE");
        xml_sql_dt.put("XmlDateTime", "DATETIME");
        xml_sql_dt.put("XmlDecimal", "DECIMAL");
        xml_sql_dt.put("XmlDouble", "DOUBLE");
        xml_sql_dt.put("XmlDuration", "TIMESTAMP");
        xml_sql_dt.put("XmlENTITIES", "TEXT");
        xml_sql_dt.put("XmlENTITY", "TEXT");
        xml_sql_dt.put("XmlFloat", "FLOAT");
        xml_sql_dt.put("XmlGDay", "TIMESTAMP(8)");
        xml_sql_dt.put("XmlGMonth", "TIMESTAMP(8)");
        xml_sql_dt.put("XmlGMonthDay", "TIMESTAMP(8)");
        xml_sql_dt.put("XmlGYear", "YEAR(4)");
        xml_sql_dt.put("XmlGYearMonth", "TIMESTAMP(8)");
        xml_sql_dt.put("XmlHexBinary", "BLOB(255)");
        xml_sql_dt.put("XmlIDREF", "VARCHAR(255)");
        xml_sql_dt.put("XmlIDREFS", "TEXT");
        xml_sql_dt.put("XmlInt", "INT");
        xml_sql_dt.put("XmlInteger", "INTEGER");
        xml_sql_dt.put("XmlLanguage", "VARCHAR(255)");
        xml_sql_dt.put("XmlLong", "BIGINT");
        xml_sql_dt.put("XmlName", "VARCHAR(255)");
        xml_sql_dt.put("XmlNCName", "VARCHAR(255)");
        xml_sql_dt.put("XmlNegativeInteger", "");
        xml_sql_dt.put("XmlNMTOKEN", "VARCHAR(255)");
        xml_sql_dt.put("XmlNMTOKENS", "TEXT");
        xml_sql_dt.put("XmlNonNegativeInteger", "INTEGER");
        xml_sql_dt.put("XmlNonPositiveInteger", "INTEGER");
        xml_sql_dt.put("XmlNormalizedString", "TEXT");
        xml_sql_dt.put("XmlNOTATION", "TEXT");
        xml_sql_dt.put("XmlPositiveInteger", "INTEGER");
        xml_sql_dt.put("XmlQName", "VARCHAR(255)");
        xml_sql_dt.put("XmlShort", "SMALLINT");
        xml_sql_dt.put("XmlString", "TEXT");
        xml_sql_dt.put("XmlTime", "TIME");
        xml_sql_dt.put("XmlToken", "VARCHAR(255)");
        xml_sql_dt.put("XmlUnsignedByte", "TINYINT");
        xml_sql_dt.put("XmlUnsignedInt", "INT");
        xml_sql_dt.put("XmlUnsignedLong", "BIGINT");
        xml_sql_dt.put("XmlUnsignedShort", "SMALLINT");
        xml_sql_dt.put("BLOB", "BLOB");
        xml_sql_dt.put("mixedContent", "TEXT");

    }    



    /**
     * This Method sets in ColumnElement the sql Type for a given xml schema type
     * @param e the ColumnElement
     * @return the ColumnElement with the mapped sql Type
     */
    public ColumnElement setSqlType4XmlType(ColumnElement e) {
        ColumnElement myElement = e;
        String xmlType = myElement.getXmlType();
        if (this.xml_sql_dt.containsKey(xmlType)) {
            String sqlType = (String) xml_sql_dt.get(xmlType);
            myElement.setSqlType(sqlType);
            return myElement;
        }
        else
            return null;
    }

    /**
     * This method sets for all ColumnElements of a Table the sql Types 
     * @param v the Vector with the ColumnElements of a Table
     */
    public void setSqlTypes4Elements(Vector v) {
        Vector elementVector = v;
        for (int i = 0; i < elementVector.size(); i++) {
            ColumnElement e = (ColumnElement) elementVector.get(i);
            setSqlType4XmlType(e);
        }
       
    }

}
