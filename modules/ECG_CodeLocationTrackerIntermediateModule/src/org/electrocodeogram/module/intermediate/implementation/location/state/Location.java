/**
 * FU Berlin, 2006 
 */

package org.electrocodeogram.module.intermediate.implementation.location.state;



/**
 * 
 */
public class Location {

    public static final int MIN_COHESION = 0; // min. level of cohesion in a location

    private int id = 0;
	private int startLinenumber = 0;
    private int length = 0;
    private Text text;
    
    public Location(Text text) {
        this.text = text;
        this.id = text.getNextLocationId();
    }
    
    public String toString() {
        return "Loc" + this.getId() + "(" + this.startLinenumber + (this.length == 1 ? "" : "-" + (this.startLinenumber+(this.length-1))) + ")";
    }

	public String printLocation() {
        String res = this.toString() + "\n";
        for (int i = 0; i < this.length; i++) {
            Line l = text.getLines().get(this.startLinenumber + i);
            res += "  " + l.toString();
        }
        return res;
	}

    public String printContents() {
        String res = "";
        for (int i = 0; i < this.length; i++) {
            Line l = text.getLines().get(this.startLinenumber + i);
            res += l.getContents()  + "\n";
        }
        return res;
    }

    public boolean checkValidity() {
        boolean check1 = (length > 0);
        Line line = this.text.getLines().get(this.startLinenumber);
        boolean check2 = (line != null);
        // first line needs low cohesion
        boolean check3 = (line.getCohesion() < MIN_COHESION);
        // link to location correct?
        boolean check4 = (line.getLocation() == this);
        assert(check1);
        assert(check2); 
        assert(check3);
        assert(check4);
        if (!check1 || !check2 || !check3 || !check4)
            return false;
        for (int i = this.startLinenumber+1; i < this.length-1; i++) {
            line = text.getLines().get(i);
            // the other lines need min cohesion
            boolean check51 = (line.getCohesion() >= MIN_COHESION);
            // link to location correct?
            boolean check52 = (line.getLocation() == this);
            assert(check51); 
            assert(check52);
            if (!check51 || !check52)
                return false;
        }            
        return true;
    }
   
    /**
     * @param loc1, needs to be at least of length 2
     * @param line, needs to be in loc1. The line will be the first line in the new location
     */
    public Location splitLocation(Line line) {

        assert (line != null && line.getLocation() != null);
            
        int firstLineNumber = this.startLinenumber;
        int lastLineNumber = this.startLinenumber + (this.length-1);
        Text text = this.text;
        assert(this.length >= 2);

        // line must not be the first line of the location
        assert(firstLineNumber < line.getLinenumber());
        assert(lastLineNumber >= line.getLinenumber());
        assert(line.getLocation() == this);

        // Create a new Location
        Location newLoc = new Location(text);

        // Decide to keep "this" as the Location with the magnitude of lines
        if (line.getLinenumber() - firstLineNumber > lastLineNumber - line.getLinenumber() + 1) { // +1 because line is in second "half"
            // Give newLoc the last "half"
            newLoc.startLinenumber = line.getLinenumber();
            newLoc.length = (lastLineNumber - line.getLinenumber()) + 1; // +1 because line itself is included
        } else {
            // Give newLoc the first "half"
            newLoc.startLinenumber = firstLineNumber;
            newLoc.length = (line.getLinenumber() - firstLineNumber);
            this.startLinenumber = line.getLinenumber();
        }
        this.length = this.length - newLoc.length;                        
        
        // Transfer lines to newLoc
        for (Line l : text.getLines().subList(newLoc.startLinenumber, 
                                            newLoc.startLinenumber + newLoc.length))
            l.setLocation(newLoc);

        // Register new location - only after lines are transfered
        text.getLocations().add(newLoc);

        assert(firstLineNumber + this.length + newLoc.length == lastLineNumber + 1);
        assert(this.checkValidity());
        assert(newLoc.checkValidity());

        return newLoc;
    }

    /**
     * @param loc1
     * @param loc2 must be directly after loc1 in text
     */
    public void mergeLocation(Location loc2) {
        if (loc2 == null)
            return; 
        
        // Deregister loc2 first
        this.text.getLocations().remove(loc2);

        // Transfer lines
        for (Line l : this.text.getLines().subList(loc2.startLinenumber, 
                                                loc2.startLinenumber + loc2.length))
            l.setLocation(this);

        // shift start at the beginning of loc2 if necessary
        if (loc2.startLinenumber < this.startLinenumber)
            this.startLinenumber = loc2.startLinenumber;

        // simply add length, formally invalidate loc2
        this.length += loc2.length;            
        loc2.length = 0;

    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Location))
            return false;
        Location loc = (Location)obj;
        return this.hashCode() == loc.hashCode();
    }

    public int hashCode() {
        return this.text.hashCode() + 47 * this.id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param startLinenumber the startLinenumber to set
     */
    public void setStartLinenumber(int startLinenumber) {
        this.startLinenumber = startLinenumber;
    }

    /**
     * @return the startLinenumber
     */
    public int getStartLinenumber() {
        return startLinenumber;
    }

    /**
     * @param length the length to set
     */
    public void setLength(int length) {
        this.length = length;
    }

    /**
     * @return the length
     */
    public int getLength() {
        return length;
    }

    /**
     * @param text the text to set
     */
    public void setText(Text text) {
        this.text = text;
    }

    /**
     * @return the text
     */
    public Text getText() {
        return text;
    }

}