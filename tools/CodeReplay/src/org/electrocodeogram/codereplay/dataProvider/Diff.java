package org.electrocodeogram.codereplay.dataProvider;

/**
 * The class Diff represents a diff(difference) between two {@link ReplayElement}s. 
 * This includes the textual difference between the two elements(represented as a String) as well as the starting-
 * and endindex of this String within the complete text.
 * 
 * in context of overall implementation:
 * Each Diff is associated to exactly one ReplayElement. It shows the additive difference between the ReplayElement
 * and its predecessor, this means, it includes all text that was added from the last ReplayElement in
 * comparison to the current one. If the difference is negative(text was removed)
 * the Diff is empty(empty String, startpos == endpos == length == 0). The creating algorithm({@link SimpleDiff})
 * provides the starting and endposition zerobased. 
 * 
 * @author marco kranz
 */
public class Diff {

	private int start;
	
	private int end;
	
	private String diff;
	
	/**
	 * @param s start index of the difference in the complete text
	 * @param e end index of the difference in the complete text
	 * @param d the textual difference
	 */
	public Diff(int s, int e, String d) {
		start = s;
		end = e;
		diff = d;
	}

	/**
	 * @return the textual difference
	 */
	public String getDiff() {
		return diff;
	}

	/**
	 * @return the end index
	 */
	public int getEnd() {
		return end;
	}

	/**
	 * @return the start index
	 */
	public int getStart() {
		return start;
	}

	/**
	 * @return String representation of this Diff
	 */
	public String toString(){
		return "Diff: starts at: "+start+"; ends at: "+end+"; diff is: "+diff;
	}
	
	/**
	 * @return the length of the difference
	 */
	public int getLength(){
		return end - start;
	}
}
