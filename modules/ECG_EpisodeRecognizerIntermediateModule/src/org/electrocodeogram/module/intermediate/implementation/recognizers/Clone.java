package org.electrocodeogram.module.intermediate.implementation.recognizers;

import java.util.*;

/**
 * 
 * @author S. Papadopoulos
 * Allgemein:
 * 1. Codedifferenzen können einen Clone ungültig werden lassen (invalid).
 *    Das Berechnen von Codedifferenzen wird bisher durch die Leventhstein-Distanz durchgeführt.
 * 2. Das Ungültigwerden eines Events bezieht sich stets auf andere Kopien im Recognizer.
 * 3. Sobald eine Kopie im Recognizer ex., die dem betrachteten Eventcode über 50% (oder andere Schwelle) ähnelt,
 *    muss der betrachtete Clone gültig (valid) bleiben. So kann der Recognizer bei einer nächsten Codecänderung
 *    diesen Eventcode wieder einbeziehen. Es können durchaus noch mehrere ähnliche Codecs im Recognizer enthalten sein.
 * 4. D.h., ist ein Clone gültig, ex. mindestens eine weitere ähnliche und beobachtete Codekopie im Recognizer.
 * 5. D.h., ist ein Clone ungültig, ex. kein ähnlicher beobachteter Eventcode mehr im Recognizer.
 * 6. Bei jeder Codeänderung müssen alle Eventcodes (valid & invalid) verglichen werden, da sich während der Codeverfolgung Änderungen an EventCodes
 *    auftreten können, die ein zuvor ungültigen Clone wieder gültig werden lassen !
 *    D.h., alle EventCodes müssen in linediffs einbezogen werden, unabhängig davon, ob sie invalid oder valid sind (q.d.e.).
 * 7. D.h., nur ein vollständig gelöschter EventCode überführt einen Clone in einen irreversiblen ungültigen Zustand (absolute_invalid).
 * 8. Obiges Verhalten implementiert ein UNDO ohne eine UNDO-Aktion erhalten zu haben.
 * n. Das Löschen des kompletten Codes läßt den Clone immer ungültig werden (invalid).
 */

public class Clone {
	  
	  private String eventCode;
	  /**public enum EventType {
		  CUT,
		  COPY,
		  PASTE,
		  CHANGE
	  }*/
	 //private int statisticEventCount = 0;
	 private int statisticSibblingCount = 0;
	  
	 private String eventFilename;
	 private String eventUserName;
	 private String eventProjectName;
	 private String eventActivityName;
	 private int eventCodeStartline;
	 private int eventCodeEndline;
	 private Date eventStartDate;
	 private boolean valid;
	 private boolean deleted;
	 
	 //private boolean valid = false;
	 //private Vector vSibblings;
	 private Vector<String> vLines;
	 private Vector<Boolean> vLinesDeleted;
	   		 
	 /*protected Clone (String activityCode, String user, String project, String activity, String file, int startL, int endL, Date date){
		 this.eventCode = activityCode;
		 this.eventUserName = user;
		 this.eventProjectName = project; 
		 this.eventActivityName = activity;
		 this.eventFilename = file;
		 this.eventCodeStartline = startL;
		 this.eventCodeEndline = endL;
		 this.eventStartDate = date;
		 //this.vSibblings = null;
		 this.deleted = false;
		 this.valid = true;
		 buildEventCodeLines();
		 calcEventEndLineNumber();
		 //statisticEventCount++;
	 }*/
	 
	 public Clone (boolean copied, boolean cutted, boolean cut_pasted, boolean pasted, String file, int startL, int endL, String code, Date creationDate){
		 this.eventCode = code;
		 this.eventFilename = file;
		 this.eventCodeStartline = startL;
		 this.eventCodeEndline = endL;
		 this.deleted = false;
		 this.valid = true;
		 this.eventStartDate = creationDate;
		 buildEventCodeLines();
		 calcEventEndLineNumber();
	 }
	 
