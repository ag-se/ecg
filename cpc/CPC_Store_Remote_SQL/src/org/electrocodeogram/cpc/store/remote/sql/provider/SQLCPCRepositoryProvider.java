package org.electrocodeogram.cpc.store.remote.sql.provider;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.provider.IManagableProvider;
import org.electrocodeogram.cpc.core.api.provider.cpcrepository.CPCRepositoryException;
import org.electrocodeogram.cpc.core.api.provider.cpcrepository.ICPCRepositoryProvider;
import org.electrocodeogram.cpc.core.api.provider.cpcrepository.ICPCRevision;
import org.electrocodeogram.cpc.core.api.provider.xml.IMappingProvider;
import org.electrocodeogram.cpc.core.api.provider.xml.IMappingRegistry;
import org.electrocodeogram.cpc.core.api.provider.xml.MappingException;
import org.electrocodeogram.cpc.core.api.provider.xml.MappingStore;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;
import org.electrocodeogram.cpc.core.utils.CoreUtils;


/**
 * A <b>proof of concept</b> {@link ICPCRepositoryProvider} which stores all cpc revision data
 * in a central SQL database.<br/>
 * <br/>
 * <b>NOTE:</b> this provider is <u>not suitable for large projects</u>. The storage mechanisms are
 * 		very simple and inefficient!
 * 
 * @author vw
 */
public class SQLCPCRepositoryProvider implements ICPCRepositoryProvider, IManagableProvider
{
	private static final Log log = LogFactory.getLog(SQLCPCRepositoryProvider.class);

	/**
	 * Our JDBC connection, it is kept alive over the entire lifetime of the provider.
	 */
	protected Connection con = null;
	protected boolean inTransaction = false;

	protected IMappingRegistry mappingRegistry = null;
	protected IMappingProvider mappingProvider = null;

	public SQLCPCRepositoryProvider()
	{
		log.trace("SQLCPCRepositoryProvider()");
	}

