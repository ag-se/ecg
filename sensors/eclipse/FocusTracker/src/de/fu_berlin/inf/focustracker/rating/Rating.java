package de.fu_berlin.inf.focustracker.rating;

import java.io.IOException;
import java.util.List;

import org.drools.FactException;
import org.drools.IntegrationException;
import org.drools.RuleBase;
import org.drools.WorkingMemory;
import org.drools.io.RuleBaseLoader;
import org.xml.sax.SAXException;

import de.fu_berlin.inf.focustracker.rating.event.EventHolder;


public class Rating {

	RuleBase ruleBase;
	
	public Rating() throws IntegrationException, SAXException, IOException {
		
		ruleBase = RuleBaseLoader.loadFromInputStream(this.getClass().getResourceAsStream( "rules.drl" ) );
		System.err.println("RuleEngine started.");
	}
	
	@SuppressWarnings("unchecked")
	public Double rateEvent(EventHolder aEventHolder) throws RatingException {

		WorkingMemory workingMemory = ruleBase.newWorkingMemory( );
		
		//put object in working memory
		try {
			workingMemory.assertObject(aEventHolder);
			workingMemory.fireAllRules();
			
			List<Double> retValues = workingMemory.getObjects(Double.class); 
			
			if(retValues != null && retValues.size() > 0) {
//				System.err.println(workingMemory.getObjects(Double.class).get(0));
				return (Double)workingMemory.getObjects(Double.class).get(0);
			} else {
				return new Double(0);
			}
			
		} catch (FactException e) {
			throw new RatingException("Exception occured during rateEvent()", e);
		}
		
	}

//	public Double rateEvent(EventHolder aEventHolder) throws RatingException {
//		return new Double(1);
//	}	
}
