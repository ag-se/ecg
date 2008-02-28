package org.electrocodeogram.cpc.store.local.sql.utils;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
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
import org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider;
import org.electrocodeogram.cpc.core.utils.CoreUtils;


public class SqlQueryUtils
{
	private static Log log = LogFactory.getLog(SqlQueryUtils.class);

	/**
	 * Keep a static reference to the clone factory provider, for performance reasons.
	 */
	private static ICloneFactoryProvider cloneFactoryProvider = (ICloneFactoryProvider) CPCCorePlugin
			.getProviderRegistry().lookupProvider(ICloneFactoryProvider.class);

	public static ICloneFile getCloneFileByPath(Connection con, String project, String path)
	{
		return getCloneFileByQuery(con, "project=? AND path=?", new Object[] { project, path });
	}

	public static ICloneFile getCloneFileByUuid(Connection con, String uuid)
	{
		return getCloneFileByQuery(con, ICloneFile.PERSISTENCE_OBJECT_IDENTIFIER + "=?", new Object[] { uuid });
	}

	private static ICloneFile getCloneFileByQuery(Connection con, String whereClause, Object[] values)
	{
		if (log.isTraceEnabled())
			log.trace("getCloneFileByField() - whereClause:" + whereClause + ", values: "
					+ CoreUtils.arrayToString(values));

		try
		{
			ResultSet rs = getResultSetByQuery(con, "SELECT * FROM "
					+ escapeSqlIdentifier(ICloneFile.PERSISTENCE_CLASS_IDENTIFIER) + " WHERE " + whereClause, values);

			List<ICloneFile> files = OrmUtils.ormCloneFiles(rs);

			if (!files.isEmpty())
			{
				if (log.isTraceEnabled())
					log.trace("getCloneFileByField() - result: " + files.get(0));
				return files.get(0);
			}
			else
			{
				log.trace("getCloneFileByField() - result: NOT FOUND");
				return null;
			}
		}
		catch (SQLException e)
		{
			log.error("getCloneFileByField(): SQL Exception for whereClause: " + whereClause + ", values: "
					+ CoreUtils.arrayToString(values) + " - " + e, e);
			return null;
		}
	}

	private static ResultSet getResultSetByQuery(Connection con, String sql, Object[] values) throws SQLException
	{
		PreparedStatement stmt = con.prepareStatement(sql);

		int i = 1;
		for (Object value : values)
		{
			if (value == null)
			{
				log.error("getResultSetByQuery() - sql parameter value is NULL for position: " + i + " in sql: " + sql,
						new Throwable());
				throw new SQLException("Illegal sql parameter value, value is NULL - query: " + sql);
			}

			else if (value instanceof String)
				stmt.setString(i, (String) value);
			else if (value instanceof Integer)
				stmt.setInt(i, (Integer) value);
			else if (value instanceof Long)
				stmt.setLong(i, (Long) value);
			else if (value instanceof Boolean)
				stmt.setBoolean(i, (Boolean) value);
			else if (value instanceof Date)
				stmt.setDate(i, new java.sql.Date(((Date) value).getTime()));
			else
			{
				log.error("getResultSetByQuery() - unsupported data type: " + value + " for position: " + i
						+ " in sql: " + sql, new Throwable());
				throw new SQLException("Illegal sql parameter value, unknown type - value: " + value
						+ ", value class: " + value.getClass() + ", query: " + sql);
			}
			i++;
		}

		ResultSet rs = stmt.executeQuery();

		return rs;
	}

	public static IClone getCloneByUuid(Connection con, String uuid)
	{
		List<IClone> clones = getClonesByField(con, IClone.PERSISTENCE_OBJECT_IDENTIFIER, uuid);

		if (clones.isEmpty())
			return null;
		else
			return clones.get(0);
	}

	public static List<IClone> getClonesByFileUuid(Connection con, String fileUuid)
	{
		return getClonesByField(con, "fileUuid", fileUuid);
	}

	public static List<IClone> getClonesByGroupUuid(Connection con, String groupUuid)
	{
		return getClonesByField(con, "groupUuid", groupUuid);
	}

