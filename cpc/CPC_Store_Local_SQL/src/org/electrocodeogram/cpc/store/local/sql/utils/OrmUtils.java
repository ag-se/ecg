package org.electrocodeogram.cpc.store.local.sql.utils;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.ICloneGroup;
import org.electrocodeogram.cpc.core.api.data.ICloneObject;
import org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension;
import org.electrocodeogram.cpc.core.api.data.ICloneObjectSupport;
import org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionMultiStatefulObject;
import org.electrocodeogram.cpc.core.api.data.special.IMultiKeyStatefulObject;
import org.electrocodeogram.cpc.core.api.data.special.IStatefulObject;
import org.electrocodeogram.cpc.core.api.data.special.IStoreCloneObject;
import org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;
import org.electrocodeogram.cpc.store.local.sql.data.ICloneFileContent;
import org.electrocodeogram.cpc.store.local.sql.provider.SQLStoreProvider.SqlType;


public class OrmUtils
{
	private static Log log = LogFactory.getLog(OrmUtils.class);

	/**
	 * Checks if the sql tables are present.
	 * 
	 * TODO: we should probably extend this check to verify that all table schemas correspond
	 * 		to the structures which we are expecting.
	 * 
	 * @param type the sql type of the database, never null.
	 * @param con an active database connection, never null.
	 * @throws SQLException if table generation fails
	 * @throws Exception may be thrown if the internal state has been corrupted for some reason 
	 */
	public static void createsTablesIfMissing(SqlType type, Connection con) throws SQLException
	{
		if (log.isTraceEnabled())
			log.trace("createsTablesIfMissing() - type: " + type + ", con: " + con);
		assert (type != null && con != null);

		try
		{
			con.createStatement().execute(
					"SELECT * FROM " + IClone.PERSISTENCE_CLASS_IDENTIFIER + " WHERE "
							+ IClone.PERSISTENCE_OBJECT_IDENTIFIER + "='doesnotexist'");
			log.trace("createsTablesIfMissing() - tables are present");
		}
		catch (SQLException e)
		{
			//ok, tables are probably missing
			log.warn("createsTablesIfMissing() - CPC tables are missing, recreating... (" + e + ")");

			dropAndCreateTables(type, con);
		}
	}

	/**
	 * Drops all tables and recreates them.
	 * 
	 * @param type
	 * @param con
	 * @throws SQLException
	 */
	public static void dropAndCreateTables(SqlType type, Connection con) throws SQLException
	{
		if (log.isTraceEnabled())
			log.trace("dropAndcreateTables() - type: " + type + ", con: " + con);
		assert (type != null && con != null);

		if (!isSupportedSqlType(type))
			throw new IllegalArgumentException("SqlType not supported: " + type);

		//build a list with all IStatefulObject which we'll need to persist
		List<IStatefulObject> statefulObjects = getRegisteredStatefulObjects();

		//drop and create tables for every IStatefulObject
		for (IStatefulObject statefulObject : statefulObjects)
		{
			Map<String, Class<? extends Object>> stateTypes = statefulObject.getStateTypes();
			String persistenceClassIdentifier = SqlQueryUtils.escapeSqlIdentifier(statefulObject
					.getPersistenceClassIdentifier());
			String persistenceObjectIdentifier = SqlQueryUtils.escapeSqlIdentifier(statefulObject
					.getPersistenceObjectIdentifier());

			//create the main table for this object type
			dropAndCreateTablesForObject(type, con, statefulObject, stateTypes, persistenceClassIdentifier,
					persistenceObjectIdentifier, null);

			//check if we need to add a sub-table
			if (statefulObject instanceof ICloneObjectExtensionMultiStatefulObject)
			{
				if (log.isTraceEnabled())
					log.trace("dropAndCreateTables() - found multi stateful extension object: " + statefulObject);

				String parentPersistenceClassIdentifier = persistenceClassIdentifier;
				ICloneObjectExtensionMultiStatefulObject multiExtStatefulObject = (ICloneObjectExtensionMultiStatefulObject) statefulObject;

				List<Map<String, Class<? extends Object>>> multiStateTypes = multiExtStatefulObject
						.getMultiStateTypes();
				List<String> multiClassIdentifiers = multiExtStatefulObject.getMultiPersistenceClassIdentifier();
				List<String> multiObjectIdentifiers = multiExtStatefulObject.getMultiPersistenceObjectIdentifier();

				for (int i = 0; i < multiClassIdentifiers.size(); ++i)
				{
					stateTypes = multiStateTypes.get(i);
					assert (!stateTypes.containsKey(ICloneObjectExtensionMultiStatefulObject.DELETION_MARK_IDENTIFIER));

					//we use the same table name, but with additional suffix "__sub"
					persistenceClassIdentifier = getMultiStatefulObjectPersistenceClassIdentifier(
							parentPersistenceClassIdentifier, multiClassIdentifiers.get(i));
					String persistenceObjectIdentifierSub = SqlQueryUtils.escapeSqlIdentifier(multiObjectIdentifiers
							.get(i));

					dropAndCreateTablesForObject(type, con, statefulObject, stateTypes, persistenceClassIdentifier,
							persistenceObjectIdentifier, persistenceObjectIdentifierSub);
				}
			}

		}

		/*
		 * add some constraints
		 */

		// UNIQUE CloneFile (project, path)
		executeSql(con, "ALTER TABLE " + SqlQueryUtils.escapeSqlIdentifier(ICloneFile.PERSISTENCE_CLASS_IDENTIFIER)
				+ " ADD CONSTRAINT clone_file_unique_project_path UNIQUE (project, path)");

		// FK Clone (fileUuid) => CloneFile (uuid)
		executeSql(con, "ALTER TABLE " + SqlQueryUtils.escapeSqlIdentifier(IClone.PERSISTENCE_CLASS_IDENTIFIER)
				+ " ADD CONSTRAINT clone_fk_clone_file FOREIGN KEY (fileUuid) REFERENCES "
				+ SqlQueryUtils.escapeSqlIdentifier(ICloneFile.PERSISTENCE_CLASS_IDENTIFIER) + " ("
				+ SqlQueryUtils.escapeSqlIdentifier(ICloneFile.PERSISTENCE_OBJECT_IDENTIFIER) + ") ON DELETE CASCADE");

		// FK Clone (groupUuid) => CloneGroup (uuid)
		executeSql(con, "ALTER TABLE " + SqlQueryUtils.escapeSqlIdentifier(IClone.PERSISTENCE_CLASS_IDENTIFIER)
				+ " ADD CONSTRAINT clone_fk_clone_group FOREIGN KEY (groupUuid) REFERENCES "
				+ SqlQueryUtils.escapeSqlIdentifier(ICloneGroup.PERSISTENCE_CLASS_IDENTIFIER) + " ("
				+ SqlQueryUtils.escapeSqlIdentifier(ICloneGroup.PERSISTENCE_OBJECT_IDENTIFIER) + ") ON DELETE SET NULL");

		// FK CloneAnnotation (cloneUuid) => Clone (uuid)
		//		executeSql(con, "ALTER TABLE "
		//				+ SqlQueryUtils.escapeSqlIdentifier(ICloneAnnotation.PERSISTENCE_CLASS_IDENTIFIER)
		//				+ " ADD CONSTRAINT clone_annotation_fk_clone FOREIGN KEY (cloneUuid) REFERENCES "
		//				+ SqlQueryUtils.escapeSqlIdentifier(ICloneAnnotation.PERSISTENCE_CLASS_IDENTIFIER) + " ("
		//				+ SqlQueryUtils.escapeSqlIdentifier(ICloneAnnotation.PERSISTENCE_OBJECT_IDENTIFIER)
		//				+ ") ON DELETE CASCADE");

		// FK _CloneFileContentClone (fileUuid) => CloneFile (uuid)
		executeSql(con, "ALTER TABLE "
				+ SqlQueryUtils.escapeSqlIdentifier(ICloneFileContent.PERSISTENCE_CLASS_IDENTIFIER)
				+ " ADD CONSTRAINT _clone_file_content_fk_clone_file FOREIGN KEY (fileUuid) REFERENCES "
				+ SqlQueryUtils.escapeSqlIdentifier(ICloneFile.PERSISTENCE_CLASS_IDENTIFIER) + " ("
				+ SqlQueryUtils.escapeSqlIdentifier(ICloneFile.PERSISTENCE_OBJECT_IDENTIFIER) + ") ON DELETE CASCADE");

		/*
		 * create some indices
		 */

		// CloneFile (project, path)
		executeSql(con, "CREATE INDEX idx_clone_file_path ON "
				+ SqlQueryUtils.escapeSqlIdentifier(ICloneFile.PERSISTENCE_CLASS_IDENTIFIER) + "(project, path)");

		// Clone (fileUuid)
		executeSql(con, "CREATE INDEX idx_clone_fileuuid ON "
				+ SqlQueryUtils.escapeSqlIdentifier(IClone.PERSISTENCE_CLASS_IDENTIFIER) + "(fileUuid)");

		// Clone (groupUuid)
		executeSql(con, "CREATE INDEX idx_clone_groupuuid ON "
				+ SqlQueryUtils.escapeSqlIdentifier(IClone.PERSISTENCE_CLASS_IDENTIFIER) + "(groupUuid)");

		// CloneAnnotation (cloneUuid)
		//		executeSql(con, "CREATE INDEX idx_clone_annotation_cloneuuid ON "
		//				+ SqlQueryUtils.escapeSqlIdentifier(ICloneAnnotation.PERSISTENCE_CLASS_IDENTIFIER) + "(cloneUuid)");
	}

