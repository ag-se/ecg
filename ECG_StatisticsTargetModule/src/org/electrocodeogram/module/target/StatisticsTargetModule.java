package org.electrocodeogram.module.target;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.msdt.MicroSensorDataType;
import org.electrocodeogram.system.SystemRoot;
import org.electrocodeogram.xml.ECGParser;
import org.electrocodeogram.xml.PropertyException;
import org.w3c.dom.Document;

/**
 *
 */
public class StatisticsTargetModule extends TargetModule
{

	private int _totalEventCount;

	private StatsFrame _dlgStats;

	private ArrayList<Day> _dayList;

	/**
	 * @param arg0
	 * @param arg1
	 */
	public StatisticsTargetModule(String arg0, String arg1)
	{
		super(arg0, arg1);
	}

	/**
	 * @param propertyName
	 * @param propertyValue
	 */
	@Override
	public void setProperty(String propertyName, String propertyValue)
	{
		if (propertyName.equals("Show Statistics"))
		{
			openDialog();
		}
	}

	protected int getTotalEventCount()
	{
		return this._totalEventCount;
	}

	protected int getDayCount()
	{
		return this._dayList.size();
	}

	/**
	 * 
	 */
	private void openDialog()
	{
		if (this._dlgStats == null)
		{
			this._dlgStats = new StatsFrame(this);
		}

		this._dlgStats.setVisible(true);

	}

	/**
	 * @see org.electrocodeogram.module.Module#analyseCoreNotification()
	 */
	@Override
	public void analyseCoreNotification()
	{

	}

	/**
	 * @see org.electrocodeogram.module.intermediate.IntermediateModule#initialize()
	 */
	@Override
	public void initialize()
	{
		this._dayList = new ArrayList<Day>();

	}

