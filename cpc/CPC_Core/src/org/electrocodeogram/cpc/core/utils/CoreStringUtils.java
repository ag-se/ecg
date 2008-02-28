package org.electrocodeogram.cpc.core.utils;


import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Some generic string utility methods which are used throughout different CPC modules.
 * 
 * @author vw
 */
public class CoreStringUtils
{
	//TODO: should we read this value from some configuration file?
	public static final int DEFAULT_TRUNCATE_LENGTH = 50;

	/**
	 * Checks a given string for the occurance of non-whitespace characters.<br/>
	 * Whitespace characters are: Space( ), Tab(t), NewLine(n), LineFeed(r)
	 * 
	 * @param str the string to check, never null.
	 * @return true if the given string contains any non-whitespace characters. 
	 */
	public static boolean containsNonWhitespace(String str)
	{
		for (char chr : str.toCharArray())
			if (chr != ' ' && chr != '\t' && chr != '\n' && chr != '\r')
				return true;

		return false;
	}

	/**
	 * Returns the given string in a special "quoted" format.<br/>
	 * <em>abc</em> becomes <em>[length] "abc"</em>.<br/>
	 * If the input is NULL, <em>null</em> (as string) is returned.
	 * 
	 * @param str the string to "quote", may be null.
	 * @return "quoted" string, never null.
	 */
	public static String quoteString(String str)
	{
		if (str == null)
			return "null";

		return "[" + str.length() + "] \"" + str + "\"";
	}

	/**
	 * Takes a string and truncates it to the given length if it is exceeded.<br/>
	 * Also adds "[N] \"<em>the-string</em>\"", where N is the size of the untruncated string in characters.<br/>
	 * 
	 * @param str the string to truncate, may be NULL.
	 * @param maxLength the maximum allowed length, must be &gt;0. 
	 * @return NULL if NULL, full string with length and quotes if length is &lt;=<em>maxLength</em>.
	 * 		Otherwise, truncated version of <em>str</em> with length and quotes and thus
	 * 		a length of <u>at least</u> <em>maxLength+9</em>.
	 */
	public static String truncateString(String str, int maxLength)
	{
		assert (maxLength > 0);

		if (str == null)
			return null;

		if (str == null || str.length() <= maxLength)
			return "[" + str.length() + "] \"" + str + "\"";

		return "[" + str.length() + "] \"" + str.substring(0, maxLength) + "...\"";
	}

	/**
	 * Convenience method.<br/>
	 * Calls {@link CoreStringUtils#truncateString(String, int)} with a maximum length of
	 * {@link #DEFAULT_TRUNCATE_LENGTH}.
	 *
	 * @see CoreStringUtils#truncateString(String, int)
	 */
	public static String truncateString(String str)
	{
		return truncateString(str, DEFAULT_TRUNCATE_LENGTH);
	}

	/**
	 * Converts a {@link Map} into a String for debugging purposes.<br/>
	 * Entries may be truncated if they are too long.<br/>
	 * Entries will be recursively converted via {@link CoreStringUtils#truncateObject(Object)}.
	 * 
	 * @param map the map to convert to a (truncated) string, may be NULL.
	 * @return the string representation, never null.
	 */
	@SuppressWarnings("unchecked")
	public static String truncateMap(Map map)
	{
		if (map == null)
			return "null";

		if (map.isEmpty())
			return "{}";

		StringBuilder sb = new StringBuilder();
		sb.append('{');
		for (Map.Entry<Object, Object> entry : (Set<Map.Entry<Object, Object>>) map.entrySet())
		{
			sb.append(truncateObject(entry.getKey()));
			sb.append('=');
			sb.append(truncateObject(entry.getValue()));
			sb.append(", "); //we end up with an extra tailing comma this way, but seriously, who cares?
		}
		sb.append('}');

		return sb.toString();
	}

	/**
	 * Converts a {@link List} into a String for debugging purposes.<br/>
	 * Entries may be truncated if they are too long.<br/>
	 * Entries will be recursively converted via {@link CoreStringUtils#truncateObject(Object)}.
	 * 
	 * @param list the list to convert to a (truncated) string, may be NULL.
	 * @return the string representation, never null.
	 */
	@SuppressWarnings("unchecked")
	public static String truncateList(List list)
	{
		if (list == null)
			return "null";

		if (list.isEmpty())
			return "[]";

		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (Object entry : list)
		{
			sb.append(truncateObject(entry));
			sb.append(", "); //we end up with an extra tailing comma this way, but seriously, who cares?
		}
		sb.append(']');

		return sb.toString();
	}

	/**
	 * Converts a {@link Set} into a String for debugging purposes.<br/>
	 * Entries may be truncated if they are too long.<br/>
	 * Entries will be recursively converted via {@link CoreStringUtils#truncateObject(Object)}.
	 * 
	 * @param set the set to convert to a (truncated) string, may be NULL.
	 * @return the string representation, never null.
	 */
	@SuppressWarnings("unchecked")
	public static String truncateSet(Set set)
	{
		if (set == null)
			return "null";

		if (set.isEmpty())
			return "()";

		StringBuilder sb = new StringBuilder();
		sb.append('(');
		for (Object entry : set)
		{
			sb.append(truncateObject(entry));
			sb.append(", "); //we end up with an extra tailing comma this way, but seriously, who cares?
		}
		sb.append(')');

		return sb.toString();
	}

	/**
	 * Converts an {@link Object} into a string for debugging purposes.<br/>
	 * The object will be inspected for its type and may be converted
	 * recursively. I.e. if the object is a list, all the list elements
	 * will be truncated individually.<br/>
	 * <br/>
	 * <b>IMPORTANT:</b> the resulting string may be VERY large, even though it was
	 * 		truncated. I.e. a list with 1000 elements may well end up generating
	 * 		a 50.000 character string.
	 * 
	 * @param obj the object to convert to a (truncated) string, may be NULL.
	 * @return the string representation, never null.
	 */
	@SuppressWarnings("unchecked")
	public static String truncateObject(Object obj)
	{
		if (obj == null)
			return "null";

		if (obj instanceof String)
			return truncateString((String) obj);

		if (obj instanceof Map)
			return truncateMap((Map) obj);

		if (obj instanceof List)
			return truncateList((List) obj);

		if (obj instanceof Set)
			return truncateSet((Set) obj);

		return truncateString(obj.toString());
	}
}