	/**
	 * 
	 * @param type
	 * @param con
	 * @param statefulObject
	 * @param stateTypes
	 * @param persistenceClassIdentifier table name, escaped, never null.
	 * @param persistenceObjectIdentifier primary key, escaped, never null.
	 * @param persistenceObjectIdentifierSub additional primary key field, escaped, may be null.
	 * @throws SQLException
	 */
	private static void dropAndCreateTablesForObject(SqlType type, Connection con, IStatefulObject statefulObject,
			Map<String, Class<? extends Object>> stateTypes, String persistenceClassIdentifier,
			String persistenceObjectIdentifier, String persistenceObjectIdentifierSub) throws SQLException
	{
		if (log.isTraceEnabled())
			log.trace("dropAndCreateTablesForObject() - type: " + type + ", stateTypes: " + stateTypes
					+ ", persistenceClassIdentifier: " + persistenceClassIdentifier + ", persistenceObjectIdentifier: "
					+ persistenceObjectIdentifier + ", persistenceObjectIdentifierSub: "
					+ persistenceObjectIdentifierSub + ", statefulObject: " + statefulObject);

		/*
		 * drop the table if it already exists
		 */
		dropTable(con, type, persistenceClassIdentifier);

		/*
		 * create a new table
		 */
		StringBuilder sqlStr = new StringBuilder();

		if (SqlType.HSQL.equals(type))
			sqlStr.append("CREATE CACHED TABLE ");
		else if (SqlType.PGSQL.equals(type))
			sqlStr.append("CREATE TABLE ");

		sqlStr.append(persistenceClassIdentifier);

		sqlStr.append(" ( ");

		//now add all fields
		sqlStr.append(createTableFieldsAndConstraintsString(statefulObject, stateTypes, persistenceObjectIdentifier,
				persistenceObjectIdentifierSub));

		if (persistenceObjectIdentifierSub != null)
		{
			//create two column primary key
			sqlStr
					.append(", PRIMARY KEY (" + persistenceObjectIdentifier + ", " + persistenceObjectIdentifierSub
							+ ")");
		}

		sqlStr.append(" ) ");

		//ok, now execute the statement
		executeSql(con, sqlStr.toString());
	}

