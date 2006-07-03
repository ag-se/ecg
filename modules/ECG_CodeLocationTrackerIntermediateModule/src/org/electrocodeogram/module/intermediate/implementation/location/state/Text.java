/**
 * 
 */
package org.electrocodeogram.module.intermediate.implementation.location.state;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;




public class Text {
    // line numbers begin with 0. Note that in events they start with 1
    private List<Line> lines = new ArrayList<Line>();
    private SortedSet<Location> locations = new TreeSet<Location>(LocationComparator.getComparator());
    private int nextLocationId = 0;
    
    public String printLocations() {
        String res = "";
        for (Location loc : locations)
            res += loc.printLocation();
        return res;
    }        

    public String printLines() {
        String res = "";
        for (Line l : lines)
            res += l.toString();
        return res;
    }
    
    public String printContents() {
        String res = "";
        for (Line l : lines)
            res += l.getContents() + "\n";
        return res;            
    }
    
    public String printContentsFromLocations() {
        String res = "";
        for (Location loc : locations)
            res += loc.printContents();
        return res;
    }
    
    public String toString() {
        String res = "";
        for (Line l : lines)
            res += l.getLinenumber() + "(" + l.getLocation().getId() + ")\t" + l.getContents() + "\n";
        res += "Locations: ";
        for (Location loc : locations)
            res += loc.toString() + " ";
        res += "\n";
        return res;
    }
    
    public boolean checkValidity() {
        boolean res = true;
        for (Location loc : this.locations) {
            res &= loc.checkValidity();
        }
        for (Line lin : this.lines) {
            res &= lin.checkValidity();
        }
        return res;
    }

    /**
     * @param lines the lines to set
     */
    public void setLines(List<Line> lines) {
        this.lines = lines;
    }

    /**
     * @return the lines
     */
    public List<Line> getLines() {
        return lines;
    }

    /**
     * @param locations the locations to set
     */
    public void setLocations(SortedSet<Location> locations) {
        this.locations = locations;
    }

    /**
     * @return the locations
     */
    public SortedSet<Location> getLocations() {
        return locations;
    }

    /**
     * @return the nextLocationId
     */
    public int getNextLocationId() {
        return nextLocationId++;
    }
}