package de.fu_berlin.inf.focustracker.monitor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.javaeditor.ClassFileEditor;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IAnnotationModelListenerExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;

import de.fu_berlin.inf.focustracker.EventDispatcher;
import de.fu_berlin.inf.focustracker.FocusTrackerPlugin;
import de.fu_berlin.inf.focustracker.SeverityHelper;
import de.fu_berlin.inf.focustracker.interaction.Action;
import de.fu_berlin.inf.focustracker.interaction.JavaInteraction;
import de.fu_berlin.inf.focustracker.interaction.Origin;
import de.fu_berlin.inf.focustracker.monitor.helper.RegionHelper;
import de.fu_berlin.inf.focustracker.rating.RatingException;
import de.fu_berlin.inf.focustracker.rating.event.ElementFoldingEvent;
import de.fu_berlin.inf.focustracker.rating.event.ElementRegion;
import de.fu_berlin.inf.focustracker.rating.event.ElementVisibiltyEvent;
import de.fu_berlin.inf.focustracker.repository.Element;
import de.fu_berlin.inf.focustracker.repository.InteractionRepository;
import de.fu_berlin.inf.focustracker.ui.preferences.PreferenceConstants;
import de.fu_berlin.inf.focustracker.util.Units;

public class JavaEditorMonitor extends AbstractFocusTrackerMonitor implements 
		IViewportListener, ITextListener, MouseMoveListener, IPropertyChangeListener, KeyListener {

	private static final long CODECHANGE_INTERVAL = Units.SECOND / 2;
	private static final long SCROLLING_INTERVAL = Units.SECOND;
	private static final long DELAY_BETWEEN_MOUSE_OVER_DETECTION = 1 * Units.SECOND;

	JavaEditor editor;

	private ProjectionAnnotationModel projectionAnnotationModel;
	private FoldingListener foldingListener;
	private List<IJavaElement> visibleElements = new ArrayList<IJavaElement>();
	private Timer delayedTextChangedTimer = new Timer(); 
	private Timer delayedViewPortChangedTimer = new Timer(); 
	private Origin origin = Origin.JAVAEDITOR;
	private IJavaElement input;
	private MouseMoveHolder lastMouseMove = null;
	private boolean mouseMoveListenerEnabled = isMouseMoveListenerEnabled();
	private StyledText textWidget;
//	private int textWidgetCaretOffset = 0;
	private IJavaElement compilationUnit;
	
	
	public JavaEditorMonitor() {

	}

//	public synchronized void selectionChanged(SelectionChangedEvent aEvent) {
		//
		// // IEditorPart editorPart =
		// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		// // JavaEditor javaEditor = (CompilationUnitEditor) editorPart;
		// // System.err.println("source: " + aEvent.getSource());
		// try {
		// IJavaElement javaElement =
		// SelectionConverter.resolveEnclosingElement((JavaEditor) editor,
		// (ITextSelection)aEvent.getSelection());
		//
		// // if(lastElementAndTimestamp != null &&
		// lastElementAndTimestamp.getElement() == javaElement &&
		// lastElementAndTimestamp.getTimeStamp() > System.currentTimeMillis() -
		// 100) {
		// // System.err.println("sth went wrong, access to fast!");
		// // return;
		// // }
		// if(editor.getViewer().getRangeIndication() != null) {
		// System.err.println("rangeind offset: " +
		// editor.getViewer().getRangeIndication().getOffset() + " length: " +
		// editor.getViewer().getRangeIndication().getLength());
		// }
		//			
		// System.err.println("indexoffset: " +
		// editor.getViewer().getTopIndexStartOffset() + " - " +
		// editor.getViewer().getBottomIndexEndOffset());
		// System.err.println("index: " + editor.getViewer().getTopIndex() + " -
		// " + editor.getViewer().getBottomIndex());
		// System.err.println("visible offset: " +
		// editor.getViewer().getVisibleRegion().getOffset() + " length: " +
		// editor.getViewer().getVisibleRegion().getLength());
		// // System.err.println("element offset: " +
		// ((ISourceReference)javaElement).getSourceRange().getOffset() + "
		// length: " +
		// ((ISourceReference)javaElement).getSourceRange().getLength());
		// System.err.println("element offset: " +
		// ((ISourceReference)javaElement).getSourceRange().getOffset() + "
		// length: " +
		// ((ISourceReference)javaElement).getSourceRange().getLength());
		//			
		// // rate neighbours
		// List<IJavaElement> neighbours = findNeighbours(javaElement);
		// for (IJavaElement neighbourElement : neighbours) {
		// double lastScore =
		// InteractionRepository.getInstance().getLastScore(neighbourElement);
		// double newScore =
		// SeverityHelper.calculateSeverity(Action.NEIGHBOUR_SELECTED,
		// lastScore);
		// JavaInteraction neighbourInteraction = new
		// JavaInteraction(Action.NEIGHBOUR_SELECTED, neighbourElement,
		// newScore, new Date(), null, Origin.JAVAEDITOR);
		// EventDispatcher.getInstance().notifyInteractionObserved(neighbourInteraction);
		// }
		//		
		// // rate selection
		// JavaInteraction interaction = new
		// JavaInteraction(Action.SELECTION_CHANGED, javaElement, 1f, new
		// Date(), null, Origin.JAVAEDITOR);
		// EventDispatcher.getInstance().notifyInteractionObserved(interaction);
		//			
		// } catch (JavaModelException e) {
		// e.printStackTrace();
		// }
		//		
//	}

	// private List<IJavaElement> findNeighbours(IJavaElement aJavaElement)
	// throws JavaModelException {
	// List<IJavaElement> elements =
	// Arrays.asList(((JavaElement)aJavaElement.getParent()).getChildren());
	// List<IJavaElement> neighbours = new ArrayList<IJavaElement>();
	//		
	// int elementIndex = elements.indexOf(aJavaElement);
	// if(elementIndex > 0) {
	// neighbours.add(elements.get(elementIndex - 1));
	// }
	// if(elementIndex < elements.size() - 1) {
	// neighbours.add(elements.get(elementIndex + 1));
	// }
	// // System.err.println("neighbours of: " + aJavaElement + " - " +
	// neighbours.toString());
	// // new Exception().printStackTrace();
	// return neighbours;
	//		
	// }

	public void viewportChanged(final int aVerticalOffset) {

		delayedViewPortChangedTimer.cancel();
		delayedViewPortChangedTimer = new Timer();
		delayedViewPortChangedTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						handleViewportChanged(aVerticalOffset);
					}
				});
			}
			
		}, SCROLLING_INTERVAL);
	}
	private void handleViewportChanged(int aVerticalOffset) {
		try {
			// editor could have been closed meanwhile
			if(editor == null || editor.getViewer() == null) {
				return;
			}
			
			int topOffset = editor.getViewer().getDocument()
					.getLineInformation(editor.getViewer().getTopIndex())
					.getOffset();
			IRegion lineInformation = editor.getViewer().getDocument()
					.getLineInformation(editor.getViewer().getBottomIndex());
			int bottomOffset = lineInformation.getOffset()
					+ lineInformation.getLength();

			Map<Boolean, List<IJavaElement>> elementsInEditor = new HashMap<Boolean, List<IJavaElement>>();
			elementsInEditor.put(true, new ArrayList<IJavaElement>());
			elementsInEditor.put(false, new ArrayList<IJavaElement>());
			
			// iterate through all java elements, to determine their status of visibility
//			int numberOfElementsVisible = 0;
			IJavaElement element = JavaUI.getEditorInputJavaElement(editor.getEditorInput());
			if (element instanceof IParent) {
				IParent compilationUnit = (IParent) element;
				List<IJavaElement> allChildren = getAllChildren(compilationUnit);
				List<JavaInteraction> interacList = new ArrayList<JavaInteraction>();
				for (IJavaElement child : allChildren) {
					// ignore imports and class, since the opened class is always visible
					if (!ignoreElement(child)) {
						ISourceReference ref = (ISourceReference) child;
						// is this element visible?
						boolean visible = ref.getSourceRange().getOffset()
								+ ref.getSourceRange().getLength() >= topOffset
								&& ref.getSourceRange().getOffset() <= bottomOffset;
						elementsInEditor.get(visible).add(child);
						}
				}
				int numberOfElementsVisible = elementsInEditor.get(true).size();
//System.err.println("#elementsVisible: " + numberOfElementsVisible);
//System.err.println(elementsInEditor.get(true));
				for (Boolean visible : elementsInEditor.keySet()) {
					for (IJavaElement child : elementsInEditor.get(visible)) {
						
						boolean wasVisibleBefore = visibleElements.contains(child);
						double lastSeverity = InteractionRepository.getInstance().getRating(child);
						Action action = null;
						
						ElementRegion elementRegion = RegionHelper.getElementRegion(editor, child);
						ElementVisibiltyEvent visibiltyEvent = new ElementVisibiltyEvent(
								action, element, visible, foldingListener.isCollapsed(element), elementRegion, numberOfElementsVisible, null);
						
						if (visible && !wasVisibleBefore) {
							visibleElements.add(child);
							action = Action.VISIBILITY_GAINED;
						} else if (!visible && lastSeverity > 0d /* && wasVisibleBefore*/) {
							visibleElements.remove(child);
							action = Action.VISIBILITY_LOST;
						} else if (visible && wasVisibleBefore && (lastSeverity != EventDispatcher.getInstance()
								.getRating().rateEvent(visibiltyEvent))) {
							action = Action.VISIBILITY_CHANGED;
						} else {
	
						}
						visibiltyEvent.setAction(action);
						
						if (action != null) {
							//						ElementRegion elementRegion = RegionHelper.getElementRegion(editor, child);
	//						ElementVisibiltyEvent visibiltyEvent = new ElementVisibiltyEvent(
	//								action, element, visible, foldingListener.isCollapsed(element), elementRegion, null);
							
							JavaInteraction interaction = new JavaInteraction(
									action, child, EventDispatcher.getInstance()
											.getRating().rateEvent(visibiltyEvent),
									new Date(), null, origin);
							interacList.add(interaction);
						}
					}

				}
				
				EventDispatcher.getInstance().notifyInteractionObserved(interacList);
			}

		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RatingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static List<IJavaElement> getAllChildren(IParent parentElement) {
		List<IJavaElement> allChildren = new ArrayList<IJavaElement>();
		try {
			for (IJavaElement child : parentElement.getChildren()) {
				allChildren.add(child);
				if (child instanceof IParent) {
					allChildren.addAll(getAllChildren((IParent) child));
				}
			}
		} catch (JavaModelException e) {
			// ignore failures
		}
		return allChildren;
	}

	public void textChanged(TextEvent aEvent) {
		
//		System.err.println("textChanged: " + aEvent.getDocumentEvent().getText() + " - " + aEvent.getDocumentEvent().getText().length());
//		if(aEvent.getReplacedText() == null || aEvent.getReplacedText().length() == 0) {
//			// no text was changed, eg a cursor key could have been pressed
//			return;
//		}
		
		// this code is heavyly inspired by the ECGEclipseSensor...
		delayedTextChangedTimer.cancel();
		delayedTextChangedTimer = new Timer();
		delayedTextChangedTimer.schedule(new DelayedTimerTask(aEvent.getOffset()), CODECHANGE_INTERVAL);
		
	}

	class DelayedTimerTask extends TimerTask {

		private int textChangedOffset;
		protected DelayedTimerTask(int aTextChangedOffset) {
			textChangedOffset = aTextChangedOffset;
		}
		
		@Override
		public void run() {
			// editor could have been closed in the meanwhile
			if(editor != null) {
				try {
	//				System.err.println("tc offset: " + textChangedOffset + " - " + unit.getElementAt(textChangedOffset));
					IJavaElement javaElement;
					if(origin == Origin.JAVAEDITOR) {
						javaElement = ((ICompilationUnit)compilationUnit).getElementAt(textChangedOffset);
					} else {
						javaElement = ((IClassFile)compilationUnit).getElementAt(textChangedOffset);
					}
					if(!JavaEditorMonitor.ignoreElement(javaElement)) {
						JavaInteraction interaction = new JavaInteraction(Action.TEXT_CHANGED, javaElement, 1d, origin);
						EventDispatcher.getInstance().notifyInteractionObserved(interaction);
					}
				} catch (JavaModelException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public void deregisterFromPart() {
//		editor.getSelectionProvider().removeSelectionChangedListener(this);
		editor.getViewer().removeViewportListener(this);
		editor.getViewer().removeTextListener(this);
		editor.getViewer().getTextWidget().removeMouseMoveListener(this);
		projectionAnnotationModel
				.removeAnnotationModelListener(foldingListener);
	}

	public void registerPart(IWorkbenchPart aPart) {
		if (aPart instanceof JavaEditor) {
			editor = (JavaEditor) aPart;
//			editor.getSelectionProvider().addSelectionChangedListener(this);
			editor.getViewer().addViewportListener(this);
			editor.getViewer().addTextListener(this);
			textWidget = editor.getViewer().getTextWidget();
			textWidget.addKeyListener(this);
			
			if (aPart instanceof CompilationUnitEditor) {
				origin = Origin.JAVAEDITOR;
				compilationUnit = (ICompilationUnit)EditorUtility.getEditorInputJavaElement(editor, false);
			} else if (aPart instanceof ClassFileEditor) {
				origin = Origin.JAVA_CLASSFILE_EDITOR;
				compilationUnit = (IClassFile)EditorUtility.getEditorInputJavaElement(editor, false);
			}
//			compilationUnit = (ICompilationUnit)EditorUtility.getEditorInputJavaElement(editor, false);

			// create a class opened event
//			EventDispatcher.getInstance().notifyInteractionObserved(
//					new JavaInteraction(Action.CLASS_OPENED, compilationUnit, 1d, Origin.JAVAEDITOR)
//					);
			InteractionRepository.getInstance().add(
					new JavaInteraction(Action.CLASS_OPENED, compilationUnit, 1d, Origin.JAVAEDITOR)
			);

			// check folding
			projectionAnnotationModel = (ProjectionAnnotationModel) editor.getAdapter(ProjectionAnnotationModel.class);
			foldingListener = new FoldingListener(origin);
			projectionAnnotationModel
					.addAnnotationModelListener(foldingListener);

			editor.getViewer().getTextWidget().addMouseMoveListener(this);
			input = EditorUtility.getEditorInputJavaElement(editor, true); 
			 
			FocusTrackerPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(this);

		} else {
			throw new IllegalArgumentException("Wrong type of part : "
					+ aPart.getClass());
		}
	}

	public FoldingListener getFoldingListener() {
		return foldingListener;
	}

	public void mouseMove(MouseEvent aE) {
		
		if(!mouseMoveListenerEnabled) {
			return;
		}
		
		Point point = new Point(aE.x, aE.y);
//		Rectangle rect = layout.getLineBounds(layout.getLineIndex(offsetInLine));

//		int lineIndex = editor.getViewer().getTextWidget().getLineIndex(point.y);
//		editor.get
		
		
//		Rectangle rect = editor.getViewer().getTextWidget(). // getTextBounds(). getBounds(); // layout.getLineIndex(offsetInLine));
//		if (!rect.contains(point)) {
//			System.err.println("PPPPPPPPPPPPPPPPPP");
//		}
		
		int offset = editor.getViewer().getTextWidget().getOffsetAtLocation(point);
		offset = widgetOffset2ModelOffset(editor.getViewer(), offset);
		try {
//			System.err.println("mouseMove: " + point + " - " + offset + " element: " + getElementAtOffset(offset).getElementName());
//			System.err.println("mouseMove: " + getElementAtOffset(offset).getElementName());
			long eventReceivedTs = System.currentTimeMillis();
			IJavaElement javaElement = getElementAtOffset(offset);

			System.err.println( "######################## " + javaElement);
			if(lastMouseMove == null || lastMouseMove.getJavaElement() != javaElement) {
				lastMouseMove = new MouseMoveHolder(javaElement, eventReceivedTs);
				return;
			}
			Element element = InteractionRepository.getInstance().getElements().get(javaElement);
			double rating = 0.01d;
			if(element != null) {
				rating += element.getRating();
			}
			
			if(lastMouseMove.getEventReceivedTs() < eventReceivedTs - DELAY_BETWEEN_MOUSE_OVER_DETECTION 
//					&& 
//					(element.getLastInteraction() == null || 
//					 element.getLastInteraction().getAction() != Action.MOUSE_OVER || 
//					 (element.getLastInteraction().getAction() == Action.MOUSE_OVER && 
//							 element.getLastInteraction().getDate().getTime() < eventReceivedTs - DELAY_BETWEEN_MOUSE_OVER_DETECTION))
					) {
				if (!ignoreElement(javaElement)) {
					EventDispatcher.getInstance().notifyInteractionObserved(
							new JavaInteraction(Action.MOUSE_OVER, element.getJavaElement(), SeverityHelper.adjustSeverity(element.getRating() + 0.1d), origin));
				}
				lastMouseMove.setEventReceivedTs(eventReceivedTs);
			}
		} catch (Throwable e) {
			// is thrown if the mousemove event occured outside of java element, do nothing in this case
//		} catch (JavaModelException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
	}

	public IJavaElement getElementAtOffset(int aOffset) throws JavaModelException {
		if (input instanceof ICompilationUnit) {
			ICompilationUnit cunit= (ICompilationUnit) input;
			JavaModelUtil.reconcile(cunit);
			IJavaElement ref = cunit.getElementAt(aOffset);
			if (ref == null)
				return input;
			else
				return ref;
		} else if (input instanceof IClassFile) {
			IJavaElement ref = ((IClassFile)input).getElementAt(aOffset);
			if (ref == null)
				return input;
			else
				return ref;
		}
		return null;
	}
	
	@Override
	public void partDeactivated() {
//		System.err.println("partDeactivated");
//		partClosed();
	}
	
	
	@Override
	public void partActivated() {
		System.err.println("partActivated : " + editor.getTitle());
	}
	
	@Override
	public void partClosed() {
		
		System.err.println("editor closed : " + editor.getTitle());
		
		IJavaElement element = JavaUI.getEditorInputJavaElement(editor.getEditorInput());
		if (element instanceof IParent) {
			IParent compilationUnit = (IParent) element;
			List<IJavaElement> allChildren = getAllChildren(compilationUnit);
			List<JavaInteraction> interacList = new ArrayList<JavaInteraction>();
			for (IJavaElement child : allChildren) {
				if(InteractionRepository.getInstance().getRating(child) > 0d) {
					JavaInteraction interaction = new JavaInteraction(Action.VISIBILITY_LOST, child, 0d, new Date(), null, origin);
					interacList.add(interaction);
				}
			}
			EventDispatcher.getInstance().notifyInteractionObserved(interacList);
		}
		// create a class closed event
		EventDispatcher.getInstance().notifyInteractionObserved(
				new JavaInteraction(Action.CLASS_CLOSED, compilationUnit, 0d, Origin.JAVAEDITOR)
				);
		
		
	}

	private boolean isMouseMoveListenerEnabled() {
		return FocusTrackerPlugin.getDefault().getPluginPreferences().getBoolean(PreferenceConstants.P_ENABLE_JAVA_EDITOR_MOUSE_MOVE_LISTENER);
	}
	
	public void propertyChange(PropertyChangeEvent aEvent) {
		if(PreferenceConstants.P_ENABLE_JAVA_EDITOR_MOUSE_MOVE_LISTENER.equals(aEvent.getProperty())) {
			mouseMoveListenerEnabled = isMouseMoveListenerEnabled();
		}
	}
	
	class MouseMoveHolder {
		
		private IJavaElement javaElement;
		private long eventReceivedTs;
		
		public MouseMoveHolder(IJavaElement aJavaElement, long aEventReceivedTs) {
			javaElement = aJavaElement;
			eventReceivedTs = aEventReceivedTs;
		}

		public long getEventReceivedTs() {
			return eventReceivedTs;
		}

		public void setEventReceivedTs(long aEventReceivedTs) {
			eventReceivedTs = aEventReceivedTs;
		}

		public IJavaElement getJavaElement() {
			return javaElement;
		}

	}

	// KeyListener, used to listen for cursor moves! 
	public void keyPressed(KeyEvent aE) {
		// do nothing
	}

	public void keyReleased(KeyEvent aE) {
//		try {
//			IJavaElement element = compilationUnit.getElementAt(widgetOffset2ModelOffset(editor.getViewer(), textWidget.getCaretOffset()));
//			JavaInteraction interaction = new JavaInteraction(Action.CURSOR_MOVED, element, 1d, origin);
//			EventDispatcher.getInstance().notifyInteractionObserved(interaction);
//		} catch (JavaModelException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	
	protected final static int widgetOffset2ModelOffset(ISourceViewer viewer, int widgetOffset) {
		if (viewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
			return extension.widgetOffset2ModelOffset(widgetOffset);
		}
		return widgetOffset + viewer.getVisibleRegion().getOffset();
	}
	
	
	public static boolean ignoreElement(IJavaElement aJavaElement) {
		return !(
				aJavaElement instanceof IMember 
				&& !(aJavaElement.getParent() instanceof CompilationUnit)
				);

	}
	
	
}

class FoldingListener implements IAnnotationModelListener,
		IAnnotationModelListenerExtension {

	private List<IJavaElement> collapsedElements = new ArrayList<IJavaElement>();
	private boolean reflectionWorks = true;
	private Origin origin;
	
	public FoldingListener(Origin aOrigin) {
		origin = aOrigin;
	}

	/**
	 * @see org.eclipse.jface.text.source.IAnnotationModelListenerExtension#modelChanged(org.eclipse.jface.text.source.AnnotationModelEvent)
	 */
	public void modelChanged(AnnotationModelEvent aEvent) {

		if (reflectionWorks) {
			for (Annotation annotation : aEvent.getChangedAnnotations()) {
				try {
					if (!(annotation instanceof ProjectionAnnotation)) {
						break;
					}
					Field element = annotation.getClass().getDeclaredField(
							"fJavaElement");
					Field comment = annotation.getClass().getDeclaredField(
							"fIsComment");
					element.setAccessible(true);
					comment.setAccessible(true);
					IJavaElement javaElement = (IJavaElement) element
							.get(annotation);
					boolean isComment = (Boolean) comment.get(annotation);
					boolean isCollapsed = ((ProjectionAnnotation) annotation)
							.isCollapsed();

					if (isCollapsed) {
						collapsedElements.add(javaElement);
					} else {
						collapsedElements.remove(javaElement);
					}

					// ignore comments!
					if (!isComment) {
						Action action;
						if (isCollapsed) {
							action = Action.FOLDING_COLLAPSED;
						} else {
							action = Action.FOLDING_EXPANDED;
						}
						try {
							ElementFoldingEvent foldingEvent = new ElementFoldingEvent(action, javaElement, isCollapsed, null);
							JavaInteraction interaction = new JavaInteraction(
									action, javaElement, EventDispatcher.getInstance().getRating().rateEvent(foldingEvent), new Date(), null,
									origin);
							EventDispatcher.getInstance().notifyInteractionObserved(interaction);
						} catch (RatingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				} catch (SecurityException e) {
					reflectionWorks = false;
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					reflectionWorks = false;
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					reflectionWorks = false;
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					reflectionWorks = false;
					e.printStackTrace();
				}
			}
		}

	}

	boolean isCollapsed(IJavaElement aJavaElement) {
		if(reflectionWorks) {
			return collapsedElements.contains(aJavaElement);
		} else {
			return false;
		}
	}

	/**
	 * @see org.eclipse.jface.text.source.IAnnotationModelListener#modelChanged(org.eclipse.jface.text.source.IAnnotationModel)
	 */
	public void modelChanged(IAnnotationModel aModel) {
		// this method is never called, because modelChanged(AnnotationModelEvent aEvent) is preferred,
		// but IAnnotationModelListener must be implemented, too
	}

}
