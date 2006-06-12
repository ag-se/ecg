package de.fu_berlin.inf.focustracker.rating;

import java.io.InputStreamReader;
import java.util.List;

import org.drools.FactException;
import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.WorkingMemory;
import org.drools.compiler.PackageBuilder;

import de.fu_berlin.inf.focustracker.rating.event.EventHolder;


public class Rating {

	RuleBase ruleBase;
	
	public Rating() throws Exception {
		
//		ruleBase = RuleBaseLoader.getInstance(). loadFromInputStream(this.getClass().getResourceAsStream( "rules.drl" ) );
		
		
        final PackageBuilder builder = new PackageBuilder();
        builder.addPackageFromDrl( new InputStreamReader( this.getClass().getResourceAsStream( "rules.drl" ) ) );

        ruleBase = RuleBaseFactory.newRuleBase();
        ruleBase.addPackage( builder.getPackage() );

		
		
		System.err.println("RuleEngine started.");
	}
	
	@SuppressWarnings("unchecked")
	public Double rateEvent(EventHolder aEventHolder) throws RatingException {

		WorkingMemory workingMemory = ruleBase.newWorkingMemory( );
		
		//put object in working memory
		try {
			workingMemory.assertObject(aEventHolder);
			workingMemory.fireAllRules();
			
//			List<Double> retValues = workingMemory.getObjects(Double.class); 
//			
//			if(retValues != null && retValues.size() > 0) {
////				System.err.println(workingMemory.getObjects(Double.class).get(0));
//				return (Double)workingMemory.getObjects(Double.class).get(0);
//			} else {
//				return new Double(0);
//			}
			return aEventHolder.getRating();
			
		} catch (FactException e) {
			throw new RatingException("Exception occured during rateEvent()", e);
//		} finally {
//			System.err.println("leaving rating: ");
		}

		
	}

//	public Double rateEvent(EventHolder aEventHolder) throws RatingException {
//		return new Double(1);
//	}	
}
