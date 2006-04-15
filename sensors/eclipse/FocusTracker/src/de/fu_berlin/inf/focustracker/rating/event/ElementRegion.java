package de.fu_berlin.inf.focustracker.rating.event;

public class ElementRegion {

	private int elementOffset;
	private int elementLength;
	private int visibleRegionOffset;
	private int visibleRegionLength;
	
	public ElementRegion(int aElementOffset, int aElementLength, int aVisibleRegionOffset, int aVisibleRegionLength) {
		super();
		elementOffset = aElementOffset;
		elementLength = aElementLength;
		visibleRegionOffset = aVisibleRegionOffset;
		visibleRegionLength = aVisibleRegionLength;
	}
	
	public int getElementLength() {
		return elementLength;
	}
	public void setElementLength(int aElementLength) {
		elementLength = aElementLength;
	}
	public int getElementOffset() {
		return elementOffset;
	}
	public void setElementOffset(int aElementOffset) {
		elementOffset = aElementOffset;
	}
	public int getVisibleRegionLength() {
		return visibleRegionLength;
	}
	public void setVisibleRegionLength(int aVisibleRegionLength) {
		visibleRegionLength = aVisibleRegionLength;
	}
	public int getVisibleRegionOffset() {
		return visibleRegionOffset;
	}
	public void setVisibleRegionOffset(int aVisibleRegionOffset) {
		visibleRegionOffset = aVisibleRegionOffset;
	}

	public double getPercentageVisible() {

//    	int elementBottom = elementOffset + elementLength;
//    	int visibleRegionBottom = visibleRegionOffset + visibleRegionLength;
//
//		// how much of the method is visible
//		int missingTop = Math.max(0, visibleRegionOffset - elementOffset);
//		int missingBottom = Math.max(0, elementBottom - visibleRegionBottom);
//		
//		if(missingTop + missingBottom > elementLength * 0.5) {
//			// more than half of the element isn't visible
//			p = 0.5d;
//		} else {
//			p = 0.75d;
//		}

		return 
		(double)(Math.min(elementOffset + elementLength, visibleRegionOffset + visibleRegionLength) - 
			Math.max(elementOffset, visibleRegionOffset)) / (double)elementLength; 
	}
	
}
