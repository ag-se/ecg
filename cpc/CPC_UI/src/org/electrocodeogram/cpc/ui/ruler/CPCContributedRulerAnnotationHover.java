package org.electrocodeogram.cpc.ui.ruler;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHoverExtension;
import org.eclipse.jface.text.source.IAnnotationHoverExtension2;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.swt.widgets.Shell;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.ui.data.CloneDataModel;


/**
 * Implements the CPC clone information ruler tool tips. 
 * 
 * @author vw
 * 
 * @see CPCContributedRulerColumn
 * @see CloneDataModel
 */
public class CPCContributedRulerAnnotationHover implements IAnnotationHover, IAnnotationHoverExtension,
		IAnnotationHoverExtension2
{
	private static final Log log = LogFactory.getLog(CPCContributedRulerAnnotationHover.class);

	public CPCContributedRulerAnnotationHover()
	{
		log.trace("CPCContributedRulerAnnotationHover()");
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.source.IAnnotationHover#getHoverInfo(org.eclipse.jface.text.source.ISourceViewer, int)
	 */
	@Override
	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber)
	{
		if (log.isTraceEnabled())
			log.trace("getHoverInfo() - sourceViewer: " + sourceViewer + ", lineNumber: " + lineNumber);

		String result = null;
		IDocument document = sourceViewer.getDocument();
		assert (document != null);

		try
		{
			//get the offset and length for this line number
			IRegion info = document.getLineInformation(lineNumber);

			//get a list of clones which intersect with this line
			List<IClone> clones = CloneDataModel.getInstance().getClonesForRange(info.getOffset(), info.getLength());

			if (clones.isEmpty())
				//no need to display any hover, if there are no clones
				return null;

			//some initialisation
			StringBuilder sb = new StringBuilder();
			IStoreProvider storeProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
					IStoreProvider.class);
			assert (storeProvider != null);

			sb.append(clones.size());
			sb.append(' ');
			sb.append("clone(s) on this line.");

			for (IClone clone : clones)
			{
				sb.append("\n * ");
				sb.append(clone.getOffset());
				sb.append(':');
				sb.append(clone.getLength());

				//display clone state, unless it is the default state
				if (!IClone.State.DEFAULT.equals(clone.getCloneState()))
				{
					sb.append(" ");
					sb.append(clone.getCloneState().toString());
				}

				if (clone.getGroupUuid() != null)
				{
					//get some extra info on this clone
					List<IClone> groupClones = storeProvider.getClonesByGroup(clone.getGroupUuid());
					if (groupClones == null)
					{
						log.error("getHoverInfo() - unable to retrieve group clones - groupUuid: "
								+ clone.getGroupUuid(), new Throwable());
						continue;
					}

					sb.append(" - ");
					sb.append(groupClones.size());
					sb.append(' ');
					sb.append("clone(s) in group");

					//try to get some file info too
					Set<String> otherFileUuids = new HashSet<String>(groupClones.size());
					for (IClone groupClone : groupClones)
					{
						if (!groupClone.getFileUuid().equals(clone.getFileUuid()))
						{
							//this group clone is located in a different file
							otherFileUuids.add(groupClone.getFileUuid());
						}
					}

					//display file names
					for (String otherfileUuid : otherFileUuids)
					{
						sb.append("\n     > ");
						ICloneFile cloneFile = storeProvider.lookupCloneFile(otherfileUuid);
						if (cloneFile == null)
						{
							log.error("getHoverInfo() - unable to retrieve clone file - fileUuid: " + otherfileUuid,
									new Throwable());
							continue;
						}
						//display only the filename
						sb.append((new Path(cloneFile.getPath())).lastSegment());
					}
				}
			}

			result = sb.toString();
		}
		catch (BadLocationException x)
		{
		}

		if (log.isTraceEnabled())
			log.trace("getHoverInfo() - result: " + result);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.source.IAnnotationHoverExtension#getHoverControlCreator()
	 */
	@Override
	public IInformationControlCreator getHoverControlCreator()
	{
		return new CPCReusableInformationControlCreator();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.source.IAnnotationHoverExtension#getHoverInfo(org.eclipse.jface.text.source.ISourceViewer, org.eclipse.jface.text.source.ILineRange, int)
	 */
	@Override
	public Object getHoverInfo(ISourceViewer sourceViewer, ILineRange lineRange, int visibleNumberOfLines)
	{
		//TODO: this will need to be changed, if we ever fix getHoverLineRange()
		return getHoverInfo(sourceViewer, lineRange.getStartLine());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.source.IAnnotationHoverExtension#getHoverLineRange(org.eclipse.jface.text.source.ISourceViewer, int)
	 */
	@Override
	public ILineRange getHoverLineRange(ISourceViewer viewer, int lineNumber)
	{
		//TODO: do a real check here
		return new LineRange(lineNumber, 1);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.source.IAnnotationHoverExtension#canHandleMouseCursor()
	 */
	@Override
	public boolean canHandleMouseCursor()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.source.IAnnotationHoverExtension2#canHandleMouseWheel()
	 */
	@Override
	public boolean canHandleMouseWheel()
	{
		return false;
	}

	class CPCReusableInformationControlCreator extends AbstractReusableInformationControlCreator
	{
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.text.AbstractReusableInformationControlCreator#doCreateInformationControl(org.eclipse.swt.widgets.Shell)
		 */
		@Override
		protected IInformationControl doCreateInformationControl(Shell parent)
		{
			return new DefaultInformationControl(parent);
		}
	}
}