	/**
	 * Returns a list of all registered IStatefulObject, external and internal.
	 */
	private static List<IStatefulObject> getRegisteredStatefulObjects()
	{
		List<IStatefulObject> statefulObjects = new LinkedList<IStatefulObject>();

		//get a clone factory provider
		ICloneFactoryProvider cloneFactoryProvider = (ICloneFactoryProvider) CPCCorePlugin.getProviderRegistry()
				.lookupProvider(ICloneFactoryProvider.class);

		//add all registered ICloneObject sub classes
		for (Class<? extends ICloneObject> cloneObjectType : cloneFactoryProvider.getRegisteredCloneObjects())
		{
			//create a temporary instance of this clone object sub class
			ICloneObject cloneObject = (ICloneObject) cloneFactoryProvider.getInstance(cloneObjectType, "TEMP");
			if (cloneObject == null)
			{
				log.error("dropAndCreateTables(): unable to get instance for: " + cloneObjectType, new Throwable());
				continue;
			}

			//by API contract all ICloneObject implementations also implement IStatefulObject
			assert (cloneObject instanceof IStatefulObject);
			statefulObjects.add((IStatefulObject) cloneObject);
		}

		//add any registered ICloneObjectSupport which implements IStatefulObject

		/*
		 * TODO: Doing this is somewhat pointless ATM. The only clone object supports which implement
		 * 		IStatefulObject should be the ones which we provide ourself (i.e. CloneFileContent).
		 * 		As no other module would have any way of storing or retrieving a stateful ICloneObjectSupport.
		 * 
		 * 		However, maybe we'll add support for such storing and retrieving operations in the long run.
		 * 		For now we could as well just manually add our own objects to the list.
		 */
		for (Class<? extends ICloneObjectSupport> cloneObjectSupportType : cloneFactoryProvider
				.getRegisteredCloneObjectSupports())
		{
			//create a temporary instance of this clone object sub class
			ICloneObjectSupport cloneObjectSupport = (ICloneObjectSupport) cloneFactoryProvider
					.getInstance(cloneObjectSupportType);
			if (cloneObjectSupport == null)
			{
				log.error("dropAndCreateTables(): unable to get instance for: " + cloneObjectSupportType,
						new Throwable());
				continue;
			}

			//we're only interested in those support objects which implement IStatefulObject
			if (cloneObjectSupport instanceof IStatefulObject)
				statefulObjects.add((IStatefulObject) cloneObjectSupport);
		}

		//add all ICloneObjectExtension implementations which implement IStatefulObject
		for (Class<? extends ICloneObjectExtension> cloneObjectExtensionType : cloneFactoryProvider
				.getRegisteredCloneObjectExtensions())
		{
			//create a temporary instance of this clone object sub class
			ICloneObjectExtension cloneObjectExtension = (ICloneObjectExtension) cloneFactoryProvider
					.getInstance(cloneObjectExtensionType);
			if (cloneObjectExtension == null)
			{
				log.error("dropAndCreateTables(): unable to get instance for: " + cloneObjectExtensionType,
						new Throwable());
				continue;
			}

			//we're only interested in those ICloneObjectExtension implementations which implement IStatefulObject
			if (cloneObjectExtension instanceof IStatefulObject)
				statefulObjects.add((IStatefulObject) cloneObjectExtension);
		}

		return statefulObjects;
	}

	/**
	 * Drops the specified table in a manner which is safe, even if the table did not exist.
	 */
	private static void dropTable(Connection con, SqlType type, String table) throws SQLException
	{
		String dropTableStr = null;

		if (SqlType.HSQL.equals(type))
			dropTableStr = "DROP TABLE " + table + " IF EXISTS CASCADE";

		else if (SqlType.PGSQL.equals(type))
			dropTableStr = "DROP TABLE IF EXISTS " + table + " CASCADE";

		//drop it
		executeSql(con, dropTableStr);
	}

	private static void executeSql(Connection con, String sql) throws SQLException
	{
		if (log.isDebugEnabled())
			log.debug("SQL: " + sql);
		assert (con != null && sql != null);

		con.createStatement().execute(sql);
	}

	/**
	 * 
	 * @param statefulObject
	 * @param stateTypes
	 * @param persistenceObjectIdentifier primary key, escaped, never null.
	 * @param persistenceObjectIdentifierSub additional primary key field, escaped, may be null.
	 * @return
	 */
	private static String createTableFieldsAndConstraintsString(IStatefulObject statefulObject,
			Map<String, Class<? extends Object>> stateTypes, String persistenceObjectIdentifier,
			String persistenceObjectIdentifierSub)
	{
		if (log.isTraceEnabled())
			log.trace("createTableFieldsAndConstraintsString() - statefulObject: " + statefulObject + ", stateTypes: "
					+ stateTypes + ", persistenceObjectIdentifier: " + persistenceObjectIdentifier
					+ ", persistenceObjectIdentifierSub: " + persistenceObjectIdentifierSub);

		StringBuilder result = new StringBuilder();

		//create a field for each key
		boolean first = true;
		for (String key : stateTypes.keySet())
		{
			if (first)
				first = false;
			else
				result.append(",\n");

			String escapedKey = SqlQueryUtils.escapeSqlIdentifier(key);

			if (log.isTraceEnabled())
				log.trace("createTableFieldsAndConstraintsString() - key: " + key + ", escapedKey: " + escapedKey
						+ ", type: " + stateTypes.get(key));

			//column name
			result.append(escapedKey);
			result.append(' ');

			//type
			result.append(convertToSqlType(stateTypes.get(key)));
			result.append(' ');

			if (escapedKey.equals(persistenceObjectIdentifier) && persistenceObjectIdentifierSub == null)
			{
				//primary key
				result.append("PRIMARY KEY");
			}
			else
			{
				//normal field
				//TODO: what about constraints like NOT NULL, do we need them?

				//we're handling some special foreign key cases only for now
				if (((statefulObject instanceof IClone) && (key.equals("fileUuid")))
				/*|| ((statefulObject instanceof ICloneAnnotation) && (key.equals("cloneUuid")))*/)
				{
					result.append("NOT NULL");
				}
			}
		}

		return result.toString();
	}

	private static String convertToSqlType(Class<? extends Object> type)
	{
		assert (type != null);

		if (type == String.class)
			return "VARCHAR";
		else if (type == Integer.class)
			return "INT";
		else if (type == Long.class)
			return "BIGINT";
		else if (type == Float.class)
			return "FLOAT";
		else if (type == Double.class)
			return "DOUBLE";
		else if (type == Boolean.class)
			//TODO:/FIXME: this might not work with all databases
			return "BOOLEAN";
		else if (type == Date.class)
			return "TIMESTAMP";
		else
			log.error("convertToSqlType() - unknown type: " + type, new Throwable());

		return null;
	}

	/**
	 * Check whether this class supports the given SQL Type.
	 * 
	 * @param type the sql type to check
	 * @return true if this class does fully support that sql type
	 */
	public static boolean isSupportedSqlType(SqlType type)
	{
		return SqlType.HSQL.equals(type) || SqlType.PGSQL.equals(type);
	}

