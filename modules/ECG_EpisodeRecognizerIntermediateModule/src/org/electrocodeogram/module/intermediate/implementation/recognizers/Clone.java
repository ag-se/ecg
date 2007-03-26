/*
 * (c) Freie Universität Berlin - AG SoftwareEngineering - 2007
 *
 *
 *  Diplomarbeit: Verfolgen von Kodekopien in Eclipse
 *  @author Sofoklis Papadopoulos
 *
 *
 */

package org.electrocodeogram.module.intermediate.implementation.recognizers;

import java.util.*;


public class Clone {
	 /**
     * The code of clone.
     */ 
     private String cloneCode;
     /**
      * This is the filename where the clone belongs to.
      */
	 private String cloneFilename;
	 /**
	  * This is the startline of the clones code.
	  */
	 private int cloneCodeStartline;
	 /**
	  * This is the endline of the clones code.
	  */
	 private int cloneCodeEndline;
	 /**
      * This is the creation date of the clone.
      */
	 private Date cloneStartDate;
	 /**
	  * This is a boolean that determines if a clone is under observation
	  */
	 private boolean valid;
	 /**
	  * This is a boolean that determines if a clone is deleted.
	  */
	 private boolean deleted;
	 /**
      * This is a container for the code of the clones. For convenient code line evaluation.
      */
	 private Vector<String> vLines;
	 /**
	  * This is a Container for the deleted lines in a clone. 
	  */
	 private Vector<Boolean> vLinesDeleted;
	 /**
	  * 
	  * @param copied if copy state
	  * @param cutted if cut state
	  * @param cut_pasted if cut_pasted state
	  * @param pasted if pasted state
	  * @param file filename whre the clon ebelongs to
	  * @param startL startline of the clone
	  * @param endL endline of the clone
	  * @param code text of the clone
	  * @param creationDate timestamp of clone creation
	  */
	 
	 public Clone (boolean copied, boolean cutted, boolean cut_pasted, boolean pasted, String file, 
			                                   int startL, int endL, String code, Date creationDate){
		 this.cloneCode = code;
		 this.cloneFilename = file;
		 this.cloneCodeStartline = startL;
		 this.cloneCodeEndline = endL;
		 this.deleted = false;
		 this.valid = true;
		 this.cloneStartDate = creationDate;
		 //fill 
		 buildCloneCodeLines();
		 calcCloneEndLineNumber();
	 }
	 
	 
	 public String toString(){
		 return "datei: " + cloneFilename + " startzeile: " + cloneCodeStartline + " endzeile: " + cloneCodeEndline + " code: " + cloneCode;
	 }
	 
	 /**
	  * @param isValid To set the boolean valid.
	  */
	 public void setValid(boolean isValid) {
		 this.valid = isValid;
	 }
	 /**
	  * @param isDeleted To set the boolean isDeleted.
	  */
	 public void setDeleted(boolean isDeleted){
		 this.deleted = isDeleted;
	 }
	 /**
	  * 
	  * @return True,if clone is deleted otherwise false.
	  */
	 public boolean isDeleted() {return this.deleted;}
	 /**
	  * 
	  * @return True,if clone is valid otherwise false.
	  */
	 public boolean isValid() {return this.valid;}
	 
	 /**
	  * Decrement the line numbers of a clone.
	  *
	  */
	 public void decrementLinenumbers() {
		 this.cloneCodeStartline--;
		 this.cloneCodeEndline--;
	 }
	 /**
	  * 
	  * Increment the line numbers of a clone.
	  */
	 public void incrementLinenumbers() {
		 this.cloneCodeStartline++;
		 this.cloneCodeEndline++;
	 }

	 public void setCloneStartDate(Date startDate) {
		 this.cloneStartDate = startDate;
	 }

	 public Date getCloneStartDate() {
		 return this.cloneStartDate;
	 }

	 public int getCloneCodeStartline() {
		 return this.cloneCodeStartline;
	 }
	 
	 public int getCloneCodeEndline() {
		 return this.cloneCodeEndline;
	 }
	 
	 public String getCloneFilename() {
		 return this.cloneFilename;
	 }
	 /**
	  * 
	  * Because of linewise evaluation the codelines of the clones are stored in a vector.
	  */
	 private void buildCloneCodeLines(){
		if(this.cloneCode != null){ 
			StringTokenizer cloneCodeLines = new StringTokenizer(cloneCode, "\n", false);
			vLines = new Vector<String>();
			vLinesDeleted = new Vector<Boolean>();
          while(cloneCodeLines.hasMoreTokens()){
          	vLines.add(cloneCodeLines.nextToken());
          	vLinesDeleted.add(Boolean.FALSE);
          }
		}
	 }
	 /**
	  * 
	  * When creating a clone calculate the end line because in a textoperation, there exist no endline of the copied, pasted, cutted clone
	  */  
	 private void calcCloneEndLineNumber() {
		 if (vLines != null)
			 cloneCodeEndline = cloneCodeStartline + vLines.size() - 1;
	 }
	 
