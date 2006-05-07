package de.fu_berlin.inf.focustracker.views;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.focustracker.EventDispatcher;
import de.fu_berlin.inf.focustracker.interaction.Interaction;
import de.fu_berlin.inf.focustracker.interaction.JavaElementToStringBuilder;
import de.fu_berlin.inf.focustracker.interaction.JavaInteraction;
import de.fu_berlin.inf.focustracker.repository.InteractionRepository;


public class LoggingView extends BeanView {

	public static final String ID = "de.fu_berlin.inf.focustracker.views.LoggingView";
	
	@Override
	public Class getClazz() {
		return JavaInteraction.class;
	}

	@Override
	public void createPartControl(Composite aParent) {
//		super.createPartControl(aParent);

        table = new Table(aParent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI
                | SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

        viewer = new TableViewer(table);
		
		viewer.setContentProvider(new BeanContentProvider());
//		viewer.setLabelProvider(new BeanLabelProvider(beanInfo));
		viewer.setInput(getViewSite());
		
		getViewer().setSorter(
				new ViewerSorter() {
					Comparator<Interaction> comparator = new Comparator<Interaction>() {
						public int compare(Interaction aO1, Interaction aO2) {
							if (aO1 != null && aO2 != null) {
								return (int)(aO1.getDate().getTime() - aO2.getDate().getTime());
							}
							return 0;
						}
					};
					@Override
					public int compare(Viewer viewer, Object e1, Object e2) {
						return comparator.compare((Interaction)e1, (Interaction)e2);
					}
				}
			);
		EventDispatcher.getInstance().addListener(this);

		TableColumn column = new TableColumn(table, SWT.LEFT);
		column.setText("Time");
		column.setWidth(100);
		column = new TableColumn(table, SWT.LEFT);
		column.setText("Origin");
		column.setWidth(150);
		column = new TableColumn(table, SWT.LEFT);
		column.setText("Action");
		column.setWidth(150);
		column = new TableColumn(table, SWT.LEFT);
		column.setText("Element");
		column.setWidth(300);
		column = new TableColumn(table, SWT.LEFT);
		column.setText("Rating");
		column.setWidth(50);
		
		viewer.setLabelProvider(new TableLabelProvider());
	}
	
	@Override
	public Object[] getObjects() {
		return InteractionRepository.getInstance().getAllInteractions().toArray();
	}
	
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		
		public String getColumnText(Object obj, int index) {
			
//			JavaInteraction interaction = (JavaInteraction)obj;
			Interaction interaction = (Interaction)obj;
			try {
				switch (index) {
				case 0:
					DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS"); // "dd.MM.yyyy HH:mm:ss:SSS"
					return df.format(interaction.getDate());
				case 1:
					return interaction.getOrigin().toString();
				case 2:
					return interaction.getAction().toString();
				case 3:
					if(interaction instanceof JavaInteraction) {
						return JavaElementToStringBuilder.toString(((JavaInteraction)interaction).getJavaElement());
					} else {
						return "";
					}
				case 4:
					return String.valueOf(interaction.getSeverity());
					
				default:
					return "";
				}
//				String prefix = "";
//				Object object = propertyDescriptors[index].getReadMethod().invoke(obj, new Object[0]);
//				if (object instanceof IJavaElement) {
//					
//					Formatter formatter = new Formatter();
//					prefix = "(" + 
//					formatter.format("%1.2f", InteractionRepository.getInstance().getRating((IJavaElement)object)).toString()
//					+ ") ";
//				}
//				return prefix + String.valueOf(propertyDescriptors[index].getReadMethod().invoke(obj, new Object[0]));
			} catch (Exception e) {
//				e.printStackTrace();
			}
			return "error";// getText(obj);
		}
		public Image getColumnImage(Object obj, int index) {
			if (obj instanceof JavaInteraction && index == 3) {
				return JavaPlugin.getImageDescriptorRegistry().get(imageProvider.getBaseImageDescriptor(((JavaInteraction)obj).getJavaElement(), 0));			
			} else {
				return null;
			}
		}
		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().
			getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
		
	}	
	
	@Override
	public void refresh(boolean aUpdateLabels) {
		super.refresh(aUpdateLabels);
		
	}
	
}