	//	private static void oldTableCreation(SqlType type, Connection con) throws SQLException
	//	{
	//		if (SqlType.HSQL.equals(type))
	//		{
	//			//drop all tables if they exist
	//			con.createStatement().execute("DROP TABLE cloneannotation IF EXISTS CASCADE");
	//			//con.createStatement().execute("DROP TABLE dirtyclone IF EXISTS CASCADE");
	//			con.createStatement().execute("DROP TABLE clone IF EXISTS CASCADE");
	//			con.createStatement().execute("DROP TABLE clonegroup IF EXISTS CASCADE");
	//			con.createStatement().execute("DROP TABLE clonefile IF EXISTS CASCADE");
	//
	//			//CloneFile table
	//			con.createStatement().execute(
	//					"CREATE CACHED TABLE clonefile (" + "uuid						VARCHAR		PRIMARY KEY,"
	//							+ "project					VARCHAR		NOT NULL," + "path						VARCHAR		NOT NULL,"
	//							+ "size						BIGINT		NOT NULL," + "modificationdate			BIGINT		NOT NULL,"
	//							+ "repositoryversion			VARCHAR," + "CONSTRAINT clonefile_unique_path UNIQUE (path)" + ")");
	//
	//			//CloneGroup table
	//			con.createStatement().execute("CREATE CACHED TABLE clonegroup (" + "uuid						VARCHAR		PRIMARY KEY" + ")");
	//
	//			//Clone table for persisted clone data
	//			con
	//					.createStatement()
	//					.execute(
	//							"CREATE CACHED TABLE clone ("
	//									+ "uuid						VARCHAR		PRIMARY KEY,"
	//									+ "creationdate				TIMESTAMP	NOT NULL,"
	//									+ "creator					VARCHAR		NOT NULL,"
	//									+ "file_uuid					VARCHAR		NOT NULL,"
	//									+ "group_uuid				VARCHAR,"
	//									+ "startoffset				INTEGER		NOT NULL,"
	//									+ "startnonwsoffset		INTEGER		NOT NULL,"
	//									+ "endoffset					INTEGER		NOT NULL,"
	//									+ "endnonwsoffset			INTEGER		NOT NULL,"
	//									+ "CONSTRAINT clone_fk_clonefile FOREIGN KEY (file_uuid) REFERENCES clonefile (uuid) ON DELETE CASCADE,"
	//									+ "CONSTRAINT clone_fk_clonegroup FOREIGN KEY (group_uuid) REFERENCES clonegroup (uuid) ON DELETE SET NULL"
	//									+ ")");
	//
	//			/* ATM NOT USED - we're caching in memory using java structures
	//			 * 
	//			 * Clone table for dirty clone data
	//			 * 
	//			 * We're using a TEMP table here. Such a table is automatically cleared by hsql when the
	//			 * connection is closed. "ON COMMIT PRESERVE ROWS" prevents hsql from clearing the table at each commit.
	//			 * 
	//			 * We use this table type because:
	//			 * 	a) it ensures that all dirtyclone data is lost in case of an eclipse crash
	//			 * 	   which means we can always be sure that the table is empty at startup.
	//			 *  b) the amount of dirty clones should be limited and the TEMP table is stored completely in memory
	//			 *     which is likely to improve performance.
	//			 *     
	//			 * TODO: we might have to switch to the CACHED type here, if we ever get to a point where
	//			 * 		the amount of open files leads to more dirty clones than we can store in memory.
	//			 */
	//			//con.createStatement().execute("CREATE GLOBAL TEMPORARY TABLE dirtyclone ("
	//			/*
	//			con.createStatement().execute("CREATE TEMP TABLE dirtyclone ("
	//					+cloneTableData
	//					+"transient					BOOLEAN		DEFAULT FALSE"
	//					+") ON COMMIT PRESERVE ROWS");
	//			 */
	//
	//			//CloneAnnotation table
	//			con
	//					.createStatement()
	//					.execute(
	//							"CREATE CACHED TABLE cloneannotation ("
	//									+ "uuid						VARCHAR		PRIMARY KEY,"
	//									+ "clone_uuid				VARCHAR		NOT NULL,"
	//									+ "author					VARCHAR		NOT NULL,"
	//									+ "date						TIMESTAMP	NOT NULL,"
	//									+ "type						TINYINT		NOT NULL,"
	//									+ "body						VARCHAR		NOT NULL,"
	//									+ "CONSTRAINT cloneannotation_fk_clone FOREIGN KEY (clone_uuid) REFERENCES clone (uuid) ON DELETE CASCADE"
	//									+ ")");
	//
	//			//create some indices for performance
	//			//TODO we definitely need an index on clonefile.path, clone.file_uuid, clone.group_uuid
	//		}
	//		else if (SqlType.PGSQL.equals(type))
	//		{
	//			//drop all tables if they exist
	//			con.createStatement().execute("DROP TABLE IF EXISTS cloneannotation CASCADE");
	//			con.createStatement().execute("DROP TABLE IF EXISTS clone CASCADE");
	//			con.createStatement().execute("DROP TABLE IF EXISTS clonegroup CASCADE");
	//			con.createStatement().execute("DROP TABLE IF EXISTS clonefile CASCADE");
	//
	//			//CloneFile table
	//			con.createStatement().execute(
	//					"CREATE TABLE clonefile (" + "uuid						VARCHAR		PRIMARY KEY," + "project					VARCHAR		NOT NULL,"
	//							+ "path						VARCHAR		NOT NULL," + "size						BIGINT		NOT NULL,"
	//							+ "modificationdate			BIGINT		NOT NULL," + "repositoryversion			VARCHAR,"
	//							+ "CONSTRAINT clonefile_unique_path UNIQUE (path)" + ")");
	//
	//			//CloneGroup table
	//			con.createStatement().execute("CREATE TABLE clonegroup (" + "uuid						VARCHAR		PRIMARY KEY" + ")");
	//
	//			//Clone table for persisted clone data
	//			con
	//					.createStatement()
	//					.execute(
	//							"CREATE TABLE clone ("
	//									+ "uuid						VARCHAR		PRIMARY KEY,"
	//									+ "creationdate				TIMESTAMP	NOT NULL,"
	//									+ "creator					VARCHAR		NOT NULL,"
	//									+ "file_uuid					VARCHAR		NOT NULL,"
	//									+ "group_uuid				VARCHAR,"
	//									+ "startoffset				INTEGER		NOT NULL,"
	//									+ "startnonwsoffset		INTEGER		NOT NULL,"
	//									+ "endoffset					INTEGER		NOT NULL,"
	//									+ "endnonwsoffset			INTEGER		NOT NULL,"
	//									+ "CONSTRAINT clone_fk_clonefile FOREIGN KEY (file_uuid) REFERENCES clonefile (uuid) ON DELETE CASCADE,"
	//									+ "CONSTRAINT clone_fk_clonegroup FOREIGN KEY (group_uuid) REFERENCES clonegroup (uuid) ON DELETE SET NULL"
	//									+ ")");
	//
	//			/* ATM NOT USED - we're caching in memory using java structures
	//			 * 
	//			 * Clone table for dirty clone data
	//			 * 
	//			 * We're using a TEMP table here. Such a table is automatically cleared by hsql when the
	//			 * connection is closed. "ON COMMIT PRESERVE ROWS" prevents hsql from clearing the table at each commit.
	//			 * 
	//			 * We use this table type because:
	//			 * 	a) it ensures that all dirtyclone data is lost in case of an eclipse crash
	//			 * 	   which means we can always be sure that the table is empty at startup.
	//			 *  b) the amount of dirty clones should be limited and the TEMP table is stored completely in memory
	//			 *     which is likely to improve performance.
	//			 *     
	//			 * TODO: we might have to switch to the CACHED type here, if we ever get to a point where
	//			 * 		the amount of open files leads to more dirty clones than we can store in memory.
	//			 */
	//			//con.createStatement().execute("CREATE GLOBAL TEMPORARY TABLE dirtyclone ("
	//			/*
	//			con.createStatement().execute("CREATE TEMP TABLE dirtyclone ("
	//					+cloneTableData
	//					+"transient					BOOLEAN		DEFAULT FALSE"
	//					+") ON COMMIT PRESERVE ROWS");
	//			 */
	//
	//			//CloneAnnotation table
	//			con
	//					.createStatement()
	//					.execute(
	//							"CREATE CACHED TABLE cloneannotation ("
	//									+ "uuid						VARCHAR		PRIMARY KEY,"
	//									+ "clone_uuid				VARCHAR		NOT NULL,"
	//									+ "author					VARCHAR		NOT NULL,"
	//									+ "date						TIMESTAMP	NOT NULL,"
	//									+ "type						TINYINT		NOT NULL,"
	//									+ "body						VARCHAR		NOT NULL,"
	//									+ "CONSTRAINT cloneannotation_fk_clone FOREIGN KEY (clone_uuid) REFERENCES clone (uuid) ON DELETE CASCADE"
	//									+ ")");
	//
	//			//create some indices for performance
	//			//TODO we definitely need an index on clonefile.path, clone.file_uuid, clone.group_uuid
	//			con.createStatement().execute("CREATE INDEX idx_clonefile_path ON clonefile(path)");
	//			con.createStatement().execute("CREATE INDEX idx_clone_fileuuid ON clone(file_uuid)");
	//			con.createStatement().execute("CREATE INDEX idx_clone_groupuuid ON clone(group_uuid)");
	//
	//		}
	//		else
	//		{
	//			log.fatal("dropAndcreateTables() - unsuported SQL type: " + type);
	//		}
	//	}

