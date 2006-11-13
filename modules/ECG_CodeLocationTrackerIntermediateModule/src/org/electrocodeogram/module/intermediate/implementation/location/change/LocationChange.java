package org.electrocodeogram.module.intermediate.implementation.location.change;

import org.electrocodeogram.module.intermediate.implementation.location.state.Location;



public class LocationChange {

    public enum LocationChangeType {
        INTIATED, // initial computation from a TextStatus not from a Diff, implies a creation of location 
        ADDED, // due to inserted lines, implies a creation of location
        CHANGED, // due to changed lines
        MERGED_DEL_AT_START, // due to merge, implies deletion of location
        MERGED_DEL_AT_END, // due to merge, implies deletion of location 
        MERGED_ADD_AT_START, // due to merge, implies change of location
        MERGED_ADD_AT_END, // due to merge, implies change of location 
        EXTENDED_AT_START, // due to inserted lines, implies change of location
        EXTENDED_IN_BETWEEN, // due to inserted lines, implies change of location 
        EXTENDED_AT_END, // due to inserted lines, implies change of location
        SPLIT_DEL_AT_START, // due to split (changed or inserted lines), implies a change of location 
        SPLIT_DEL_AT_END, // due to split (changed or inserted lines), implies a change of location
        SPLIT_ADD_AT_START, // due to split (changed or inserted lines), implies a creation of location
        SPLIT_ADD_AT_END, // due to split (changed or inserted lines), implies a creation of location
        SHORTENED_AT_ALL, // due to deleted lines, implies deletion of location
        SHORTENED_AT_START, // due to deleted lines, implies change of location
        SHORTENED_IN_BETWEEN, // due to deleted lines, implies change of location
        SHORTENED_AT_END, // due to deleted lines, implies change of location
        UNKNOWN
    }
    
    private BlockChange blockChange = null;  // if null, it is of type initiation
    private String contents = null;
    private LocationChangeType type = LocationChangeType.UNKNOWN;
    private Location location = null;
    private int relatedLocId = -1;
    
    public LocationChange(Location location, 
            LocationChangeType type,
            int relatedLocId) {
        this.location = location;
        this.type = type;
        this.contents = (isAlive() ? location.getContents() : "");
        this.relatedLocId = relatedLocId;
    }
    
    public boolean isAlive() {
        return (type != LocationChangeType.MERGED_DEL_AT_START && 
                type != LocationChangeType.MERGED_DEL_AT_END &&
                type != LocationChangeType.SHORTENED_AT_ALL);
    }
    
    public boolean isFresh() {
        return (type == LocationChangeType.INTIATED || 
                type == LocationChangeType.ADDED ||
                type == LocationChangeType.SPLIT_ADD_AT_START ||
                type == LocationChangeType.SPLIT_ADD_AT_END);        
    }
    
    public boolean isSplit() {    
        return (type == LocationChangeType.SPLIT_ADD_AT_END ||
                type == LocationChangeType.SPLIT_ADD_AT_START || 
                type == LocationChangeType.SPLIT_DEL_AT_START ||
                type == LocationChangeType.SPLIT_DEL_AT_END);
    }
    
    public boolean isMerge() {
        return (type == LocationChangeType.MERGED_ADD_AT_END ||
                type == LocationChangeType.MERGED_ADD_AT_START || 
                type == LocationChangeType.MERGED_DEL_AT_START ||
                type == LocationChangeType.MERGED_DEL_AT_END);        
    }
    
    public String toString() {
        String res = "(" + location.getId() + ") of type " + this.type;
        if (isSplit() || isMerge())
            res += " (-> " + relatedLocId + ")"; 
        return res;
    }
    
    /**
     * @param blockChange the blockChange to set
     */
    public void setBlockChange(BlockChange blockChange) {
        this.blockChange = blockChange;
    }

    /**
     * @return the blockChange
     */
    public BlockChange getBlockChange() {
        return blockChange;
    }

    /**
     * @param contents the contents to set
     */
    public void setContents(String contents) {
        this.contents = contents;
    }

