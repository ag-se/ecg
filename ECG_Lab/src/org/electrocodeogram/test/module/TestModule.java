package org.electrocodeogram.test.module;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.ModuleProperty;
import org.electrocodeogram.msdt.MicroSensorDataType;
import org.electrocodeogram.msdt.registry.MicroSensorDataTypeRegistrationException;
import org.electrocodeogram.system.Core;
import org.electrocodeogram.system.ModuleSystem;

/**
 * This is a simple test module. every event that is received is
 * immediatly send.
 */
public class TestModule extends Module {

    private static Logger _logger = LogHelper.createLogger(TestModule.class
        .getName());

    /**
     * This creates the TestModule as a Module.INTERMEDIATE_MODULE
     */
    public TestModule() {
        super(ModuleType.INTERMEDIATE_MODULE,
            "org.electrocodeogram.module.TestModule", "TestModule");
    }

    /**
     * @see org.electrocodeogram.module.Module#receiveEventPacket(org.electrocodeogram.event.ValidEventPacket)
     */
    @Override
    public void receiveEventPacket(ValidEventPacket eventPacket) {
        sendEventPacket(eventPacket);

    }

    /**
     * @see org.electrocodeogram.module.Module#update()
     */
    @Override
    public void update() {
    // not needed

    }

    /*
     * (non-Javadoc)
     * @see org.electrocodeogram.module.Module#initialize()
     */
    @Override
    public void initialize() {
    // TODO Auto-generated method stub

    }

    public void registerMSDTs() {
        _logger.entering(this.getClass().getName(), "registerMSDTs");

        _logger.log(Level.INFO,
            "Registering predefined MSDTs dor SourceModule.");

        MicroSensorDataType[] msdts = ModuleSystem.getInstance()
            .getPredefinedMicroSensorDataTypes();

        if (msdts == null) {
            return;
        }

        for (MicroSensorDataType msdt : msdts) {
            try {
                ModuleSystem.getInstance().requestMsdtRegistration(msdt, this);

            } catch (MicroSensorDataTypeRegistrationException e) {
                _logger
                    .log(
                        Level.SEVERE,
                        "An Exception occured while registering predefined MSDTs for this SourceModule: "
                                        + this.getName());

                return;
            }
        }

        this._logger.exiting(this.getClass().getName(), "registerMSDTs");
    }

    /* (non-Javadoc)
     * @see org.electrocodeogram.module.Module#propertyChanged(org.electrocodeogram.module.ModuleProperty)
     */
    @Override
    protected void propertyChanged(ModuleProperty moduleProperty) {
        // TODO Auto-generated method stub
        
    }
}
