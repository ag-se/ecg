package org.electrocodeogram.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.Module.ModuleType;
import org.electrocodeogram.module.ModuleDescriptor;
import org.electrocodeogram.module.ModuleProperty;
import org.electrocodeogram.module.intermediate.IntermediateModule;
import org.electrocodeogram.module.intermediate.IntermediateModule.ProcessingMode;
import org.electrocodeogram.module.registry.ModuleClassException;
import org.electrocodeogram.module.registry.ModuleInstanceException;
import org.electrocodeogram.system.SystemRoot;
import org.electrocodeogram.ui.modules.ModuleGraph;

/**
 * @author 7oas7er
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MenuManager
{

	static JFrame _frame;

	private static JPopupMenu modulePopupMenu = null;

	private static JPopupMenu edgePopupMenu = null;

	private static JMenuItem mniModuleDetails = new JMenuItem("Details");

	private static JMenuItem mniModuleRemove = new JMenuItem("Remove");

	private static JMenuItem mniEdgeRemove = new JMenuItem("Remove");

	private static JMenuItem mniModuleConnectTo = new JMenuItem("Connect to...");

	private static JMenuItem mniModuleStop = new JMenuItem("Stop");

	private static JMenuItem mniModuleStart = new JMenuItem("Start");

	private static JMenuItem mniMsgWindowShow = new JMenuItem("Event Window");

	private static JMenuItem mniMakeAnnotator = new JMenuItem("Annotator");

	private static JMenuItem mniMakeFilter = new JMenuItem("Filter");

	static
	{
		_frame = SystemRoot.getSystemInstance().getFrame();

		mniModuleRemove.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{

				try
				{
					SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(ModuleGraph.getSelectedModule()).remove();
				}
				catch (ModuleInstanceException e1)
				{

					JOptionPane.showMessageDialog(MenuManager.this._frame, e1.getMessage(), "Remove Module", JOptionPane.ERROR_MESSAGE);
				}

			}
		});

		mniModuleDetails.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				SystemRoot.getSystemInstance().getGui().showModuleDetails();
			}
		});

		mniModuleStop.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{

				try
				{
					SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(ModuleGraph.getSelectedModule()).deactivate();
				}
				catch (ModuleInstanceException e1)
				{

					JOptionPane.showMessageDialog(MenuManager.this._frame, e1.getMessage(), "Stop Module", JOptionPane.ERROR_MESSAGE);
				}

			}
		});

		mniModuleStart.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{

				try
				{
					SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(ModuleGraph.getSelectedModule()).activate();
				}
				catch (Exception e1)
				{
					JOptionPane.showMessageDialog(MenuManager.this._frame, e1.getMessage(), "Start Module", JOptionPane.ERROR_MESSAGE);
				}

			}
		});

		mniMsgWindowShow.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				SystemRoot.getSystemInstance().getGui().showMessagesWindow();

			}
		});

		mniMakeAnnotator.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				Module module = null;

				try
				{
					SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(ModuleGraph.getSelectedModule());
				}
				catch (ModuleInstanceException e1)
				{
					JOptionPane.showMessageDialog(MenuManager.this._frame, e1.getMessage(), "Annotator", JOptionPane.ERROR_MESSAGE);
				}

				if (module instanceof IntermediateModule)
				{
					IntermediateModule eventProcessor = (IntermediateModule) module;

					eventProcessor.setProcessingMode(ProcessingMode.ANNOTATOR);

				}

			}
		});

		mniModuleConnectTo.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				SystemRoot.getSystemInstance().getGui().enterModuleConnectionMode(ModuleGraph.getSelectedModule());

			}
		});

		mniMakeFilter.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				Module module = null;

				try
				{
					SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(ModuleGraph.getSelectedModule());
				}
				catch (ModuleInstanceException e1)
				{
					JOptionPane.showMessageDialog(MenuManager.this._frame, e1.getMessage(), "Filter", JOptionPane.ERROR_MESSAGE);
				}

				if (module instanceof IntermediateModule)
				{
					IntermediateModule eventProcessor = (IntermediateModule) module;

					eventProcessor.setProcessingMode(IntermediateModule.ProcessingMode.FILTER);

				}

			}
		});

	}

	public static void showModuleMenu(int moduleId, Component c, int x, int y)
	{
		modulePopupMenu = new JPopupMenu();

		modulePopupMenu.add(mniModuleStart);

		modulePopupMenu.add(mniModuleStop);

		modulePopupMenu.addSeparator();

		try
		{
			if (!SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(moduleId).isModuleType(ModuleType.TARGET_MODULE))
			{
				modulePopupMenu.add(mniModuleConnectTo);
			}
		}
		catch (ModuleInstanceException e2)
		{
			JOptionPane.showMessageDialog(_frame, e2.getMessage(), "Mneu Initialisation", JOptionPane.ERROR_MESSAGE);
		}

		modulePopupMenu.add(mniModuleRemove);

		modulePopupMenu.addSeparator();

		modulePopupMenu.add(mniMsgWindowShow);

		try
		{
			if (SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(moduleId).isModuleType(ModuleType.INTERMEDIATE_MODULE))
			{
				modulePopupMenu.addSeparator();

				modulePopupMenu.add(mniMakeAnnotator);

				modulePopupMenu.add(mniMakeFilter);
			}
		}
		catch (ModuleInstanceException e1)
		{
			JOptionPane.showMessageDialog(_frame, e1.getMessage(), "Mneu Initialisation", JOptionPane.ERROR_MESSAGE);
		}

		ModuleProperty[] moduleProperties = null;

		String moduleClassId = null;
		try
		{
			moduleClassId = SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(moduleId).getClassId();
		}
		catch (ModuleInstanceException e)
		{
			JOptionPane.showMessageDialog(_frame, e.getMessage(), "Mneu Initialisation", JOptionPane.ERROR_MESSAGE);
		}

		ModuleDescriptor moduleDescriptor = null;
		try
		{
			moduleDescriptor = SystemRoot.getSystemInstance().getSystemModuleRegistry().getModuleDescriptor(moduleClassId);
		}
		catch (ModuleClassException e)
		{
			JOptionPane.showMessageDialog(_frame, e.getMessage(), "Mneu Initialisation", JOptionPane.ERROR_MESSAGE);
		}

		moduleProperties = moduleDescriptor.getProperties();

		if (moduleProperties != null)
		{
			for (ModuleProperty moduleProperty : moduleProperties)
			{
				String propertyName = moduleProperty.getName();

				Class propertyType = moduleProperty.getType();

				String propertyValue = moduleProperty.getValue();

				JMenuItem menuItem = new JMenuItem(propertyName);

				menuItem.addActionListener(new PropertyActionAdapter(moduleId,
						propertyType, propertyName, propertyValue));

				modulePopupMenu.add(menuItem);

			}

		}

		modulePopupMenu.addSeparator();

		modulePopupMenu.add(mniModuleDetails);

		modulePopupMenu.show(c, x, y);
	}

	public static void showEdgeMenu(int parentId, int childId, Component c, int x, int y)
	{
		edgePopupMenu = new JPopupMenu();

		mniEdgeRemove.addActionListener(new EdgeRemoveAdapter(parentId, childId));

		edgePopupMenu.add(mniEdgeRemove);

		edgePopupMenu.show(c, x, y);

	}

	private static class EdgeRemoveAdapter implements ActionListener
	{

		private int _parentId;

		private int _childId;

		public EdgeRemoveAdapter(int parentId, int childId)
		{

			this._parentId = parentId;

			this._childId = childId;
		}

		public void actionPerformed(ActionEvent e)
		{

			try
			{
				SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(this._parentId).connectReceiverModule(SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(this._childId));
			}
			catch (Exception e1)
			{
				JOptionPane.showMessageDialog(MenuManager._frame, e1.getMessage(), "Remove Edge", JOptionPane.ERROR_MESSAGE);
			}

		}

	}

	private static class PropertyActionAdapter implements ActionListener
	{

		private int _moduleId;

		private String _propertyName;

		private String _propertyResult;

		private String _propertyValue;

		private Class _propertyType;

		public PropertyActionAdapter(int moduleId, Class propertyType, String propertyName, String propertyValue)
		{
			this._moduleId = moduleId;

			this._propertyName = propertyName;

			this._propertyType = propertyType;

			this._propertyValue = propertyValue;

		}

		public void actionPerformed(ActionEvent e)
		{

			try
			{
				if (_propertyType.equals(Class.forName("java.lang.String")))
				{
					_propertyResult = JOptionPane.showInputDialog(MenuManager._frame, "Please enter a new value for " + this._propertyName, this._propertyName, JOptionPane.QUESTION_MESSAGE);
				}
				else if (_propertyType.equals(Class.forName("java.lang.Integer")))
				{
					_propertyResult = JOptionPane.showInputDialog(MenuManager._frame, "Please enter a new value for " + this._propertyName, this._propertyName, JOptionPane.QUESTION_MESSAGE);
				}
				else if (_propertyType.equals(Class.forName("java.io.File")))
				{
					JFileChooser fileChooser = new JFileChooser();

					int result = fileChooser.showOpenDialog(MenuManager._frame);

					switch (result)
					{
						case JFileChooser.CANCEL_OPTION:
							return;
						case JFileChooser.ERROR_OPTION:
							return;
						case JFileChooser.APPROVE_OPTION:

							_propertyResult = fileChooser.getSelectedFile().getAbsolutePath();

							return;
						default:
							return;
					}
				}
				else if (this._propertyType.equals(Class.forName("java.lang.reflect.Method")))
				{
					this._propertyResult = this._propertyValue;
				}
				else if (this._propertyType.equals(Class.forName("java.lang.Boolean")))
				{
					int result = JOptionPane.showConfirmDialog(MenuManager._frame, "Do you want to enable the " + this._propertyName + " property?", this._propertyName, JOptionPane.YES_NO_OPTION);

					if (result == JOptionPane.YES_OPTION)
					{
						this._propertyResult = "true";
					}
					else
					{
						this._propertyResult = "false";
					}
				}
			}
			catch (Exception e2)
			{
				JOptionPane.showMessageDialog(MenuManager._frame, e2.getMessage(), this._propertyName, JOptionPane.ERROR_MESSAGE);
			}

			if (this._propertyResult == null)
			{
				return;
			}

			try
			{
				SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(this._moduleId).setProperty(this._propertyName, this._propertyResult);
			}
			catch (Exception e1)
			{
				JOptionPane.showMessageDialog(MenuManager._frame, e1.getMessage(), this._propertyName, JOptionPane.ERROR_MESSAGE);
			}

		}

	}

	public static void populateModuleMenu(JMenu menu, int id)
	{
		if (id == -1)
		{
			return;
		}

		menu.removeAll();

		menu.add(mniModuleStart);

		menu.add(mniModuleStop);

		menu.addSeparator();

		menu.add(mniModuleConnectTo);

		menu.add(mniModuleRemove);

		ModuleProperty[] moduleProperties = null;

		String moduleId = null;

		try
		{
			moduleId = SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(id).getClassId();
		}
		catch (ModuleInstanceException e)
		{
			JOptionPane.showMessageDialog(MenuManager._frame, e.getMessage(), "Menu Initialisation", JOptionPane.ERROR_MESSAGE);
		}

		ModuleDescriptor moduleDescriptor = null;
		try
		{
			moduleDescriptor = SystemRoot.getSystemInstance().getSystemModuleRegistry().getModuleDescriptor(moduleId);
		}
		catch (ModuleClassException e)
		{
			JOptionPane.showMessageDialog(MenuManager._frame, e.getMessage(), "Menu Initialisation", JOptionPane.ERROR_MESSAGE);
		}

		moduleProperties = moduleDescriptor.getProperties();

		if (moduleProperties != null)
		{
			for (ModuleProperty moduleProperty : moduleProperties)
			{
				String propertyName = moduleProperty.getName();

				Class propertyType = moduleProperty.getType();

				String propertyValue = moduleProperty.getValue();

				JMenuItem menuItem = new JMenuItem(propertyName);

				menuItem.addActionListener(new PropertyActionAdapter(id,
						propertyType, propertyName, propertyValue));

				menu.add(menuItem);

			}

		}

		menu.add(mniModuleDetails);

	}
}
