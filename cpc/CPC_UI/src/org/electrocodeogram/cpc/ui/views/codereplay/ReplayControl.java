package org.electrocodeogram.cpc.ui.views.codereplay;


import java.util.Date;

import org.eclipse.jface.util.ListenerList;
import org.eclipse.swt.widgets.Display;


/**
 * The ReplayControl is the main class of the plugin control layer. This means that the
 * access to the datamodel is only possible via this class. The only exceptions to that are
 * the different contentprovider classes, but they are only used by the widgets they belong to.
 * The ReplayControl, like all major classes, is implemented as a singleton.
 * Objects that are interested in receiving {@link ReplayActionEvent}s have to register at this 
 * singleton instance.
 * This class also maintains the reader and writer instances(important if you want to add new ones).
 * 
 * @author marco kranz
 */
public class ReplayControl implements IModelChangeListener
{

	/**
	 * identifier for realtime mode
	 */
	public static final int REALTIME = 0;
	/**
	 * identifier for burst mode
	 */
	public static final int BURST = 1;

	volatile private boolean realtime;

	// singleton instance
	private static ReplayControl control;

	private Timer timer;

	private ListenerList listener_list = new ListenerList();

	private Date timeToNextStep;

	volatile private int burstspeed = 1;

	private ReplayControl()
	{
		DataProvider.getInstance().addModelChangeListener(this);

	}

	/**
	 * @return singleton instance
	 */
	public static ReplayControl getInstance()
	{
		if (control == null)
			control = new ReplayControl();
		return control;
	}

	/**
	 * Add an IReplayActionListener to receive {@link ReplayActionEvent}s
	 * 
	 * @param listener the listener to add
	 */
	public void addReplayActionListener(IReplayActionListener listener)
	{
		listener_list.add(listener);
	}

	/**
	 * @param listener remove listener
	 */
	public void removeReplayActionListener(IReplayActionListener listener)
	{
		listener_list.remove(listener);
	}

