import java.util.Date;


import org.electrocodeogram.module.annotator.EventAnnotator;
import org.electrocodeogram.sensorwrapper.EventPacket;


/*
 * Created on 10.03.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
/**
 * @author 7oas7er
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LineNumberAnnotator extends EventAnnotator
{
    private int lineCounter = 0;

    /**
     * @param name
     */
    public LineNumberAnnotator()
    {
        super(EventAnnotator.PRE_ANNOTATION,":\n","LineNumberAnnotator");
        //this.addObserver(new LoggerEventWriter("LoggereventWriter"));
    }

    /* (non-Javadoc)
     * @see net.datenfabrik.microstat.annotator.EventAnnotator#annotate(java.lang.String)
     */
    public EventPacket annotate(EventPacket eventPacket)
    {
        return new EventPacket(this.getId(),new Date(),new Integer(++lineCounter).toString(),null);
    }

}
