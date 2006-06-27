package de.fu_berlin.inf.focustracker.evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.jdt.internal.core.SourceMethod;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;

public class ECGLogAnalyzer {

	private static final long TIMEOFFSET = 0;

//	private String logFileName = "C:/Dokumente und Einstellungen/wenzlaff/ecg_log/out.log";
	private String logFileName = "/jekutsch_060621-4_events.log";
	private String analysisFileName = "/sitzung-4.csv";
	private String outputFile = "/output-4.csv";
	private List<Interaction> rememberedInteractions = new ArrayList<Interaction>();
	
	
	public static void main(String[] args) throws IOException, SAXException, URISyntaxException, NumberFormatException, ParseException {
		new ECGLogAnalyzer();
	}
	
	public ECGLogAnalyzer() throws IOException, SAXException, URISyntaxException, NumberFormatException, ParseException {
		analyzeLog();
	}
	
	public void analyzeLog() throws IOException, SAXException, URISyntaxException, NumberFormatException, ParseException {
		
		DateFormat ISO8601Local = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); // change for any new sessions!
//		DateFormat ISO8601Local = new SimpleDateFormat(ECGExporter.ISO8601_DATE_FORMAT);
		List<Interaction> interactionsLog = new ArrayList<Interaction>();
		List<Interaction> interactionsAnalyse = new ArrayList<Interaction>();

		BufferedReader logBr = new BufferedReader(new FileReader(ECGLogAnalyzer.class.getResource(logFileName).getFile()));
		BufferedReader analyseBr = new BufferedReader(new FileReader(ECGLogAnalyzer.class.getResource(analysisFileName).getFile()));
		BufferedWriter outputBw = new BufferedWriter(new FileWriter(ECGLogAnalyzer.class.getResource(outputFile).getFile()));
		
		
		// read logfile
		while(true) {
			String line = logBr.readLine();
			if(line == null) {
				break;
			}
			// read entries from focustracker, only
			if(line.indexOf("add;msdt.focus.xsd;<?xml version=\"1.0\"?>") != -1) {
				String xml = line.substring(line.indexOf("<?xml version=\"1.0\"?>"));
				DOMParser parser = new DOMParser();
				parser.parse(new InputSource(new StringReader(xml)));
				Document doc = parser.getDocument();
				String resourceName = doc.getElementsByTagName("resourcename").item(0).getTextContent();
				String elementName = doc.getElementsByTagName("element").item(0).getTextContent();
				String elementType = doc.getElementsByTagName("elementtype").item(0).getTextContent();
				String focus = doc.getElementsByTagName("hasfocus").item(0).getTextContent();
				String rating = null;
				if(doc.getElementsByTagName("rating").item(0) != null) { 
					rating = doc.getElementsByTagName("rating").item(0).getTextContent();
				}
				String timestamp = doc.getElementsByTagName("detectedtimestamp").item(0).getTextContent();
				
				if(SourceMethod.class.getName().equals(elementType)) {
					Double ratingDouble = null;
					if(rating != null) {
						ratingDouble = Double.parseDouble(rating); 
					}
					interactionsLog.add(new Interaction(ISO8601Local.parse(timestamp), resourceName, elementName, elementType, Boolean.valueOf(focus), ratingDouble));
					
				}
				
			}
			
		}

		// sort interactions
		Interaction[] sortedInteractions = (Interaction[])interactionsLog.toArray(new Interaction[interactionsLog.size()]); 
		Arrays.sort(sortedInteractions);
		interactionsLog = new ArrayList<Interaction>(Arrays.asList(sortedInteractions));
		// assign endtimestamp
		int i = 0;
		for (Interaction interaction : interactionsLog) {
			if(i<interactionsLog.size()-2) {
				interaction.setEndTimestamp(findEndTimestamp(interactionsLog.subList(i + 1, interactionsLog.size()-1), interaction ));
				i++;
			}
		}
		