	private static List<IClone> getClonesByField(Connection con, String field, String value)
	{
		if (log.isTraceEnabled())
			log.trace("getCloneByField() - field:" + field + ", value: " + value);

		try
		{
			PreparedStatement stmt = con.prepareStatement("SELECT * FROM "
					+ escapeSqlIdentifier(IClone.PERSISTENCE_CLASS_IDENTIFIER) + " WHERE " + field + "=?");

			stmt.setString(1, value);

			ResultSet rs = stmt.executeQuery();

			List<IClone> clones = OrmUtils.ormClones(rs);
			loadCloneObjectExtensions(con, IClone.class, clones);

			if (log.isTraceEnabled())
				log.trace("getClonesByField() - result: " + clones);

			return clones;
		}
		catch (SQLException e)
		{
			log.error("getCloneByField(): SQL Exception for field: " + field + ", value: " + value + " - " + e, e);
			return null;
		}
	}

	public static ICloneGroup getCloneGroupByUuid(Connection con, String uuid)
	{
		return getCloneGroupByField(con, ICloneGroup.PERSISTENCE_OBJECT_IDENTIFIER, uuid);
	}

	private static ICloneGroup getCloneGroupByField(Connection con, String field, String value)
	{
		if (log.isTraceEnabled())
			log.trace("getCloneGroupByField() - field: " + field + ", value: " + value);

		try
		{
			PreparedStatement stmt = con.prepareStatement("SELECT * FROM "
					+ escapeSqlIdentifier(ICloneGroup.PERSISTENCE_CLASS_IDENTIFIER) + " WHERE " + field + "=?");

			stmt.setString(1, value);

			ResultSet rs = stmt.executeQuery();
			List<ICloneGroup> groups = OrmUtils.ormCloneGroups(rs);
			loadCloneObjectExtensions(con, ICloneGroup.class, groups);

			if (!groups.isEmpty())
			{
				if (log.isTraceEnabled())
					log.trace("getCloneGroupByField() - result: " + groups.get(0));
				return groups.get(0);
			}
			else
			{
				log.trace("getCloneGroupByField() - result: NOT FOUND");
				return null;
			}
		}
		catch (SQLException e)
		{
			log.error("getCloneGroupByField(): SQL Exception for field: " + field + ", value: " + value + " - " + e, e);
			return null;
		}
	}

	public static IStatefulObject getStatefulObjectByUuid(Connection con, IStatefulObject statefulObject, String uuid)
	{
		return getStatefulObjectByField(con, statefulObject, statefulObject.getPersistenceObjectIdentifier(), uuid);
	}

	@SuppressWarnings("unchecked")
	private static IStatefulObject getStatefulObjectByField(Connection con, IStatefulObject statefulObject,
			String field, String value)
	{
		if (log.isTraceEnabled())
			log.trace("getStatefulObjectByField() - field: " + field + ", value: " + value);

		try
		{
			PreparedStatement stmt = con.prepareStatement("SELECT * FROM "
					+ escapeSqlIdentifier(statefulObject.getPersistenceClassIdentifier()) + " WHERE " + field + "=?");

			stmt.setString(1, value);

			ResultSet rs = stmt.executeQuery();
			List<IStatefulObject> statefulObjects = OrmUtils.ormStatefulObjects(statefulObject.getClass(), rs);
			if (statefulObject instanceof ICloneObject)
				loadCloneObjectExtensions(con, ((ICloneObject) statefulObject).getClass(), (List) statefulObjects);

			if (!statefulObjects.isEmpty())
			{
				if (log.isTraceEnabled())
					log.trace("getStatefulObjectByField() - result: " + statefulObjects.get(0));
				return statefulObjects.get(0);
			}
			else
			{
				log.trace("getStatefulObjectByField() - result: NOT FOUND");
				return null;
			}
		}
		catch (SQLException e)
		{
			log.error(
					"getStatefulObjectByField(): SQL Exception for field: " + field + ", value: " + value + " - " + e,
					e);
			return null;
		}
	}