    /**
     * @return the contents
     */
    public String getContents() {
        return contents;
    }

    /**
     * @param type the type to set
     */
    public void setType(LocationChange.LocationChangeType type) {
        this.type = type;
    }

    /**
     * @return the type
     */
    public LocationChange.LocationChangeType getType() {
        return type;
    }

    /**
     * @param locId the locId to set
     */
    public void setLocation(Location loc) {
        this.location = loc;
    }

    /**
     * @return the locId
     */
    public Location getLocation() {
        return this.location;
    }

    /**
     * @param relatedLocId the relatedLocId to set
     */
    public void setRelatedLocId(int relatedLocId) {
        this.relatedLocId = relatedLocId;
    }

    /**
     * @return the relatedLocId
     */
    public int getRelatedLocId() {
        return relatedLocId;
    }
    
    /**
     * 
     */
    public String asEventString(String creator, String id, String projectname, String username) {
        /* For some myterious reason, the xml way doesn't work here. The nodes (apart from the cdata section) simply ignore their values
        MicroActivity microActivity;
        Element locchange_path;
        Element locchange_type;
        Element locchange_id;
        Element locchange_related;
        Element locchange_location;
        Element locchange_contents;
        CDATASection locchange_contents_cdata;
        
        microActivity = new MicroActivity();

        Document microactivity_doc = microActivity.getMicroActivityDoc();
        Element locchange = microactivity_doc.createElement("codelocation");
        locchange_path = microactivity_doc.createElement("path");
        locchange_type = microactivity_doc.createElement("type");
        locchange_id = microactivity_doc.createElement("id");
        locchange_related = microactivity_doc.createElement("related");
        locchange_location = microactivity_doc.createElement("location");
        locchange_contents = microactivity_doc.createElement("contents");
        locchange_contents_cdata = microactivity_doc.createCDATASection("");

        locchange.appendChild(locchange_path);
        locchange.appendChild(locchange_type);
        locchange.appendChild(locchange_id);
        locchange.appendChild(locchange_related);
        locchange.appendChild(locchange_location);
        locchange.appendChild(locchange_contents);
        locchange_contents.appendChild(locchange_contents_cdata);

        microActivity.setCustomElement(locchange);            

        CommonData commonData = microActivity.getCommonData();
        commonData.setVersion(1); // 1 is default
        commonData.setCreator(CREATOR_STRING); 
        commonData.setUsername(getUsername(eventPacket));
        commonData.setProjectname(getProjectname(eventPacket));
        
        for (LocationChange lc : locChanges) {
            locchange_path.setNodeValue(""); // TODO
            locchange_type.setNodeValue(lc.getType().name());
            locchange_id.setNodeValue(Integer.toString(lc.getLocation().getId()));
            locchange_related.setNodeValue(Integer.toString(lc.getRelatedLocId()));
            locchange_location.setNodeValue(
                    lc.getLocation().getText().getId() + ";" +  
                    lc.getLocation().getStart() + ";" +
                    lc.getLocation().getLength());
            locchange_contents_cdata.setTextContent(lc.getContents());

            String data = microActivity.getSerializedMicroActivity(); 
*/

        String data = "<?xml version=\"1.0\"?><microActivity><commonData>";
        data += "<version>1</version>";
        data += "<creator>" + creator + "</creator>";
        data += "<username>" + username + "</username>";
        data += "<projectname>" + projectname + "</projectname>";
        data += "<id>" + id + "</id>";
        data += "</commonData><codelocation>";
        data += "<path>" + "" + "</path>"; // TODO
        data += "<type>" + this.getType().name() + "</type>";
        data += "<id>" + Integer.toString(this.getLocation().getId()) + "</id>";
        data += "<related>" + Integer.toString(this.getRelatedLocId()) + "</related>";
        data += "<location>" + this.getLocation().getText().getId() + ";" + 
                this.getLocation().getStart() + ";" + 
                this.getLocation().getLength() + "</location>";
        data += "<contents><![CDATA[" + this.getContents() + "]]></contents>";
        data += "</codelocation></microActivity>";
//System.out.println(data);
        return data;
    }

}
