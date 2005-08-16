package org.electrocodeogram.sensor.eclipse;

import java.util.TimerTask;

import org.eclipse.jface.text.IDocument;

/**
 * The CodeChangeTimerTask is a TimerTask that is sending codechange events
 * after a predefined time interval. 
 *
 */
public class CodeChangeTimerTask extends TimerTask
{

    private IDocument $document = null;
    
    private String $documentName = null;

    /**
     * This creates the Task.
     * @param document Is the document in which the codechange has occured.
     * @param documentName Is the name of the document the codechange has occured.
     */
    public CodeChangeTimerTask(IDocument document, String documentName)
    {
       this.$document = document;
       
       this.$documentName = documentName;
    }

    /**
     * @see java.util.TimerTask#run()
     */
    @Override
    public void run()
    {
        ECGEclipseSensor sensor = ECGEclipseSensor.getInstance();

        sensor.processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+sensor.getUsername()+"</username><projectname>"+sensor.getProjectname()+"</projectname></commonData><codechange><document><![CDATA["+this.$document.get()+"]]></document><documentname>"+this.$documentName+"</documentname></codechange></microActivity>");

    }
}