package org.electrocodeogram.cpc.track.repository;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.IDocument;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.track.codediff.CPCPositionUpdater;


public class DocumentDescriptor
{
	private static Log log = LogFactory.getLog(DocumentDescriptor.class);

	private ICloneFile cloneFile;
	private IDocument document;
	private long lastModification;
	private volatile boolean dirty;
	private volatile boolean stale;

	private CPCPositionUpdater cpcPositionUpdater;

	public DocumentDescriptor(ICloneFile cloneFile, IDocument document)
	{
		if (log.isTraceEnabled())
			log.trace("DocumentDescriptor() - cloneFile: " + cloneFile + ", document: " + document);

		this.cloneFile = cloneFile;
		this.document = document;
		this.lastModification = System.currentTimeMillis();
		this.dirty = false;
		this.stale = false;
	}

	/**
	 * @return the clone file which the document represents, never null.
	 */
	public ICloneFile getCloneFile()
	{
		assert (cloneFile != null);
		return cloneFile;
	}

	/**
	 * @return the document underlying this descriptor, never null.
	 */
	public IDocument getDocument()
	{
		assert (document != null);
		return document;
	}

	/**
	 * @return the system time in milliseconds of the last modification to the clone data in this document.
	 * 		System time as returned by {@link System#currentTimeMillis()}. The default value is the
	 * 		instantiation time of this object.
	 */
	public long getLastModification()
	{
		assert (lastModification > 0);
		return lastModification;
	}

	public void setLastModification(long lastModification)
	{
		if (log.isTraceEnabled())
			log.trace("setLastModification(): " + lastModification);
		assert (lastModification > 0);

		this.lastModification = lastModification;
	}

	/**
	 * @return true if any of the clone data inside this document was modified and needs to be reconciled
	 * 		with the store provider.
	 */
	public boolean isDirty()
	{
		return dirty;
	}

	public void setDirty(boolean dirty)
	{
		if (log.isTraceEnabled())
			log.trace("setDirty(): " + dirty);

		this.dirty = dirty;
	}

	/**
	 * @return true if the clone data for this document was potentially modified by some 3rd party.
	 * 		In this case a fresh copy of the clone data needs to be obtained from the store provider
	 * 		before any action on the clone data can take place.
	 */
	public boolean isStale()
	{
		return stale;
	}

	public void setStale(boolean stale)
	{
		if (log.isTraceEnabled())
			log.trace("setStale(): " + stale);

		this.stale = stale;
	}

	public CPCPositionUpdater getCpcPositionUpdater()
	{
		return cpcPositionUpdater;
	}

	public void setCpcPositionUpdater(CPCPositionUpdater cpcPositionUpdater)
	{
		if (log.isTraceEnabled())
			log.trace("setCpcPositionUpdater(): " + cpcPositionUpdater);

		this.cpcPositionUpdater = cpcPositionUpdater;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "DocumentDescriptor[cloneFile: " + cloneFile + ", lastMod: " + lastModification + ", dirty: " + dirty
				+ ", stale: " + stale + ", document: " + document + "]";
	}

}
