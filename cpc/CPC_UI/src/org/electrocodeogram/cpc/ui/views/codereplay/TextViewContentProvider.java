package org.electrocodeogram.cpc.ui.views.codereplay;


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;


/**
 * While named 'content provider', this class is NOT a content provider in terms of the eclipse model.
 * It just... provides the content for the main textviewer. :)
 * 
 * @author marco kranz
 */
public class TextViewContentProvider
{

	private Device device;

	// number of changes to show
	// must be >= 0
	//private int noOfChanges = 5;

	public TextViewContentProvider(Device device)
	{
		this.device = device;
	}

	/**
	 * @return the text(source) of the current ReplayElement
	 */
	public String getText()
	{
		Replay rep = DataProvider.getInstance().getActiveReplay();
		//System.out.println("TextViewContentProvider.getText(): "+rep.getCurrentElement().getSource()+" change:"+rep.getCurrentElement().getChange());
		return rep.getCurrentElement().getSource();
	}

	/**
	 * This method produces the highlighting information for the TextViewer.
	 * This information is stored in so called StyleRanges, that can be applied to the text of a TextViewer.
	 * (If you want to change the current highlighting, this is the place to start.)
	 * 
	 * @return an array of StyleRanges
	 */
	public StyleRange[] getStyleRanges()
	{
		Color color;
		StyleRange[] ranges;
		StyleRange range = new StyleRange();
		Replay rep = DataProvider.getInstance().getActiveReplay();
		ReplayElement elem;

		elem = rep.getCurrentElement();
		ranges = new StyleRange[1];
		Diff diff = elem.getDiff();

		/*
		 * Choose colour according to diff type
		 */
		if (diff.getLength() > 0 && (diff.getDiff() == null || diff.getDiff().length() == 0))
		{
			//only deletion - RED background
			//we mark the area around the removal
			color = new Color(device, 255, 178, 178);
			range.background = color;
			range.start = Math.max(diff.getStart() - 1, 0);
			range.length = 2;
			//make sure we're not exceeding the length of the source here
			if (range.start + range.length > elem.getSource().length())
				//only set background colour for one char 
				range.length = 1;
		}
		else if (diff.getLength() > 0 && diff.getDiff().length() > 0)
		{
			//deletion and insertion - ORANGE font
			color = new Color(device, 224, 172, 0);
			range.foreground = color;
			range.start = diff.getStart();
			range.length = diff.getDiff().length();
		}
		else
		{
			//insertion only - GREEN font
			color = new Color(device, 0, 200, 0);
			range.foreground = color;
			range.start = diff.getStart();
			range.length = diff.getDiff().length();
		}

		range.fontStyle = SWT.BOLD;
		ranges[0] = range;

		/*StyleRange range;
		ArrayList list = new ArrayList(0);
		for(int i = 0; i < noOfChanges; i++){
			elem = rep.getElementAt(noOfChanges-(i+1));
			if(elem != null){
				color = new Color(device, Math.round(255/noOfChanges-i), 0, 0);
				range = new StyleRange();
				range.start = elem.getDiff().getStart();
				range.length = elem.getDiff().getLength();
				range.foreground = color;
				range.fontStyle = SWT.BOLD;
				list.add(range);
			}
		}
		ranges = new StyleRange[list.size()];
		for(int i = 0; i < list.size(); i++){
			ranges[i] = (StyleRange)list.get(i);
		}*/
		return ranges;
	}
}
