package org.electrocodeogram.cpc.track.codediff;


import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IDocumentPartitioningListener;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;


/**
 * An {@link IDocument} proxy object which only supports the following operations:
 * <ul>
 * 	<li>{@link IDocument#get()}</li>
 * 	<li>{@link IDocument#get(int, int)}</li>
 * 	<li>{@link IDocument#getLength()}</li>
 * </ul>
 * All other methods will throw an {@link NotImplementedException}.
 * 
 * @author vw
 */
public class DocumentProxy implements IDocument
{
	private String content;

	/**
	 * Create a new {@link DocumentProxy} with the given content.<br/>
	 * The content can not be changed later.
	 * 
	 * @param content the content for this document proxy, never null.
	 */
	public DocumentProxy(String content)
	{
		assert (content != null);

		this.content = content;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#get()
	 */
	@Override
	public String get()
	{
		return content;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#get(int, int)
	 */
	@Override
	public String get(int offset, int length) throws BadLocationException
	{
		return content.substring(offset, offset + length);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocument#getLength()
	 */
	@Override
	public int getLength()
	{
		return content.length();
	}

	/*
	 * All other methods are not implemented.
	 */

	@Override
	public void addDocumentListener(IDocumentListener listener)
	{
		throw new NotImplementedException();
	}

	@Override
	public void addDocumentPartitioningListener(IDocumentPartitioningListener listener)
	{
		throw new NotImplementedException();
	}

	@Override
	public void addPosition(Position position) throws BadLocationException
	{
		throw new NotImplementedException();
	}

	@Override
	public void addPosition(String category, Position position) throws BadLocationException,
			BadPositionCategoryException
	{
		throw new NotImplementedException();
	}

	@Override
	public void addPositionCategory(String category)
	{
		throw new NotImplementedException();
	}

	@Override
	public void addPositionUpdater(IPositionUpdater updater)
	{
		throw new NotImplementedException();
	}

	@Override
	public void addPrenotifiedDocumentListener(IDocumentListener documentAdapter)
	{
		throw new NotImplementedException();
	}

	@Override
	public int computeIndexInCategory(String category, int offset) throws BadLocationException,
			BadPositionCategoryException
	{
		throw new NotImplementedException();
	}

	@Override
	public int computeNumberOfLines(String text)
	{
		throw new NotImplementedException();
	}

	@Override
	public ITypedRegion[] computePartitioning(int offset, int length) throws BadLocationException
	{
		throw new NotImplementedException();
	}

	@Override
	public boolean containsPosition(String category, int offset, int length)
	{
		throw new NotImplementedException();
	}

	@Override
	public boolean containsPositionCategory(String category)
	{
		throw new NotImplementedException();
	}

	@Override
	public char getChar(int offset) throws BadLocationException
	{
		throw new NotImplementedException();
	}

	@Override
	public String getContentType(int offset) throws BadLocationException
	{
		throw new NotImplementedException();
	}

	@Override
	public IDocumentPartitioner getDocumentPartitioner()
	{
		throw new NotImplementedException();
	}

	@Override
	public String[] getLegalContentTypes()
	{
		throw new NotImplementedException();
	}

	@Override
	public String[] getLegalLineDelimiters()
	{
		throw new NotImplementedException();
	}

	@Override
	public String getLineDelimiter(int line) throws BadLocationException
	{
		throw new NotImplementedException();
	}

	@Override
	public IRegion getLineInformation(int line) throws BadLocationException
	{
		throw new NotImplementedException();
	}

	@Override
	public IRegion getLineInformationOfOffset(int offset) throws BadLocationException
	{
		throw new NotImplementedException();
	}

	@Override
	public int getLineLength(int line) throws BadLocationException
	{
		throw new NotImplementedException();
	}

	@Override
	public int getLineOfOffset(int offset) throws BadLocationException
	{
		throw new NotImplementedException();
	}

	@Override
	public int getLineOffset(int line) throws BadLocationException
	{
		throw new NotImplementedException();
	}

	@Override
	public int getNumberOfLines()
	{
		throw new NotImplementedException();
	}

	@Override
	public int getNumberOfLines(int offset, int length) throws BadLocationException
	{
		throw new NotImplementedException();
	}

	@Override
	public ITypedRegion getPartition(int offset) throws BadLocationException
	{
		throw new NotImplementedException();
	}

	@Override
	public String[] getPositionCategories()
	{
		throw new NotImplementedException();
	}

	@Override
	public IPositionUpdater[] getPositionUpdaters()
	{
		throw new NotImplementedException();
	}

	@Override
	public Position[] getPositions(String category) throws BadPositionCategoryException
	{
		throw new NotImplementedException();
	}

	@Override
	public void insertPositionUpdater(IPositionUpdater updater, int index)
	{
		throw new NotImplementedException();
	}

	@Override
	public void removeDocumentListener(IDocumentListener listener)
	{
		throw new NotImplementedException();
	}

	@Override
	public void removeDocumentPartitioningListener(IDocumentPartitioningListener listener)
	{
		throw new NotImplementedException();
	}

	@Override
	public void removePosition(Position position)
	{
		throw new NotImplementedException();
	}

	@Override
	public void removePosition(String category, Position position) throws BadPositionCategoryException
	{
		throw new NotImplementedException();
	}

	@Override
	public void removePositionCategory(String category) throws BadPositionCategoryException
	{
		throw new NotImplementedException();
	}

	@Override
	public void removePositionUpdater(IPositionUpdater updater)
	{
		throw new NotImplementedException();
	}

	@Override
	public void removePrenotifiedDocumentListener(IDocumentListener documentAdapter)
	{
		throw new NotImplementedException();
	}

	@Override
	public void replace(int offset, int length, String text) throws BadLocationException
	{
		throw new NotImplementedException();
	}

	@Override
	public int search(int startOffset, String findString, boolean forwardSearch, boolean caseSensitive,
			boolean wholeWord) throws BadLocationException
	{
		throw new NotImplementedException();
	}

	@Override
	public void set(String text)
	{
		throw new NotImplementedException();
	}

	@Override
	public void setDocumentPartitioner(IDocumentPartitioner partitioner)
	{
		throw new NotImplementedException();
	}

}
