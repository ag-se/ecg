package test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IResource;

public class Similarity
{
	public Similarity()
	{
		
	}
	
	public static void main(String[] args)
	{
		System.out.println("Hello World!");
	}
	
	public static void main(String[] args){System.out.println("Hello World!");}

	public static void main(String[] args)
	{
		System.out.println("Tux Tux Tux");
	}
	
	public static void newfuncname(String[] newparamname)
	{
		System.out.println("newstring");
	}
	
	/*
	 * Some long but pointless comment block.
	 * Some long but pointless comment block.
	 * Some long but pointless comment block.
	 * Some long but pointless comment block.
	 * Some long but pointless comment block.
	 */
	public static void main(String[] args)
	{
		System.out.println("Tux Tux Tux");
	}

	/*
	 * Another large non-code section which differs
	 * from the earlier one.
	 * Another large non-code section which differs
	 * from the earlier one.
	 * Another large non-code section which differs
	 * from the earlier one.
	 */
	public static void main(String[] args)
	{
		System.out.println("Tux Tux Tux");
	}

	public int somefunc(String someparam)
	{
		if (true)
		{
			return 4+6;
		}
		else
		{
			return -1;
		}
	}
	
	/*
	 * Original version.
	 */
	private IResource[] getUncoveredResources(CompoundResourceTraversal otherTraversal) {
		Set result = new HashSet();
		for (Iterator iter = otherTraversal.files.iterator(); iter.hasNext();) {
			IResource resource = (IResource) iter.next();
			if (!isCovered(resource, IResource.DEPTH_ZERO)) {
				result.add(resource);
			}
		}
		for (Iterator iter = otherTraversal.zeroFolders.iterator(); iter.hasNext();) {
			IResource resource = (IResource) iter.next();
			if (!isCovered(resource, IResource.DEPTH_ZERO)) {
				result.add(resource);
			}
		}
		for (Iterator iter = otherTraversal.shallowFolders.iterator(); iter.hasNext();) {
			IResource resource = (IResource) iter.next();
			if (!isCovered(resource, IResource.DEPTH_ONE)) {
				result.add(resource);
			}
		}
		for (Iterator iter = otherTraversal.deepFolders.iterator(); iter.hasNext();) {
			IResource resource = (IResource) iter.next();
			if (!isCovered(resource, IResource.DEPTH_INFINITE)) {
				result.add(resource);
			}
		}
		return (IResource[]) result.toArray(new IResource[result.size()]);
	}

	/*
	 * Renamed version.
	 */
	private IResource[] newFuncName2(CompoundResourceTraversal newParamName2) {
		Set endData = new HashSet();
		for (Iterator someIter = newParamName2.files.iterator(); someIter.hasNext();) {
			IResource someResult = (IResource) someIter.next();
			if (!isCovered(someResult, IResource.DEPTH_ZERO)) {
				endData.add(someResult);
			}
		}
		for (Iterator someIter = newParamName2.zeroFolders.iterator(); someIter.hasNext();) {
			IResource someResult = (IResource) someIter.next();
			if (!isCovered(someResult, IResource.DEPTH_ZERO)) {
				endData.add(someResult);
			}
		}
		for (Iterator someIter = newParamName2.shallowFolders.iterator(); someIter.hasNext();) {
			IResource someResult = (IResource) someIter.next();
			if (!isCovered(someResult, IResource.DEPTH_ONE)) {
				endData.add(someResult);
			}
		}
		for (Iterator someIter = newParamName2.deepFolders.iterator(); someIter.hasNext();) {
			IResource someResult = (IResource) someIter.next();
			if (!isCovered(someResult, IResource.DEPTH_INFINITE)) {
				endData.add(someResult);
			}
		}
		return (IResource[]) endData.toArray(new IResource[endData.size()]);
	}

}
