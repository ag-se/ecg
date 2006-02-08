package org.electrocodeogram.module.intermediate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.xerces.parsers.DOMParser;
import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.modulepackage.ModuleProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.darwinsys.diff.Diff;

/**
 *
 */
public class CodechangeDifferIntermediateModule extends IntermediateModule {

    private static Logger logger = LogHelper
        .createLogger(CodechangeDifferIntermediateModule.class.getName());

    private Diff diff;

    private String lastCode;

    private ArrayList < ValidEventPacket > eventList;

    public enum DeltaType {
        CHANGE,

        INSERT,

        DELETE,

        MOVE,

        NOCHANGE
    }

    /**
     * @param arg0
     * @param arg1
     */
    public CodechangeDifferIntermediateModule(String arg0, String arg1) {
        super(arg0, arg1);
    }

    /**
     * @see org.electrocodeogram.module.intermediate.IntermediateModule#analyse(org.electrocodeogram.event.ValidEventPacket)
     */
    @Override
    public ValidEventPacket analyse(ValidEventPacket packet) {
        ValidEventPacket event = null;

        if (packet.getMicroSensorDataType().getName().equals(
            "msdt.codechange.xsd")) {
            if (this.lastCode == null) {
                this.lastCode = getCode(packet);

                return null;
            }

            Date timestamp = packet.getTimeStamp();

            String currentCode = getCode(packet);

            String data = "<?xml version=\"1.0\"?><microActivity><diff>\n";

            String msdt = "msdt.diff.xsd";

            String deltas = "";

            this.diff = new Diff();

            this.diff.doDiff(this.lastCode, currentCode);

            this.lastCode = currentCode;

            logger.log(Level.FINE, "Diff found " + this.diff.deltas.size()
                                   + " deltas");

            for (Delta delta : this.diff.deltas) {

                switch (delta.getType()) {
                    case CHANGE:

                        deltas += "<delta><type>changed</type><linenumber>"
                                  + delta.getLinenumber() + "</linenumber>"
                                  + "<from>" + delta.getFrom() + "</from><to>"
                                  + delta.getTo() + "</to></delta>\n";

                        break;

                    case INSERT:

                        deltas += "<delta><type>inserted before</type><linenumber>"
                                  + delta.getLinenumber()
                                  + "</linenumber>"
                                  + "<from>"
                                  + delta.getFrom()
                                  + "</from><to>"
                                  + delta.getTo() + "</to></delta>\n";

                        break;

                    case DELETE:

                        deltas += "<delta><type>deleted</type><linenumber>"
                                  + delta.getLinenumber() + "</linenumber>"
                                  + "<from>" + delta.getFrom() + "</from><to>"
                                  + delta.getTo() + "</to></delta>\n";

                        break;

                    case MOVE:

                        deltas += "<delta><type>moved</type><linenumber>"
                                  + delta.getLinenumber() + "</linenumber>"
                                  + "<from>" + delta.getFrom() + "</from><to>"
                                  + delta.getTo() + "</to></delta>\n";

                        break;

                    default:
                        break;
                }
            }

            data += deltas;

            data += "</diff></microActivity>";

            logger.log(Level.FINE, data);

            String[] args = {WellFormedEventPacket.HACKYSTAT_ADD_COMMAND, msdt,
                data};

            try {
                event = new ValidEventPacket(this.getId(), timestamp,
                    WellFormedEventPacket.HACKYSTAT_ACTIVITY_STRING, Arrays
                        .asList(args));

            } catch (IllegalEventParameterException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        return event;

    }

    /**
     * @param code
     * @return
     * @throws IOException 
     */
    private String[] getLines(String code) throws IOException {

        if (code == null) {
            return null;
        }

        ArrayList < String > lines = new ArrayList < String >();

        BufferedReader reader = new BufferedReader(new StringReader(code));

        String line = null;

        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }

        return lines.toArray(new String[0]);

    }

    private String getCode(ValidEventPacket packet) {
        Object object = packet.getArgList().get(
            ValidEventPacket.MICROACTIVITY_INDEX);

        assert (object instanceof String);

        String microActivity = (String) object;

        Document document = null;

        InputSource inputSource = new InputSource(new StringReader(
            microActivity));

        DOMParser parser = new DOMParser();

        try {
            parser.parse(inputSource);
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        document = parser.getDocument();

        Node documentNode = document.getElementsByTagName("document").item(0);

        return documentNode.getFirstChild().getNodeValue();

    }

    /**
     * @param propertyName
     * @param propertyValue
     */
    @Override
    public void propertyChanged(ModuleProperty moduleProperty) {

    }

    /**
     * @see org.electrocodeogram.module.Module#analyseCoreNotification()
     */
    @Override
    public void update() {

    }

    /**
     * @see org.electrocodeogram.module.intermediate.IntermediateModule#initialize()
     */
    @Override
    public void initialize() {

        this.setProcessingMode(ProcessingMode.FILTER);

        this.diff = new Diff();

        this.eventList = new ArrayList < ValidEventPacket >();

    }

}
