/**
 * 
 */
package org.electrocodeogram.module.intermediate.implementation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;


public class Text {
    // line numbers begin with 0. Note that in events they start with 1
    public List<Line> lines = new ArrayList<Line>();
    public SortedSet<Location> locations = new TreeSet<Location>(LocationComparator.getComparator());
    public int nextLocationId = 0;
    
    public String printLocations() {
        String res = "";
        for (Location loc : locations)
            res += loc.toString();
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
            res += l.contents + "\n";
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
            res += l.linenumber + "(" + l.location.getId() + ")\t" + l.contents + "\n";
        res += "Locations: ";
        for (Location loc : locations)
            res += loc.getId() + "(" + loc.startLinenumber + (loc.length == 1 ? "" : "-" + (loc.startLinenumber+(loc.length-1))) + ") ";
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
}