/**
 * 
 */
package org.electrocodeogram.module.target.implementation;

import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.target.exceptions.NoMetadataVectorException;

/**
 * @author jule
 * @version 1.0
 * This class holds the Information about the Metadata
 * of each Table in the Database 
 * The class ist realized with the Singleton Pattern 
 */

public class DBTablesMetadataPool {
	
    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
            .createLogger(DBTablesMetadataPool.class.getName());
    /**
	 * the instance which represents the unique instance of the class
	 */
	private static DBTablesMetadataPool poolInstance= null;
	
	private HashMap <String,Vector>metaInformationVectors = new HashMap<String,Vector>();
	
	/**
	 * private Constructor, so other classes can't instanciate
	 */
	private DBTablesMetadataPool(){
	    
	}
    
    /**
     * get the Metadata from all Tables in the database
     */
    private static void getMetadataForAllTables() {
        Vector v = DBCommunicator.getTableNames();
        for (int i = 0; i<v.size(); i++){
            String tableName= (String) v.get(i);
            Instance().addMetadataVector(tableName, DBCommunicator.getMetadataInColumnOrder(tableName));
        }
    }
	
	/**
	 * this Method 'hides' the constructor
	 * it has access to the variable that holds the unique instance
	 * @return the unique instance of the class DBTablesMetadataPool
	 */
	public static DBTablesMetadataPool Instance(){
		
		if(poolInstance == null)
			poolInstance = new DBTablesMetadataPool();
		
		return poolInstance;
	}
	
	
	/**
	 * This Method returns a Metadata Vector for a given tablename.
	 * If the unique singleton instance is null an Exception ist thrown,
	 * as well as there is no MetadataVector for the given tablename
	 * @param tableName the name of the table
	 * @return the Vector containing the Metadata for the giben tablename
	 * @throws NoMetadataVectorException 
	 */
	public Vector getMetadataVector(String tableName) throws NoMetadataVectorException{
		if(metaInformationVectors.isEmpty())
            getMetadataForAllTables();
        
        if (poolInstance.metaInformationVectors.get(tableName)== null){
			logger.warning("No MetadataVector found for "+tableName);
            throw new NoMetadataVectorException();
		}
		else return (Vector)poolInstance.metaInformationVectors.get(tableName);
	}
	
	/**
	 * With this method you can add a tablename with the corresponding 
	 * MetaInformationVector to the HashMap which contains all 
	 * available metadata vectors
	 * @param tableName
	 * @param v
	 */
	public void addMetadataVector(String tableName, Vector v){
		if(poolInstance == null)
            System.out.println("NULL");
        
        poolInstance.metaInformationVectors.put(tableName, v);
	}
	
	
	
	/**
     * 
     * @return the Vector with the Metadata Information for all Tables
	 */
    public HashMap getMetaInformationVectors(){
		if(this.metaInformationVectors.isEmpty())
            getMetadataForAllTables();
        return this.metaInformationVectors;
	}
	
}
