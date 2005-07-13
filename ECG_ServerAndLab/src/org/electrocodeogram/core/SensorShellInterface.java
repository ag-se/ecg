package org.electrocodeogram.core;

import java.util.Date;
import java.util.List;

public interface SensorShellInterface
{
    public boolean doCommand(Date timeStamp, String commandName, List argList);
}