	 public String toString(){
		 return "datei: " + eventFilename + " startzeile: " + eventCodeStartline + " endzeile: " + eventCodeEndline + " code: " + eventCode;
	 }
	 
	 public void setValid(boolean isValid) {
		 this.valid = isValid;
	 }
	 
	 public void setDeleted(boolean isDeleted){
		 this.deleted = isDeleted;
	 }
	 
	 public boolean isDeleted() {return this.deleted;}
	 
	 public boolean isValid() {return this.valid;}
	 
	 public void decrementLinenumbers() {
		 this.eventCodeStartline--;
		 this.eventCodeEndline--;
	 }
	 
	 public void incrementLinenumbers() {
		 this.eventCodeStartline++;
		 this.eventCodeEndline++;
	 }
	 
	 public void setEventStartDate(Date startDate) {
		 this.eventStartDate = startDate;
	 }
	 
	 public Date getEventStartDate() {
		 return this.eventStartDate;
	 }
	 
	 public int getEventCodeStartline() {
		 return this.eventCodeStartline;
	 }
	 
	 public int getEventCodeEndline() {
		 return this.eventCodeEndline;
	 }
	 
	 public String getEventFilename() {
		 return this.eventFilename;
	 }
	 
	 private void buildEventCodeLines(){
		if(this.eventCode != null){ 
			//StringTokenizer eventCodeLines = new StringTokenizer(eventCode, "\\n", false);
			StringTokenizer eventCodeLines = new StringTokenizer(eventCode, "\n", false);
			vLines = new Vector<String>();
			vLinesDeleted = new Vector<Boolean>();
          while(eventCodeLines.hasMoreTokens()){
          	vLines.add(eventCodeLines.nextToken());
          	vLinesDeleted.add(new Boolean(false));
          }
		}
	 }
	 
	 private void calcEventEndLineNumber() {
		 if (vLines != null)
			 eventCodeEndline = eventCodeStartline + vLines.size() - 1;
	 }
	 
	 public int getEventEndLine(){
		 if(vLines != null){
			 int vLinesSize = 0;
			 return vLinesSize = eventCodeStartline + vLines.size() -1;
		 }
		 else return 0;
	 }
	 
	 public boolean deleteLine(int linenumber) {
		 boolean inside = false;
		 if (linenumber < this.eventCodeStartline) {
			 this.decrementLinenumbers();
			 System.out.println("event -> zeile: " + linenumber + " vor beobachtetem textblock gelöscht & zeilen dekrementiert");
		 }
		 else if (linenumber >= this.eventCodeStartline && linenumber <= this.eventCodeEndline)
			 //inside = deleteEventCodeLine(linenumber);
			 inside = markEventCodeLineAsDeleted(linenumber);
		 return inside;
	 }
	 
	 public boolean insertLine(int linenumber, String codeLine) {
		 boolean inside = false;
		 if (linenumber < this.eventCodeStartline) {
			 this.incrementLinenumbers();
			 System.out.println("event -> zeile: " + linenumber + " vor beobachtetem textblock eingefügt & zeilen inkrementiert");
		 }
		 else if (linenumber >= this.eventCodeStartline && linenumber <= this.eventCodeEndline)
			 inside = insertEventCodeLine(linenumber, codeLine);
		 return inside;
	 }
	 
	 public boolean changeLine(int linenumber, String codeLine) {
		 boolean inside = false;
		 if (linenumber >= this.eventCodeStartline && linenumber <= this.eventCodeEndline) {
			 inside = changeEventCodeLine(linenumber, codeLine);
		 }
		 return inside;
	 }
	 
