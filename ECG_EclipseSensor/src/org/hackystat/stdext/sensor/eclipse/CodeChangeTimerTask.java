package org.hackystat.stdext.sensor.eclipse;

import java.util.TimerTask;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

public class CodeChangeTimerTask extends TimerTask
{
    
//    private static int BUFFER_SIZE = 300;
//    
//    private static char[] recordedString = new char[BUFFER_SIZE];
//    
//    private static int minOffset = -1;
//    
//    private static int maxOffset = -1;
//    
//    private static int pivotOffset = -1;
//
//    private static int currentOffset = -1;
    
    private static IDocument oldDocument = null;
    
    private static IDocument newDocument = null;
    
    private static boolean waiting = false;
    
    
//    public CodeChangeTimerTask(IDocument document, int offset, String text)
//    {
//    // TODO : handle overflow
//        assert(text.toCharArray().length == 1);
//        
//        if(CodeChangeTimerTask.pivotOffset == -1)
//        {
//            
//            CodeChangeTimerTask.document = new Document();
//            
//            CodeChangeTimerTask.document.set(document.get());
//            
//            pivotOffset = offset;
//            
//            minOffset = offset;
//            
//            maxOffset = offset;
//            if(text.equals(""))
//            {
//                recordedString[BUFFER_SIZE/2 + currentOffset] = Character.UNASSIGNED;
//            }
//            else
//            {
//                recordedString[BUFFER_SIZE/2] = text.toCharArray()[0];
//            }
//        }
//        else
//        {
//            if(offset < minOffset) minOffset = offset;
//            
//            if(offset > maxOffset) maxOffset = offset;
//            
//            currentOffset = offset - pivotOffset;
//            
//            if(text.equals(""))
//            {
//                //maxOffset--;
//                recordedString[BUFFER_SIZE/2 + currentOffset] = Character.UNASSIGNED;
//            }
//            else
//            {
//                recordedString[BUFFER_SIZE/2 + currentOffset] = text.toCharArray()[0];
//            }
//            
//        }
//        
//    }
    
    CodeChangeTimerTask(IDocument oldDocument, IDocument newDocument)
    {
        
        if(!waiting)
        {
            CodeChangeTimerTask.oldDocument = new Document();
            
            CodeChangeTimerTask.oldDocument.set(oldDocument.get());
            
            CodeChangeTimerTask.newDocument = newDocument;
            
            waiting = true;
        }
        else
        {
            CodeChangeTimerTask.newDocument = newDocument;
        }
        
    }

    public void run()
    {
        EclipseSensor sensor = EclipseSensor.getInstance();
      
//        int firstIndex = BUFFER_SIZE/2 - (minOffset - pivotOffset);
//        
//        int lastIndex = BUFFER_SIZE/2 + (maxOffset - pivotOffset);
//        
//        String newString = new String();
//        
//        int j = 0;
//        
//        for(int i=firstIndex;i<=lastIndex;i++)
//        {
//            if(recordedString[i] == Character.UNASSIGNED)
//            {
//                continue;
//            }
//            newString += recordedString[i];
//        }
//        
//        String oldString = "UNKNOWN";
//        
//        try {
//            oldString = document.get(minOffset,maxOffset-minOffset);
//        }
//        catch (BadLocationException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        
        sensor.processStateChangeActivity(CodeChangeTimerTask.oldDocument,CodeChangeTimerTask.newDocument);
        
//        document = null;
//        
//        pivotOffset = -1;
//        
//        minOffset = -1;
//        
//        maxOffset = -1;
//        
//        currentOffset = -1;
//        
//        recordedString = new char[300];
    }
}