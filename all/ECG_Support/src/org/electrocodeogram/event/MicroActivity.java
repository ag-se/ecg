/**
 * 
 */
package org.electrocodeogram.event;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * MicroActivity is a data structure which encapsulates the generation 
 * of the XML section of every ECG event, where <microActivity> is the
 * root element always containing a <commonData> followed by a custom 
 * XML element
 * 
 * TODO: Could better be done with a framework like Castor etc.
 */
public class MicroActivity {

    private CommonData commonData = null;
    
    // XML elements 
    private Document microactivity_doc;
    private Element microactivity_elem;
    private Element commondata_elem;
    private Element custom_elem = null;

    public MicroActivity() {
        microactivity_doc = XMLSupport.getDocumentBuilder().newDocument();
        microactivity_elem = microactivity_doc.createElement("microActivity");
        microactivity_doc.appendChild(microactivity_elem);
        commonData = new CommonData(microactivity_doc);
        commondata_elem = commonData.getCommonDataElement();
        microactivity_elem.appendChild(commondata_elem);
    }

/*        
    public MicroActivity(CommonData commonData, Element custom) {
        microactivity_doc = XMLSupport.getDocumentBuilder().newDocument();
        commonData = new CommonData(microactivity_doc);
        commondata_elem = commonData.getCommonDataElement();
        custom_elem = custom;
        microactivity_doc.appendChild(commondata_elem);
        microactivity_doc.appendChild(custom_elem);
    }

    public MicroActivity(int version, String username, String projectname, String id, Element custom) {
        microactivity_doc = XMLSupport.getDocumentBuilder().newDocument();
        commonData = new CommonData(version, username, projectname, id, microactivity_doc);
        commondata_elem = commonData.getCommonDataElement();
        custom_elem = custom; 
        microactivity_doc.appendChild(commondata_elem);
        microactivity_doc.appendChild(custom_elem);
    }
*/    
    public void setCustomElement(Element custom) {
        custom_elem = custom;
        microactivity_elem.appendChild(custom_elem);                
    }
    
    public Document getMicroActivityDoc() {
        return microactivity_doc;
    }
    
    public Element getMicroActivityElem() {
        return microactivity_elem;
    }
    
    public String getSerializedMicroActivity() {
        return XMLSupport.serialize(microactivity_doc);
    }
    
    public CommonData getCommonData() {
        return commonData;
    }
  
}
