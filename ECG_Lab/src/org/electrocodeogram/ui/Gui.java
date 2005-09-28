package org.electrocodeogram.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import com.zfqjava.swing.JStatusBar;
import org.electrocodeogram.event.TypedValidEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.Module.ModuleType;
import org.electrocodeogram.module.ModuleDescriptor;
import org.electrocodeogram.module.registry.ModuleInstanceException;
import org.electrocodeogram.module.registry.ModuleRegistry;
import org.electrocodeogram.module.registry.ModuleSetupLoadException;
import org.electrocodeogram.module.registry.ModuleSetupStoreException;
import org.electrocodeogram.system.SystemRoot;
import org.electrocodeogram.ui.event.EventWindow;
import org.electrocodeogram.ui.modules.ModuleGraph;

public class Gui extends JFrame implements IGui
{

	private static Logger _logger = LogHelper.createLogger(Gui.class.getName());

	private static final long serialVersionUID = 1L;

	private EventWindow _frmEvents = null;

	private ModuleLabPanel _pnlModules;

	private JStatusBar _statusBar;

	private ModuleFinderPanel _pnlButtons;

	private JSplitPane _splitPane;

	private JMenu _menuModule;

	private boolean _moduleConnectionMode;

	private int _sourceModuleId;

	public Gui(Observable observable)
	{
		super();

		observable.addObserver(this);

		initializeLookAndFeel();

		initializeFrame();

		initializeMenu();

		this._pnlModules = new ModuleLabPanel(this);

		this._pnlButtons = new ModuleFinderPanel(this);

		initializeSplitPane();

		initializeStatusBar();

		setVisible(true);
	}

	private void initializeSplitPane()
	{
		this._splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		this._splitPane.add(this._pnlButtons, 0);

		this._splitPane.add(this._pnlModules, 1);

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.weighty = 2;
		c.weightx = 2;

		getContentPane().add(this._splitPane, c);
	}

	private void initializeStatusBar()
	{
		this._statusBar = new JStatusBar(JStatusBar.EXPLORER);

		GridBagConstraints c2 = new GridBagConstraints();
		c2.anchor = GridBagConstraints.SOUTHWEST;
		c2.fill = GridBagConstraints.HORIZONTAL;
		c2.gridx = 0;
		c2.gridy = 2;
		c2.weighty = 0;
		c2.weightx = 2;

		getContentPane().add(_statusBar, c2);
	}

