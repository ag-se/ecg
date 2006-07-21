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
        SPLIT_ADD_AT_END, // due to split (changed or inserted lines), implies a creation of location
        SPLIT_ADD_AT_START, // due to split (changed or inserted lines), implies a creation of location
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

}
