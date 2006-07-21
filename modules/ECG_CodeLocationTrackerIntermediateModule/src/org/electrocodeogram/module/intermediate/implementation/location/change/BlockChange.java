/**
 * 
 */
package org.electrocodeogram.module.intermediate.implementation.location.change;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.electrocodeogram.module.intermediate.implementation.location.change.LineChange.LineChangeType;
import org.electrocodeogram.module.intermediate.implementation.location.state.Text;


/**
 * @author jekutsch
 *
 */
public class BlockChange {

    public enum BlockChangeType {
        CREATED,
        INSERTED,
        DELETED,
        CHANGED, 
        REPLACED,
        UNKNOWN
    }
    
    public static BlockChangeType convertTypeFromLineChange(LineChangeType type) {
        if (type == LineChangeType.INSERTED) {
            return BlockChangeType.INSERTED;
        } else if (type == LineChangeType.DELETED) {
            return BlockChangeType.DELETED;
        } else if (type == LineChangeType.CHANGED) {
            return BlockChangeType.CHANGED;
        }
        return BlockChangeType.UNKNOWN;
    }
    
    /**
     * 
     */
    private List<LocationChange> locationChanges = new ArrayList<LocationChange>();

    /**
     * 
     */
    private List<LineChange> lineChanges = new ArrayList<LineChange>();

    /**
     * 
     */
    private Date timeStamp;
    
    BlockChangeType blockType = BlockChangeType.UNKNOWN;
    int blockStart = -1;
    int blockLength = 1;
    Text text = null;

    /**
     * @param timeStamp
     */
    public BlockChange(Text text, Date timeStamp) {
        this.text = text;
        this.timeStamp = timeStamp;
    }
    
    public String toString() {
        return "at " + timeStamp + " " + blockLength + " line(s) were " + 
            blockType + " beginning at line number " + blockStart;
    }

    public BlockChange(Text text, Date timeStamp, BlockChangeType blockType, int blockStart, int blockLength) {
        this(text, timeStamp);
        this.blockType = blockType;
        this.blockStart = blockStart;
        this.blockLength = blockLength;
    }

    /**
     * @param locationChanges the locationChanges to set
     */
    protected void setLocationChanges(List<LocationChange> locationChanges) {
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

    public int getBlockLength() {
        return blockLength;
    }

    public void setBlockLength(int blockLength) {
        this.blockLength = blockLength;
    }

    public int getBlockStart() {
        return blockStart;
    }

    public void setBlockStart(int blockStart) {
        this.blockStart = blockStart;
    }

    public BlockChangeType getBlockType() {
        return blockType;
    }

    public void setBlockType(BlockChangeType blockType) {
        this.blockType = blockType;
    }

    public List<LineChange> getLineChanges() {
        return lineChanges;
    }

    public void setLineChanges(List<LineChange> lineChanges) {
        this.lineChanges = lineChanges;
    }
    
    public int getBlockEnd() {
        return blockStart + blockLength - 1;
    }

    public Text getText() {
        return text;
    }
    
}