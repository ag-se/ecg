/**
 * 
 */
package org.electrocodeogram.module.intermediate.implementation.location.change;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;




/**
 * @author jekutsch
 *
 */
public class BlockChange {

    /**
     * 
     */
    private List<LineChange> lineChanges = new ArrayList<LineChange>();

    /**
     * 
     */
    private List<LocationChange> locationChanges = new ArrayList<LocationChange>();

    /**
     * 
     */
    private Date timeStamp;
    
    /**
     * @param timeStamp
     */
    public BlockChange(Date timeStamp) {
        this.timeStamp = timeStamp;
    }
    
    public String toString() {
        return lineChanges.size() + " line changes in " + locationChanges.size() + " location changes at " + timeStamp;
    }

    /**
     * @param lineChanges the lineChanges to set
     */
    public void setLineChanges(List<LineChange> lineChanges) {
        this.lineChanges = lineChanges;
    }

    /**
     * @return the lineChanges
     */
    public List<LineChange> getLineChanges() {
        return lineChanges;
    }

    /**
     * @param locationChanges the locationChanges to set
     */
    public void setLocationChanges(List<LocationChange> locationChanges) {
        this.locationChanges = locationChanges;
    }

    /**
     * @return the locationChanges
     */
    public List<LocationChange> getLocationChanges() {
        return locationChanges;
    }

    /**
     * @param timeStamp the timeStamp to set
     */
    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * @return the timeStamp
     */
    public Date getTimeStamp() {
        return timeStamp;
    }
    
}