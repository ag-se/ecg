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

		return 
		(double)(Math.min(elementOffset + elementLength, visibleRegionOffset + visibleRegionLength) - 
			Math.max(elementOffset, visibleRegionOffset)) / (double)elementLength; 
	}
	
	public boolean isFillingCompleteView() {
		return (elementOffset <= visibleRegionOffset && elementLength + elementOffset >= visibleRegionLength + visibleRegionOffset);
	}
	
	public double getPercentageOfView() {
		return 
		(double)(Math.min(elementOffset + elementLength, visibleRegionOffset + visibleRegionLength) - 
			Math.max(elementOffset, visibleRegionOffset)) / (double)visibleRegionLength; 
	}
	
}
