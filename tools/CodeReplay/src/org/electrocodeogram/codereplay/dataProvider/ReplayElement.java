package org.electrocodeogram.codereplay.dataProvider;

import java.util.*;

import org.w3c.dom.*;
import org.xml.sax.*;


/**
 * A ReplayElement represents a single change in an {@link Replay}. It comprises the following data:
 * name: name of the represented text area(e.g. method name, class name...)
 * path: the path describing its position in a directory-like tree structure
 * exact change: identifier giving detailed information about the cause of the change represented by this element
 * timestamp: the time when the event happened(or was recorded)
 * identifier: the unique identifier of the replay this element belongs to
 * source: the complete source(text) of this element. 
 * {@link Diff}: describes the (textual) difference between this and the chronologically previous element
 * 
 * @author marco kranz
 */
public class ReplayElement implements Comparable{

	// TODO: add finals for type of change
	// replay name the element belongs to
	private String name;
	// path as provided by event
	private String[] path;
	// kind of change
	private String exact_change;
	// timestamp
	private Date timestamp;
    // ordinal - the number of the event
    private int ordinal;
	// unique identifier of the replay this element belongs to
	private String identifier;
	// complete source of that element
	private String source;
	// the diff: contains difference between this element
	// and its predecessor.
	private Diff diff;
	
	
	/**
	 * @param ts timestamp of creation
	 * @param path the path
	 * @param change exact type of change
	 * @param name the name
	 * @param identifier internal identifier
	 * @param source the complete source(text)
	 */
	public ReplayElement(Date ts, int ordinal, String[] path, 
                         String change, String name, 
                         String identifier, String source) {
		this.timestamp = ts;
        this.ordinal = ordinal;
		this.path = path;
		this.exact_change = change;
		this.identifier = identifier;
		this.source = source;
		this.diff = new Diff(0, 0, "");
		this.name = name;
	}
	
	
	/**
	 * @param d the new Diff to be set for this element
	 */
	void setDiff(Diff d){
		diff = d;
	}
	
	/**
	 * @param id the new internal identifier to be set for this element
	 */
	void setIdentifier(String id){
		identifier = id;
	}
	
	/**
	 * @param p the new path to be set for this element
	 */
	void setPath(String[] p){
		path = p;
	}
	

	/**
	 * @return the basic cause for this element(see {@link Causes})
	 */
	public String getChange() {
		return Causes.getBasicCause(exact_change);
	}
	
	
	/**
	 * @return the detailed change information
	 */
	public String getExactChange(){
		return exact_change;
	}

	/**
	 * @return the diff between this element and its predecessor(can be null)
	 */
	public Diff getDiff() {
		return diff;
	}


	/**
	 * @return the name of the textarea represented by this elements Replay
	 */
	public String getName() {
		return name;
	}


	/**
	 * @return the path as String array(can be of length 0)
	 */
	public String[] getPath() {
		return path;
	}


	/**
	 * @return the(textual) source
	 */
	public String getSource() {
		return source;
	}


	/**
	 * @return timestamp of creation
	 */
	public Date getTimestamp() {
		return timestamp;
	}
	
	
    /**
     * @return timestamp of creation
     */
    public int getOrdinal() {
        return ordinal;
    }
    
    
	/**
	 * @return the internal identifier
	 */
	public String getIdentifier(){
		return identifier;
	}
	
	/**
	 * @return String representation of this element
	 */
	public String toString(){
		String p = "";
		for(int i =0; i<path.length; i++){
			p = p+"/"+path[i];
		}
		return "Replay Element: Name: "+name+" Path: "+p+" Change: "+exact_change+" Timestamp: "+timestamp+" (Ordinal:"+ordinal+") Identifier: "+identifier+" Source: "+source+" "+diff;
	}


    public int compareTo(Object o) {
        if (!this.getClass().isInstance(o))
            return 0;
        ReplayElement ro = (ReplayElement)o;
        int datecomp = this.getTimestamp().compareTo(ro.getTimestamp());
        if (datecomp == 0)
            return this.ordinal - ro.getOrdinal();
        else
            return datecomp;
    }

}
