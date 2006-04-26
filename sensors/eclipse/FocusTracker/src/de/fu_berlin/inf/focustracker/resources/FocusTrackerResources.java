package de.fu_berlin.inf.focustracker.resources;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import de.fu_berlin.inf.focustracker.FocusTrackerPlugin;

public class FocusTrackerResources {

	private static Map<ImageDescriptor, Image> imageMap = new HashMap<ImageDescriptor, Image>();

	private static final URL baseURL = FocusTrackerPlugin.getDefault().getBundle().getEntry("/icons/");

	public static final ImageDescriptor CLEAR = create("clear.gif");
	public static final ImageDescriptor RESTORE_LOG = create("restore_log.gif");
	public static final ImageDescriptor PIN_EDITOR = create("pin_editor.gif");
	
	private static ImageDescriptor create(String name) {
		try {
			return ImageDescriptor.createFromURL(new URL(baseURL, name));
		} catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	/**
	 * Lazily initializes image map.
	 */
	public static Image getImage(ImageDescriptor imageDescriptor) {
		Image image = imageMap.get(imageDescriptor);
		if (image == null) {
			image = imageDescriptor.createImage();
			imageMap.put(imageDescriptor, image);
		}
		return image;
	}	
}