	private void initializeMenu()
	{
		JMenuBar menuBar = new JMenuBar();

		JMenu menuFile = new JMenu("File");

		JMenuItem mniExit = new JMenuItem("Exit");

		mniExit.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				SystemRoot.getSystemInstance().quit();

			}
		});

		JMenuItem mniSave = new JMenuItem("Save module setup");

		mniSave.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				try
				{
					JFileChooser fileChooser = new JFileChooser();

					fileChooser.setDialogTitle("Select the file to store the module setup in");

					fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);

					File file = null;

					int result = fileChooser.showOpenDialog(Gui.this);

					switch (result)
					{
						case JFileChooser.CANCEL_OPTION:
							return;
						case JFileChooser.ERROR_OPTION:
							return;
						case JFileChooser.APPROVE_OPTION:

							file = new File(
									fileChooser.getSelectedFile().getAbsolutePath());

							break;
						default:

							return;
					}

					SystemRoot.getSystemInstance().getSystemModuleRegistry().storeModuleSetup(file);
				}
				catch (ModuleSetupStoreException e1)
				{
					JOptionPane.showMessageDialog(Gui.this, e1.getMessage(), "Module setup storage error", JOptionPane.ERROR_MESSAGE);
				}

			}
		});

		JMenuItem mniLoad = new JMenuItem("Load module setup");
		mniLoad.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				try
				{
					JFileChooser fileChooser = new JFileChooser();

					fileChooser.setDialogTitle("Select the file to load the module setup from");

					fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);

					File file = null;

					int result = fileChooser.showOpenDialog(Gui.this);

					switch (result)
					{
						case JFileChooser.CANCEL_OPTION:
							return;
						case JFileChooser.ERROR_OPTION:
							return;
						case JFileChooser.APPROVE_OPTION:

							file = new File(
									fileChooser.getSelectedFile().getAbsolutePath());

							break;

						default:
							return;

					}

					SystemRoot.getSystemInstance().getSystemModuleRegistry().loadModuleSetup(file);
				}
				catch (ModuleSetupLoadException e1)
				{
					JOptionPane.showMessageDialog(Gui.this, e1.getMessage(), "Module setup loading error", JOptionPane.ERROR_MESSAGE);
				}

			}
		});

		menuFile.add(mniSave);
		menuFile.add(mniLoad);
		menuFile.addSeparator();
		menuFile.add(mniExit);

		Gui.this._menuModule = new JMenu("Module");
		Gui.this._menuModule.addMouseListener(new MouseAdapter()
		{

			@Override
			public void mouseEntered(MouseEvent e)
			{
				MenuManager.populateModuleMenu(Gui.this._menuModule, Gui.this._pnlModules.getSelectedModule());
			}
		});

		JMenu menuWindow = new JMenu("Window");
		JMenuItem mniShow = new JMenuItem("Event Window");

		mniShow.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{

				showMessagesWindow();

			}
		});
		menuWindow.add(mniShow);

		menuBar.add(menuFile);
		menuBar.add(Gui.this._menuModule);
		menuBar.add(menuWindow);

		this.setJMenuBar(menuBar);
	}

	private void initializeFrame()
	{
		setTitle("ElectroCodeoGram - ECG Lab");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setExtendedState(JFrame.MAXIMIZED_BOTH);

		setBounds(0, 0, 800, 600);

		getContentPane().setLayout(new GridBagLayout());
	}

	private void initializeLookAndFeel()
	{
		try
		{

			UIManager.setLookAndFeel("org.fife.plaf.Office2003.Office2003LookAndFeel");
		}
		catch (UnsupportedLookAndFeelException e)
		{
			try
			{
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			catch (Exception e1)
			{
				_logger.log(Level.WARNING, "Can not set the LookAndFeel");

				_logger.log(Level.FINEST, e.getMessage());
			}

		}
		catch (Exception e)
		{
			_logger.log(Level.WARNING, "Can not set the LookAndFeel");

			_logger.log(Level.FINEST, e.getMessage());
		}
	}

	public void showMessagesWindow()
	{
		if (this._frmEvents == null)
		{
			this._frmEvents = new EventWindow();

			this._frmEvents.setSelectedModul(Gui.this._pnlModules.getSelectedModule());
		}

		this._frmEvents.setVisible(true);
	}

	public void update(Observable o, Object arg)
	{

		/*
		 * if the ModuleRegistry is sending the event, a module-instance has
		 * been added or removed or a module class has been installed
		 */
		if (o instanceof ModuleRegistry)
		{

			// a module has been added or removed
			if (arg instanceof Module)
			{

				Module module = (Module) arg;

				if (this._pnlModules.containsModule(module.getId()))
				{
					this._pnlModules.removeModule(module.getId());
				}
				else
				{

					this._pnlModules.createModule(module.getModuleType(), module.getId(), module.getName(), module.isActive());

				}

			}
			// a module class has been intalled
			else if (arg instanceof ModuleDescriptor)
			{

				ModuleDescriptor moduleDescriptor = (ModuleDescriptor) arg;

				this._pnlButtons.addModule(moduleDescriptor);

				this._splitPane.resetToPreferredSizes();

			}
		}

		else if (arg instanceof TypedValidEventPacket)
		{
			if (this._frmEvents != null)
			{
				this._frmEvents.append((TypedValidEventPacket) arg);
			}
		}
		else if (arg instanceof Module)
		{
			Module module = (Module) arg;

			int id = module.getId();

			if (this._pnlModules.containsModule(id))
			{
				this._pnlModules.updateModule(id, module);
			}
		}
	}

	public void enableModuleMenu(boolean enable)
	{
		this._menuModule.setEnabled(enable);
	}

	/**
	 * 
	 */
	public void showModuleDetails()
	{

		int id = this._pnlModules.getSelectedModule();

		if (id != -1)
		{

			String text = "";

			try
			{
				text = SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(id).getDetails();
			}
			catch (ModuleInstanceException e)
			{
				JOptionPane.showMessageDialog(this, e.getMessage(), "Module Details", JOptionPane.ERROR_MESSAGE);
			}

			JOptionPane.showMessageDialog(this, text, "Module Details", JOptionPane.INFORMATION_MESSAGE);
		}

	}

	/**
	 * @param selectedModuleCellId2
	 */
	public void enterModuleConnectionMode(int selectedModuleCellId)
	{

		this._moduleConnectionMode = true;

		this._sourceModuleId = selectedModuleCellId;

	}

	/**
	 * @return
	 */
	public boolean getModuleConnectionMode()
	{

		return this._moduleConnectionMode;
	}

	/**
	 * @return
	 */
	public int getSourceModule()
	{

		return this._sourceModuleId;
	}

	/**
	 * 
	 */
	public void exitModuleConnectionMode()
	{
		this._moduleConnectionMode = false;

		this._sourceModuleId = -1;

	}

	private static class ModuleLabPanel extends JPanel
	{
		private Gui _gui;

		private ModuleGraph _moduleGraph;

		private JScrollPane _scrollPane;

		public ModuleLabPanel(Gui gui)
		{
			this._gui = gui;

			this._moduleGraph = new ModuleGraph(this._gui);

			this.setLayout(new GridLayout(1, 1));

			this.setBackground(Color.WHITE);

			this.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)),
					"Module Setup"));

			this._scrollPane = new JScrollPane(this._moduleGraph);

			this.add(this._scrollPane);

			//this.add(this._moduleGraph);
		}

		/**
		 * @return
		 */
		public int getSelectedModule()
		{
			return ModuleGraph.getSelectedModule();
		}

		/**
		 * @param moduleType
		 * @param id
		 * @param name
		 * @param b
		 */
		public void createModule(ModuleType moduleType, int id, String name, boolean b)
		{
			this._moduleGraph.createModuleCell(moduleType, id, name, b);

		}

		/**
		 * @param id
		 */
		public void removeModule(int id)
		{
			this._moduleGraph.removeModuleCell(id);

		}

		public boolean containsModule(int id)
		{
			return this._moduleGraph.containsModuleCell(id);
		}

		public void updateModule(int id, Module module)
		{
			this._moduleGraph.updateModuleCell(id, module);
		}
	}

	private static class ModuleFinderPanel extends JPanel
	{
		private JPanel _pnlSourceModules;

		private JPanel _pnlIntermediateModules;

		private JPanel _pnlTargetModules;

		public ModuleFinderPanel(Gui gui)
		{
			this._pnlSourceModules = new InnerFinderPanel(
					ModuleType.SOURCE_MODULE);

			this._pnlIntermediateModules = new InnerFinderPanel(
					ModuleType.INTERMEDIATE_MODULE);

			this._pnlTargetModules = new InnerFinderPanel(
					ModuleType.TARGET_MODULE);

			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

			this.setBackground(Color.WHITE);

			this.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)),
					"Available Modules"));

			this.add(this._pnlSourceModules);

			this.add(this._pnlIntermediateModules);

			this.add(this._pnlTargetModules);
		}

		public void addModule(ModuleDescriptor moduleDescriptor)
		{
			JButton btnModule = new JButton(moduleDescriptor.getName());

			btnModule.addActionListener(new ActionAdapter(
					moduleDescriptor.getId(), moduleDescriptor.getName()));

			switch (moduleDescriptor.get_moduleType())
			{
				case SOURCE_MODULE:

					this._pnlSourceModules.add(btnModule);

					break;

				case INTERMEDIATE_MODULE:

					this._pnlIntermediateModules.add(btnModule);

					break;

				default:

					this._pnlTargetModules.add(btnModule);

					break;

			}
		}
	}

	private static class InnerFinderPanel extends JPanel
	{
		public InnerFinderPanel(ModuleType moduleType)
		{
			//this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

			this.setBackground(Color.WHITE);

			switch (moduleType)
			{
				case SOURCE_MODULE:

					this.setBorder(new TitledBorder(
							new LineBorder(Color.GREEN), "Source Modules"));

					//this.setBackground(Color.GREEN);

					break;

				case INTERMEDIATE_MODULE:

					this.setBorder(new TitledBorder(
							new LineBorder(Color.ORANGE),
							"Intermediate Modules"));

					//this.setBackground(Color.ORANGE);

					break;

				default:

					this.setBorder(new TitledBorder(new LineBorder(Color.BLUE),
							"Target Modules"));

					//this.setBackground(Color.BLUE);

					break;
			}
		}
	}
}