	/**
	 * Converts the given result set into a list of objects which correspond to the given interface or class.
	 * 
	 * @param type the type to convert the data to, must be registered with the currently active <em>ICloneFactoryProvider</em>
	 * 			and any implementation of the type must implement the <em>IStatefulObject</em> interface. Never null.
	 * @param rs the result set to convert, may be null.
	 * @return the converted results list, never null.
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	public static List<IStatefulObject> ormStatefulObjects(Class type, ResultSet rs) throws SQLException
	{
		if (log.isTraceEnabled())
			log.trace("ormStatefulObjects() - type:" + type + ", resultSet: " + rs);
		assert (type != null);

		List<IStatefulObject> result = new LinkedList<IStatefulObject>();

		if (rs == null)
		{
			log.debug("ormStatefulObjects(): result set was NULL");
			return result;
		}

		//get a clone factory provider
		ICloneFactoryProvider cloneFactoryProvider = (ICloneFactoryProvider) CPCCorePlugin.getProviderRegistry()
				.lookupProvider(ICloneFactoryProvider.class);

		IStatefulObject statefulObject = null;
		Map<String, Class<? extends Object>> stateTypes = null;
		while (rs.next())
		{
			//first try to get an instance 
			statefulObject = (IStatefulObject) cloneFactoryProvider.getInstance(type);
			if (statefulObject == null)
			{
				log.error("ormStatefulObjects() - unable to obtain instance for type: " + type, new Throwable());
				return result;
			}

			//cache state types
			if (stateTypes == null)
				stateTypes = statefulObject.getStateTypes();

			Map<String, Comparable<?>> stateMap = convertResultSetToMap(stateTypes, rs);

			//restore the state
			statefulObject.setState(stateMap);

			//if this is a cloneobject, mark it as non-dirty
			if (statefulObject instanceof IStoreCloneObject)
				((IStoreCloneObject) statefulObject).setDirty(false);

			//add to result list
			result.add(statefulObject);
		}

		if (log.isTraceEnabled())
			log.trace("ormStatefulObjects() - result: " + result);

		return result;
	}

	/**
	 * Takes a state types map and a result set and returns a list of state maps which represents the
	 * contents of the result set.
	 * 
	 * @param stateTypes a state types map as returned by {@link IStatefulObject#getStateTypes()}, never null.
	 * @param rs the result set to convert, may be NULL.
	 * @return a list of {@link IStatefulObject#getState()} style state maps, never null.
	 */
	public static List<Map<String, Comparable<? extends Object>>> ormStateMaps(
			Map<String, Class<? extends Object>> stateTypes, ResultSet rs) throws SQLException
	{
		if (log.isTraceEnabled())
			log.trace("ormStateMaps() - resultSet: " + rs);

		List<Map<String, Comparable<? extends Object>>> result = new LinkedList<Map<String, Comparable<? extends Object>>>();

		if (rs == null)
		{
			log.debug("ormStateMaps(): result set was NULL");
			return result;
		}

		while (rs.next())
		{
			Map<String, Comparable<? extends Object>> stateMap = convertResultSetToMap(stateTypes, rs);
			result.add(stateMap);
		}

		if (log.isTraceEnabled())
			log.trace("ormStateMaps() - result: " + CoreStringUtils.truncateList(result));

		return result;
	}

	@SuppressWarnings("unchecked")
	public static List<IClone> ormClones(ResultSet rs) throws SQLException
	{
		if (log.isTraceEnabled())
			log.trace("ormClones(): " + rs);

		List result = ormStatefulObjects(IClone.class, rs);

		//we know that the list only contains IClone implementations
		return (List<IClone>) result;
	}

	@SuppressWarnings("unchecked")
	public static List<ICloneFile> ormCloneFiles(ResultSet rs) throws SQLException
	{
		if (log.isTraceEnabled())
			log.trace("ormCloneFiles(): " + rs);

		List result = ormStatefulObjects(ICloneFile.class, rs);

		//we know that the list only contains ICloneFile implementations
		return (List<ICloneFile>) result;
	}