	/**
	 * Checks if there are any persisted {@link ICloneObjectExtension}s for the given list of
	 * {@link ICloneObject}s. If any extensions are found, they are automatically loaded and
	 * added to the corresponding {@link ICloneObject}s.
	 * 
	 * @param con
	 * @param implClass
	 * @param cloneObjects
	 * @throws SQLException 
	 */
	private static void loadCloneObjectExtensions(Connection con, Class<? extends ICloneObject> implClass,
			List<? extends ICloneObject> cloneObjects) throws SQLException
	{
		if (log.isTraceEnabled())
			log.trace("loadCloneObjectExtensions() - implClass: " + implClass + ", cloneObjects: " + cloneObjects);

		//make sure there are actually some clone objects to process
		if (cloneObjects.isEmpty())
		{
			log.trace("loadCloneObjectExtensions() - no clone objects, returning.");
			return;
		}

		//get a list of all extensions which apply to the given clone object type
		List<ICloneObjectExtension> extensions = cloneFactoryProvider
				.getRegisteredCloneObjectExtensionObjects(implClass);

		//make sure we have at least one potential extension
		if (extensions.isEmpty())
		{
			log.trace("loadCloneObjectExtensions() - no extensions for this clone object type, returning.");
			return;
		}

		//we know that the list only contains ICloneObject implementations
		for (ICloneObject cloneObject : cloneObjects)
		{
			if (log.isTraceEnabled())
				log.trace("loadCloneObjectExtensions() - checking clone object: " + cloneObject);

			//check if this clone object has the extension flag set, otherwise skip it (performance improvement)
			if (!cloneObject.hasExtensions())
			{
				log.trace("loadCloneObjectExtensions() - hasExtensions() is false, skipping.");
				continue;
			}

			//check for any potential clone object extensions which need to be loaded
			for (ICloneObjectExtension extension : extensions)
			{
				if (!(extension instanceof ICloneObjectExtensionStatefulObject))
				{
					//this isn't a stateful extension
					if (log.isTraceEnabled())
						log.trace("loadCloneObjectExtensions() - ignoring non-stateful extension: " + extension);
					continue;
				}

				//this extension is registered for our current implClass, do a lookup and
				//check if it refers to our UUID somewhere
				loadCloneObjectExtension(con, cloneObject, (ICloneObjectExtensionStatefulObject) extension);
			}

			//make sure the clone object is still marked as non-dirty
			((IStoreCloneObject) cloneObject).setDirty(false);
		}
	}

	/**
	 * Takes a clone object and an extension and object and restores the persisted extension data.
	 * 
	 * @param con
	 * @param cloneObject
	 * @param statefulExt
	 * @throws SQLException
	 */
	private static void loadCloneObjectExtension(Connection con, ICloneObject cloneObject,
			ICloneObjectExtensionStatefulObject statefulExt) throws SQLException
	{
		if (log.isTraceEnabled())
			log.trace("loadCloneObjectExtension() - checking for presence of extension of type: " + statefulExt);

		//now try to find any entries for this extension which refer to the particular clone object uuid
		PreparedStatement stmt = con.prepareStatement("SELECT * FROM "
				+ escapeSqlIdentifier(statefulExt.getPersistenceClassIdentifier()) + " WHERE "
				+ statefulExt.getPersistenceObjectIdentifier() + "=?");

		stmt.setString(1, cloneObject.getUuid());

		ResultSet rs = stmt.executeQuery();
		List<IStatefulObject> statefulObjects = OrmUtils.ormStatefulObjects(statefulExt.getExtensionInterfaceClass(),
				rs);
		stmt.close();

		if (log.isTraceEnabled())
			log.trace("loadCloneObjectExtension() - extension result: " + statefulObjects);

		//there should now be 0 or 1 results in the list
		if (statefulObjects.isEmpty())
		{
			//no extension object (in that case there can't be any multi stateful object data either)
			log.trace("loadCloneObjectExtension() - no extension instances found in db.");
			return;
		}
		assert (statefulObjects.size() == 1);

		//get the extension which we just loaded
		ICloneObjectExtensionStatefulObject loadedExtension = (ICloneObjectExtensionStatefulObject) statefulObjects
				.get(0);

		//add it to the clone object
		cloneObject.addExtension(loadedExtension);

		//now check for any sub-elements
		loadCloneObjectExtensionSubElements(con, cloneObject, loadedExtension, false);
	}

