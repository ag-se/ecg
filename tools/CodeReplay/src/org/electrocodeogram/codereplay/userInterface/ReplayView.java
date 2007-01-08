package org.electrocodeogram.codereplay.userInterface;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.electrocodeogram.codereplay.dataProvider.DataProvider;
import org.electrocodeogram.codereplay.dataProvider.IModelChangeListener;
import org.electrocodeogram.codereplay.dataProvider.ModelChangeEvent;
import org.electrocodeogram.codereplay.pluginControl.ElementListContentProvider;
import org.electrocodeogram.codereplay.pluginControl.IReplayActionListener;
import org.electrocodeogram.codereplay.pluginControl.ReplayActionEvent;
import org.electrocodeogram.codereplay.pluginControl.ReplayControl;
import org.electrocodeogram.codereplay.pluginControl.TextViewContentProvider;

/**
 * This class represents the TextView of the GUI part of this plugin.
 * So, all GUI elements in the right hand view are created here, including the textviewer.
 * 
 * @author marco kranz
 */
public class ReplayView extends ViewPart implements IModelChangeListener{
	
	// replay editor
	private ReplayEditor replayview;
	// next element
	private Button nextElement;
	// previous element
	private Button previousElement;
//	   start button
	private Button beginButton;
	// stop button
	private Button endButton;
//	   first element
	private Button firstElement;
	// last element
	private Button lastElement;
	private TreeViewer elementSelector;
	private ElementListContentProvider elemlist_contentprov;
	// indicates if replay is running
	private boolean running = false;
	
	/**
	 * The constructor.
	 */
	public ReplayView() {
		elemlist_contentprov = new ElementListContentProvider();
		DataProvider.getInstance().addModelChangeListener(this);
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		
		// -------------------
		// parent
		// -------------------
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		parent.setLayout(gridLayout);
		
		// -------------------
		// editor window group
		// -------------------
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		Group group1 = new Group(parent, SWT.SHADOW_NONE);
		group1.setLayout(gridLayout);
		group1.setLayoutData(gridData);
		
		// ---------------------
		// control element group
		// ---------------------	
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		Group group2 = new Group(parent, SWT.SHADOW_NONE);
		group2.setLayoutData(gridData);
		group2.setLayout(gridLayout);
		
		// ----------
		// group1
		// ----------
//		--------------------		
//		 text editor
//		--------------------
		replayview = new ReplayEditor(group1);
		
		// --------------------
		// element selector
		// --------------------
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.widthHint = 135;
		
		elementSelector = new TreeViewer(group1, SWT.H_SCROLL | SWT.V_SCROLL);
		Tree treeWidget = elementSelector.getTree();
		treeWidget.setLayoutData(gridData);
		elementSelector.setContentProvider(elemlist_contentprov);
		elementSelector.setLabelProvider(elemlist_contentprov);
		elementSelector.setSorter(new ViewerSorter());
		elementSelector.setInput(getViewSite());
		elementSelector.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				ISelection selection = event.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				ReplayControl.getInstance().setSelectedElement(obj);
			}
		});

		
		// -----------------------------------
		// group2, buttons and labels
		// -----------------------------------
		Composite buttons = new Composite(group2, SWT.EMBEDDED);
		Composite labels = new Composite(group2, SWT.EMBEDDED);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 8;
		buttons.setLayout(gridLayout);
		buttons.setLayoutData(gridData);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridLayout = new GridLayout();
		gridLayout.numColumns = 6;
		labels.setLayout(gridLayout);
		labels.setLayoutData(gridData);
		
//	    --------------------		
//		 button begin of replay
//		--------------------
		firstElement = new Button(buttons, SWT.PUSH);
		firstElement.setText("First Element");
		firstElement.setToolTipText("Jump to first element.");
		firstElement.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				ReplayControl.getInstance().jumpToFirstElement();
			}
		});
		
//		--------------------		
//		 button previous element
//		--------------------
		previousElement = new Button(buttons, SWT.PUSH);
		previousElement.setText("Previous Element");
		previousElement.setToolTipText("Jump to previous element.");
		previousElement.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				ReplayControl.getInstance().stepToPreviousElement();
			}
		});
		
		
//		--------------------		
//		 button start replay
//		--------------------
		beginButton = new Button(buttons, SWT.TOGGLE);
		beginButton.setText("Start Replay");
		beginButton.setToolTipText("Press to start Replay.");
		beginButton.setSelection(running);
		beginButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				if(!running){
					if(!ReplayControl.getInstance().isEndOfReplay()){
						ReplayControl.getInstance().startReplay();
						beginButton.setText("Stop Replay");
						beginButton.setToolTipText("Press to stop Replay.");
						running = !running;
					}
					else
						beginButton.setSelection(false);
				}
				else{
					ReplayControl.getInstance().stopReplay();
				}
			}
		});
		ReplayControl.getInstance().addReplayActionListener(new IReplayActionListener(){
			public void ReplayAction(ReplayActionEvent event) {
				if(event.getCause() == ReplayActionEvent.REPLAY_STOPPED){
					beginButton.setSelection(false);
					beginButton.setText("Start Replay");
					beginButton.setToolTipText("Press to start Replay.");
					running = !running;
				}
			}
		});				
		