	@SuppressWarnings("unchecked")
	public static List<ICloneGroup> ormCloneGroups(ResultSet rs) throws SQLException
	{
		if (log.isTraceEnabled())
			log.trace("ormCloneGroups(): " + rs);

		List result = ormStatefulObjects(ICloneGroup.class, rs);

		//we know that the list only contains ICloneFile implementations
		return (List<ICloneGroup>) result;
	}

	/**
	 * Inserts the given stateful object into the database.
	 * 
	 * @param con an open database connection, never null.
	 * @param object the stateful object to insert, never null.
	 * @param stateMap optional state map, if not present, it will be retrieved from the stateful object, may be NULL. 
	 */
	public static void ormInsert(Connection con, IStatefulObject object,
			Map<String, Comparable<? extends Object>> stateMap) throws SQLException
	{
		if (log.isTraceEnabled())
			log.trace("ormInsert() - object: " + object + ", stateMap: " + CoreStringUtils.truncateMap(stateMap));
		assert (con != null && object != null);

		PreparedStatement stmt = prepareInsertStatement(con, object, stateMap);

		boolean success = (stmt.executeUpdate() == 1);

		stmt.close();

		//if we didn't get an sql exception, then this should always be successful
		if (!success)
			log.error("ormUpdate() - failed to insert object, but no SQLException was thrown - object: " + object,
					new Throwable());

		if (log.isTraceEnabled())
			log.trace("ormInsert() - success: " + success);
	}

	/**
	 * Updates the given stateful object in the database.
	 * 
	 * @param con an open database connection, never null.
	 * @param object the stateful object to update, never null.
	 * @param stateMap optional state map, if not present, it will be retrieved from the stateful object, may be NULL.
	 *  
	 * @return the number of affected entries. Only 0 and 1 are returned.
	 */
	public static int ormUpdate(Connection con, IStatefulObject object,
			Map<String, Comparable<? extends Object>> stateMap) throws SQLException
	{
		if (log.isTraceEnabled())
			log.trace("ormUpdate() - object: " + object + ", stateMap: " + CoreStringUtils.truncateMap(stateMap));
		assert (con != null && object != null);

		PreparedStatement stmt = prepareUpdateStatement(con, object, stateMap);
		if (stmt == null)
		{
			log
					.trace("ormUpdate() - nothing needs to be updated for this object, it consists solely out of primary keys.");

			//even though we don't need to update anything, we still need to check whether the db entry exists.
			//our clients depend on a correct return value.

			return (statefulObjectExists(con, object) ? 1 : 0);
		}

		int affectedRows = stmt.executeUpdate();

		//we're updating by primary key, affected Rows should always be 0 or 1.
		if (affectedRows > 1)
			log.error("ormUpdate() - more than one affected row on primary key update - affectedRows: " + affectedRows
					+ ", object: " + object, new Throwable());

		stmt.close();

		if (log.isTraceEnabled())
			log.trace("ormUpdate() - affectedRows: " + affectedRows);

		return affectedRows;
	}

	/**
	 * Deletes the given stateful object from the database, if it exists.
	 * 
	 * @param con an open database connection, never null.
	 * @param object the stateful object to delete, never null.
	 * @param stateMap optional state map, if not present, it will be retrieved from the stateful object, may be NULL.
	 *  
	 * @return the number of affected entries. Only 0 and 1 are returned.
	 */
	public static int ormDelete(Connection con, IStatefulObject object,
			Map<String, Comparable<? extends Object>> stateMap) throws SQLException
	{
		if (log.isTraceEnabled())
			log.trace("ormDelete() - object: " + object + ", stateMap: " + CoreStringUtils.truncateMap(stateMap));
		assert (con != null && object != null);

		PreparedStatement stmt = prepareDeleteStatement(con, object, stateMap);

		int affectedRows = stmt.executeUpdate();

		//we're deleting by primary key, affected Rows should always be 0 or 1.
		if (affectedRows > 1)
			log.error("ormDelete() - more than one affected row on primary key deletion - affectedRows: "
					+ affectedRows + ", object: " + object, new Throwable());

		stmt.close();

		if (log.isTraceEnabled())
			log.trace("ormDelete() - affectedRows: " + affectedRows);

		return affectedRows;
	}

	/**
	 * Checks whether the given {@link IStatefulObject} has a corresponding database entry or not.<br/>
	 * It does <b>not</b> check whether the values of the object and the db entry match.
	 * 
	 * @param con a db connection, never null.
	 * @param object the {@link IStatefulObject} to check, never null.
	 * @return true if the object exists in the db, false otherwise.
	 */
	public static boolean statefulObjectExists(Connection con, IStatefulObject statefulObject) throws SQLException
	{
		if (log.isTraceEnabled())
			log.trace("statefulObjectExists(): " + statefulObject);

		String persistenceObjectIdentifier = SqlQueryUtils.escapeSqlIdentifier(statefulObject
				.getPersistenceObjectIdentifier());

		PreparedStatement stmt = con.prepareStatement("SELECT " + persistenceObjectIdentifier + " FROM "
				+ SqlQueryUtils.escapeSqlIdentifier(statefulObject.getPersistenceClassIdentifier()) + " WHERE "
				+ persistenceObjectIdentifier + "=?");

		Object keyValue = statefulObject.getState().get(statefulObject.getPersistenceObjectIdentifier());

		setStatementValue(stmt, 1, keyValue);

		ResultSet rs = stmt.executeQuery();
		boolean exists = rs.next();

		stmt.close();

		if (log.isTraceEnabled())
			log.trace("statefulObjectExists() - result: " + exists);

		return exists;
	}

	/**
	 * Checks if the given group uuid is already listed in the clonegroup table and inserts it
	 * if it is missing.
	 * 
	 * @param groupUuid
	 * @throws SQLException 
	 */
	public static void persistGroupUuid(Connection con, String groupUuid) throws SQLException
	{
		if (log.isTraceEnabled())
			log.trace("persistGroupUuid(): " + groupUuid);
		assert (con != null && groupUuid != null);

		if (!groupExists(con, groupUuid))
		{
			PreparedStatement stmt = con.prepareStatement("INSERT INTO "
					+ SqlQueryUtils.escapeSqlIdentifier(ICloneGroup.PERSISTENCE_CLASS_IDENTIFIER) + " ("
					+ SqlQueryUtils.escapeSqlIdentifier(ICloneGroup.PERSISTENCE_OBJECT_IDENTIFIER) + ") VALUES (?)");
			stmt.setString(1, groupUuid);
			stmt.execute();
			stmt.close();
		}

	}

