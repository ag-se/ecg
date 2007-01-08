package org.electrocodeogram.codereplay.dataProvider;


/**
 * This class contains a static method that computes the difference between 2 texts.
 * The algorithm is as follows: first the two texts are compared starting from the beginning.
 * They are compared char by char(1st <-> 1st ; 2nd <-> 2nd and so on...) until a difference is found.
 * Then the same is done starting at the end of both texts. The resulting difference is the textarea between 
 * the first different char and the last different char.
 * The difference is only calculated additive, that means that only added text is recognized, but if there was
 * something removed in the new text, this will not be recorded. It could be done without problem, but was not
 * needed yet since it could not be displayed. 
 * This algorithm is, of course, only useful as long as the texts only have one different textarea. But since this should be
 * the case for the vast majority of all texts in this context(since its about somebody writing in an editor),
 * this was the algoritm of choice. In addition to that its also of better performance(linear growth) as the usual
 * 'longest common subsequence' algorithm(polynomial growth).
 * 
 * @author marco kranz
 */
public class SimpleDiff {

// a simple diff algorythm	
/**
 * @param olddoc the old text
 * @param newdoc the new text
 * @return the difference between the old and the new text
 */
public static Diff getSimpleDiff(String olddoc, String newdoc){
		
		int start = 0;
		int end = 0;
		if(newdoc.length() > 0)
			end = newdoc.length()-1;
		else return new Diff(0,0,"");
		int oldend = 0;
		if(olddoc.length() > 0)
			oldend = olddoc.length()-1;
		else return new Diff(0,0,"");
		int minlength = Math.min(olddoc.length()-1, newdoc.length()-1);
		
		//System.out.println("newdoc.length(): "+newdoc.length()+" olddoc.length(): "+olddoc.length());
		// DANGER!!! in case start == minlength(only difference is the suffix of newdoc) this only works
		// because the term after '&&' is not evaluated... otherwise this would lead to an exception.
		// dunno if this behaviour is part of JVM specification... but no time to change this yet. :P 
		while(start <= minlength && olddoc.charAt(start) == newdoc.charAt(start)){
			if(start == end){
				//System.out.println("->");
				return new Diff(0,0,"");
			}
			start++;
		}
		while(olddoc.charAt(oldend) == newdoc.charAt(end)){
			if(oldend == start || end == start){
				//System.out.println("old = "+olddoc.charAt(oldend-1)+"  new = "+newdoc.charAt(end-1)+"  "+newdoc.substring(start, end));
				return new Diff(start,end,newdoc.substring(start, end));
			}
			oldend--;
			end--;
		}
		//System.out.println(newdoc.substring(start, end+1));
		return new Diff(start,end,newdoc.substring(start, end+1));
	}
}
