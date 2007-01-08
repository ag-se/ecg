package org.electrocodeogram.codereplay.pluginControl;


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.electrocodeogram.codereplay.dataProvider.DataProvider;
import org.electrocodeogram.codereplay.dataProvider.Replay;
import org.electrocodeogram.codereplay.dataProvider.ReplayElement;


/**
 * While named 'content provider', this class is NOT a content provider in terms of the eclipse model.
 * It just... provides the content for the main textviewer. :)
 * 
 * @author marco kranz
 */
public class TextViewContentProvider{

	private Device device;
	// number of changes to show
	// must be >= 0
	private int noOfChanges = 5;

	public TextViewContentProvider(Device device){
		this.device = device;
	}
	
	
	/**
	 * @return the text(source) of the current ReplayElement
	 */
	public String getText() {
		Replay rep = DataProvider.getInstance().getActiveReplay();
		//System.out.println("TextViewContentProvider.getText(): "+rep.getCurrentElement().getSource()+" change:"+rep.getCurrentElement().getChange());
		return rep.getCurrentElement().getSource();
	}

	// get changed regions
	/**
	 * This method produces the highlighting information for the TextViewer.
	 * This information is stored in so called StyleRanges, that can be applied to the text of a TextViewer.
	 * (If you want to change the current highlighting, this is the place to start.)
	 * 
	 * @return an array of StyleRanges
	 */
	public StyleRange[] getStyleRanges(){
		Color color;
		StyleRange[] ranges;
		StyleRange range = new StyleRange();
		Replay rep = DataProvider.getInstance().getActiveReplay();
		ReplayElement elem;
		
		elem = rep.getCurrentElement();
		ranges = new StyleRange[1];
		if(elem.getDiff().getLength() > 0){
			color = new Color(device, 255, 0, 0);
			range.start = elem.getDiff().getStart();
			range.length = elem.getDiff().getLength();
			range.foreground = color;
			range.fontStyle = SWT.BOLD;
			ranges[0] = range;
			//System.out.println("getStyleRanges()... range.length = "+range.length+" (if)");
		}
		else{
			color = new Color(device, 0, 255, 0);
			range.start = elem.getDiff().getStart();
			range.length = elem.getDiff().getLength();
			range.background = color;
			range.fontStyle = SWT.BOLD;
			ranges[0] = range;
			//System.out.println("getStyleRanges() range.length = "+range.length+" (else)");
		}
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
