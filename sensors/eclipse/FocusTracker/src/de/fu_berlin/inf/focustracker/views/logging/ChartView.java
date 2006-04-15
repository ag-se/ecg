package de.fu_berlin.inf.focustracker.views.logging;


import java.awt.Frame;
import java.awt.GridLayout;
import java.util.Date;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.part.ViewPart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeTableXYDataset;
import org.jfree.ui.RectangleInsets;

import de.fu_berlin.inf.focustracker.EventDispatcher;
import de.fu_berlin.inf.focustracker.interaction.Interaction;
import de.fu_berlin.inf.focustracker.interaction.InteractionListener;
import de.fu_berlin.inf.focustracker.interaction.JavaInteraction;
import de.fu_berlin.inf.focustracker.repository.InteractionRepository;
import de.fu_berlin.inf.focustracker.resources.FocusTrackerResources;


public class ChartView extends ViewPart implements InteractionListener {

	public static final String ID = "de.fu_berlin.inf.focustracker.views.logging.ChartView";
	
	private static final double RANGE = 300 * 1000;
	
	private ChartFilter filter = new ChartFilter();
	private TimeTableXYDataset xyDataset;

	// actions
	private FiltersAction filtersAction;
	private Action clearAction;
	private JFreeChart chart;

	private ChartPanel cp;
	
	public static ChartView findMe() {
		return (ChartView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(ChartView.ID);
	}
	
	@Override
	public void createPartControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.EMBEDDED);
		composite.setLayout(new FillLayout());
		EventDispatcher.getInstance().addListener(this);

		makeActions();
		fillAcionBars();
//		setContentDescription("Please select a filter!");

		createChart(composite);
	}

	private void createChart(Composite aParent) {
		

		
		initChart();
		
		// grab a new AWT frame from our shell
		Frame chartFrame = SWT_AWT.new_Frame(aParent);
		
		// set the layout of our frame to a GridLayout so the chart will
		// automatically fill the entire area
		chartFrame.setLayout(new GridLayout());
		cp = new ChartPanel(chart);
		chartFrame.add(cp);
		cp.setMouseZoomable(true);
		cp.setDisplayToolTips(true);
		
	}
	
	private void initChart() {
		xyDataset = new TimeTableXYDataset();
//		xyDataset.setDomainIsPointsInTime(true);
		chart = ChartFactory.createTimeSeriesChart(null, null, null, xyDataset, true, true, false);

		// x-axis
		chart.getXYPlot().getDomainAxis().setRange(System.currentTimeMillis(), System.currentTimeMillis() + RANGE);
		chart.getXYPlot().getDomainAxis().setFixedAutoRange(RANGE);
		chart.getXYPlot().getDomainAxis().setAutoRange(true);
		
		// y-axis
		chart.getXYPlot().getRangeAxis().setRange(0, 1);
//		chart.getXYPlot().getDomainAxis().setAutoRange(false);
		
		
//		chart.getXYPlot().getRenderer().setBaseItemLabelGenerator(new StandardXYItemLabelGenerator());
//		chart.getXYPlot().getRenderer().setToolTipGenerator(new StandardXYToolTipGenerator());
		chart.getXYPlot().setAxisOffset(new RectangleInsets(2.0, 2.0, 2.0, 2.0));
		
//        XYItemRenderer r = chart.getXYPlot().getRenderer();
//        if (r instanceof XYLineAndShapeRenderer) {
//            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
//            renderer.setShapesVisible(true);
//            renderer.setShapesFilled(true);
//        }
		
		if(cp != null) {
			cp.setChart(chart);
		}
		
	}
	
	
	private void makeActions() {
        // filters...
        filtersAction = new FiltersAction(this, "filter"); //$NON-NLS-1$
        filtersAction.setText("Filter");
        filtersAction.setToolTipText("Filter");
        filtersAction.setImageDescriptor(IDEWorkbenchPlugin.getIDEImageDescriptor("elcl16/filter_ps.gif"));//$NON-NLS-2$//$NON-NLS-1$); //$NON-NLS-1$

		clearAction = new Action() {
			public void run() {
				synchronized (xyDataset) {
//					xyDataset = new TimeTableXYDataset();
//					chart.getXYPlot().setDataset(xyDataset);
					initChart();
				}
			}
		};
		clearAction.setText("Clear");
		clearAction.setToolTipText("Clear");
		clearAction.setImageDescriptor(FocusTrackerResources.CLEAR);
        
	}
	
	private void fillAcionBars() {
        IActionBars actionBars = getViewSite().getActionBars();
        IToolBarManager toolBar = actionBars.getToolBarManager();
        toolBar.add(filtersAction);
        toolBar.add(clearAction);
	}
	
	@Override
	public void setFocus() {
	}

	public void notifyInteractionObserved(Interaction aInteraction) {
		
		if (aInteraction instanceof JavaInteraction) {
			Display.getDefault().asyncExec(new Updater((JavaInteraction) aInteraction));
		}
	}
	
	@Override
	public void dispose() {
		EventDispatcher.getInstance().removeListener(this);
	}
	
	class Updater implements Runnable {

		private JavaInteraction javaInteraction;
		Updater(JavaInteraction aJavaInteraction) {
			javaInteraction = aJavaInteraction;
		}
		public void run() {
			if(filter != null && filter.getJavaElements().contains(javaInteraction.getJavaElement())) {
				// check for time delta, add point with last severity if necessary
				JavaInteraction lastInteraction = (JavaInteraction)javaInteraction.getLastInteraction();
				if(lastInteraction != null && javaInteraction.getDate().getTime() - lastInteraction.getDate().getTime() > 1000 ) {
					Second second = new Second(new Date(javaInteraction.getDate().getTime() - 1000));
					xyDataset.add(second, lastInteraction.getSeverity(), lastInteraction.getJavaElement().getElementName());
				}
				
				Second second = new Second(javaInteraction.getDate());  
				xyDataset.add(second, javaInteraction.getSeverity(), javaInteraction.getJavaElement().getElementName());

				// add annotation, if this action has an assigned annotation
				String annotationText = ChartAnnotation.getChartAnnotation(javaInteraction.getAction());
				if(annotationText != null) {
					XYPointerAnnotation annotation = new XYPointerAnnotation(annotationText, second.getFirstMillisecond(), javaInteraction.getSeverity(), 3*Math.PI/4); 
					annotation.setTipRadius(0);
					annotation.setBaseRadius(20);
					chart.getXYPlot().addAnnotation(annotation);
				}
			}
		}
	}

	public ChartFilter getFilter() {
		return filter;
	}

	public void filterChanged() {
		System.err.println("filterChanged");
		String contentString = "";
		for (IJavaElement element : filter.getJavaElements()) {
			contentString += " " + element.getElementName();
		}
//		setContentDescription("filtering for: " + contentString );
	}
	
}