//		--------------------		
// 		button next element
//		--------------------
		nextElement = new Button(buttons, SWT.PUSH);
		nextElement.setText("Next Element");
		nextElement.setToolTipText("Jump to next element.");
		nextElement.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				ReplayControl.getInstance().stepToNextElement();
			}
		});
		
//	    --------------------		
//		 button end of replay
//		--------------------
		lastElement = new Button(buttons, SWT.PUSH);
		lastElement.setText("Last Element");
		lastElement.setToolTipText("Jump to last element.");
		lastElement.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				ReplayControl.getInstance().jumpToLastElement();
			}
		});
		
//		 ---------------------
		// Replamode selection
		// ---------------------
		Combo replayModeSelection = new Combo(buttons, SWT.DROP_DOWN | SWT.READ_ONLY);
		replayModeSelection.add("Realtime Mode", 0);
		replayModeSelection.add("Burst Mode", 1);
		replayModeSelection.select(1);
		ReplayControl.getInstance().setReplayMode(ReplayControl.BURST);
		replayModeSelection.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				if(((Combo)e.widget).getSelectionIndex() == 0)
					ReplayControl.getInstance().setReplayMode(ReplayControl.REALTIME);
				else
					ReplayControl.getInstance().setReplayMode(ReplayControl.BURST);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
//		 ------------------
		// textlabel Speed selection
		// ------------------
		Label speedBurstModeText = new Label(buttons, SWT.HORIZONTAL | SWT.CENTER);
		speedBurstModeText.setFont(new Font(parent.getDisplay(), new FontData(parent.getFont().getFontData().toString(), 10, SWT.NORMAL)));
		speedBurstModeText.setText(" Speed selection: ");
		
		
		Combo burstSpeedSelection = new Combo(buttons, SWT.DROP_DOWN | SWT.READ_ONLY);
		burstSpeedSelection.add("1 Sec.", 0);
		burstSpeedSelection.add("2 Sec.", 1);
		burstSpeedSelection.add("3 Sec.", 2);
		burstSpeedSelection.add("4 Sec.", 3);
		burstSpeedSelection.add("5 Sec.", 4);
		burstSpeedSelection.select(0);
		ReplayControl.getInstance().setBurstSpeed(1);
		burstSpeedSelection.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				ReplayControl.getInstance().setBurstSpeed(((Combo)e.widget).getSelectionIndex()+1);
			}
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		
//		 ------------------
		// textlabel
		// ------------------
		Label timeToNextStep = new Label(labels, SWT.HORIZONTAL | SWT.CENTER);
		timeToNextStep.setFont(new Font(parent.getDisplay(), new FontData(parent.getFont().getFontData().toString(), 10, SWT.NORMAL)));
		timeToNextStep.setText("  Time to next step:  ");
		
