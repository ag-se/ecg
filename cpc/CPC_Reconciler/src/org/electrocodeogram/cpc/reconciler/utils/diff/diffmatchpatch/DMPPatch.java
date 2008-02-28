package org.electrocodeogram.cpc.reconciler.utils.diff.diffmatchpatch;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;


/**
 * Class representing one patch operation.
 */
public class DMPPatch
{
	public LinkedList<DMPDiff> diffs;
	public int start1;
	public int start2;
	public int length1;
	public int length2;

	/**
	 * Constructor.  Initializes with an empty list of diffs.
	 */
	public DMPPatch()
	{
		this.diffs = new LinkedList<DMPDiff>();
	}

	/**
	 * Emmulate GNU diff's format.
	 * Header: @@ -382,8 +481,9 @@
	 * Indicies are printed as 1-based, not 0-based.
	 * @return The GNU diff string
	 */
	@Override
	public String toString()
	{
		String coords1, coords2;
		if (this.length1 == 0)
		{
			coords1 = this.start1 + ",0";
		}
		else if (this.length1 == 1)
		{
			coords1 = Integer.toString(this.start1 + 1);
		}
		else
		{
			coords1 = (this.start1 + 1) + "," + this.length1;
		}
		if (this.length2 == 0)
		{
			coords2 = this.start2 + ",0";
		}
		else if (this.length2 == 1)
		{
			coords2 = Integer.toString(this.start2 + 1);
		}
		else
		{
			coords2 = (this.start2 + 1) + "," + this.length2;
		}
		String txt = "@@ -" + coords1 + " +" + coords2 + " @@\n";
		// Escape the body of the patch with %xx notation.
		for (DMPDiff aDiff : this.diffs)
		{
			switch (aDiff.operation)
			{
				case DELETE:
					txt += "-";
					break;
				case EQUAL:
					txt += " ";
					break;
				case INSERT:
					txt += "+";
					break;
				default:
					assert false : "Invalid diff operation in patch_obj.toString()";
			}
			try
			{
				txt += URLEncoder.encode(aDiff.text, "UTF-8").replace('+', ' ') + "\n";
			}
			catch (UnsupportedEncodingException e)
			{
				// This is a system which does not support UTF-8.  (Not likely)
				System.out.println("Error in Patch.toString: " + e);
				return "";
			}
		}
		// Replicate the JavaScript encodeURI() function (not including %20)
		txt = txt.replace("%3D", "=").replace("%3B", ";").replace("%27", "'").replace("%2C", ",").replace("%2F", "/")
				.replace("%7E", "~").replace("%21", "!").replace("%40", "@").replace("%23", "#").replace("%24", "$")
				.replace("%26", "&").replace("%28", "(").replace("%29", ")").replace("%2B", "+").replace("%3A", ":")
				.replace("%3F", "?");
		return txt;
	}

	/**
	 * Compute and return the source text (all equalities and deletions).
	 * @return Source text
	 */
	public String text1()
	{
		String txt = "";
		for (DMPDiff aDiff : this.diffs)
		{
			if (aDiff.operation != DiffMatchPatch.Operation.INSERT)
			{
				txt += aDiff.text;
			}
		}
		return txt;
	}

	/**
	 * Compute and return the destination text (all equalities and insertions).
	 * @return Destination text
	 */
	public String text2()
	{
		String txt = "";
		for (DMPDiff aDiff : this.diffs)
		{
			if (aDiff.operation != DiffMatchPatch.Operation.DELETE)
			{
				txt += aDiff.text;
			}
		}
		return txt;
	}
}
