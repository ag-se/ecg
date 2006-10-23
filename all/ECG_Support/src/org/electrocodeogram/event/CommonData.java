/**
 * (c) Freie Universitaet Berlin, 2006
 */
package org.electrocodeogram.event;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * CommonData is a data structure which encapsulates the generation 
 * of the common data section which is part of every ECG event
 * 
 * TODO: Could better be done with a framework like Castor etc.
 *
 */
public class CommonData {
    
    private int version = 0;
    private String creator = null;
    private String username = null;
    private String projectname = null;
    private String id = null;
    
    // XML elements 
    private Element commondata_elem;
    private Element version_elem;
    private Element creator_elem;
    private Element username_elem;
    private Element projectname_elem;
    private Element id_elem;        


    /**
     * Creates simple common data
     * 
     * @param version
     * @param username
     * @param projectname
     * @param id
     */
    public CommonData(int version, String creator, String username, String projectname, String id) {
        this(version, creator, username, projectname, id, XMLSupport.getDocumentBuilder().newDocument());
    }
    
    public CommonData(Document doc) {
        this(1, null, null, null, null, doc);
    }
    
    public CommonData(int version, String creator, String username, String projectname, String id, Document doc) {
        commondata_elem = doc.createElement("commonData");
        version_elem = doc.createElement("version");
        creator_elem = doc.createElement("creator");
        username_elem = doc.createElement("username"); 
        projectname_elem = doc.createElement("projectname");
        id_elem = doc.createElement("id");
        commondata_elem.appendChild(version_elem);
        commondata_elem.appendChild(creator_elem);
        commondata_elem.appendChild(username_elem);
        commondata_elem.appendChild(projectname_elem);
        commondata_elem.appendChild(id_elem);
        setVersion(version);
        setCreator(creator);
        setUsername(username);
        setProjectname(projectname);
        setId(id);
    }
    
    public Element getCommonDataElement() {
        return commondata_elem;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
        id_elem.setTextContent(id);
    }

    /**
     * @return the projectname
     */
    public String getProjectname() {
        return projectname;
    }

    /**
     * @param projectname the projectname to set
     */
    public void setProjectname(String projectname) {
        this.projectname = projectname;
        projectname_elem.setTextContent(projectname);
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
        username_elem.setTextContent(username);
    }

    /**
     * @return the version
     */
    public int getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(int version) {
        this.version = version;
        version_elem.setTextContent(Integer.toString(version));
    }
    
    /**
     * @param creator the cretaor string to set
     */
    public void setCreator(String creator) {
        this.creator = creator;
        creator_elem.setTextContent(creator);
    }
    
    
  
}
