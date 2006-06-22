/**
 * 
 */
package org.electrocodeogram.module.intermediate.implementation;


/**
 * 
 */
public class Location {

    public int id = 0;
	public int startLinenumber = 0;
    public int length = 0;
//		public Location parent = null;
	public Location nextLocation = null;
	public Location prevLocation = null;
//		public List<Location> children = new ArrayList<Location>();
    public Text text;
    
    public Location(Text text) {
        this.text = text;
        this.id = ++text.nextLocationId;
    }
    
    public int getId() {
        return id;
    }
	
	public String toString() {
        String res = "Loc" + this.getId() + "(" + this.startLinenumber + "-" + (this.startLinenumber+length-1) + ")\n";
        for (int i = 0; i < this.length; i++) {
            Line l = text.lines.get(this.startLinenumber + i);
            res += "  " + l.toString() + "\n";
        }
        return res;
	}

    public String printContents() {
        String res = "";
        for (int i = 0; i < this.length; i++) {
            Line l = text.lines.get(this.startLinenumber + i);
            res += l.contents  + "\n";
        }
        return res;
    }

    public boolean checkValidity() {
        boolean check1 = (length > 0);
        Line line = this.text.lines.get(this.startLinenumber);
        boolean check2 = (line != null);
        // first line needs low cohesion
        boolean check3 = (line.cohesion < CodeLocationTrackerIntermediateModule.MIN_COHESION);
        // link to location correct?
        boolean check4 = (line.location == this);
        assert(check1);
        assert(check2); 
        assert(check3);
        assert(check4);
        if (!check1 || !check2 || !check3 || !check4)
            return false;
        boolean check5 = true;
        for (int i = this.startLinenumber+1; i < this.length-1; i++) {
            line = text.lines.get(i);
            // the other lines need min cohesion
            boolean check51 = (line.cohesion >= CodeLocationTrackerIntermediateModule.MIN_COHESION);
            // link to location correct?
            boolean check52 = (line.location == this);
            assert(check51); 
            assert(check52);
            if (!check51 || !check52)
                return false;
        }            
        return true;
    }
   
}