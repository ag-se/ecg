/**
 * 
 */
package org.electrocodeogram.module.intermediate.implementation;

import java.util.Comparator;

public class LocationComparator implements Comparator<Location> {

    private static LocationComparator comparator = null;
    
    public int compare(Location loc1, Location loc2) {
        if (loc1 == null) {
            if (loc2 == null)
                return 0;
            return -1;
        }
        if (loc2 == null)
            return 1;
        if (loc1 == loc2)
            return 0;
        if (loc1.startLinenumber > loc2.startLinenumber)
            return 1;
        if (loc1.startLinenumber < loc2.startLinenumber)
            return -1;            
        return 0;
    }
    
    public static LocationComparator getComparator() {
        if (comparator == null)
            comparator = new LocationComparator();
        return comparator;
    }    
}