	 private boolean changeEventCodeLine(int lineNumber, String codeLine) {
		 boolean inside = true;
		 if (vLines != null && codeLine != null && vLines.size() > 0 && lineNumber >= 0 && this.eventCodeStartline <= lineNumber
				 && this.eventCodeEndline >= lineNumber && vLines.size() == (this.eventCodeEndline - this.eventCodeStartline + 1)) {
			 try {
				 //if(!codeLine.equals("")){
					 vLines.setElementAt(codeLine,lineNumber - this.eventCodeStartline);
					 changeEventCode();
					 System.out.println("event -> zeile: " + lineNumber + " in beobachtetem textblock geändert");
				 //}
				 /*else{
					 deleteEventCodeLine(lineNumber);
				 }*/
			 } catch (Exception e) {e.printStackTrace();}
		 }
		 else inside = false;
		 return inside;
	 }
	 /**
	  * 
	  * @param lineNumber
	  * In der alten Version die tatsächliche Zeilennummer in der Datei.
	  * In der neuen Version der Index im Zeilenvector.
	  * @return
	  * In der alten Version true, falls Zeile im Codeblock gelöscht.
	  * In der neuen Version ohne Bedeutung.
	  */
	 private void deleteEventCodeLine(int lineNumber) {
		 if (vLines != null && vLines.size() > 0 && lineNumber >= 0 && vLines.size() == (this.eventCodeEndline - this.eventCodeStartline + 1)) {
			 try {
				vLines.removeElementAt(lineNumber);
				vLinesDeleted.removeElementAt(lineNumber);
				this.eventCodeEndline--;
				changeEventCode();
				System.out.println("event -> zeile: " + lineNumber + " in beobachtetem textblock gelöscht & zeilen dekrementiert");
			} catch (Exception e) {e.printStackTrace();}
		 }
		 if (vLines != null && vLines.size() <= 0) this.deleted = true;
	 }
	 private boolean insertEventCodeLine(int lineNumber, String codeLine) {
		 boolean inside = true;
		 if (vLines != null && codeLine != null && vLines.size() > 0 && lineNumber >= 0 && this.eventCodeStartline <= lineNumber
				&& this.eventCodeEndline >= lineNumber && vLines.size() == (this.eventCodeEndline - this.eventCodeStartline + 1)) {
			try {
				vLines.insertElementAt(codeLine,lineNumber-this.eventCodeStartline);
				vLinesDeleted.insertElementAt(new Boolean(false),lineNumber-this.eventCodeStartline);
				this.eventCodeEndline++;
				changeEventCode();
				System.out.println("event -> zeile: " + lineNumber + " in beobachtetem textblock eingefügt & zeilen inkrementiert");
			} catch (Exception e) {e.printStackTrace();}
		}
		else inside = false;
		return inside;
	 }
	 private boolean markEventCodeLineAsDeleted(int lineNumber) {
		 boolean inside = true;
		 if (vLines != null && vLines.size() > 0 && lineNumber >= 0 && this.eventCodeStartline <= lineNumber && this.eventCodeEndline >= lineNumber
					&& vLines.size() == (this.eventCodeEndline - this.eventCodeStartline + 1)) {
			 try {
				 vLinesDeleted.setElementAt(new Boolean(true),lineNumber-this.eventCodeStartline);
				 System.out.println("event -> zeile: " + lineNumber + " in beobachtetem textblock als gelöscht markiert");
			 } catch (Exception e) {e.printStackTrace();}
		 }
		 else inside = false;
		 return inside;
	 }
	 public void clearDeletedLines() {
		 int size = 0;
		 if (vLines != null && vLinesDeleted != null) {
			 size = vLinesDeleted.size();
			 for (int index = 0;index < size;index++) {
				 if (vLinesDeleted.elementAt(index)) {
					 deleteEventCodeLine(index);
					 size--;
					 index--;
				 }
			 }
		 }
	 }
	 public String getEventCode(){
		 //if(eventCode != null){
		 	return this.eventCode;
		 //}
		 //else return null;
	 }
	 
	 private void changeEventCode(){
		 if (vLines != null) {
			 int lines = vLines.size();
			 eventCode = new String();
			 for (int index = 0; index < lines; index++) {
				//eventCode += vLines.get(index) + "\n";
				 eventCode += vLines.get(index);
			 }
		 }
	 }
	 
  }