	/**
	 * Takes an {@link ICloneObject} and a {@link ICloneObjectExtensionStatefulObject} and checks whether
	 * it is of type {@link ICloneObjectExtensionMultiStatefulObject}. If it is, any available sub-element
	 * data is loaded into the given extension object.<br/>
	 * The extension object is <b>modified in place</b>. The clone object is not modified.<br/>
	 * <br/>
	 * Sub-elements of {@link ICloneObjectExtensionLazyMultiStatefulObject} extensions are only loaded if the
	 * <em>nonLazy</em> parameter is set to <em>true</em>. 
	 * 
	 * @param cloneObject the clone object for this extension, not modified, never null.
	 * @param loadedExtension the extension to load sub-elements for, modified in place, never null.
	 * @param nonLazy true if all sub-elements should be loaded for lazy multi stateful extensions.
	 */
	public static void loadCloneObjectExtensionSubElements(Connection con, ICloneObject cloneObject,
			ICloneObjectExtensionStatefulObject loadedExtension, boolean nonLazy) throws SQLException
	{
		if (log.isTraceEnabled())
			log.trace("loadCloneObjectExtensionSubElements() - cloneObject: " + cloneObject + ", loadedExtension: "
					+ loadedExtension + ", nonLazy: " + nonLazy);

		if (!(loadedExtension instanceof ICloneObjectExtensionMultiStatefulObject))
			//this isn't also a multi stateful extension, we're done
			return;

		//we will never restore entries for lazy extensions
		if (!nonLazy && (loadedExtension instanceof ICloneObjectExtensionLazyMultiStatefulObject))
		{
			log
					.trace("loadCloneObjectExtensionSubElements() - ignoring sub-elements for lazy multi stateful extension.");

			//set extension to partial
			((ICloneObjectExtensionLazyMultiStatefulObject) loadedExtension).setPartial(true);

			return;
		}

		log
				.trace("loadCloneObjectExtensionSubElements() - extension is a multi stateful extension, checking for sub-elements.");

		/*
		 * Now restore the sub-element data.
		 */

		ICloneObjectExtensionMultiStatefulObject multiStatefulExt = (ICloneObjectExtensionMultiStatefulObject) loadedExtension;

		//get the required data from the extension
		List<String> multiClassIdentifier = multiStatefulExt.getMultiPersistenceClassIdentifier();
		List<Map<String, Class<? extends Object>>> multiStateTypes = multiStatefulExt.getMultiStateTypes();
		List<List<Map<String, Comparable<? extends Object>>>> resultStateMaps = new ArrayList<List<Map<String, Comparable<? extends Object>>>>(
				multiClassIdentifier.size());

		for (int i = 0; i < multiClassIdentifier.size(); ++i)
		{
			//get the name of the table
			String combinedClassIdentifier = OrmUtils.getMultiStatefulObjectPersistenceClassIdentifier(loadedExtension
					.getPersistenceClassIdentifier(), multiClassIdentifier.get(i));

			if (log.isTraceEnabled())
				log.trace("loadCloneObjectExtensionSubElements() - checking for sub-element class: "
						+ multiClassIdentifier.get(0) + ", table: " + combinedClassIdentifier);

			//get any sub-element data from the db
			PreparedStatement stmt = con.prepareStatement("SELECT * FROM "
					+ escapeSqlIdentifier(combinedClassIdentifier) + " WHERE "
					+ loadedExtension.getPersistenceObjectIdentifier() + "=?");

			stmt.setString(1, cloneObject.getUuid());

			ResultSet rs = stmt.executeQuery();
			List<Map<String, Comparable<? extends Object>>> stateMaps = OrmUtils.ormStateMaps(multiStateTypes.get(i),
					rs);
			stmt.close();

			if (log.isTraceEnabled())
				log.trace("loadCloneObjectExtensionSubElements() - sub-element result: " + stateMaps);

			resultStateMaps.add(stateMaps);
		}

		//now we need to add the sub-element data to the extension
		log.trace("loadCloneObjectExtensionSubElements() - setting sub-element state for extension.");
		multiStatefulExt.setMultiState(resultStateMaps);

		//if this is a lazy extension, mark it as being non-partial
		if (loadedExtension instanceof ICloneObjectExtensionLazyMultiStatefulObject)
			((ICloneObjectExtensionLazyMultiStatefulObject) multiStatefulExt).setPartial(false);
	}

	/**
	 * Deletes the given clone file and all clones which might be added for it.
	 * 
	 * @param fileUuid file uuid of the file which should be deleted, never null 
	 */
	public static void deleteCloneFile(Connection con, String fileUuid)
	{
		if (log.isTraceEnabled())
			log.trace("deleteCloneFile() - fileUuid: " + fileUuid);

		try
		{
			PreparedStatement stmt = con.prepareStatement("DELETE FROM "
					+ escapeSqlIdentifier(IClone.PERSISTENCE_CLASS_IDENTIFIER) + " WHERE fileUuid=?");
			stmt.setString(1, fileUuid);
			stmt.execute();

			stmt = con.prepareStatement("DELETE FROM " + escapeSqlIdentifier(ICloneFile.PERSISTENCE_CLASS_IDENTIFIER)
					+ " WHERE " + escapeSqlIdentifier(ICloneFile.PERSISTENCE_OBJECT_IDENTIFIER) + "=?");
			stmt.setString(1, fileUuid);
			stmt.execute();
		}
		catch (SQLException e)
		{
			log.error("deleteCloneFile(): SQL Exception for fileUuid: " + fileUuid + " - " + e, e);
		}
	}