	protected Day getDay(int day)
	{
		if (this._dayList.size() == 0)
		{
			return null;
		}

		if (day >= this._dayList.size())
		{
			return null;
		}

		return this._dayList.get(day);
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.module.target.TargetModule#write(org.electrocodeogram.event.TypedValidEventPacket)
	 */
	@Override
	public void write(ValidEventPacket arg0)
	{
		this._totalEventCount++;

		Day dayOfPacket = new Day(arg0.getTimeStamp());
		
	
		if (this._dayList.size() == 0)
		{

			dayOfPacket.addEvents();

			dayOfPacket.setBegin(arg0.getTimeStamp());

			dayOfPacket.setEnd(arg0.getTimeStamp());
			
			dayOfPacket.addEvent(arg0.getMicroSensorDataType());

			this._dayList.add(dayOfPacket);
			
			this._dlgStats.updateTableModel();
		}
		else
		{
			if (this._dayList.contains(dayOfPacket))
			{
				int index = this._dayList.indexOf(dayOfPacket);

				Day day = this._dayList.get(index);

				day.addEvents();

				day.setBegin(arg0.getTimeStamp());

				day.setEnd(arg0.getTimeStamp());
				
				day.addEvent(arg0.getMicroSensorDataType());

			}
			else
			{
				dayOfPacket.addEvents();

				dayOfPacket.setBegin(arg0.getTimeStamp());

				dayOfPacket.setEnd(arg0.getTimeStamp());

				dayOfPacket.addEvent(arg0.getMicroSensorDataType());
				
				this._dayList.add(dayOfPacket);
				
				this._dlgStats.updateTableModel();
			}
		}

		this._dlgStats.update();
		

	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.module.target.TargetModule#startWriter()
	 */
	@Override
	public void startWriter() throws TargetModuleException
	{
		openDialog();

	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.module.target.TargetModule#stopWriter()
	 */
	@Override
	public void stopWriter()
	{
		closeDialog();

	}

	/**
	 * 
	 */
	private void closeDialog()
	{
		this._dlgStats.dispose();

		this._dlgStats = null;

	}

	private static class StatsFrame extends JFrame
	{

		private StatisticsTargetModule _statsModule;

		private JTable _statsTable;

		public StatsFrame(StatisticsTargetModule statsModule)
		{

			this._statsModule = statsModule;
			
			setTitle("Statisctics");

			setLayout(new GridLayout(1,1));
			
			setSize(500,300);
			
			this._statsTable = new JTable(new StatsTableModel(this._statsModule));
		
			this._statsTable.setAutoCreateColumnsFromModel(true);
			
			JScrollPane scrollPane = new JScrollPane(this._statsTable);

			this.getContentPane().add(scrollPane);
			
			
		}

		public void update()
		{
			this._statsTable.repaint();
		}
		
		public void updateTableModel()
		{
			this._statsTable.setModel(new StatsTableModel(this._statsModule));
	
		}

	}

	private static class Day
	{
		private static final String TIME_FORMAT_PATTERN = "HH:mm:ss";

		private static final String DATE_FORMAT_PATTERN = "dd.MM.yyyy";

		private SimpleDateFormat _timeFormat;

		private SimpleDateFormat _dateFormat;

		private Date _date;

		private Date _begin;

		private Date _end;
		
		private ArrayList<String> _changedFiles;
		
		private HashMap<MicroSensorDataType,Integer> _events;

		private Calendar _calendar;

		private int _eventsTotal;

		public Day(Date date)
		{
			this._date = date;

			this._calendar = Calendar.getInstance();

			this._calendar.setTime(this._date);

			this._timeFormat = new SimpleDateFormat(TIME_FORMAT_PATTERN);

			this._dateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN);
			
			this._changedFiles = new ArrayList<String>();
			
			this._events = new HashMap<MicroSensorDataType,Integer>();
		}

		public void addEvent(MicroSensorDataType msdt)
		{
			if(this._events.containsKey(msdt))
			{
				Integer count = this._events.get(msdt);
				
				count = new Integer(count.intValue() + 1);
				
				this._events.remove(msdt);
				
				this._events.put(msdt,count);
			}
			else
			{
				this._events.put(msdt,new Integer(1));
			}
		}
		
		
		public void addChangedFile(String filename)
		{
			if(filename == null)
			{
				return;
			}
			
			if(this._changedFiles.size() == 0)
			{
				this._changedFiles.add(filename);
			}
			else if(!this._changedFiles.contains(filename))
			{
				this._changedFiles.add(filename);
			}
		}
		
		public int getEventsTotal()
		{
			return this._eventsTotal;
		}

		public int getDayOfYear()
		{
			return this._calendar.get(Calendar.DAY_OF_YEAR);
		}

		public int getYear()
		{
			return this._calendar.get(Calendar.YEAR);
		}

		public void addEvents()
		{
			this._eventsTotal++;
		}

		public String getBegin()
		{
			return this._timeFormat.format(this._begin);
		}

		public String getDate()
		{
			return this._dateFormat.format(this._date);
		}

		public String getEnd()
		{
			return this._timeFormat.format(this._end);
		}

		public void setBegin(Date begin)
		{
			if (this._begin == null)
			{
				this._begin = begin;
			}
			else if (this._begin.compareTo(begin) > 0)
			{
				this._begin = begin;
			}
		}

		public void setEnd(Date end)
		{
			if (this._end == null)
			{
				this._end = end;
			}
			else if (this._end.compareTo(end) < 0)
			{
				this._end = end;
			}
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj instanceof Day)
			{
				Day day = (Day) obj;

				if ((day.getDayOfYear() == this.getDayOfYear()) && (day.getYear() == this.getYear()))
				{
					return true;
				}

			}

			return false;
		}

		/**
		 * @return
		 */
		public String getChangedFiles()
		{

			String toReturn = "";
			
			for(String filename : this._changedFiles)
			{
				toReturn += filename + "\n";
			}
			
			return toReturn;
		}

	}
	
	private static class StatsTableModel extends AbstractTableModel
	{

		private int _rowCount;
		
		private int _msdtCount = SystemRoot.getModuleInstance().getModuleMsdtRegistry().getMicroSensorDataTypes().length;

		private StatisticsTargetModule _statsModule;

		public StatsTableModel(StatisticsTargetModule statsModule)
		{
			this._statsModule = statsModule;

			this._rowCount = 4 + this._msdtCount;
		}

		public int getRowCount()
		{
			return this._rowCount;
		}

		@Override
		public String getColumnName(int columnIndex)
		{

			if (columnIndex == 0)
			{
				return "";
			}

			Day day = this._statsModule.getDay(columnIndex - 1);

			if (day == null)
			{
				return "";
			}

			return day.getDate();

		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		public int getColumnCount()
		{
			return this._statsModule.getDayCount() + 1;
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		public Object getValueAt(int rowIndex, int columnIndex)
		{

			if (columnIndex == 0)
			{
				return getRowHeadline(rowIndex);
			}

			Day day = this._statsModule.getDay(columnIndex - 1);

			if (day == null)
			{
				return "";
			}
			
			return getRowContent(day,rowIndex);
			

		}

		/**
		 * @param rowIndex
		 * @return
		 */
		private Object getRowContent(Day day,int rowIndex)
		{
			if(rowIndex == 0)
			{
				return day.getDate();
			}
			else if(rowIndex == 1)
			{
				return day.getBegin();
			}
			else if(rowIndex == 2)
			{
				return day.getEnd();
			}
			else if(rowIndex > 2 && rowIndex < this._msdtCount + 3)
			{
				Integer count = day._events.get(SystemRoot.getModuleInstance().getModuleMsdtRegistry().getMicroSensorDataTypes()[rowIndex - 3]);
				
				if(count == null)
				{
					return "";
				}
				else
				{
					return count.toString();
				}
			}
			else
			{
				return day._eventsTotal;
			}
			
		}

		/**
		 * @param rowIndex
		 */
		private String getRowHeadline(int rowIndex)
		{
			
			if(rowIndex == 0)
			{
				return "Date";
			}
			else if(rowIndex == 1)
			{
				return "Begin";
			}
			else if(rowIndex == 2)
			{
				return "End";
			}
			else if(rowIndex > 2 && rowIndex < this._msdtCount + 3)
			{
				return SystemRoot.getModuleInstance().getModuleMsdtRegistry().getMicroSensorDataTypes()[rowIndex - 3].getName();
			}
			else
			{
				return "Events total";
			}
			
		}

	}
}
