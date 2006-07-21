/**
 * 
 */
package org.electrocodeogram.module.intermediate.implementation.location.state;

import java.util.Comparator;



public class LocationComparator implements Comparator<Location> {

    private static LocationComparator comparator = null;
    
    private LocationComparator() {
    }
    
    /**
     * orderes according to the start line number (regardless of the length)
     * invalid locations are handled as greater than valid locations
     *  
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Location loc1, Location loc2) {
        if (loc1 == loc2)
            return 0;
        if (loc1 == null || loc2 == null)
            return 0;
        if (loc1.isInvalid()) {
            if (loc2.isInvalid())
                return Integer.signum(loc1.getId() - loc2.getId());
            else
                return 1;
        }
        if (loc2.isInvalid()) // here loc1 isnt invalid
            return -1;
        if (loc1.getStart() > loc2.getStart())
            return 1;
        if (loc1.getStart() < loc2.getStart())
            return -1;
        return 0;
    }
    
    public static LocationComparator getComparator() {
        if (comparator == null)
            comparator = new LocationComparator();
        return comparator;
    }    
}