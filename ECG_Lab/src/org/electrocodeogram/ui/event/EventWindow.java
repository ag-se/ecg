package org.electrocodeogram.ui.event;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.event.ValidEventPacket.DELIVERY_STATE;
import org.electrocodeogram.module.registry.ModuleInstanceException;
import org.electrocodeogram.system.SystemRoot;
import org.electrocodeogram.ui.modules.ModuleGraph;

public class EventWindow extends JFrame implements IEventTarget
{

	GuiWriter _guiWriter;

	private JPanel _pnlMessages;

	JScrollPane _scrollPane;

	private JTextArea _textArea;

	boolean _autoscroll = false;

	private TitledBorder _titledBorder = null;

	private static final String NO_MODULE_SELECTED = "No module selected";

	private static final String MODULE_SELECTED_AND_SENT = "Events sent by the Module: ";

	private static final String MODULE_SELECTED_AND_RECEIVED = "Events received by the Module: ";

	JRadioButtonMenuItem _menuSent;

	JRadioButtonMenuItem _menuReceived;

	int _selectedModuleId = -1;

	public void append(String text)
	{
		this._textArea.append(text);

		JScrollBar vertBar = this._scrollPane.getVerticalScrollBar();

		if (vertBar.getValue() == vertBar.getMaximum() - vertBar.getVisibleAmount())
		{
			this._autoscroll = true;
		}
	}

	public EventWindow()
	{

		this._guiWriter = new GuiWriter();

		this.setTitle("Event Window");

		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		this.setBounds(0, 0, 400, 300);

		JMenuBar menuBar = new JMenuBar();

		JMenu menuView = new JMenu("View");

		this._menuSent = new JRadioButtonMenuItem("Show sent events");

		this._menuSent.setSelected(true);

		this._menuSent.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				EventWindow.this._guiWriter.set_deliveryState(DELIVERY_STATE.SENT);

				setSelectedModul(EventWindow.this._selectedModuleId);

			}
		});

		this._menuReceived = new JRadioButtonMenuItem("Show received events");

		this._menuReceived.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				EventWindow.this._guiWriter.set_deliveryState(DELIVERY_STATE.RECEIVED);

				setSelectedModul(EventWindow.this._selectedModuleId);

			}
		});

		ButtonGroup group = new ButtonGroup();

		group.add(this._menuSent);

		group.add(this._menuReceived);

		menuView.add(this._menuSent);

		menuView.add(this._menuReceived);

		menuBar.add(menuView);

		this.setJMenuBar(menuBar);

		this._textArea = new JTextArea();
		this._textArea.setEditable(false);
		this._textArea.setLineWrap(true);

		this._scrollPane = new JScrollPane(this._textArea);
		this._scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this._scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		this._scrollPane.getVerticalScrollBar().getModel().addChangeListener(new ChangeListener()
		{

			public void stateChanged(ChangeEvent e)
			{
				if (EventWindow.this._autoscroll)
				{
					JScrollBar vertBar = EventWindow.this._scrollPane.getVerticalScrollBar();
					vertBar.setValue(vertBar.getMaximum());
					EventWindow.this._autoscroll = false;
				}

			}
		});

		this._titledBorder = new TitledBorder(
				new LineBorder(new Color(0, 0, 0)), NO_MODULE_SELECTED);

		this._pnlMessages = new JPanel(new GridLayout(1, 1));
		this._pnlMessages.setBorder(this._titledBorder);
		this._pnlMessages.add(this._scrollPane);

		this.getContentPane().add(this._pnlMessages);

		this._guiWriter.setTarget(this);
	}

	public void setSelectedModul(int moduleId)
	{
		try
		{
			this._selectedModuleId = moduleId;

			if (moduleId == -1)
			{
				this._titledBorder.setTitle(NO_MODULE_SELECTED);
			}
			else
			{
				if (this._guiWriter.get_deliveryState().equals(DELIVERY_STATE.SENT))
				{

					this._titledBorder.setTitle(MODULE_SELECTED_AND_SENT + SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(moduleId).getName());

				}
				else
				{
					this._titledBorder.setTitle(MODULE_SELECTED_AND_RECEIVED + SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(moduleId).getName());
				}

			}
			repaint();
		}
		catch (ModuleInstanceException e)
		{
			JOptionPane.showMessageDialog(SystemRoot.getSystemInstance().getFrame(), e.getMessage(), "Module Selection", JOptionPane.ERROR_MESSAGE);
		}
	}

	private static class GuiWriter
	{

		private IEventTarget _target;

		private DELIVERY_STATE _deliveryState;

		public GuiWriter()
		{
			this._deliveryState = DELIVERY_STATE.SENT;
		}

		public void write(ValidEventPacket eventPacket)
		{
			if (this._target != null)
			{
				if (eventPacket.getSourceId() == ModuleGraph.getSelectedModule())
				{
					if (eventPacket.getDeliveryState() != null)
					{
						if (eventPacket.getDeliveryState().equals(this._deliveryState))
						{
							this._target.append(eventPacket.getTimeStamp().toString() + "," + eventPacket.getSensorDataType());

							List argList = eventPacket.getArglist();

							if (argList != null)
							{

								Object[] args = eventPacket.getArglist().toArray();

								int count = args.length;

								for (int i = 0; i < count; i++)
								{
									String str = (String) args[i];

									if (str.equals(""))
									{
										continue;
									}
									this._target.append("," + str);

								}

							}
							this._target.append("\n");
						}
					}
				}
			}
		}

		public void setTarget(EventWindow frame)
		{

			this._target = frame;

		}

		public DELIVERY_STATE get_deliveryState()
		{
			return this._deliveryState;
		}

		public void set_deliveryState(DELIVERY_STATE state)
		{
			this._deliveryState = state;
		}

	}

	/**
	 * @param packet
	 */
	public void append(ValidEventPacket packet)
	{
		this._guiWriter.write(packet);

	}
}