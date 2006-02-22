package org.electrocodeogram.test.module;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.module.Module;
import org.electrocodeogram.modulepackage.ModuleProperty;
import org.electrocodeogram.modulepackage.ModuleType;
import org.electrocodeogram.msdt.MicroSensorDataType;
import org.electrocodeogram.msdt.registry.MicroSensorDataTypeRegistrationException;
import org.electrocodeogram.system.ModuleSystem;

/**
 * This is a simple test module. every event that is received is
 * immediatly sent by it.
 */
public class TestModule extends Module {

    /**
     * Creates the TestModule.
     */
    public TestModule() {
        super(ModuleType.INTERMEDIATE_MODULE,
            "org.electrocodeogram.module.TestModule", "TestModule");
    }

    /**
     * @see org.electrocodeogram.module.Module#receiveEventPacket(org.electrocodeogram.event.ValidEventPacket)
     * This is simply sending the received event.
     */
    @Override
    public final void receiveEventPacket(final ValidEventPacket eventPacket) {
        sendEventPacket(eventPacket);

    }

    /**
     * @see org.electrocodeogram.module.Module#update()
     * This is not implemented for this module.
     */
    @Override
    public void update() {
    // not implemented

    }

    /**
     * @see org.electrocodeogram.module.Module#initialize()
     * This is not implemented for this module.
     */
    @Override
    public void initialize() {
    // not implemented

    }

    /**
     * This is an addition to {@link org.electrocodeogram.module.intermediate.IntermediateModule}.
     * It is used to force the registration of the predefined <em>MicroSensorDataTypes</em>
     * for testing purposes.
     *
     */
    public final void registerMSDTs() {
        MicroSensorDataType[] msdts = ModuleSystem.getInstance()
            .getPredefinedMicroSensorDataTypes();

        if (msdts == null) {
            return;
        }

        for (MicroSensorDataType msdt : msdts) {
            try {
                ModuleSystem.getInstance().requestMsdtRegistration(msdt, this);

            } catch (MicroSensorDataTypeRegistrationException e) {

                return;
            }
        }
    }

    /**
     * @see org.electrocodeogram.module.Module#propertyChanged(org.electrocodeogram.modulepackage.ModuleProperty)
     * This is not implemented for this module.
     */
    @Override
    protected void propertyChanged(@SuppressWarnings("unused")
    final ModuleProperty moduleProperty) {
    // not implemented

    }
}