	/*
	 * ICPCRepositoryProvider
	 */

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.cpcrepository.ICPCRepositoryProvider#createRevision()
	 */
	@Override
	public ICPCRevision createRevision()
	{
		log.trace("createRevision()");

		return new CPCRevision();
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.cpcrepository.ICPCRepositoryProvider#isAvailable()
	 */
	@Override
	public synchronized boolean isAvailable()
	{
		log.trace("isAvailable()");

		try
		{
			startDbBlock();
		}
		catch (CPCRepositoryException e1)
		{
			return false;
		}

		try
		{
			if (con == null || !con.isValid(2000))
				return false;
		}
		catch (SQLException e)
		{
			log.error("isAvailable() - sql error while checking connection validity - " + e, e);
			return false;
		}
		finally
		{
			endDbBlock();
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.cpcrepository.ICPCRepositoryProvider#putRevision(org.electrocodeogram.cpc.core.api.provider.cpcrepository.ICPCRevision)
	 */
	@Override
	public synchronized void putRevision(ICPCRevision cpcRevision) throws CPCRepositoryException
	{
		if (log.isTraceEnabled())
			log.trace("putRevision() - cpcRevision: " + cpcRevision);
		assert (cpcRevision != null);

		if (!cpcRevision.isValid())
			throw new CPCRepositoryException("The cpc revision object is not valid - cpcRevision: " + cpcRevision);

		startDbBlock();

		try
		{
			boolean success = _putRevision(cpcRevision);
			if (!success)
			{
				log.error("putRevision() - error, failed to persist cpc revision - cpcRevision: " + cpcRevision);
				throw new CPCRepositoryException("Error - failed to persist cpc revision");
			}
		}
		catch (SQLException e)
		{
			log.error("putRevision() - sql error, failed to persist cpc revision - cpcRevision: " + cpcRevision + " - "
					+ e, e);
			throw new CPCRepositoryException("SQL Error - failed to persist cpc revision - " + e, e);
		}
		catch (MappingException e)
		{
			log.error("putRevision() - mapping error, failed to persist cpc revision - cpcRevision: " + cpcRevision
					+ " - " + e, e);
			throw new CPCRepositoryException("Mapping Error - failed to persist cpc revision - " + e, e);
		}
		finally
		{
			endDbBlock();
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.cpcrepository.ICPCRepositoryProvider#getRevision(java.lang.String, java.lang.String)
	 */
	@Override
	public synchronized ICPCRevision getRevision(String revisionId, String cloneFileUuid) throws CPCRepositoryException
	{
		if (log.isTraceEnabled())
			log.trace("getRevision() - revisionId: " + revisionId + ", cloneFileUuid: " + cloneFileUuid);
		assert (revisionId != null && cloneFileUuid != null);

		startDbBlock();

		try
		{
			CPCRevision result = _getRevision(revisionId, cloneFileUuid);

			if (log.isTraceEnabled())
				log.trace("getRevision() - result: " + result);

			return result;
		}
		catch (SQLException e)
		{
			log.error("getRevision() - sql error, failed to retrieve cpc revision - revisionId: " + revisionId
					+ ", cloneFileUuid: " + cloneFileUuid + " - " + e, e);
			throw new CPCRepositoryException("SQL Error - failed to retrieve cpc revision - " + e, e);
		}
		catch (MappingException e)
		{
			log.error("getRevision() - mapping error, failed to retrieve cpc revision - revisionId: " + revisionId
					+ ", cloneFileUuid: " + cloneFileUuid + " - " + e, e);
			throw new CPCRepositoryException("Mapping Error - failed to retrieve cpc revision - " + e, e);
		}
		finally
		{
			endDbBlock();
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.cpcrepository.ICPCRepositoryProvider#getRevision(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public synchronized ICPCRevision getRevision(String revisionId, String project, String filePath)
			throws CPCRepositoryException
	{
		if (log.isTraceEnabled())
			log
					.trace("getRevision() - revisionId: " + revisionId + ", project: " + project + ", filePath: "
							+ filePath);
		assert (revisionId != null && project != null && filePath != null);

		startDbBlock();

		try
		{
			CPCRevision result = _getRevision(revisionId, project, filePath);

			if (log.isTraceEnabled())
				log.trace("getRevision() - result: " + result);

			return result;
		}
		catch (SQLException e)
		{
			log.error("getRevision() - sql error, failed to retrieve cpc revision - revisionId: " + revisionId
					+ ", project: " + project + ", filePath: " + filePath + " - " + e, e);
			throw new CPCRepositoryException("SQL Error - failed to retrieve cpc revision - " + e, e);
		}
		catch (MappingException e)
		{
			log.error("getRevision() - mapping error, failed to retrieve cpc revision - revisionId: " + revisionId
					+ ", project: " + project + ", filePath: " + filePath + " - " + e, e);
			throw new CPCRepositoryException("Mapping Error - failed to retrieve cpc revision - " + e, e);
		}
		finally
		{
			endDbBlock();
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.cpcrepository.ICPCRepositoryProvider#purgeRevision(java.lang.String, java.lang.String)
	 */
	@Override
	public synchronized boolean purgeRevision(String revisionId, String cloneFileUuid) throws CPCRepositoryException
	{
		if (log.isTraceEnabled())
			log.trace("purgeRevision() - revisionId: " + revisionId + ", cloneFileUuid: " + cloneFileUuid);
		assert (revisionId != null && cloneFileUuid != null);

		startDbBlock();

		try
		{
			boolean success = _purgeRevision(revisionId, cloneFileUuid);

			if (log.isTraceEnabled())
				log.trace("purgeRevision() - success: " + success);

			return success;
		}
		catch (SQLException e)
		{
			log.error("getRevision() - sql error, failed to purge cpc revision - revisionId: " + revisionId
					+ ", cloneFileUuid: " + cloneFileUuid + " - " + e, e);
			throw new CPCRepositoryException("SQL Error - failed to purge cpc revision - " + e, e);
		}
		finally
		{
			endDbBlock();
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.cpcrepository.ICPCRepositoryProvider#purgeRevision(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public synchronized boolean purgeRevision(String revisionId, String project, String filePath)
			throws CPCRepositoryException
	{
		if (log.isTraceEnabled())
			log.trace("purgeRevision() - revisionId: " + revisionId + ", project: " + project + ", filePath: "
					+ filePath);
		assert (revisionId != null && project != null && filePath != null);

		startDbBlock();

		try
		{
			boolean success = _purgeRevision(revisionId, project, filePath);

			if (log.isTraceEnabled())
				log.trace("purgeRevision() - success: " + success);

			return success;
		}
		catch (SQLException e)
		{
			log.error("getRevision() - sql error, failed to purge cpc revision - revisionId: " + revisionId
					+ ", project: " + project + ", filePath: " + filePath + " - " + e, e);
			throw new CPCRepositoryException("SQL Error - failed to purge cpc revision - " + e, e);
		}
		finally
		{
			endDbBlock();
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.cpcrepository.ICPCRepositoryProvider#hintStartTransaction()
	 */
	@Override
	public synchronized void hintStartTransaction() throws CPCRepositoryException
	{
		log.trace("hintStartTransaction()");

		if (inTransaction)
			throw new IllegalStateException("transactions may not be nested");

		if (!dbConnect())
			throw new CPCRepositoryException("unable to connect to remote server");

		inTransaction = true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.cpcrepository.ICPCRepositoryProvider#hintEndTransaction()
	 */
	@Override
	public synchronized void hintEndTransaction()
	{
		log.trace("hintEndTransaction()");

		if (!inTransaction)
			throw new IllegalStateException("no transaction in progress");

		dbDisconect();

		inTransaction = false;
	}

	/*
	 * IProvider
	 */

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IProvider#getProviderName()
	 */
	@Override
	public String getProviderName()
	{
		return "SQL CPC Repository Provider (Proof of Concept)";
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IProvider#onLoad()
	 */
	@Override
	public void onLoad()
	{
		log.trace("onLoad()");

		mappingRegistry = (IMappingRegistry) CPCCorePlugin.getProviderRegistry().lookupProvider(IMappingRegistry.class);
		assert (mappingRegistry != null);

		mappingProvider = (IMappingProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(IMappingProvider.class);
		assert (mappingProvider != null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IProvider#onUnload()
	 */
	@Override
	public void onUnload()
	{
		log.trace("onUnload()");
	}

	/*
	 * Private methods.
	 */

	/**
	 * Ensure that a database connection is available.
	 * 
	 * @throws CPCRepositoryException if db connection can't be established. 
	 */
	protected void startDbBlock() throws CPCRepositoryException
	{
		if (inTransaction)
			//within a "transaction", nothing to do
			return;

		log.trace("startDbBlock() - connecting to db.");

		if (!dbConnect())
			throw new CPCRepositoryException("unable to connect to remote server");
	}

	/**
	 * Shutdown any database connection which was started up by us.
	 */
	protected void endDbBlock()
	{
		if (inTransaction)
			//within a "transaction", nothing to do
			return;

		log.trace("startDbBlock() - disconnecting from db.");

		dbDisconect();
	}

	/**
	 * Creates a connection to the default database.
	 * 
	 * @return true on success, false otherwise.
	 */
	protected boolean dbConnect()
	{
		//TODO: get the SQL server data from a preference page here
		return dbConnect("org.postgresql.Driver", "jdbc:postgresql://10.0.0.2/cpcrepodb", "cpcrepo", "bla");
	}

	/**
	 * Creates a connection to the specified database.
	 * 
	 * @param jdbcDriver
	 * @param jdbcUrl
	 * @param username
	 * @param password
	 * @return true on success, false otherwise.
	 */
	protected boolean dbConnect(String jdbcDriver, String jdbcUrl, String username, String password)
	{
		log.trace("dbConnect()");

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
			con.setAutoCommit(true);
		}
		catch (SQLException e)
		{
			log.fatal("dbConnect() - failed to connect to db: " + jdbcUrl + " as " + username + " - " + e, e);

			return false;
		}

		//FIXME: what do we do if the connection fails? We should probably somehow deactivate
		//		 this proivder on the fly. We might also want to retry to open the connection
		//		 every now and then.

		try
		{
			_createsTablesIfMissing();
		}
		catch (Exception e)
		{
			log.fatal("dbConnect(): table creation failed - " + e, e);
		}

		return true;
	}

	protected void dbDisconect()
	{
		log.trace("dbDisconect()");

		try
		{
			if (con != null)
				con.close();

			con = null;
		}
		catch (SQLException e)
		{
		}
	}

	/**
	 * Checks whether the tables needed by this provider exist and recreates them
	 * if they are missing.
	 */
	protected void _createsTablesIfMissing() throws SQLException
	{
		log.trace("createsTablesIfMissing()");

		try
		{
			con.createStatement().execute(
					"SELECT * FROM revision WHERE revid='doesnotexist' AND file_uuid='doesnotexist'");
			log.trace("createsTablesIfMissing() - tables are present");
		}
		catch (SQLException e)
		{
			//ok, tables are probably missing
			log.warn("createsTablesIfMissing() - CPC tables are missing, recreating... (" + e + ")");

			_dropAndCreateTables();
		}
	}

	/**
	 * Drops and recreates all tables and indices needed by the sql cpc repository provider.<br/>
	 * <b>All data is lost.</b> 
	 */
	protected void _dropAndCreateTables() throws SQLException
	{
		log.trace("dropAndcreateTables()");

		con.setAutoCommit(false);

		//drop all tables if they exist
		con.createStatement().execute("DROP TABLE IF EXISTS revision CASCADE");

		//revision data table
		con.createStatement().execute("CREATE TABLE revision (" //
				+ "revid					VARCHAR		NOT NULL," //
				+ "file_uuid				VARCHAR		NOT NULL," //
				+ "project					VARCHAR		NOT NULL," //
				+ "path						VARCHAR		NOT NULL," //
				+ "cpcdata					TEXT		NOT NULL," //

				//mainly for debugging purposes
				+ "date						TIMESTAMP	NOT NULL DEFAULT CURRENT_TIMESTAMP," //
				+ "username					VARCHAR," //

				//primary key
				+ "CONSTRAINT revision_pk PRIMARY KEY (file_uuid, revid)" + ")");

		//create some search indices
		con.createStatement().execute("CREATE INDEX idx_revision_project_path ON revision(project, path)");

		con.commit();
		con.setAutoCommit(true);
	}

	protected boolean _putRevision(ICPCRevision cpcRevision) throws SQLException, MappingException
	{
		assert (cpcRevision != null);

		//map cpc data to string
		assert (mappingProvider != null);
		MappingStore mappingStore = new MappingStore(cpcRevision.getCloneFile(), cpcRevision.getClones());
		String cpcData = mappingProvider.mapToString(mappingStore, true);

		PreparedStatement stmt = con
				.prepareStatement("INSERT INTO revision (revid,file_uuid,project,path,cpcdata,username) VALUES (?,?,?,?,?,?)");
		stmt.setString(1, cpcRevision.getRevisionId());
		stmt.setString(2, cpcRevision.getCloneFile().getUuid());
		stmt.setString(3, cpcRevision.getCloneFile().getProject());
		stmt.setString(4, cpcRevision.getCloneFile().getPath());
		stmt.setString(5, cpcData);
		stmt.setString(6, CoreUtils.getUsername());

		//might trigger an SQL exception if the revid+file_uuid are already in use
		int affectedRows = stmt.executeUpdate();

		return affectedRows > 0; //should always be true
	}

	protected CPCRevision _getRevision(String revisionId, String fileUuid) throws SQLException, MappingException
	{
		assert (revisionId != null && fileUuid != null);

		PreparedStatement stmt = con.prepareStatement("SELECT * FROM revision WHERE revid=? AND file_uuid=?");
		stmt.setString(1, revisionId);
		stmt.setString(2, fileUuid);

		ResultSet rs = stmt.executeQuery();

		return _ormRevision(rs);
	}

	protected CPCRevision _getRevision(String revisionId, String project, String path) throws SQLException,
			MappingException
	{
		assert (con != null && revisionId != null && project != null && path != null);

		PreparedStatement stmt = con.prepareStatement("SELECT * FROM revision WHERE revid=? AND project=? AND path=?");
		stmt.setString(1, revisionId);
		stmt.setString(2, project);
		stmt.setString(3, path);

		ResultSet rs = stmt.executeQuery();

		return _ormRevision(rs);
	}

	protected boolean _purgeRevision(String revisionId, String fileUuid) throws SQLException
	{
		assert (revisionId != null && fileUuid != null);

		PreparedStatement stmt = con.prepareStatement("DELETE FROM revision WHERE revid=? AND file_uuid=?");
		stmt.setString(1, revisionId);
		stmt.setString(2, fileUuid);

		int affectedRows = stmt.executeUpdate();

		return affectedRows > 0;
	}

	protected boolean _purgeRevision(String revisionId, String project, String path) throws SQLException
	{
		assert (revisionId != null && project != null && path != null);

		PreparedStatement stmt = con.prepareStatement("DELETE FROM revision WHERE revid=? AND project=? AND path=?");
		stmt.setString(1, revisionId);
		stmt.setString(2, project);
		stmt.setString(3, path);

		int affectedRows = stmt.executeUpdate();

		return affectedRows > 0;
	}

	protected CPCRevision _ormRevision(ResultSet rs) throws SQLException, MappingException
	{
		//make sure that the result set contains a result row
		if (rs == null || !rs.next())
		{
			//lookup failed, no such entry
			log.trace("ormRevision() - result set contained no data, returning NULL - rs: " + rs);
			return null;
		}

		//get the interesting fields
		String revisionId = rs.getString("revid");
		String cpcData = rs.getString("cpcdata");

		if (revisionId == null || cpcData == null)
		{
			log.error("ormRevision() - get data from sql db - revisionId: " + revisionId + ", cpcData: "
					+ CoreStringUtils.truncateString(cpcData), new Throwable());
			return null;
		}

		//ok, now map the xml data back to cpc data objects
		assert (mappingRegistry != null);
		MappingStore mappingStore = mappingRegistry.mapFromString(cpcData);
		if (mappingStore == null || mappingStore.getCloneFile() == null || mappingStore.getClones() == null)
		{
			log.error("ormRevision() - failed to parse cpc data", new Throwable());
			log.info("ormRevision() - CPCDATA: " + CoreStringUtils.quoteString(cpcData));
			return null;
		}

		CPCRevision result = new CPCRevision();

		result.setRevisionId(revisionId);
		result.setCloneFile(mappingStore.getCloneFile());
		result.setClones(mappingStore.getClones());

		if (log.isTraceEnabled())
			log.trace("ormRevision() - result: " + result);

		return result;

	}

}
