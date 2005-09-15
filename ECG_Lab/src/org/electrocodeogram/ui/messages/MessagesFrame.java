package org.electrocodeogram.ui.messages;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.electrocodeogram.event.TypedValidEventPacket;
import org.electrocodeogram.event.TypedValidEventPacket.DELIVERY_STATE;
import org.electrocodeogram.module.registry.ModuleInstanceException;
import org.electrocodeogram.system.SystemRoot;

public class MessagesFrame extends JFrame implements MessagesTarget
{

	private GuiWriter guiWriter;

	private JPanel pnlMessages;

	private JScrollPane scrollPane;

	private JTextArea textArea;

	private boolean shouldScroll = false;

	private TitledBorder titledBorder = null;

	private static final String NO_MODULE_SELECTED = "No module selected";

	private static final String MODULE_SELECTED_AND_SENT = "Events sent by the Module: ";

	private static final String MODULE_SELECTED_AND_RECEIVED = "Events received by the Module: ";

	JRadioButtonMenuItem menuSent;

	JRadioButtonMenuItem menuReceived;

	private int selectedModuleId = -1;

	public void append(String text)
	{
		textArea.append(text);

		JScrollBar vertBar = scrollPane.getVerticalScrollBar();
		if (vertBar.getValue() == vertBar.getMaximum() - vertBar.getVisibleAmount())
		{
			shouldScroll = true;
		}
	}

	public MessagesFrame()
	{

		this.guiWriter = new GuiWriter();

		this.setTitle("Event window");

		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		this.setBounds(0, 0, 400, 300);

		JMenuBar menuBar = new JMenuBar();

		JMenu menuView = new JMenu("View");

		menuSent = new JRadioButtonMenuItem("Sent events");

		menuSent.setSelected(true);

		menuSent.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				guiWriter.set_deliveryState(DELIVERY_STATE.SENT);
				
				setSelectedModul(selectedModuleId);

			}
		});

		menuReceived = new JRadioButtonMenuItem("Received events");

		menuReceived.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				guiWriter.set_deliveryState(DELIVERY_STATE.RECEIVED);
				
				setSelectedModul(selectedModuleId);

			}
		});
		
		ButtonGroup group = new ButtonGroup();
		
		group.add(menuSent);

		group.add(menuReceived);

		menuView.add(menuSent);

		menuView.add(menuReceived);
		
		menuBar.add(menuView);

		this.setJMenuBar(menuBar);

		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setLineWrap(true);

		scrollPane = new JScrollPane(textArea);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getVerticalScrollBar().getModel().addChangeListener(new ChangeListener()
		{

			public void stateChanged(ChangeEvent e)
			{
				if (shouldScroll)
				{
					JScrollBar vertBar = scrollPane.getVerticalScrollBar();
					vertBar.setValue(vertBar.getMaximum());
					shouldScroll = false;
				}

			}
		});

		titledBorder = new TitledBorder(new LineBorder(new Color(0, 0, 0)),
				NO_MODULE_SELECTED);

		pnlMessages = new JPanel(new GridLayout(1, 1));
		pnlMessages.setBorder(titledBorder);
		pnlMessages.add(scrollPane);

		this.getContentPane().add(pnlMessages);

		this.guiWriter.setTarget(this);
	}

	public void setSelectedModul(int moduleId)
	{
		try
		{
			this.selectedModuleId  = moduleId;
			
			if (moduleId == -1)
			{
				this.titledBorder.setTitle(NO_MODULE_SELECTED);
			}
			else
			{
				if (this.guiWriter.get_deliveryState().equals(DELIVERY_STATE.SENT))
				{

					this.titledBorder.setTitle(MODULE_SELECTED_AND_SENT + SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(moduleId).getName());

				}
				else
				{
					this.titledBorder.setTitle(MODULE_SELECTED_AND_RECEIVED + SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(moduleId).getName());
				}

			}
			repaint();
		}
		catch (ModuleInstanceException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static class GuiWriter
	{

		private MessagesTarget _target;

		private DELIVERY_STATE _deliveryState;

		public GuiWriter()
		{
			this._deliveryState = DELIVERY_STATE.SENT;
		}

		public void write(TypedValidEventPacket eventPacket)
		{
			if (this._target != null)
			{
				if (eventPacket.getSourceId() == SystemRoot.getSystemInstance().getGui().getSelectedModuleCellId())
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

							// JScrollBar vertBar =
							// scrollPane.getVerticalScrollBar();
							// if (vertBar.getValue() == vertBar.getMaximum() -
							// vertBar.getVisibleAmount()) {
							// shouldScroll = true;
							// }
						}
					}
				}
			}
		}

		public void setTarget(MessagesFrame frame)
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
	public void append(TypedValidEventPacket packet)
	{
		this.guiWriter.write(packet);

	}
}