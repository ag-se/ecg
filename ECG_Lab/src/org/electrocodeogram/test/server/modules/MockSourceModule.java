/**
 * 
 */
package org.electrocodeogram.test.server.modules;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.module.ModuleProperty;
import org.electrocodeogram.module.source.EventReader;
import org.electrocodeogram.module.source.SourceModule;

/**
 *
 */
public class MockSourceModule extends SourceModule {

    public MockSourceModule() {
        super("org.electrocodeogram.test.server.modules.TestSourceModule",
            "TestSourceModule");
    }

    /**
     * @see org.electrocodeogram.module.Module#update()
     */
    @Override
    public void update() {
    //      Not used

    }

    /**
     * @see org.electrocodeogram.module.Module#initialize()
     */
    @Override
    public void initialize() {
    // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.electrocodeogram.module.Module#propertyChanged(org.electrocodeogram.module.ModuleProperty)
     */
    @Override
    protected void propertyChanged(ModuleProperty moduleProperty) {
    // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.electrocodeogram.module.source.SourceModule#getEventReader()
     */
    @Override
    public EventReader[] getEventReader() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.electrocodeogram.module.source.SourceModule#preStart()
     */
    @Override
    public void preStart() {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.electrocodeogram.module.source.SourceModule#postStop()
     */
    @Override
    public void postStop() {
        // TODO Auto-generated method stub
        
    }

    /**
     * @param event
     */
    public final void appendDirectly(final WellFormedEventPacket event) {
        this.append(event);
    }
}
