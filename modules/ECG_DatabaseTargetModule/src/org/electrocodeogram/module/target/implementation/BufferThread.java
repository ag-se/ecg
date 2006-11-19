package org.electrocodeogram.module.target.implementation;

import org.electrocodeogram.event.ValidEventPacket;


public class BufferThread extends Thread {
	
	DatabaseTargetModule dbTargetModule;
	
	DBCommunicator dbCommunicator;

	
	public BufferThread(DatabaseTargetModule dbTarget){
		this.dbTargetModule = dbTarget;
		this.dbCommunicator = dbTarget.getDbCommunicator();
	}
	
	
	public void run(){
		while (true){
			if(dbCommunicator.getDBConnection()== null){
				try {
					this.wait(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			
			EventBuffer eventBuffer = dbTargetModule.getEventBuffer();
			ValidEventPacket currentPacket = eventBuffer.get();
			if(dbCommunicator.insertEvent(currentPacket)){
				continue;
			}
			else{
				dbTargetModule.getEventBuffer().put(currentPacket);
				try {
					this.wait(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}

}