		DateFormat df = SimpleDateFormat.getDateTimeInstance();
		// read analyse file
		analyseBr.readLine(); // ignore the header
		Interaction interaction = null;
		while(true) {
			String line = analyseBr.readLine();
			if(line == null) {
				break;
			}
			// Datum;Zeit;Element;Resource;Klasse;;;
			String[] parts = line.split(";");
			Date timestamp = df.parse(parts[0] + " " + parts[1]);
			if(interaction != null) {
				interaction.setEndTimestamp(timestamp);
			}
			interaction = new Interaction(timestamp, parts[3], parts[2], null, true, 1d);
			interactionsAnalyse.add(interaction);
		}
		int outputCtr = 1;
		for (Interaction interactionAnalyse : interactionsAnalyse) {
			List<Interaction> extendedInteractions = new ArrayList<Interaction>(rememberedInteractions);
			rememberedInteractions = new ArrayList<Interaction>();
			extendedInteractions.addAll(interactionsLog);
			Interaction[] sorted = extendedInteractions.toArray(new Interaction[extendedInteractions.size()]);
			Arrays.sort(sorted);
			extendedInteractions = Arrays.asList(sorted);
			
			interactionAnalyse.setAssignedInteractions(
					retrieveInteractionsWithinTimespan(extendedInteractions, 
							interactionAnalyse.getTimestamp(), interactionAnalyse.getEndTimestamp())
			);
//			System.err.println("reference;" + interactionAnalyse);
			long timespanReference = ((interactionAnalyse.getEndTimestamp().getTime() - interactionAnalyse.getTimestamp().getTime())/1000);
			System.err.println("reference;" + timespanReference + ";" + interactionAnalyse.getTimestamp() + ";" + interactionAnalyse.getElementName() + ";" + interactionAnalyse.getRating() + ";" + interactionAnalyse.getResourceName());
			outputBw.write(outputCtr++ + ";" + interactionAnalyse.getTimestamp() + ";" + interactionAnalyse.getElementName() + ";1.0;");
			timespanReference = 0;
			double sumOfDeltas = 0.0d;
			for (Interaction assignedInteraction : interactionAnalyse.getAssignedInteractions()) {
				long timespan = ((assignedInteraction.getEndTimestamp().getTime() - assignedInteraction.getTimestamp().getTime())/1000);
				double difference = 1.0d;
				double rating = 0d;
				if(assignedInteraction.getRating() != null) {
					rating = assignedInteraction.getRating();
				}
				if(interactionAnalyse.getElementName().equals(assignedInteraction.getElementName())) {
					difference = 1d - rating;
				} else {
					difference = rating;
				}
				System.err.println(difference + ";" + timespan + ";" + assignedInteraction.getTimestamp() + ";" + assignedInteraction.getElementName() + ";" + assignedInteraction.getRating() + ";" + assignedInteraction.getResourceName() + ";" + assignedInteraction.getEndTimestamp());
//				System.err.println(";" + assignedInteraction);
				sumOfDeltas += (difference * timespan);
				if(difference != 0) {
					timespanReference += timespan;
				}
			}
			if(timespanReference == 0) {
				timespanReference = 1;
			}
			String deltaOutput = (sumOfDeltas / timespanReference) + ";"  + ( 1  - (sumOfDeltas / timespanReference));
			deltaOutput = deltaOutput.replace('.', ',');
			System.err.println(deltaOutput);
			outputBw.close();
		}
	}

	private Date findEndTimestamp(List<Interaction> aInteractions, Interaction aReference) {
		for (Interaction interaction : aInteractions) {
			if(interaction != aReference && (
				(interaction.getRating() != null && interaction.getRating() == 1)
					|| interaction.getElementName().equals(aReference.getElementName())
			)		
			) {
//				System.err.println("found: " + interaction.getElementName() + "[" + interaction.getRating() + "]" + " for " + aReference.getElementName()  + "[" + aReference.getRating() + "]");
				return interaction.getTimestamp();
			}
		}
		return null;
	}

	private List<Interaction> retrieveInteractionsWithinTimespan(
			List<Interaction> aInteractions, Date aBeginTimestamp, Date aEndTimestamp) {
		
		List<Interaction> interactions = new ArrayList<Interaction>();
		for (Interaction interaction : aInteractions) {
			if(interaction.getTimestamp().getTime() >= aBeginTimestamp.getTime() - TIMEOFFSET &&
					interaction.getTimestamp().getTime() < aEndTimestamp.getTime() + TIMEOFFSET) {
				
				if(interaction.getEndTimestamp().getTime() > aEndTimestamp.getTime()) {
					Interaction clonedInteraction = new Interaction(aEndTimestamp, interaction.getResourceName(), interaction.getElementName(), interaction.getElementType(), interaction.isFocus(), interaction.getRating());
					clonedInteraction.setEndTimestamp(interaction.getEndTimestamp());
					rememberedInteractions.add(clonedInteraction);
					interaction.setEndTimestamp(aEndTimestamp);
				}
				interactions.add(interaction);
			}
		}
		return interactions;
	}
	
	
	class Interaction implements Comparable<Interaction>{
		
		private Date timestamp;
		private Date endTimestamp = new GregorianCalendar(2099, 1, 1).getTime();
		private String resourceName;
		private String elementName;
		private String elementType;
		private boolean focus;
		private Double rating;
		private List<Interaction> assignedInteractions;
		
		public Interaction(Date aTimestamp, String aResourceName, String aElementName, String aElementType, boolean aFocus, Double aRating) {
			timestamp = aTimestamp;
			resourceName = aResourceName;
			elementName = aElementName;
			elementType = aElementType;
			focus = aFocus;
			rating = aRating;
			assignedInteractions = new ArrayList<Interaction>();
		}


		public int compareTo(Interaction aCompareTo) {
			return (int)(timestamp.getTime() - aCompareTo.timestamp.getTime());
		}


		@Override
		public String toString() {
			DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
			return dateFormat.format(timestamp) + ";" +elementName + ";" + rating + ";" + elementType + ";" + focus + ";" + dateFormat.format(endTimestamp) + ";" + assignedInteractions;
		}

		public String getElementName() {
			return elementName;
		}



		public String getElementType() {
			return elementType;
		}



		public boolean isFocus() {
			return focus;
		}



		public Double getRating() {
			return rating;
		}



		public Date getTimestamp() {
			return timestamp;
		}


		public Date getEndTimestamp() {
			return endTimestamp;
		}


		public void setEndTimestamp(Date aEndTimestamp) {
			endTimestamp = aEndTimestamp;
		}


		public List<Interaction> getAssignedInteractions() {
			return assignedInteractions;
		}


		public void setAssignedInteractions(List<Interaction> aAssignedInteractions) {
			assignedInteractions = aAssignedInteractions;
		}


		public String getResourceName() {
			return resourceName;
		}
	}
}