	private static boolean groupExists(Connection con, String groupUuid) throws SQLException
	{
		if (log.isTraceEnabled())
			log.trace("groupExists(): " + groupUuid);
		assert (groupUuid != null);

		PreparedStatement stmt = con.prepareStatement("SELECT * FROM "
				+ SqlQueryUtils.escapeSqlIdentifier(ICloneGroup.PERSISTENCE_CLASS_IDENTIFIER) + " WHERE "
				+ SqlQueryUtils.escapeSqlIdentifier(ICloneGroup.PERSISTENCE_OBJECT_IDENTIFIER) + "=?");

		stmt.setString(1, groupUuid);

		ResultSet rs = stmt.executeQuery();
		boolean exists = rs.next();

		stmt.close();

		if (log.isTraceEnabled())
			log.trace("groupExists() - result: " + exists);

		return exists;
	}

	/**
	 * Creates a prepared statement for the insertion of the given stateful object.
	 * Also sets all the parameters of the statement accordingly.
	 * 
	 * @param stateMap optional state map, if not present, it will be retrieved from the <em>statefulObject</em>, may be NULL. 
	 */
	private static PreparedStatement prepareInsertStatement(Connection con, IStatefulObject statefulObject,
			Map<String, Comparable<? extends Object>> stateMap) throws SQLException
	{
		if (log.isTraceEnabled())
			log.trace("prepareInsertStatement() - statefulObject: " + statefulObject + ", stateMap: "
					+ CoreStringUtils.truncateMap(stateMap));
		assert (con != null && statefulObject != null);

		StringBuilder query = new StringBuilder();

		query.append("INSERT INTO ");
		query.append(SqlQueryUtils.escapeSqlIdentifier(statefulObject.getPersistenceClassIdentifier()));
		query.append(" (");

		//add all keys from the state map
		if (stateMap == null)
			stateMap = statefulObject.getState();
		List<String> keyList = new ArrayList<String>(stateMap.keySet());

		for (int i = 0; i < keyList.size(); ++i)
		{
			if (i != 0)
				query.append(',');

			query.append(SqlQueryUtils.escapeSqlIdentifier(keyList.get(i)));
		}

		query.append(") VALUES (");

		//now add one question mark for every key
		for (int i = 0; i < keyList.size(); ++i)
		{
			if (i != 0)
				query.append(',');

			query.append('?');
		}

		query.append(')');

		if (log.isDebugEnabled())
			log.debug("SQL: " + query.toString());

		PreparedStatement stmt = con.prepareStatement(query.toString());

		setStatementValues(stmt, keyList, stateMap);

		return stmt;
	}

	/**
	 * Creates a prepared statement for the update of the given stateful object.<br/>
	 * Also sets all the parameters of the statement accordingly.<br/>
	 * Will return NULL if the object itself consists only of primary keys and therefore
	 * nothing needs to be updated.
	 * 
	 * @param stateMap optional state map, if not present, it will be retrieved from the <em>statefulObject</em>, may be NULL.
	 * 
	 * @return a ready-to-execute {@link PreparedStatement} with all values filled in or NULL if nothing needs to be updated. 
	 */
	private static PreparedStatement prepareUpdateStatement(Connection con, IStatefulObject statefulObject,
			Map<String, Comparable<? extends Object>> stateMap) throws SQLException
	{
		if (log.isTraceEnabled())
			log.trace("prepareUpdateStatement() - statefulObject: " + statefulObject + ", stateMap: "
					+ CoreStringUtils.truncateMap(stateMap));
		assert (con != null && statefulObject != null);

		//add all keys from the state map
		if (stateMap == null)
			stateMap = statefulObject.getState();
		List<String> finalKeyList = new ArrayList<String>(stateMap.size());

		//make sure that this entry has more than just a primary key.
		if (stateMap.size() == 1
				|| ((statefulObject instanceof IMultiKeyStatefulObject) && ((IMultiKeyStatefulObject) statefulObject)
						.getPersistenceObjectIdentifiers().size() == stateMap.size()))
		{
			log.trace("prepareUpdateStatement() - skipping update for object which contains only primary keys.");
			return null;
		}

		StringBuilder query = new StringBuilder();

		query.append("UPDATE ");
		query.append(SqlQueryUtils.escapeSqlIdentifier(statefulObject.getPersistenceClassIdentifier()));
		query.append(" SET ");

		int realPos = 0;
		for (String key : stateMap.keySet())
		{
			//don't update the primary key
			if (statefulObject instanceof IMultiKeyStatefulObject)
			{
				if (((IMultiKeyStatefulObject) statefulObject).getPersistenceObjectIdentifiers().contains(key))
					continue;
			}
			else if (statefulObject.getPersistenceObjectIdentifier().equals(key))
				continue;

			if (realPos != 0)
				query.append(',');

			query.append(SqlQueryUtils.escapeSqlIdentifier(key));
			query.append("=?");

			finalKeyList.add(key);

			++realPos;
		}

		query.append(" WHERE ");
		if (statefulObject instanceof IMultiKeyStatefulObject)
		{
			//multi column primary key
			boolean first = true;
			for (String key : ((IMultiKeyStatefulObject) statefulObject).getPersistenceObjectIdentifiers())
			{
				if (first)
					first = false;
				else
					query.append(" AND ");

				query.append(SqlQueryUtils.escapeSqlIdentifier(key));
				query.append("=?");

				finalKeyList.add(key);
			}
		}
		else
		{
			//single column primary key
			query.append(SqlQueryUtils.escapeSqlIdentifier(statefulObject.getPersistenceObjectIdentifier()));
			query.append("=?");

			finalKeyList.add(statefulObject.getPersistenceObjectIdentifier());
		}

		if (log.isDebugEnabled())
			log.debug("SQL: " + query.toString());

		PreparedStatement stmt = con.prepareStatement(query.toString());

		setStatementValues(stmt, finalKeyList, stateMap);

		return stmt;
	}

