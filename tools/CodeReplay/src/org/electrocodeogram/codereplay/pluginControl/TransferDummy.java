package org.electrocodeogram.codereplay.pluginControl;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.electrocodeogram.codereplay.dataProvider.TreeNode;

/**
 * Dummy class providing a transfer instance, needed to activate
 * drag'n drop support on the treeviewer.
 * 
 * @author marco kranz
 */
public class TransferDummy extends ByteArrayTransfer {

	private static final String MYTYPENAME = "Dummy";
	private static final int MYTYPEID = registerType(MYTYPENAME);
	private static TransferDummy instance = new TransferDummy();

	/**
	 * We just don't need more than one of this...
	 * 
	 * @return singleton instance
	 */
	public static TransferDummy getInstance () {
		return instance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.Transfer#getTypeNames()
	 */
	public String [] getTypeNames () {
		return new String [] {MYTYPENAME};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.Transfer#getTypeIds()
	 */
	public int [] getTypeIds () {
		return new int [] {MYTYPEID};
	}

	
	/**
	 * Validate is actually needed, its used to check whether an item can be droped in a control or not.
	 * Only treeparent objects are allowed to be droped into our tree, this is what this method assures.
	 * (called eclipse-internally)
	 */
	public boolean validate(Object object) {
		if (object == null || 
				!(object instanceof TreeNode[]) || 
				((TreeNode[])object).length == 0) {
				return false;
			}
			TreeNode[] myTypes = (TreeNode[])object;
			for (int i = 0; i < myTypes.length; i++) {
				if (myTypes[i] == null || 
					myTypes[i].getIdentifier() == null || 
					myTypes[i].getIdentifier().length() == 0) {
					return false;
				}
			}
			return true;
	}
	
	/**
	 * Not needed(not implemented)
	 */
	public void javaToNative(Object object, TransferData transferData) {
		//System.out.println("javaToNative() object: "+object);
		/*if (!validate(object) || !isSupportedType (transferData)) {
			DND.error(DND.ERROR_INVALID_DATA);
		}
		TreeParent[] myTypes = (TreeParent[])object;
		try {
			// write data to a byte array and then ask super to convert to pMedium
			ByteArrayOutputStream out = new ByteArrayOutputStream ();
			DataOutputStream writeOut = new DataOutputStream (out);
			for (int i = 0, length = myTypes.length; i < length; i++) {
				byte [] buffer = myTypes[i].getIdentifier().getBytes ();
				writeOut.writeInt (buffer.length);
				writeOut.write (buffer);
			}
			byte [] buffer = out.toByteArray ();
			writeOut.close ();
			super.javaToNative (buffer, transferData);
		}
		catch(IOException e) {}*/
	}
	/**
	 * Not needed(not implemented)
	 */
	public Object nativeToJava (TransferData transferData) {
		//System.out.println("nativeToJava() called");
		/*if (isSupportedType(transferData)) {
			byte[] buffer = (byte[]) super.nativeToJava(transferData);
			if(buffer == null)
				return null;

			TreeParent[] myData = new TreeParent[0];
			try {
				ByteArrayInputStream in = new ByteArrayInputStream(buffer);
				DataInputStream readIn = new DataInputStream(in);
				while (readIn.available () > 20) {
					int size = readIn.readInt ();
					byte [] name = new byte[size];
					readIn.read(name);
					TreeParent id = new TreeParent(new Replay(new ReplayElement(null, null, null, null, new String(name), null, null)), "bla");
					TreeParent[] newMyData = new TreeParent[myData.length + 1];
					System.arraycopy(myData, 0, newMyData, 0, myData.length);
					newMyData[myData.length] = id;
					myData = newMyData;
				}
				readIn.close();
			}
			catch(IOException ex) {
				return null;
			}
			System.out.println("nativeToJava() mydata: "+myData);
			return myData;
		}*/
		return null;
	}
}
