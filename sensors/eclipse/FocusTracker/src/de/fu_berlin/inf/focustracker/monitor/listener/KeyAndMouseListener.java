package de.fu_berlin.inf.focustracker.monitor.listener;

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;

public class KeyAndMouseListener implements KeyListener, MouseListener, MouseMoveListener {

	// keylistener
	public void keyPressed(KeyEvent aE) {
		System.err.println("keyPressed");
	}

	public void keyReleased(KeyEvent aE) {
		System.err.println("keyReleased");
	}

	// mouselistener
	public void mouseDoubleClick(MouseEvent aE) {
		System.err.println("mouseDoubleClick");
	}

	public void mouseDown(MouseEvent aE) {
		System.err.println("mouseDown");
	}

	public void mouseUp(MouseEvent aE) {
		System.err.println("mouseUp");
	}

	// mousemovelistener
	public void mouseMove(MouseEvent aE) {
		System.err.println("mouseMove " + aE.x + " - " + aE.y);
	}
	
}
