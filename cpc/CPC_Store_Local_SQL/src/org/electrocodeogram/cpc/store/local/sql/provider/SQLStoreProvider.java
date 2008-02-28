package org.electrocodeogram.cpc.store.local.sql.provider;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Preferences;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.ICloneGroup;
import org.electrocodeogram.cpc.core.api.data.ICloneObject;
import org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension;
import org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionLazyMultiStatefulObject;
import org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionMultiStatefulObject;
import org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionStatefulObject;
import org.electrocodeogram.cpc.core.api.data.special.IStatefulObject;
import org.electrocodeogram.cpc.core.api.data.special.IStoreCloneObject;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;
import org.electrocodeogram.cpc.store.local.sql.CPCStoreLocalSQLPlugin;
import org.electrocodeogram.cpc.store.local.sql.data.CloneFileContent;
import org.electrocodeogram.cpc.store.local.sql.data.ICloneFileContent;
import org.electrocodeogram.cpc.store.local.sql.preferences.CPCPreferenceConstants;
import org.electrocodeogram.cpc.store.local.sql.utils.OrmUtils;
import org.electrocodeogram.cpc.store.local.sql.utils.SqlQueryUtils;
import org.electrocodeogram.cpc.store.local.sql.utils.StatefulObjectAdapter;
import org.electrocodeogram.cpc.store.provider.AbstractStoreProvider;


/**
 * SQL based implementation of {@link IStoreProvider} based on the {@link AbstractStoreProvider}.
 * <br>
 * By default is uses an internal HSQL database as SQL database.
 * <br>
 * Use of an external JDBC compatible database is possible. However, it has so far only been tested with
 * PostgreSQL.
 * <p>
 * Limitations of this implementation:
 * <ul>
 * 	<li>Not tested with anything but HSQL and PostgreSQL</li>
 * 	<li>It is not possible to migrate the data from one database to another, using this store provider.
 * 		External tools would be required for that.</li>
 * 	<li>The SQL tables are only generated when they are not found. No checking is done to ensure
 * 		that the SQL schema is the latest version. This will pose a problem in situations where
 * 		additional extensions are added, which would need new tables.
 * 		In the long run a full SQL schema check at startup should be done, followed by a graceful
 * 		update of all parts of the SQL schema which are not up to date.
 * 		All of this would need to work without data loss.</li>
 * </ul>
 * 
 * @author vw
 * 
 * @see IStoreProvider
 * @see AbstractStoreProvider
 */
public class SQLStoreProvider extends AbstractStoreProvider implements IStoreProvider
{
	private static Log log = LogFactory.getLog(SQLStoreProvider.class);

	/**
	 * Our JDBC connection, it is kept alive over the entire lifetime of the provider.
	 */
	protected Connection con = null;

	/**
	 * Cached preference value.
	 * 
	 * @see CPCPreferenceConstants#PREF_SQLSTORE_INTERNALHSQLMODE
	 */
	protected boolean hsqldbMode = false;

	/**
	 * Specification of the underlying database type.
	 */
	public enum SqlType
	{
		/**
		 * HSQLDB (tested with 1.8.0.8)
		 */
		HSQL,

		/**
		 * PostgreSQL (tested with 8.2.5)
		 */
		PGSQL
	}

	public SQLStoreProvider()
	{
		log.trace("SQLStoreProvider()");

		Preferences prefs = CPCStoreLocalSQLPlugin.getDefault().getPluginPreferences();

		if (prefs.getBoolean(CPCPreferenceConstants.PREF_SQLSTORE_INTERNALHSQLMODE))
		{
			//we put the hsqldb in our plugins meta data directory
			IPath stateLoc = CPCStoreLocalSQLPlugin.getDefault().getStateLocation().makeAbsolute();
			log.info("SQLStoreProvider() - using internal HSQLDB - state location for db: " + stateLoc);

			if (!dbConnect("org.hsqldb.jdbcDriver", "jdbc:hsqldb:file:" + stateLoc + "/db/cpcdb", "sa", ""))
				return;

			hsqldbMode = true;
		}
		else
		{
			//use an external database
			if (!dbConnect(prefs.getString(CPCPreferenceConstants.PREF_SQLSTORE_JDBCDRIVER), prefs
					.getString(CPCPreferenceConstants.PREF_SQLSTORE_JDBCURL), prefs
					.getString(CPCPreferenceConstants.PREF_SQLSTORE_USERNAME), prefs
					.getString(CPCPreferenceConstants.PREF_SQLSTORE_PASSWORD)))
				return;
		}

		//TODO: This should require a user confirmation, it means that either a new workspace was
		//		created, the plugin is first used with a specific workspace or our data base was lost for
		//		some reason.
		try
		{
			if (prefs.getBoolean(CPCPreferenceConstants.PREF_SQLSTORE_INTERNALHSQLMODE))
				OrmUtils.createsTablesIfMissing(SqlType.HSQL, con);
			else
				//TODO: add support for more db types here
				OrmUtils.createsTablesIfMissing(SqlType.PGSQL, con);
		}
		catch (Exception e)
		{
			log.fatal("SQLStoreProvider(): table creation failed - " + e, e);
		}
	}

