package org.electrocodeogram.cpc.imports.sdd.imports;


import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.soc.sdd.comparer.Chain;
import org.eclipse.soc.sdd.indexer.Indexer;
import org.eclipse.soc.sdd.indexer.InvertedIndexer;


public class SDDCompareResult
{

	private List<IFile> _files;

	private InvertedIndexer _ii;

	private Indexer _indexer;

	private List<Chain[]> _parts;

	public void setFiles(List<IFile> files)
	{
		_files = files;
	}

	public IFile getOriginalFile(int docid)
	{
		return _files.get(docid);
	}

	public void setInvertedIndexer(InvertedIndexer ii)
	{
		_ii = ii;
	}

	public void setIndexer(Indexer indexer)
	{
		_indexer = indexer;
	}

	public InvertedIndexer getInvertedIndexer()
	{
		return _ii;
	}

	public Indexer getIndexer()
	{
		return _indexer;
	}

	public void setSimilarParts(List<Chain[]> results)
	{
		_parts = results;
	}

	public List<Chain[]> getSimilarParts()
	{
		return _parts;
	}

	public int getStartOffset(Chain chain)
	{
		return getIndexer().get(chain.getDocid(), chain.getStartChunkNo()).getStartPosition();
	}

	public int getEndOffset(Chain chain)
	{
		return getIndexer().get(chain.getDocid(), chain.getEndChunkNo()).getEndPosition() + 1;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "SDDCompareResult[file count: " + _files.size() + ", part count: " + _parts.size() + ", ii: " + _ii
				+ ", indexer: " + _indexer + "]";
	}
}
