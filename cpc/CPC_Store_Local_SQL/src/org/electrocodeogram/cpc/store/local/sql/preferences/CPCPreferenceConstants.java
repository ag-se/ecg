package org.electrocodeogram.cpc.store.local.sql.preferences;


public class CPCPreferenceConstants
{
	/**
	 * Whether to use the internal HSQL DB or an external JDBC SQL DB.
	 * <br>
	 * <em>true</em> for HSQL DB, <em>false</em> for external DB.
	 * <p>
	 * Boolean
	 */
	public static final String PREF_SQLSTORE_INTERNALHSQLMODE = "cpc.storelocal.sql.internalHsqldbMode";

	/**
	 * JDBC Driver for external DB.
	 * <p>
	 * String
	 */
	public static final String PREF_SQLSTORE_JDBCDRIVER = "cpc.storelocal.sql.jdbcdriver";

	/**
	 * JDBC URL for external DB.
	 * <p>
	 * String
	 */
	public static final String PREF_SQLSTORE_JDBCURL = "cpc.storelocal.sql.jdbcurl";

	/**
	 * DB Username for external DB.
	 * <p>
	 * String
	 */
	public static final String PREF_SQLSTORE_USERNAME = "cpc.storelocal.sql.username";

	/**
	 * DB Password for external DB.
	 * <p>
	 * String
	 */
	public static final String PREF_SQLSTORE_PASSWORD = "cpc.storelocal.sql.password";
}
