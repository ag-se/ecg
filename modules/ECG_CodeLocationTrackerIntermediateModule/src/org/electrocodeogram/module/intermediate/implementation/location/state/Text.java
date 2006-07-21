/**
 * 
 */
package org.electrocodeogram.module.intermediate.implementation.location.state;

import java.util.ArrayList;
import java.util.Collection;
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
            res += loc.getContents();
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
     * @return the nextLocationId
     */
    private int getNextLocationId() {
        return nextLocationId++;
    }

    // --------------------------------------------

    public void addLocation(Location loc) {
        if (loc.getId() < 0)
            loc.setId(this.getNextLocationId());
        loc.setText(this);
        this.locations.add(loc);
    }
    
    public void removeLocation(Location loc) {
        this.locations.remove(loc);
    }
    
    public Location getLastLocation() {
        return this.locations.last();
    }
    
    public Collection<Location> getLocations() {
        return this.locations;
    }
    
    public SortedSet<Location> getLastLocations(Location loc) {
        return this.locations.tailSet(loc);
    }

    public boolean containsLocation(Location loc) {
        return this.locations.contains(loc);
    }
    
    // --------------------------------------------
    
    public void changeLine(int lnr, Line line) {
        this.lines.set(lnr, line);
    }
    
    public void insertLine(int lnr, Line line) {
        this.lines.add(lnr, line);
    }
    
    public void deleteLine(int lnr) {
        this.lines.remove(lnr);
    }
    
    public void setLines(List<Line> lines) {
        this.lines = lines;
    }
    
    /**
     * @param lnr line number
     * @return Line at line numner lnr if available, null otherwise
     */
    public Line getLine(int lnr) {
        if (lnr >= 0 && lnr < this.lines.size())
            return this.lines.get(lnr);
        return null;
    }
    
    /**
     * @param lnr1
     * @param lnr2
     * @return list of line from line numbers lnr1 inclusive and lnr2 exclusive
     */
    public List<Line> getLines(int lnr1, int lnr2) {
        return this.lines.subList(lnr1, lnr2);
    }
    
    public List<Line> getLastLines(int lnr) {
        return getLines(lnr, this.lines.size());
    }

}