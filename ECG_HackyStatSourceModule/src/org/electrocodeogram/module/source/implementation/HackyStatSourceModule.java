// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   HackyStatSourceModule.java

package org.electrocodeogram.module.source.implementation;

import org.electrocodeogram.module.source.EventReader;
import org.electrocodeogram.module.source.ServerModule;
import org.electrocodeogram.module.source.SourceModule;
import org.electrocodeogram.module.source.SourceModuleException;
import org.electrocodeogram.modulepackage.ModuleProperty;
import org.electrocodeogram.modulepackage.ModulePropertyException;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpServer;
import org.mortbay.http.SocketListener;

// Referenced classes of package org.electrocodeogram.module.source.implementation:
//            HackyEventReader, HackyHandler

public class HackyStatSourceModule extends SourceModule
    implements ServerModule
{
    
    private HttpServer server;
    
    private HackyEventReader eventReader;

    public HackyStatSourceModule(String id, String name)
    {
        super(id, name);
    }

    public void initialize()
    {
        this.eventReader = new HackyEventReader(this);
    }

    public EventReader[] getEventReader()
    {
        return new EventReader[] {this.eventReader};
    }

    public void preStart()
        throws SourceModuleException
    {
        this.server = new HttpServer();
        
        SocketListener listener = new SocketListener();
        
        listener.setPort(10557);
        
        this.server.addListener(listener);
        
        HttpContext context = new HttpContext();
        
        context.setContextPath("/");
        
        this.server.addContext(context);
        
        org.mortbay.http.HttpHandler handler = new HackyHandler(this.eventReader);
        
        context.addHandler(handler);
        
        
        try
        {
            this.server.start();
        }
        catch(Exception e)
        {
            throw new SourceModuleException(e.getMessage(), getName());
        }
    }

    public void postStop()
    {
        if(this.server != null)
        {
            try {
                this.server.stop();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
    }

    public void update()
    {
    }

    protected void propertyChanged(ModuleProperty moduleproperty)
        throws ModulePropertyException
    {
    }

    
}
