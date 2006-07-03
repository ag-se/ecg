/*
 * Classname: MockSourceModule
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.test.eventtransport;

import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.module.source.EventReader;
import org.electrocodeogram.module.source.ServerModule;
import org.electrocodeogram.module.source.SourceModule;
import org.electrocodeogram.modulepackage.ModuleProperty;

/**
 * Is a {@link org.electrocodeogram.module.source.SourceModule} implementation
 * used by the {@link org.electrocodeogram.test.eventtransport.ModuleEventTransportTests}.
 * Instead of reading events from an external location like any other <em>SourceModule</em> is doing,
 * the method {@link #appendDirectly(WellFormedEventPacket)} is called to pass
 * an event dircetly into this module.
 */
public class MockSourceModule extends SourceModule implements ServerModule {

    /**
     * Creates the module.
     *
     */
    public MockSourceModule() {
        super(MockSourceModule.class.getName(), MockSourceModule.class
            .getSimpleName());
    }

    /**
     * @see org.electrocodeogram.module.Module#update()
     * This is not implemented for this module.
     */
    @Override
    public void update() {
    // Not implemented

    }

    /**
     * @see org.electrocodeogram.module.Module#initialize()
     * This is not implemented for this module.
     */
    @Override
    public void initialize() {
    // Not implemented
    }

    /**
     * @see org.electrocodeogram.module.Module#propertyChanged(org.electrocodeogram.modulepackage.ModuleProperty)
     * This is not implemented for this module.
     */
    @Override
    protected void propertyChanged(@SuppressWarnings("unused")
    final ModuleProperty moduleProperty) {
    // Not implemented

    }

    /**
     * @see org.electrocodeogram.module.source.SourceModule#getEventReader()
     * This is not implemented for this module.
     */
    @Override
    public final EventReader[] getEventReader() {
        // Not implemented
        return null;
    }

    /**
     * @see org.electrocodeogram.module.source.SourceModule#preStart()
     * This is not implemented for this module.
     */
    @Override
    public void preStart() {
        // Not implemented

    }

    /**
     * @see org.electrocodeogram.module.source.SourceModule#postStop()
     * This is not implemented for this module.
     */
    @Override
    public void postStop() {
    // Not implemented
    }

    /**
     * This is the only addition to a {@link SourceModule}. This method
     * is used by {@link ModuleTestHelper} to directly pass an event into
     * this module.
     * Normally a <em>SourceModule</em> has to read in events by using
     * an {@link EventReader}. This is not appropriate here.
     * @param event Is the event
     */
    public final void appendDirectly(final WellFormedEventPacket event) {
        this.append(event);
    }
}
