package org.electrocodeogram.cpc.reconciler.provider;


import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.IManagableProvider;
import org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider;
import org.electrocodeogram.cpc.core.api.provider.reconciler.IDiffProvider;
import org.electrocodeogram.cpc.core.api.provider.reconciler.IDiffResult;
import org.electrocodeogram.cpc.core.api.provider.reconciler.IReconcilerProvider;
import org.electrocodeogram.cpc.core.api.provider.reconciler.IReconciliationResult;
import org.electrocodeogram.cpc.core.utils.CoreClonePositionUtils;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;
import org.electrocodeogram.cpc.core.utils.CoreUtils;
import org.electrocodeogram.cpc.reconciler.api.strategy.IReconcilerStrategy;
import org.electrocodeogram.cpc.reconciler.strategy.AllChangesAfterClonesStrategy;
import org.electrocodeogram.cpc.reconciler.strategy.DefaultDiffStrategy;
import org.electrocodeogram.cpc.reconciler.strategy.WhitespaceOnlyChangeStrategy;


/**
 * Default {@link IReconcilerProvider} implementation.
 * 
 * @author vw
 */
public class ExternalModificationReconcilerProvider implements IReconcilerProvider, IManagableProvider
{
	private static Log log = LogFactory.getLog(ExternalModificationReconcilerProvider.class);

	/**
	 * Return value for {@link ExternalModificationReconcilerProvider#notifyUser(ICloneFile, List)}. 
	 */
	private enum ReconciliationUserChoice
	{
		/**
		 * User indicated that the modification should be reconciled.
		 */
		RECONCILE,

		/**
		 * User indicated that all clone data for the file in question should be dropped.
		 */
		DROP_ALL
	}

	private List<IReconcilerStrategy> registeredStrategies = null;

