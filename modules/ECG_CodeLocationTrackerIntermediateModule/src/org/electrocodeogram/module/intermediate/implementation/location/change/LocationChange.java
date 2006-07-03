package org.electrocodeogram.module.intermediate.implementation.location.change;



public class LocationChange {

    public enum LocationChangeType {
        FORKED,
        CHANGED, // without split or extend
        EMPTIED, 
        MERGEDAWAY, 
        EXTENDEDABOVE, // inplies a changed
        EXTENDEDBELOW, // inplies a changed
        INTIATED, 
        SPLITBELOW,
        SPLITABOVE
    }
    
    private BlockChange blockChange = null;  // if null, it is of type initiation
    private String contents = null;
    private LocationChange.LocationChangeType type = null;
    private int locId = -1;
    private int relatedLocId = -1;
    
    public LocationChange(int locId, 
            LocationChangeType type, 
            String contents, 
            int relatedLocId, 
            BlockChange blockChange) {
        this.type = type;
        this.contents = contents;
        this.blockChange = blockChange;
        this.locId = locId;
        this.relatedLocId = relatedLocId;
    }
    
    public String toString() {
        String res = "(" + locId + ") at ";
        if (blockChange != null)
            res += this.blockChange.getTimeStamp() + ", of type " + this.type;
        else
            res += "the beginning, of type " + this.type;
        if (type == LocationChangeType.FORKED ||
                type == LocationChangeType.MERGEDAWAY || 
                type == LocationChangeType.EXTENDEDABOVE ||
                type == LocationChangeType.EXTENDEDBELOW ||
                type == LocationChangeType.SPLITABOVE ||
                type == LocationChangeType.SPLITBELOW)
            res += " (-> " + relatedLocId + ")"; 
        return res;
    }
    
    public String printContents() {
        return this.contents;
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
    public void setLocId(int locId) {
        this.locId = locId;
    }

    /**
     * @return the locId
     */
    public int getLocId() {
        return locId;
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
