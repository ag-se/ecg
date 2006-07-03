/**
 * 
 */
package org.electrocodeogram.module.intermediate.implementation.location.state;

import java.util.Comparator;



public class LocationComparator implements Comparator<Location> {

    private static LocationComparator comparator = null;
    
    private LocationComparator() {
    }
    
    public int compare(Location loc1, Location loc2) {
        if (loc1 == loc2)
            return 0;
        if (loc1 == null || loc2 == null)
            return 0;
        if (loc1.getStartLinenumber() > loc2.getStartLinenumber())
            return 1;
        if (loc1.getStartLinenumber() < loc2.getStartLinenumber())
            return -1;
/*
        if (loc1.startLinenumber == loc2.startLinenumber)
            if (loc1.length < loc2.length) // does this make sense?
                return -1;
            else
                return 1;    
*/
        return 0;
    }
    
    public static LocationComparator getComparator() {
        if (comparator == null)
            comparator = new LocationComparator();
        return comparator;
    }    
}