	/**
	 * Constructor for standalone test mode.
	 */
	public SQLStoreProvider(SqlType sqlType, String jdbcDriver, String jdbcUrl, String username, String password)
	{
		//standalone test mode
		super(true);

		if (log.isTraceEnabled())
			log.trace("SQLStoreProvider() - jdbcDriver: " + jdbcDriver + ", jdbcUrl: " + jdbcUrl + ", user: "
					+ username);

		log.info("SQLStoreProvider() - using jdbc - " + jdbcUrl);

		dbConnect(jdbcDriver, jdbcUrl, username, password);

		//TODO: this should require a user confirmation, it means that either a new workspace was
		//		created, the plugin is first used with a specific workspace or our data base was lost for
		//		some reason.
		try
		{
			OrmUtils.createsTablesIfMissing(sqlType, con);
		}
		catch (Exception e)
		{
			log.fatal("SQLStoreProvider(): table creation failed - " + e, e);
		}
	}

	/*
	 * IStoreProvider Methods
	 */

	//all implemented by AbstractStoreProvider
	/*
	 * AbstractStoreProvider Support Methods
	 */

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.provider.AbstractStoreProvider#subGetCloneFileByPath(java.lang.String)
	 */
	@Override
	protected ICloneFile subGetCloneFileByPath(String project, String filePath)
	{
		if (log.isTraceEnabled())
			log.trace("subGetCloneFileByPath() - project: " + project + ", filePath: " + filePath);
		assert (project != null && filePath != null);

		return SqlQueryUtils.getCloneFileByPath(con, project, filePath);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.provider.AbstractStoreProvider#subPersistCloneFile(org.electrocodeogram.cpc.store.data.CloneFile)
	 */
	@Override
	protected boolean subPersistCloneFile(ICloneFile file)
	{
		if (log.isTraceEnabled())
			log.trace("subPersistCloneFile() - file: " + file);
		assert (file != null);

		try
		{
			if (OrmUtils.ormUpdate(con, (IStatefulObject) file, null) == 0)
			{
				OrmUtils.ormInsert(con, (IStatefulObject) file, null);
			}
			((IStoreCloneObject) file).setDirty(false);

			return true;
		}
		catch (SQLException e)
		{
			log.error("lookupCloneFileByPath() - SQL Error for: " + file, e);

			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.provider.AbstractStoreProvider#subGetCloneFileByUuid(java.lang.String)
	 */
	@Override
	protected ICloneFile subGetCloneFileByUuid(String fileUuid)
	{
		if (log.isTraceEnabled())
			log.trace("subGetCloneFileByUuid() - fileUuid: " + fileUuid);
		assert (fileUuid != null);

		return SqlQueryUtils.getCloneFileByUuid(con, fileUuid);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.provider.AbstractStoreProvider#subGetCloneByUuid(java.lang.String, java.lang.String)
	 */
	@Override
	protected IClone subGetCloneByUuid(String cloneUuid, String fileUUid)
	{
		if (log.isTraceEnabled())
			log.trace("subGetCloneByUuid() - cloneUuid: " + cloneUuid + ", fileUUid: " + fileUUid);
		assert (cloneUuid != null);

		return SqlQueryUtils.getCloneByUuid(con, cloneUuid);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.provider.AbstractStoreProvider#subGetCloneGroupByUuid(java.lang.String)
	 */
	@Override
	protected ICloneGroup subGetCloneGroupByUuid(String groupUuid)
	{
		if (log.isTraceEnabled())
			log.trace("subGetCloneGroupByUuid() - groupUuid: " + groupUuid);
		assert (groupUuid != null);

		return SqlQueryUtils.getCloneGroupByUuid(con, groupUuid);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.provider.AbstractStoreProvider#subGetClonesByFile(java.lang.String)
	 */
	@Override
	protected List<IClone> subGetClonesByFile(String fileUuid)
	{
		if (log.isTraceEnabled())
			log.trace("subGetClonesByFile() - fileUuid: " + fileUuid);
		assert (fileUuid != null);

		/*
		 * TODO:/FIXME: API specification states that the return value must never be null.
		 * 		However, here and in other methods of this provider we actually return
		 * 		null if an SQL error occurs internally.
		 */
		return SqlQueryUtils.getClonesByFileUuid(con, fileUuid);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.provider.AbstractStoreProvider#subGetClonesByGroup(java.lang.String)
	 */
	@Override
	protected List<IClone> subGetClonesByGroup(String groupUuid)
	{
		if (log.isTraceEnabled())
			log.trace("subGetClonesByGroup() - groupUuid: " + groupUuid);
		assert (groupUuid != null);

		return SqlQueryUtils.getClonesByGroupUuid(con, groupUuid);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.provider.AbstractStoreProvider#subGetFullCloneObjectExtension(org.electrocodeogram.cpc.core.api.data.ICloneObject, org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionLazyMultiStatefulObject)
	 */
	@Override
	protected ICloneObjectExtensionLazyMultiStatefulObject subGetFullCloneObjectExtension(ICloneObject cloneObject,
			ICloneObjectExtensionLazyMultiStatefulObject extension)
	{
		if (log.isTraceEnabled())
			log.trace("subGetFullCloneObjectExtension() - cloneObject: " + cloneObject + ", extension: " + extension);
		assert (cloneObject != null && extension != null);

		try
		{
			ICloneObjectExtensionLazyMultiStatefulObject result = (ICloneObjectExtensionLazyMultiStatefulObject) extension
					.clone();

			SqlQueryUtils.loadCloneObjectExtensionSubElements(con, cloneObject, result, true);

			if (log.isTraceEnabled())
				log.trace("subGetFullCloneObjectExtension() - result: " + result);

			return result;
		}
		catch (CloneNotSupportedException e)
		{
			log.error("subGetFullCloneObjectExtension() - unable to clone extension - " + extension + " - " + e, e);
		}
		catch (SQLException e)
		{
			log.error("subGetFullCloneObjectExtension() - unable to load extension sub-element data - SQL ERROR - "
					+ extension + " - " + e, e);
		}

		log
				.warn("subGetFullCloneObjectExtension() - loading of extension sub-element data failed, returning old extension instance.");

		//on error, just return the unmodified extension
		return extension;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.provider.AbstractStoreProvider#subPersistData(org.electrocodeogram.cpc.store.data.CloneFile, java.util.Set)
	 */
	@Override
	protected void subPersistData(ICloneFile file, Set<IClone> clones)
	{
		if (log.isTraceEnabled())
			log.trace("subPersistData() - file: " + file + ", clones: " + clones);
		assert (file != null && clones != null);

		try
		{
			//execute all the following commands in one transaction
			con.setAutoCommit(false);

			if (((IStoreCloneObject) file).isDirty())
			{
				//first update or insert file
				if (OrmUtils.ormUpdate(con, (IStatefulObject) file, null) == 0)
				{
					OrmUtils.ormInsert(con, (IStatefulObject) file, null);
				}
				((IStoreCloneObject) file).setDirty(false);
			}
			else
			{
				if (log.isTraceEnabled())
					log.trace("subPersistData() - skipping non-dirty file: " + file);
			}

			//first delete all clones for this file which are not part of the given set
			SqlQueryUtils.deleteAllClonesForFileExcept(con, file.getUuid(), clones);

			//now update or insert all clones
			for (IClone clone : clones)
			{
				if (((IStoreCloneObject) clone).isDirty())
				{
					if (clone.getGroupUuid() != null)
					{
						//make sure that the group is added if it's not already in the db
						OrmUtils.persistGroupUuid(con, clone.getGroupUuid());
					}

					//preload state map
					Map<String, Comparable<? extends Object>> stateMap = ((IStatefulObject) clone).getState();

					//update or add the clone
					if (OrmUtils.ormUpdate(con, (IStatefulObject) clone, stateMap) == 0)
					{
						OrmUtils.ormInsert(con, (IStatefulObject) clone, stateMap);
					}

					//now insert/update/delete extensions, if needed
					persistExtensionData(clone);

					//clear dirty flag
					((IStoreCloneObject) clone).setDirty(false);

					if (log.isTraceEnabled())
						log.trace("subPersistData() - persisted clone: " + clone);
				}
				else
				{
					if (log.isTraceEnabled())
						log.trace("subPersistData() - skipping non-dirty clone: " + clone);
				}
			}

			//commit work
			con.commit();
		}
		catch (SQLException e)
		{
			log
					.fatal("subPersistData(): unable to persist data - file: " + file + ", clones: " + clones + " - "
							+ e, e);

			try
			{
				con.rollback();
			}
			catch (SQLException e2)
			{
				log.fatal("subPersistData(): unable to roll back transaction - " + e2, e2);
			}
		}
		finally
		{
			//re-enable auto commit
			try
			{
				con.setAutoCommit(true);
			}
			catch (SQLException e2)
			{
				log.fatal("subPersistData(): unable to re-enable auto commit - " + e2, e2);
			}
		}
	}

	/**
	 * Persists any existing stateful {@link ICloneObjectExtension}s which are currently added for
	 * the given clone object by inserting/updating the corresponding extensions.<br/>
	 * Also persists {@link ICloneObjectExtensionMultiStatefulObject} sub-elements.<br/>
	 * <br/>
	 * Deletes any extension from the database which part of the deletion list of the given clone object.<br/>
	 * Deletion of {@link ICloneObjectExtensionMultiStatefulObject} sub-elements are also performed.
	 * 
	 * @param cloneObject clone object to persist all {@link ICloneObjectExtensionStatefulObject}s for, never null.
	 */
	protected void persistExtensionData(ICloneObject cloneObject) throws SQLException
	{
		if (log.isTraceEnabled())
			log.trace("persistExtensionData() - cloneObject: " + cloneObject);
		assert (cloneObject != null);

		/*
		 * Delete no longer existing extensions.
		 */

		for (ICloneObjectExtension extension : ((IStoreCloneObject) cloneObject).getDeletedExtensions())
		{
			//we're only interested in stateful extensions
			if (!(extension instanceof ICloneObjectExtensionStatefulObject))
				continue;

			if (log.isTraceEnabled())
				log.trace("persistExtensionData() - removing extension: " + extension);

			//ok, delete the entry
			OrmUtils.ormDelete(con, (ICloneObjectExtensionStatefulObject) extension, null);
		}

		//clear out deleted extension data
		((IStoreCloneObject) cloneObject).purgeDeletedExtensions();

		/*
		 * Update/Insert existing extensions.
		 */

		for (ICloneObjectExtension extension : cloneObject.getExtensions())
		{
			//we're only interested in stateful extensions
			if (!(extension instanceof ICloneObjectExtensionStatefulObject))
				continue;

			ICloneObjectExtensionStatefulObject statefulExtension = (ICloneObjectExtensionStatefulObject) extension;

			if (log.isTraceEnabled())
				log.trace("persistExtensionData() - persisting extension: " + extension);

			//skip non-dirty extensions
			if (!statefulExtension.isDirty())
			{
				log.trace("persistExtensionData() - skpping persistence step, extension is not dirty.");
				continue;
			}

			//first get the state map
			Map<String, Comparable<? extends Object>> stateMap = statefulExtension.getState();

			/*
			 * An extension which implements ICloneObjectExtensionStatefulObject will always contain
			 * a "parent_uuid" key which points to the UUID of the parent ICloneObject.
			 * We can thus just insert it into the db.
			 */

			//update or add the extension
			if (OrmUtils.ormUpdate(con, statefulExtension, stateMap) == 0)
			{
				OrmUtils.ormInsert(con, statefulExtension, stateMap);
			}

			//now check if this extension is more than a simple stateful object
			//we might have to persist additional data
			if (extension instanceof ICloneObjectExtensionMultiStatefulObject)
			{
				//ok, we'll also need to persist the sub-element lists
				ICloneObjectExtensionMultiStatefulObject multiStatefulExt = (ICloneObjectExtensionMultiStatefulObject) extension;

				List<String> multiClassIdentifiers = multiStatefulExt.getMultiPersistenceClassIdentifier();
				List<String> multiObjectIdentifiers = multiStatefulExt.getMultiPersistenceObjectIdentifier();
				List<List<Map<String, Comparable<? extends Object>>>> multiStates = multiStatefulExt.getMultiState();

				if (log.isTraceEnabled())
					log.trace("persistExtensionData() - persisting sub-element data for extension sub-types: "
							+ multiClassIdentifiers);

				//for each sub-element type
				for (int i = 0; i < multiClassIdentifiers.size(); ++i)
				{
					//the real internal class identifier is composed out of the class identifier of the parent clone object
					//and the class identifier provided for this extension sub-element type.
					String internalPersistenceClassIdentifier = OrmUtils
							.getMultiStatefulObjectPersistenceClassIdentifier(multiStatefulExt
									.getPersistenceClassIdentifier(), multiClassIdentifiers.get(i));

					//for each sub element
					for (Map<String, Comparable<? extends Object>> state : multiStates.get(i))
					{
						//create an adapter object
						IStatefulObject adapter = new StatefulObjectAdapter(internalPersistenceClassIdentifier, Arrays
								.asList(multiStatefulExt.getPersistenceObjectIdentifier(), multiObjectIdentifiers
										.get(i)), state);

						//check whether this entry was deleted
						Boolean deleted = (Boolean) state
								.get(ICloneObjectExtensionMultiStatefulObject.DELETION_MARK_IDENTIFIER);
						if (deleted != null && deleted.booleanValue())
						{
							//deletion
							OrmUtils.ormDelete(con, adapter, state);
						}
						else
						{
							//update or add
							if (OrmUtils.ormUpdate(con, adapter, state) == 0)
							{
								OrmUtils.ormInsert(con, adapter, state);
							}
						}
					}
				}

				//clear out deleted sub-element data
				multiStatefulExt.purgeDeletedEntries();
			}

			//mark as clean
			statefulExtension.setDirty(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.provider.AbstractStoreProvider#subGetCloneFileContent(java.lang.String)
	 */
	@Override
	protected String subGetCloneFileContent(String fileUuid)
	{
		if (log.isTraceEnabled())
			log.trace("subGetCloneFileContent() - fileUuid: " + fileUuid);
		assert (fileUuid != null);

		String result = null;

		ICloneFileContent fileContent = (ICloneFileContent) SqlQueryUtils.getStatefulObjectByUuid(con,
				new CloneFileContent("TEMP", null), fileUuid);
		if (fileContent != null)
			result = fileContent.getContent();

		if (log.isTraceEnabled())
			log.trace("subGetCloneFileContent() - result: " + CoreStringUtils.truncateString(result));

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.provider.AbstractStoreProvider#subPersistCloneFileContent(java.lang.String, java.lang.String)
	 */
	@Override
	protected void subPersistCloneFileContent(String fileUuid, String content)
	{
		if (log.isTraceEnabled())
			log.trace("subPersistCloneFileContent() - fileUuid: " + fileUuid + ", content: "
					+ CoreStringUtils.truncateString(content));
		assert (fileUuid != null && content != null);

		try
		{
			//execute all the following commands in one transaction
			con.setAutoCommit(false);

			//package the data
			ICloneFileContent fileContent = new CloneFileContent(fileUuid, content);

			//first update or insert the data 
			if (OrmUtils.ormUpdate(con, fileContent, null) == 0)
			{
				OrmUtils.ormInsert(con, fileContent, null);
			}

			//commit work
			con.commit();
		}
		catch (SQLException e)
		{
			log.fatal("subPersistCloneFileContent(): unable to persist data - fileUuid: " + fileUuid + ", content: "
					+ content + " - " + e, e);

			try
			{
				con.rollback();
			}
			catch (SQLException e2)
			{
				log.fatal("subPersistCloneFileContent(): unable to roll back transaction - " + e2, e2);
			}
		}
		finally
		{
			//re-enable auto commit
			try
			{
				con.setAutoCommit(true);
			}
			catch (SQLException e2)
			{
				log.fatal("subPersistCloneFileContent(): unable to re-enable auto commit - " + e2, e2);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.provider.AbstractStoreProvider#subMoveCloneFile(org.electrocodeogram.cpc.core.api.data.ICloneFile, java.lang.String, java.lang.String)
	 */
	@Override
	protected void subMoveCloneFile(ICloneFile cloneFile, String project, String path)
	{
		if (log.isTraceEnabled())
			log.trace("subMoveCloneFile() - cloneFile: " + cloneFile + ", project: " + project + ", path: " + path);
		assert (cloneFile != null && project != null && path != null);

		SqlQueryUtils.updateCloneFileLocation(con, cloneFile.getUuid(), project, path);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.provider.AbstractStoreProvider#subPurgeCache()
	 */
	@Override
	protected void subPurgeCache()
	{
		log.debug("subPurgeCache() - NOT IMPLEMENTED");
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.provider.AbstractStoreProvider#subPurgeCloneFile(org.electrocodeogram.cpc.core.api.data.ICloneFile)
	 */
	@Override
	protected void subPurgeCloneFile(ICloneFile file)
	{
		if (log.isTraceEnabled())
			log.trace("subPurgeCloneFile() - file: " + file);
		assert (file != null);

		SqlQueryUtils.deleteCloneFile(con, file.getUuid());
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.provider.AbstractStoreProvider#subPurgeData()
	 */
	@Override
	protected void subPurgeData()
	{
		log.trace("subPurgeData()");

		try
		{
			OrmUtils.dropAndCreateTables(SqlType.HSQL, con);
		}
		catch (SQLException e)
		{
			log.fatal("subPurgeData(): table creation failed - " + e, e);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.provider.AbstractStoreProvider#subCheckDataIntegrity()
	 */
	@Override
	protected boolean subCheckDataIntegrity()
	{
		log.debug("subCheckDataIntegrity() - NOT IMPLEMENTED");
		//TODO implement some database integrity checking here
		return true;
	}

	/*
	 * IProvider Methods
	 */

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IProvider#getProviderName()
	 */
	@Override
	public String getProviderName()
	{
		log.trace("getProviderName()");

		return "CPC Store Local SQL: org.electrocodeogram.cpc.store.local.sql.provider.StoreProvider";
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IProvider#onLoad()
	 */
	@Override
	public void onLoad()
	{
		super.onLoad();

		log.trace("onLoad()");
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IProvider#onUnload()
	 */
	@Override
	public void onUnload()
	{
		log.trace("onUnload()");

		dbDisconect();

		super.onUnload();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		//TODO: add something more meaningful here
		return super.toString();
	}

	/*
	 * Further Methods
	 */

	/**
	 * Returns the internal jdbc connection of this store provider.<br/>
	 * This method shouldn't be used for anything but testing and debugging.
	 * 
	 * @deprecated for testing/debugging only
	 */
	@Deprecated
	public Connection getConnection()
	{
		return con;
	}

	/*
	 * Private Methods
	 */

	protected boolean dbConnect(String jdbcDriver, String jdbcUrl, String username, String password)
	{
		if (log.isTraceEnabled())
			log.trace("dbConnect() - jdbcDriver: " + jdbcDriver + ", jdbcUrl: " + jdbcUrl + ", username: " + username
					+ ", password: *hidden*");

		try
		{
			Class.forName(jdbcDriver);
		}
		catch (Exception e)
		{
			log.fatal("dbConnect() - failed to load JDBC driver: " + jdbcDriver + " - " + e, e);

			return false;
		}

		try
		{
			con = DriverManager.getConnection(jdbcUrl, username, password);
		}
		catch (SQLException e)
		{
			log.fatal("dbConnect() - failed to connect to db: " + jdbcUrl + " as " + username + " - " + e, e);

			return false;
		}

		//FIXME: what do we do if the connection fails? We should probably somehow deactivate
		//		 this proivder on the fly. We might also want to retry to open the connection
		//		 every now and then.

		return true;
	}

	protected void dbDisconect()
	{
		log.trace("dbDisconnect()");

		/*
		 * HSQLDB needs explicit shutdown command.
		 */
		if (hsqldbMode)
		{
			log.debug("dbDisconnect() - sending HSQLDB SHUTDOWN command.");
			try
			{
				con.createStatement().execute("SHUTDOWN");
			}
			catch (SQLException e)
			{
				log.error("dbDisconnect() - error while sending HSQLDB SHUTDOWN command - " + e, e);
			}
		}

		try
		{
			if (con != null)
				con.close();
		}
		catch (SQLException e)
		{
			log.warn("dbDisconnect() - exception while closing connection - " + e, e);
		}
	}

}
