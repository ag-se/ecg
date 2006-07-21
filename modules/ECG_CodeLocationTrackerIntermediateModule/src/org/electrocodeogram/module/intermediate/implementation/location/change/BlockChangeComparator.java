/**
 * 
 */
package org.electrocodeogram.module.intermediate.implementation.location.change;

import java.util.Comparator;



public class BlockChangeComparator implements Comparator<BlockChange> {

    private static BlockChangeComparator comparator = null;
    
    private BlockChangeComparator() {
    }
    
    public int compare(BlockChange bc1, BlockChange bc2) {
        if (bc1 == bc2)
            return 0;
        if (bc1 == null || bc2 == null)
            return 0;
        if (bc1.getLocationChanges().size() < bc2.getLocationChanges().size())
            return 1;
        if (bc1.getLocationChanges().size() > bc2.getLocationChanges().size())
            return -1;
        return 0;
    }
    
    public static BlockChangeComparator getComparator() {
        if (comparator == null)
            comparator = new BlockChangeComparator();
        return comparator;
    }    
}