	/**
	 * Deletes all clones for the given file which are not listed in the given set.
	 * 
	 * @param fileUuid file uuid for which all clones should be deleted, never null 
	 * @param exceptedClones list of clones which shall not be deleted
	 */
	public static void deleteAllClonesForFileExcept(Connection con, String fileUuid, Set<IClone> exceptedClones)
	{
		if (log.isTraceEnabled())
			log.trace("deleteAllClonesForFileExcept() - fileUuid: " + fileUuid + ", exceptedClones: " + exceptedClones);

		try
		{
			StringBuilder uuidStr = new StringBuilder();
			boolean tmpFirst = true;
			for (IClone clone : exceptedClones)
			{
				if (!tmpFirst)
					uuidStr.append(",");
				else
					tmpFirst = false;

				uuidStr.append("'");
				uuidStr.append(clone.getUuid());
				uuidStr.append("'");
			}

			if (log.isTraceEnabled())
				log.trace("deleteAllClonesForFileExcept() - uuidStr: " + uuidStr.toString());

			PreparedStatement stmt = con.prepareStatement("DELETE FROM "
					+ escapeSqlIdentifier(IClone.PERSISTENCE_CLASS_IDENTIFIER)
					+ " WHERE fileUuid=?"
					+ (!exceptedClones.isEmpty() ? " AND " + IClone.PERSISTENCE_OBJECT_IDENTIFIER + " NOT IN ("
							+ uuidStr.toString() + ")" : ""));
			stmt.setString(1, fileUuid);
			stmt.execute();
		}
		catch (SQLException e)
		{
			log.error("deleteAllClonesForFileExcept(): SQL Exception for fileUuid: " + fileUuid + ", exceptedClones: "
					+ exceptedClones + " - " + e, e);
		}
	}

	/**
	 * Updates the project and path for the given clone file entry.
	 */
	public static void updateCloneFileLocation(Connection con, String fileUuid, String project, String path)
	{
		if (log.isTraceEnabled())
			log
					.trace("updateCloneFileLocation() - fileUuid: " + fileUuid + ", project: " + project + ", path: "
							+ path);
		assert (con != null && fileUuid != null && project != null && path != null);

		try
		{
			/*
			 * We're making use of the IStatefulObject API specification here.
			 * It states that the keys used (and thus the table column names) must match the filed names.
			 * As such it is guaranteed by the API that the columns "project" and "path" can be used in
			 * this manner.
			 * If this fails, it is a violation of the API by the ICloneFile implementation.
			 */
			PreparedStatement stmt = con.prepareStatement("UPDATE "
					+ escapeSqlIdentifier(ICloneFile.PERSISTENCE_CLASS_IDENTIFIER) + " SET project=?, path=? WHERE "
					+ escapeSqlIdentifier(ICloneFile.PERSISTENCE_OBJECT_IDENTIFIER) + "=?");
			stmt.setString(1, project);
			stmt.setString(2, path);
			stmt.setString(3, fileUuid);
			stmt.execute();
		}
		catch (SQLException e)
		{
			log.error("updateCloneFileLocation(): SQL Exception for fileUuid: " + fileUuid + ", project: " + project
					+ ", path: " + path + " - " + e, e);
		}
	}

	/**
	 * Takes an identifier and escapes any SQL keywords or illegal characters.
	 * The result can safely be used as a table or column name.
	 * 
	 * @param identifier identifier to escape, never null.
	 * @return safe identifier, never null.
	 */
	public static String escapeSqlIdentifier(String identifier)
	{
		assert (identifier != null);

		//TODO: we need to do some escaping of SQL keywords here
		identifier = identifier.replace(".", "___");

		return identifier;
	}

	/**
	 * Undoes any escaping done by <em>escapeSqlIdentifier()</em>.
	 */
	public static String unescapeSqlIdentifier(String identifier)
	{
		assert (identifier != null);

		//TODO: we need to do undo any escaping of SQL keywords here
		identifier = identifier.replace("___", ".");

		return identifier;
	}

}