	/*
	 * IExternalModificationReconcilerProvider methods.
	 */

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.reconciler.IExternalModificationReconcilerProvider#reconcile(org.electrocodeogram.cpc.core.api.data.ICloneFile, java.util.List, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public IReconciliationResult reconcile(ICloneFile cloneFile, List<IClone> persistedClones,
			String persistedFileContent, String newFileContent, boolean notifyUser)
	{
		assert (cloneFile != null && persistedClones != null);

		if (log.isTraceEnabled())
			log.trace("reconcile() - cloneFile: " + cloneFile + ", persistedClones: " + persistedClones
					+ ", persistedFileContent: " + CoreStringUtils.truncateString(persistedFileContent)
					+ ", newFileContent: " + CoreStringUtils.truncateString(newFileContent) + ", notifyUser: "
					+ notifyUser);

		ReconciliationResult result = new ReconciliationResult();

		//check if we even have any clone data for that file
		if (persistedClones.isEmpty())
		{
			//nope, no clone data, so we have nothing to do
			log.trace("reconcile() - file contains no clones, nothing to do - FULL_RECONCILIATION.");

			result.setStatus(IReconciliationResult.Status.FULL_RECONCILIATION);

			if (log.isTraceEnabled())
				log.trace("reconcile() - result: " + result);

			return result;
		}

		//we won't be able to do anything if we don't get the file contents
		if (persistedFileContent == null || newFileContent == null)
		{
			log.warn("reconcile() - missing file content - cloneFile: " + cloneFile + ", persistedClones: "
					+ persistedClones + ", persistedFileContent: "
					+ CoreStringUtils.truncateString(persistedFileContent) + ", newFileContent: "
					+ CoreStringUtils.truncateString(newFileContent));

			//TODO: we should probably notify the user here?

			//mark all clones as removed
			log.info("reconcile() - removing all clones - NO_RECONCILIATION");
			CoreUtils.cloneCloneList(persistedClones, result.getRemovedClones());
			result.setStatus(IReconciliationResult.Status.NO_RECONCILIATION);

			if (log.isTraceEnabled())
				log.trace("reconcile() - result: " + result);

			return result;
		}

		//do a first quick check whether the file contents haven't actually changed at all
		if (persistedFileContent.length() == newFileContent.length() && persistedFileContent.equals(newFileContent))
		{
			//there wasn't any change! We're done now.
			log.trace("reconcile() - file content remained unchanged, nothing to do - FULL_RECONCILIATION.");

			result.setStatus(IReconciliationResult.Status.FULL_RECONCILIATION);

			if (log.isTraceEnabled())
				log.trace("reconcile() - result: " + result);

			return result;
		}

		/*
		 * Notify the user about the external modification.
		 */
		if (notifyUser)
		{
			ReconciliationUserChoice choice = notifyUser(cloneFile, persistedClones);
			if (ReconciliationUserChoice.DROP_ALL.equals(choice))
			{
				log.info("reconcile() - user requested all clone data to be dropped");
				CoreUtils.cloneCloneList(persistedClones, result.getRemovedClones());
				result.setStatus(IReconciliationResult.Status.NO_RECONCILIATION);

				if (log.isTraceEnabled())
					log.trace("reconcile() - result: " + result);

				return result;
			}
		}

		//ok, the contents differ somehow.
		//compute the differences between the two files
		IDiffProvider diffProvider = (IDiffProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				IDiffProvider.class);
		assert (diffProvider != null);
		List<IDiffResult> differences = diffProvider.charDiff(persistedFileContent, newFileContent);

		//create the "todo list" with cloned versions of all persisted clones
		LinkedList<IClone> pendingClones = new LinkedList<IClone>();
		CoreUtils.cloneCloneList(persistedClones, pendingClones);

		//the clones probably have no or outdated non-whitespace offsets, lets update them
		//some of our strategies might want to fall back to these values.
		ICloneFactoryProvider cloneFactoryProvider = (ICloneFactoryProvider) CPCCorePlugin.getProviderRegistry()
				.lookupProvider(ICloneFactoryProvider.class);
		assert (cloneFactoryProvider != null);
		CoreClonePositionUtils.extractPositions(cloneFactoryProvider, pendingClones, persistedFileContent);

		/*
		 * Call registered strategies.
		 */
		callStrategies(cloneFile, persistedClones, persistedFileContent, newFileContent, differences, pendingClones,
				result);

		/*
		 * Now do some cleanup work.
		 */

		//check if any clones are still pending
		if (!pendingClones.isEmpty())
		{
			//some clones were not handled by any strategy, we have to consider them to be lost at this point.
			log
					.info("reconcile() - some clones were lost due to an external edit, not handled by any strategy - lost clones: "
							+ pendingClones);
			result.getLostClones().addAll(pendingClones);
		}

		//update the result status
		if (result.getLostClones().isEmpty())
		{
			//nothing was lost
			log.trace("reconcile() - all clone data was reconciled");
			result.setStatus(IReconciliationResult.Status.FULL_RECONCILIATION);
		}
		else if (result.getLostClones().size() == persistedClones.size())
		{
			//all clones were lost
			//TODO: maybe downgrade this to log level INFO
			log.warn("reconcile() - all clone data for file was lost due to external modification - file: "
					+ cloneFile.getPath());
			result.setStatus(IReconciliationResult.Status.NO_RECONCILIATION);
		}
		else
		{
			//at least some clones were lost
			//TODO: maybe downgrade this to log level INFO
			log.warn("reconcile() - some clone data for file was lost due to external modification - file: "
					+ cloneFile.getPath());
			result.setStatus(IReconciliationResult.Status.PARTIAL_RECONCILIATION);
		}

		if (log.isTraceEnabled())
			log.trace("reconcile() - result: " + result);

		return result;
	}

	/*
	 * IProvider methods
	 */

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IProvider#getProviderName()
	 */
	@Override
	public String getProviderName()
	{
		log.trace("getProviderName()");

		return "CPC Reconciler: org.electrocodeogram.cpc.reconciler.provider.ExternalModificationReconcilerProvider";
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IProvider#onLoad()
	 */
	@Override
	public void onLoad()
	{
		log.trace("onLoad()");

		//get all registered strategies
		initialiseStrategies();
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
	 * Displays a notification message to the user, indicating the fact that a (not recommended)
	 * external modification was detected and offering him the choice of how to handle it.<br/>
	 * <br/>
	 * Possible choices are:
	 * <ul>
	 * 	<li>try to reconcile the modification</li>
	 * 	<li>drop all clone data for the file</li>
	 * </ul>
	 * 
	 * TODO: use a custom dialog here or even better, a wizard?
	 */
	private ReconciliationUserChoice notifyUser(final ICloneFile cloneFile, final List<IClone> persistedClones)
	{
		/*
		 * TODO:/FIXME: there should be a better solution for this.
		 * 
		 * We have a potential deadlock problem between this thread and main thread here.
		 * 
		 * We may be called by a background thread which is requesting the clone data for an externally
		 * modified file from the store provider. The store provider will then detect the modification
		 * and call us. In that situation the current thread may hold a lock on the document and on
		 * the store provider.
		 * 
		 * However, the popup needs to be dispatched in the UI thread and the sync exec may cause a deadlock
		 * if the main thread is currently trying to access the document or the store provider (and is thus
		 * blocked by us).
		 * 
		 * For now we simply log a warning and expect that the user will want the
		 * data to be reconciled.
		 */

		log.warn("notifyUser() - external file modification detected, trying to reconcile changes."
				+ "This may lead to cpc clone data loss. External modifications are strongy discouraged. - cloneFile: "
				+ cloneFile + ", number of clones in file: " + persistedClones.size());

		return ReconciliationUserChoice.RECONCILE;

		//		final List<ReconciliationUserChoice> result = new ArrayList<ReconciliationUserChoice>(1);
		//
		//		//try to display a message to the user
		//		Display.getDefault().syncExec(new Runnable()
		//		{
		//			public void run()
		//			{
		//				IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		//				Shell shell = workbenchWindow.getShell();
		//				boolean yesSelected = MessageDialog
		//						.openQuestion(
		//								shell,
		//								"CPC: External file modification",
		//								"CPC has detected an external modification of a file.\n"
		//										+ "External modifications are discouraged as they may lead to the loss of CPC clone data.\n"
		//										+ "CPC can now try to automatically reconcile the changes or drop all clone data for this file.\n"
		//										+ "Should CPC try to reconcile the changes?\n\n" + "Affected file: "
		//										+ cloneFile.getProject() + "/" + cloneFile.getPath() + "\n"
		//										+ "Number of clones in file: " + persistedClones.size());
		//
		//				if (yesSelected)
		//					result.add(ReconciliationUserChoice.RECONCILE);
		//				else
		//					result.add(ReconciliationUserChoice.DROP_ALL);
		//			}
		//		});
		//
		//		assert (result != null && result.size() == 1);
		//
		//		return result.get(0);
	}

	/**
	 * Executes all registered strategies in order of their priority till the first strategy
	 * returns {@link IReconcilerStrategy.Status#BREAK} or all strategies
	 * have been run.
	 */
	private void callStrategies(ICloneFile cloneFile, List<IClone> persistedClones, String persistedFileContent,
			String newFileContent, List<IDiffResult> differences, LinkedList<IClone> pendingClones,
			IReconciliationResult result)
	{
		//now run each strategy in turn
		for (IReconcilerStrategy strategy : registeredStrategies)
		{
			if (log.isTraceEnabled())
				log.trace("callStrategies() - strategy: " + strategy);

			IReconcilerStrategy.Status status = strategy.reconcile(cloneFile, persistedClones, persistedFileContent,
					newFileContent, differences, pendingClones, result);

			if (log.isTraceEnabled())
				log.trace("callStrategies() - status: " + status);

			if (IReconcilerStrategy.Status.BREAK.equals(status))
			{
				log.trace("callStrategies() - aborting further execution of strategies by request of strategy: "
						+ strategy);
				break;
			}
		}

		if (log.isTraceEnabled())
			log.trace("callStrategies() - result: " + result);
	}

	/**
	 * Retrieves all registered {@link IReconcilerStrategy} extensions from the
	 * corresponding extension point and adds them to the <em>registeredStrategies</em> list,
	 * ordered by descending priority.
	 */
	private void initialiseStrategies()
	{
		log.trace("initialiseStrategies()");

		registeredStrategies = new LinkedList<IReconcilerStrategy>();

		//TODO: use extension point here

		registeredStrategies.add(new AllChangesAfterClonesStrategy());
		registeredStrategies.add(new WhitespaceOnlyChangeStrategy());
		registeredStrategies.add(new DefaultDiffStrategy());
	}
}
