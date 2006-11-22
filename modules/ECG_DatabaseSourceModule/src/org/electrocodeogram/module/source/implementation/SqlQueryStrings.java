/**
 * 
 */
package org.electrocodeogram.module.source.implementation;

import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * this class proviedes several methods to generate Strings which are SQL
 * Statements for the Database which then can be executed by the DBCommunicator
 * 
 * @author jule
 * @version 1.0
 * 
 */
public class SqlQueryStrings {

	/**
	 * This is the logger.
	 */
	private static Logger logger = LogHelper
			.createLogger(DatabaseSourceModule.class.getName());

	/**
	 * empty Constructor
	 */
	public SqlQueryStrings() {
	}

	public static String getDataByID(String id, String tableName) {
		String stmt = "SELECT * FROM " + tableName + " where linkID = " + id
				+ ";";
		return stmt;
	}

	/**
	 * This Method return a String which is a join Statement over the all tables
	 * concerning an event the tables are joined by the value of their idLink
	 * column
	 * 
	 * @param idValue
	 *            the value if the idLink column
	 * @param tables
	 *            the tables to be joined
	 * @return the String representing the JOIN Statement
	 */
	// public static String createJoinStmt(int idValue, Vector tables) {
	// logger.entering(CreateSqlStrings.class.getName(), "createJoinStmt");
	//
	// // there must be at least two tables for the join statement
	// if (tables.size() < 1) {
	// logger.warning("Cannot make join over one table");
	// return null;
	// }
	//
	// // The String representing the Statment
	// String join = "SELECT * FROM commondata";
	// join = join + " LEFT JOIN (";
	//
	// // each tablename has to be included in the statement
	// for (int i = 0; i < tables.size(); i++) {
	// String tablename = (String) tables.get(i);
	// if (i == (tables.size() - 1)) {
	// join = join + tablename + ")";
	// }
	// else {
	// join = join + tablename + ", ";
	// }
	// }
	// // the element over which the tables are joined
	// join = join + " USING (linkid)where linkid=" + idValue;
	// logger.info("Created join Statment: " + join);
	// logger.exiting(CreateSqlStrings.class.getName(), "createJoinStmt");
	// return join;
	// }
	public static String queryWithUsernameAndDate(String username, String date) {
		String queryString = "SELECT * FROM commondata ";
		queryString = queryString + "where username = '" + username;
		queryString = queryString + "' AND (LEFT(timestamp, 10)) = '" + date
				+ "';";
		logger.info("queryString");

		return queryString;
	}

	public static String queryRunsWithTimediff() {
		String queryString = " SELECT  c1.linkid AS run1, c1.timestamp AS run1_TS, c2.linkid AS run2, c2.timestamp AS run2_TS ";
		queryString = queryString + "FROM commondata c1, commondata c2 WHERE ";
		queryString = queryString
				+ "TIMESTAMPDIFF(SECOND,c2.timestamp,c1.timestamp)"
				+ "<'30'AND c1.msdt='msdt.editor.xsd';";
		logger.info("queryString");

		return queryString;
	}

	public static String queryEventsBetweenTimestamps(String run1_TS,
			String run2_TS) {

		String queryString = "SELECT * FROM commondata WHERE";
		queryString = queryString + " timestamp > '" + run1_TS + "' ";
		queryString = queryString + "AND timestamp < '" + run2_TS + "';";
		logger.info("queryString");

		return queryString;
	}

	public static String queryEventsBetweenTwoEvents(String firstID,
			String secondID) {
		String queryString = "SELECT * FROM commondata WHERE ";
		queryString = queryString + "linkid > " + firstID;
		queryString = queryString + " AND linkid < " + secondID + ";";
		logger.info("queryString");

		return queryString;
	}

}