//		 --------------------------
		// time until next step label
		// --------------------------
		final Label timeToNextStepLabel = new Label(labels, SWT.HORIZONTAL | SWT.CENTER | SWT.BORDER);
		timeToNextStepLabel.setFont(new Font(parent.getDisplay(), new FontData(parent.getFont().getFontData().toString(), 12, SWT.NORMAL)));
		timeToNextStepLabel.setText(" "+new SimpleDateFormat("mm:ss").format(new Date(0))+" ");
		timeToNextStepLabel.setBackground(new Color(parent.getDisplay(), 255, 255, 255));
		ReplayControl.getInstance().addReplayActionListener(new IReplayActionListener(){
			public void ReplayAction(ReplayActionEvent event) {
				if(event.getCause() == ReplayActionEvent.COUNTDOWN_CHANGED && ReplayControl.getInstance().isTimerRunning()){
					Date newTime = ReplayControl.getInstance().getTimeUntilNextStep();
					timeToNextStepLabel.setText(" "+new SimpleDateFormat("mm:ss").format(newTime)+" ");
				}
			}
		});
		DataProvider.getInstance().addModelChangeListener(new IModelChangeListener(){
			public void modelChange(ModelChangeEvent event) {
				if(event.getCause() == ModelChangeEvent.REPLAY_CHANGED || event.getCause() == ModelChangeEvent.ELEMENT_CHANGED)
					timeToNextStepLabel.setText(new SimpleDateFormat("mm:ss").format(new Date(0)));
			}
		});
		
		
		// ------------------
		// textlabel
		// ------------------
		Label timeStampText = new Label(labels, SWT.HORIZONTAL | SWT.CENTER);
		timeStampText.setFont(new Font(parent.getDisplay(), new FontData(parent.getFont().getFontData().toString(), 10, SWT.NORMAL)));
		timeStampText.setText("  Timestamp:  ");
		
		// ---------------------
		// Timestamp label
		// ---------------------
		final Label timestamplabel = new Label(labels, SWT.HORIZONTAL | SWT.CENTER | SWT.BORDER);
		timestamplabel.setFont(new Font(parent.getDisplay(), new FontData(parent.getFont().getFontData().toString(), 12, SWT.NORMAL)));
		timestamplabel.setText("      < Timestamp >      ");
		timestamplabel.setSize(timestamplabel.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		timestamplabel.setBackground(new Color(parent.getDisplay(), 255, 255, 255));
		DataProvider.getInstance().addModelChangeListener(new IModelChangeListener(){
			public void modelChange(ModelChangeEvent event) {
				if(event.getCause() == ModelChangeEvent.ELEMENT_CHANGED){
					String timestamp = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(DataProvider.getInstance().getActiveReplay().getCurrentElement().getTimestamp());
					timestamplabel.setText(" "+timestamp+" ");
					timestamplabel.setSize(timestamplabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
				}
				else if(event.getCause() == ModelChangeEvent.NEW_ELEMENT)
					timestamplabel.setText("                 ");
			}
		});
		

//		 ------------------
		// textlabel
		// ------------------
		Label changeTypeText = new Label(labels, SWT.HORIZONTAL | SWT.CENTER);
		changeTypeText.setFont(new Font(parent.getDisplay(), new FontData(parent.getFont().getFontData().toString(), 10, SWT.NORMAL)));
		changeTypeText.setText(" Change cause : ");
		
		// ---------------------
		// Timestamp label
		// ---------------------
		final Label changeTypeLabel = new Label(labels, SWT.HORIZONTAL | SWT.CENTER | SWT.BORDER);
		changeTypeLabel.setFont(new Font(parent.getDisplay(), new FontData(parent.getFont().getFontData().toString(), 12, SWT.NORMAL)));
		changeTypeLabel.setText("   < Cause >   ");
		changeTypeLabel.setSize(changeTypeLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		changeTypeLabel.setBackground(new Color(parent.getDisplay(), 255, 255, 255));
		DataProvider.getInstance().addModelChangeListener(new IModelChangeListener(){
			public void modelChange(ModelChangeEvent event) {
				if(event.getCause() == ModelChangeEvent.ELEMENT_CHANGED){
					changeTypeLabel.setText(" "+ReplayControl.getInstance().getExactCause()+" ");
					changeTypeLabel.setSize(timestamplabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
				}
				else if(event.getCause() == ModelChangeEvent.NEW_ELEMENT)
					changeTypeLabel.setText("                 ");
			}
		});
	}				// end of createPartControl
	
	

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		replayview.setFocus();
	}
	
	
//	 implementation of IModelChangeListener, if model has changed, refresh view
	public void modelChange(ModelChangeEvent event){
		int cause = event.getCause();
		if(cause == ModelChangeEvent.REPLAY_CHANGED){
			//System.out.println("ReplayView.modelChange: cause REPLAY_CHANGED"); 
			elementSelector.setInput(event);
		}
		else if(cause == ModelChangeEvent.ELEMENT_CHANGED){
			Object[] elem = {elemlist_contentprov.getCurrentElement()}; 
			elementSelector.setSelection(new StructuredSelection(elem), true);
			elementSelector.reveal(elemlist_contentprov.getCurrentElement());
		}
	}
	
	
	
	// maintaining state of the editor window
	private class ReplayEditor implements IModelChangeListener{

		// the StyledText text viewer
		private StyledText textviewer;
		// editor font size
		private int fontsize = 10;
		// font
		private Font font = null;
		// content provider
		private TextViewContentProvider contentprovider;
		
		
		public ReplayEditor(Group group){
			DataProvider.getInstance().addModelChangeListener(this);
			GridData gridData = new GridData();
			gridData.horizontalSpan = 1;
			gridData.horizontalAlignment = SWT.FILL;
			gridData.verticalAlignment = SWT.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.grabExcessVerticalSpace = true;
			textviewer = new StyledText(group, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
			textviewer.setLayoutData(gridData);
			textviewer.setEditable(false);
			font = new Font(textviewer.getDisplay(), "Courier New", fontsize, SWT.NORMAL);
			textviewer.setFont(font);
			contentprovider = new TextViewContentProvider(textviewer.getDisplay());
		}
		
		
		public void setFontSize(int size){
			fontsize = size;
			textviewer.setFont(font);
		}
		
		
		public void setFocus(){
			textviewer.setFocus();
		}

		// implementation of IModelChangeListener
		public void modelChange(ModelChangeEvent event) {
			if(event.getCause() == ModelChangeEvent.ELEMENT_CHANGED){
				// get text from contentprovider
				textviewer.setText(contentprovider.getText());
				// get styleranges from contentprovider
				StyleRange[] styleranges = contentprovider.getStyleRanges();
				for(int i = 0; i < styleranges.length; i++){
					if(styleranges[i].length > 0)
						textviewer.setStyleRange(styleranges[i]);
					else 
						textviewer.setLineBackground(textviewer.getLineAtOffset(styleranges[i].start),1,styleranges[i].background);
				}
				textviewer.setSelection(styleranges[styleranges.length-1].start + styleranges[styleranges.length-1].length);
			}
		}
	}
}