	private void callReplayActionListener(int cause)
	{
		Object[] listeners = listener_list.getListeners();
		for (int i = 0; i < listeners.length; ++i)
		{
			((IReplayActionListener) listeners[i]).ReplayAction(new ReplayActionEvent(cause));
		}
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.codereplay.dataProvider.IModelChangeListener#modelChange(org.electrocodeogram.codereplay.dataProvider.ModelChangeEvent)
	 */
	public void modelChange(ModelChangeEvent event)
	{
		if (event.getCause() == ModelChangeEvent.REPLAY_CHANGED)
		{
			//System.out.println("modelChange in replaycontrol: replay stopped!!!");
			stopReplay();
		}
	}

	/**
	 * Starts the replay.
	 */
	public void startReplay()
	{
		if (DataProvider.getInstance().getActiveReplay() != null)
		{
			timer = new Timer(Display.getCurrent());
			timer.start();
		}
	}

	/**
	 * Stops the replay.
	 */
	public void stopReplay()
	{
		if (timer != null)
		{
			timer.stopThread();
			timer = null;
			callReplayActionListener(ReplayActionEvent.REPLAY_STOPPED);
		}
	}

	// kill threads in case of object disposal(e.g. program closed...)
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize()
	{
		if (timer != null)
		{
			timer.stopThread();
			timer = null;
		}
	}

	// timer callback in case Replay is 'running'
	// 
	private void timeElapsed(long interval)
	{
		//Calendar calendar = Calendar.getInstance();
		//calendar.set(Calendar.MINUTE, 0);
		//calendar.set(Calendar.SECOND, 0);
		//calendar.add(Calendar.MILLISECOND, (int)(interval));
		timeToNextStep = new Date(interval);
		callReplayActionListener(ReplayActionEvent.COUNTDOWN_CHANGED);
		//System.out.println("time left: "+(interval/1000)+" seconds");
	}

	/**
	 * The Date returned is the standard base date(January 1, 1970, 00:00:00 GMT) plus the time in 
	 * millis until the next step.
	 * 
	 * @return a Date representing the time left to the next step
	 */
	public Date getTimeUntilNextStep()
	{
		return timeToNextStep;
	}

	/**
	 * Move ReplayElement pointer of active Replay to the next element.
	 */
	public void stepToNextElement()
	{
		if (DataProvider.getInstance().getActiveReplay() != null)
			DataProvider.getInstance().incrementReplayPosition();
	}

	/**
	 * Move ReplayElement pointer of active Replay to the previous element.
	 */
	public void stepToPreviousElement()
	{
		stopReplay();
		if (DataProvider.getInstance().getActiveReplay() != null)
			DataProvider.getInstance().decrementReplayPosition();
	}

	/**
	 * Move ReplayElement pointer of active Replay to the first element.
	 */
	public void jumpToFirstElement()
	{
		stopReplay();
		if (DataProvider.getInstance().getActiveReplay() != null)
			DataProvider.getInstance().jumpToFirstPosition();
	}

	/**
	 * Move ReplayElement pointer of active Replay to the last element.
	 */
	public void jumpToLastElement()
	{
		stopReplay();
		if (DataProvider.getInstance().getActiveReplay() != null)
			DataProvider.getInstance().jumpToLastPosition();
	}

	/**
	 * Move ReplayElement pointer of active Replay to the provided element.
	 * 
	 * @param obj the object(ReplayElement) that should be set as 'active' element
	 */
	public void setSelectedElement(Object obj)
	{
		stopReplay();
		DataProvider.getInstance().setActiveElement((ReplayElement) obj);
	}

	/**
	 * Param mode should be either {@link ReplayControl.REALTIME} or {@link ReplayControl.BURST}.
	 * Otherwise nothing will happen.
	 * 
	 * @param mode the replay mode.
	 */
	public void setReplayMode(int mode)
	{
		if (mode == REALTIME)
			realtime = true;
		else if (mode == BURST)
			realtime = false;
	}

	/**
	 * Sets the time interval between steps when replay is running in burst mode.
	 * 
	 * @param i value for interval between steps in seconds 
	 */
	public void setBurstSpeed(int i)
	{
		burstspeed = i;
	}

	/**
	 * @return The detailed cause of change for the currently selected(visible) Element as given in the Event.
	 */
	public String getExactCause()
	{
		return DataProvider.getInstance().getActiveReplay().getCurrentElement().getExactChange();
	}

	/**
	 * @return true if Replay has reached the last element, false otherwise.
	 */
	public boolean isEndOfReplay()
	{
		return DataProvider.getInstance().getActiveReplay().isEndOfReplay();
	}

	/**
	 * @return true if replay is running, false otherwise
	 */
	public boolean isTimerRunning()
	{
		if (timer != null)
			return timer.isRunning();
		return false;
	}

	// timer class, instance will be created(and started) every time the replay is started.
	// it basically either counts down the time between two steps(compared by timestamp) 
	// or just waits for the burst timer to run out for each step.
	private class Timer extends Thread
	{

		private int delay = 50;
		volatile private boolean running = true;
		private Display display;
		private long difference;

		public Timer(Display d)
		{
			display = d;
		}

		public void stopThread()
		{
			running = false;
			//System.out.println("stop thread");
		}

		public boolean isRunning()
		{
			return running;
		}

		@Override
		public void run()
		{
			Date currentDate;
			Date nextDate;

			if (DataProvider.getInstance().getActiveReplay().isEndOfReplay())
				return;

			//System.out.println("start thread");

			while (running)
			{
				currentDate = DataProvider.getInstance().getActiveReplay().getCurrentElement().getTimestamp();
				nextDate = DataProvider.getInstance().getActiveReplay().getNextElement().getTimestamp();
				difference = nextDate.getTime() - currentDate.getTime();
				//System.out.println("running...");

				while (running)
				{
					if (realtime)
					{
						//System.out.println("difference is: "+difference);
						if (difference > 0)
						{
							try
							{
								sleep(delay);

								//							 eclipse way to get thread access to the UI
								if (display.isDisposed())
									return;
								display.syncExec(new Runnable()
								{
									public void run()
									{
										timeElapsed(difference); // in class ReplayControl
									}
								});
								difference -= delay;
							}
							catch (InterruptedException e)
							{
								e.printStackTrace();
							}
						}
						else
						{
							// eclipse way to get thread access to the UI
							if (display.isDisposed())
								return;
							display.syncExec(new Runnable()
							{
								public void run()
								{
									DataProvider.getInstance().incrementReplayPosition();
								}
							});
							break;
						}
					} //end if
					else
					{
						try
						{
							sleep(burstspeed * 1000);
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
						}
						//						 eclipse way to get thread access to the UI
						if (display.isDisposed())
							return;
						display.syncExec(new Runnable()
						{
							public void run()
							{
								DataProvider.getInstance().incrementReplayPosition();
							}
						});
						break;
					} // end else
				} // end while
				//System.out.println("isEndOfReplay(): " + DataProvider.getDataProvider().getActiveReplay().isEndOfReplay());
				if (DataProvider.getInstance().getActiveReplay().isEndOfReplay())
				{
					display.syncExec(new Runnable()
					{
						public void run()
						{
							stopReplay();
						}
					});
				}
			}
		}
	}
}