	/**
	 * Creates a prepared statement for the deletion of the given stateful object.<br/>
	 * Also sets all the parameters of the statement accordingly.
	 * 
	 * @param stateMap optional state map, if not present, it will be retrieved from the <em>statefulObject</em>, may be NULL.
	 * 
	 * @return a ready-to-execute {@link PreparedStatement} with all values filled in, never null. 
	 */
	private static PreparedStatement prepareDeleteStatement(Connection con, IStatefulObject statefulObject,
			Map<String, Comparable<? extends Object>> stateMap) throws SQLException
	{
		if (log.isTraceEnabled())
			log.trace("prepareDeleteStatement() - statefulObject: " + statefulObject + ", stateMap: "
					+ CoreStringUtils.truncateMap(stateMap));
		assert (con != null && statefulObject != null);

		//add all keys from the state map
		if (stateMap == null)
			stateMap = statefulObject.getState();
		List<String> finalKeyList = new ArrayList<String>(stateMap.size());

		StringBuilder query = new StringBuilder();

		query.append("DELETE FROM ");
		query.append(SqlQueryUtils.escapeSqlIdentifier(statefulObject.getPersistenceClassIdentifier()));

		query.append(" WHERE ");
		if (statefulObject instanceof IMultiKeyStatefulObject)
		{
			//multi column primary key
			boolean first = true;
			for (String key : ((IMultiKeyStatefulObject) statefulObject).getPersistenceObjectIdentifiers())
			{
				if (first)
					first = false;
				else
					query.append(" AND ");

				query.append(SqlQueryUtils.escapeSqlIdentifier(key));
				query.append("=?");

				finalKeyList.add(key);
			}
		}
		else
		{
			//single column primary key
			query.append(SqlQueryUtils.escapeSqlIdentifier(statefulObject.getPersistenceObjectIdentifier()));
			query.append("=?");

			finalKeyList.add(statefulObject.getPersistenceObjectIdentifier());
		}

		if (log.isDebugEnabled())
			log.debug("SQL: " + query.toString());

		PreparedStatement stmt = con.prepareStatement(query.toString());

		setStatementValues(stmt, finalKeyList, stateMap);

		return stmt;
	}

	/**
	 * Takes the given list of keys and a Map which maps those keys to values and
	 * sets all parameters of the given statement accordingly.
	 */
	private static void setStatementValues(PreparedStatement stmt, List<String> keyList,
			Map<String, Comparable<?>> valueMap) throws SQLException
	{
		if (log.isTraceEnabled())
			log.trace("setStatementValues() - stmt: " + stmt + ", keyList: " + keyList + ", valueMap: "
					+ CoreStringUtils.truncateMap(valueMap));
		assert (stmt != null && keyList != null && valueMap != null);

		//set the values
		int i = 1;
		for (String key : keyList)
		{
			Object value = valueMap.get(key);

			setStatementValue(stmt, i, value);

			++i;
		}
	}

	/**
	 * Sets the parameter at the given position to the given value.
	 * Internally casts the supported types to the correct class.
	 */
	private static void setStatementValue(PreparedStatement stmt, int position, Object value) throws SQLException
	{
		assert (stmt != null && position >= 1);

		if (value == null)
			stmt.setObject(position, null);

		else if (value instanceof String)
			stmt.setString(position, (String) value);

		else if (value instanceof Integer)
			stmt.setInt(position, (Integer) value);

		else if (value instanceof Long)
			stmt.setLong(position, (Long) value);

		else if (value instanceof Float)
			stmt.setFloat(position, (Float) value);

		else if (value instanceof Double)
			stmt.setDouble(position, (Double) value);

		else if (value instanceof Boolean)
			stmt.setBoolean(position, (Boolean) value);

		else if (value instanceof Date)
			stmt.setTimestamp(position, new Timestamp(((Date) value).getTime()));

		else
			log.error("setStatementValue() - unsupported data type: " + value + " for position: " + position,
					new Throwable());
	}

	private static Map<String, Comparable<? extends Object>> convertResultSetToMap(
			Map<String, Class<? extends Object>> stateTypes, ResultSet rs) throws SQLException
	{
		if (log.isTraceEnabled())
			log.trace("convertResultSetToMap() - stateTypes: " + stateTypes + ", resultSet: " + rs);
		assert (stateTypes != null && rs != null);

		//create a new result map
		Map<String, Comparable<? extends Object>> result = new HashMap<String, Comparable<? extends Object>>(stateTypes
				.size());

		//iterate through all possible keys and copy the corresponding value over to the result map
		for (String key : stateTypes.keySet())
		{
			result.put(key, convertResultSetValue(key, stateTypes.get(key), rs));
		}

		if (log.isTraceEnabled())
			log.trace("convertResultSetToMap() - result: " + CoreStringUtils.truncateMap(result));

		//we're done, return the result
		return result;
	}

	private static Comparable<? extends Object> convertResultSetValue(String key, Class<? extends Object> type,
			ResultSet rs) throws SQLException
	{
		if (log.isTraceEnabled())
			log.trace("convertResultSetValue() - key: " + key + ", type: " + type + ", resultSet: " + rs);
		assert (key != null && type != null && rs != null);

		String escapedKey = SqlQueryUtils.escapeSqlIdentifier(key);
		Comparable<? extends Object> result = null;

		if (type == String.class)
			result = rs.getString(escapedKey);

		else if (type == Integer.class)
			result = rs.getInt(escapedKey);

		else if (type == Long.class)
			result = rs.getLong(escapedKey);

		else if (type == Float.class)
			result = rs.getFloat(escapedKey);

		else if (type == Double.class)
			result = rs.getDouble(escapedKey);

		else if (type == Boolean.class)
			result = rs.getBoolean(escapedKey);

		else if (type == Date.class)
		{
			Timestamp ts = rs.getTimestamp(escapedKey);
			if (ts != null)
				result = new Date(ts.getTime());
		}

		else
			log.error("convertResultSetValue() - unsupported data type: " + type + " for key: " + key, new Throwable());

		if (log.isTraceEnabled())
			log.trace("convertResultSetValue() - result: "
					+ (result != null ? CoreStringUtils.truncateString(result.toString()) : "null"));

		return result;
	}

	/**
	 * Takes the persistence class identifiers of an {@link ICloneObject} parent object and an
	 * {@link ICloneObjectExtensionMultiStatefulObject} child object and returns the corresponding
	 * combined persistence class identifier.
	 * 
	 * @param parentPersistenceClassIdentifier persistence class identifier of the {@link ICloneObject} parent object, never null.
	 * @param persistenceClassIdentifier persistence class identifier of the {@link ICloneObjectExtensionMultiStatefulObject} child object, never null.
	 * @return new combined persistence class identifier, never null.
	 */
	public static String getMultiStatefulObjectPersistenceClassIdentifier(String parentPersistenceClassIdentifier,
			String persistenceClassIdentifier)
	{
		return parentPersistenceClassIdentifier + "__" + persistenceClassIdentifier;
	}

}