	 public int getCloneEndLine(){
		 if(vLines != null){
			 return cloneCodeStartline + vLines.size() -1;
		 }
		 else return 0;
	 }
	 
	 
	 public boolean deleteLine(int linenumber) {
     //A linediff delete is recognized. If linenumber < startline the linediff-event takes place outside this clone.
		 boolean inside = false;
		 if (linenumber < this.cloneCodeStartline) {
			 this.decrementLinenumbers();
		 }
		 else if (linenumber >= this.cloneCodeStartline && linenumber <= this.cloneCodeEndline)
			 inside = markCloneCodeLineAsDeleted(linenumber);
		 return inside;
	 }
	 /**
	  * @param lineNumber and codeline
	  * The index in vLines and the new code line to insert.
	  * @return boolean True, if insertion inside of a clone, otherwise false.  
	  */
	 public boolean insertLine(int linenumber, String codeLine) {
     //A linediff insert is recognized if linenumber < startline linediff-event takes place outside the clone.
		 boolean inside = false;
		 if (linenumber < this.cloneCodeStartline) {
			 this.incrementLinenumbers();
		 }
		 else if (linenumber >= this.cloneCodeStartline && linenumber <= this.cloneCodeEndline)
			 inside = insertCloneCodeLine(linenumber, codeLine);
		 return inside;
	 }
	 /**
	  * @param lineNumber and codeline
	  * The line number of the codeline to insert 
	  * @return boolean Ture, if change inside of a clone, otherwise false
	  */
	 public boolean changeLine(int linenumber, String codeLine) {
     //A linediff change inside the clone is recognized.
		 boolean inside = false;
		 if (linenumber >= this.cloneCodeStartline && linenumber <= this.cloneCodeEndline) {
			 inside = changeCloneCodeLine(linenumber, codeLine);
		 }
		 return inside;
	 }


     /**
      * The real changing of a clone-objects code line
      * linenNumber is the code line to change 
      */
	 private boolean changeCloneCodeLine(int lineNumber, String codeLine) {
		 boolean inside = true;
		 if (vLines != null && codeLine != null && vLines.size() > 0 && lineNumber >= 0 && this.cloneCodeStartline <= lineNumber
				 && this.cloneCodeEndline >= lineNumber && vLines.size() == (this.cloneCodeEndline - this.cloneCodeStartline + 1)) {
			 try {
				 vLines.setElementAt(codeLine,lineNumber - this.cloneCodeStartline);
				 changeCloneCode();
			 } catch (Exception e) {e.printStackTrace();}
		 }
		 else inside = false;
		 return inside;
	 }
	 /**
	  * The real deleting of a clone-objects code line 
	  * linenNumber is the index in vLines
	  */
	 private void deleteCloneCodeLine(int lineNumber) {
		 if (vLines != null && vLines.size() > 0 && lineNumber >= 0 && vLines.size() == (this.cloneCodeEndline - this.cloneCodeStartline + 1)) {
			 try {
				vLines.removeElementAt(lineNumber);
				vLinesDeleted.removeElementAt(lineNumber);
				this.cloneCodeEndline--;
				changeCloneCode();
			} catch (Exception e) {e.printStackTrace();}
		 }
		 if (vLines != null && vLines.size() <= 0) this.deleted = true;
	 }
	 /**
	  * The real inserting of a clone-objects code line 
	  * lineNumber is the index in vLines
	  */
	 
	 private boolean insertCloneCodeLine(int lineNumber, String codeLine) {
		 boolean inside = true;
		 if (vLines != null && codeLine != null && vLines.size() > 0 && lineNumber >= 0 && this.cloneCodeStartline <= lineNumber
				&& this.cloneCodeEndline >= lineNumber && vLines.size() == (this.cloneCodeEndline - this.cloneCodeStartline + 1)) {
			try {
				vLines.insertElementAt(codeLine,lineNumber-this.cloneCodeStartline);
				vLinesDeleted.insertElementAt(Boolean.FALSE,lineNumber-this.cloneCodeStartline);
				this.cloneCodeEndline++;
				changeCloneCode();
			} catch (Exception e) {e.printStackTrace();}
		}
		else inside = false;
		return inside;
	 }
	 /**
	  * 
	  * @param lineNumber
	  * @return True, if lineNumber is inside a clone.
	  */
	 private boolean markCloneCodeLineAsDeleted(int lineNumber) {
		 boolean inside = true;
		 if (vLines != null && vLines.size() > 0 && lineNumber >= 0 && this.cloneCodeStartline <= lineNumber && this.cloneCodeEndline >= lineNumber
					&& vLines.size() == (this.cloneCodeEndline - this.cloneCodeStartline + 1)) {
			 try {
				 vLinesDeleted.setElementAt(Boolean.TRUE,lineNumber-this.cloneCodeStartline);
			 } catch (Exception e) {e.printStackTrace();}
		 }
		 else inside = false;
		 return inside;
	 }
	 /**
	  *  To delete the lines tah are marked as deleted lines. 
	  *
	  */
	 public void clearDeletedLines() {
		 int size = 0;
		 if (vLines != null && vLinesDeleted != null) {
			 size = vLinesDeleted.size();
			 for (int index = 0;index < size;index++) {
				 if (vLinesDeleted.elementAt(index)) {
					 deleteCloneCodeLine(index);
					 size--;
					 index--;
				 }
			 }
			 if (vLines.size() == 0) deleted = true;
		 }
	 }
	 
	 public String getCloneCode(){
		 	return this.cloneCode;
	 }
	/**
	 * The changing of code lines in vLines.
	 *
	 */
	 private void changeCloneCode(){
		 if (vLines != null) {
			 int lines = vLines.size();
			 cloneCode = "";
			 for (int index = 0; index < lines; index++) {
				 cloneCode += vLines.get(index);
			 }
		 }
	 }
	 
  }