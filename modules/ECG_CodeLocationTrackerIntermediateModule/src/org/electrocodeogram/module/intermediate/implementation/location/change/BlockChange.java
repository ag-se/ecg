/**
 * 
 */
package org.electrocodeogram.module.intermediate.implementation.location.change;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.misc.xml.ECGParser;
import org.electrocodeogram.misc.xml.NodeException;
import org.electrocodeogram.module.intermediate.implementation.location.change.LineChange.LineChangeType;
import org.electrocodeogram.module.intermediate.implementation.location.state.IText;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


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
    IText text = null;

    /**
     * @param timeStamp
     */
    public BlockChange(IText text, Date timeStamp) {
        this.text = text;
        this.timeStamp = timeStamp;
    }
    
    public String toString() {
        return "at " + timeStamp + " " + blockLength + " line(s) were " + 
            blockType + " beginning at line number " + blockStart;
    }

    public BlockChange(IText text, Date timeStamp, BlockChangeType blockType, int blockStart, int blockLength) {
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

    public IText getText() {
        return text;
    }
    
    // ---------------------------------------------------
    
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
    
    public static List<BlockChange> parseLineDiffsEvent(IText text, ValidEventPacket eventPacket) {
        //  TODO Better DOM usage necessary!
        Document xmlDoc = eventPacket.getDocument();
        Node[] linediffs = null;
        try {
            linediffs = ECGParser.getChildNodes( 
                    ECGParser.getChildNode(
                            ECGParser.getChildNode(
                                    ECGParser.getChildNode(xmlDoc, "microActivity"), 
                                    "linediff"),
                            "lines"),  
                    "line"); 
        } catch (NodeException e) {
            // TODO report this
            e.printStackTrace();
            return null;
        }
        
        List<BlockChange> blockChanges = new ArrayList<BlockChange>();
        // The blockChange is for putting line changes together in blocks
        BlockChange blockChange = null;
    
        // process each line diff
        for (int i = 0; i < linediffs.length; i++) {
            
            // parse line change
            Node linediff = linediffs[i];
            LineChangeType lineType = LineChangeType.UNKNOWN;
            String from = null;
            String to = null;
            LineChange lineChange = null;
            int linenumber = -1;
            try {
                String diffType = ECGParser.getNodeValue(ECGParser.getChildNode(linediff, "type"));
                lineType = LineChange.getLineChangeTypeFromString(diffType);
                linenumber = Integer.parseInt(
                    ECGParser.getNodeValue(ECGParser.getChildNode(linediff, "linenumber")));
                linenumber -= 1; // in events the line numbers start with 1
                if (ECGParser.hasChildNode(linediff, "from"))
                    from = ECGParser.getNodeValue(ECGParser.getChildNode(linediff, "from"));
                if (from == null)
                    from = "";
                if (ECGParser.hasChildNode(linediff, "to"))
                    to = ECGParser.getNodeValue(ECGParser.getChildNode(linediff, "to"));
                if (to == null)
                    to = "";
                lineChange = new LineChange(lineType, linenumber, from, to);
            } catch (NodeException e) {
                // TODO report this
                e.printStackTrace();
            }
            
            // Convert from LineChangeType to BlockChangeType
            BlockChangeType blockType = BlockChange.convertTypeFromLineChange(lineType);
            
    /*
    System.out.println("  New change: '" + type + "' at line " + linenumber);
    System.out.println("  from:" + from);
    System.out.println("    to:" + to);
    System.out.println("  at " + eventPacket.getTimeStamp() + "\n");
    */
    
            // The following is segmenting a LineDiff event in a set of linediff blocks of 
            //   equal type and consequtive line numbers which is required by the location
            //   change and identity retaining algorithm
            if (blockChange == null) {
                // Initialize blockChange, a line change collector to combine similar consequtive line changes
                blockChange = new BlockChange(text, eventPacket.getTimeStamp(), blockType, linenumber, 1);
                blockChange.getLineChanges().add(0, lineChange);
                blockChanges.add(blockChange);
            } else {
                // Check for break in consequtiveness of the line changes
                BlockChangeType oldBlockType = blockChange.getBlockType();
                int blockStart = blockChange.getBlockStart();
                int blockLength = blockChange.getBlockLength();                
                if (blockType != oldBlockType || linenumber != blockStart + blockLength)  
                {
                    // if block type changes or if line number is not consequtive
                    blockChange = new BlockChange(text, eventPacket.getTimeStamp(), blockType, linenumber, 1);
                    blockChange.getLineChanges().add(0, lineChange);
                    blockChanges.add(blockChange);
                }   
                else {
                    // put this line in blockChange as well
                    blockChange.setBlockLength(blockLength+1);
                    blockChange.getLineChanges().add(blockLength, lineChange);
                }
            }            
        }
        return blockChanges;
    }
}