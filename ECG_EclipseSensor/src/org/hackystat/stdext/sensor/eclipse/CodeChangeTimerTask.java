package org.hackystat.stdext.sensor.eclipse;

import java.util.TimerTask;

public class CodeChangeTimerTask extends TimerTask
{
    
    private static String text = "";

    public CodeChangeTimerTask(String text)
    {
        CodeChangeTimerTask.text += text;
    }
    
    public void run()
    {
        EclipseSensor sensor = EclipseSensor.getInstance();
      
        sensor.processStateChangeActivity(CodeChangeTimerTask.text);
        
        CodeChangeTimerTask.text = "";
    }
}