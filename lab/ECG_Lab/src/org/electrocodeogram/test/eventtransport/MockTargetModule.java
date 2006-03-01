/*
 * Classname: MockTargetModule
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.test.eventtransport;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.module.target.TargetModule;
import org.electrocodeogram.modulepackage.ModuleProperty;

/**
 * Is a {@link org.electrocodeogram.module.target.TargetModule} implementation
 * used by the {@link org.electrocodeogram.test.eventtransport.ModuleEventTransportTests}.
 * Instead of writing an event to a external location like any other <em>TargetModule</em> would do,
 * this module passes every received event to {@link org.electrocodeogram.test.eventtransport.ModuleTestHelper#comparePackets(ValidEventPacket)}.
 */
public class MockTargetModule extends TargetModule {

    /**
     * A reference to the object, which is doing the event comparison.
     */
    private ModuleTestHelper moduleTestHelper;

    /**
     * Creates the module.
     * @param helper Is the object that is doing the event comparison
     */
    public MockTargetModule(final ModuleTestHelper helper) {
        super(MockTargetModule.class.getName(), MockTargetModule.class
            .getSimpleName());

        this.moduleTestHelper = helper;
    }

    /**
     * @see org.electrocodeogram.module.target.TargetModule#write(org.electrocodeogram.event.ValidEventPacket)
     * Instead of writing an event to a external location
     * this passes every received event to {@link org.electrocodeogram.test.eventtransport.ModuleTestHelper#comparePackets(ValidEventPacket)}.
     */
    @Override
    public final void write(final ValidEventPacket eventPacket) {
        this.moduleTestHelper.comparePackets(eventPacket);

    }

    /**
     * @see org.electrocodeogram.module.target.TargetModule#initialize()
     * This is not implemented for this module.
     */
    @Override
    public void initialize() {
    // not implemented

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
     * @see org.electrocodeogram.module.target.TargetModule#startWriter()
     * This is not implemented for this module.
     */
    @Override
    public void startWriter() {
    // not implemented

    }

    /**
     * @see org.electrocodeogram.module.target.TargetModule#stopWriter()
     * This is not implemented for this module.
     */
    @Override
    public void stopWriter() {
    // not implemented

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
