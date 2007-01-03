/*
 * (c) Freie Universität Berlin - AG SoftwareEngineering - 2006
 */

package org.electrocodeogram.module.intermediate.implementation;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.intermediate.IntermediateModule;
import org.electrocodeogram.modulepackage.ModuleProperty;
import org.electrocodeogram.modulepackage.ModulePropertyException;

/**
 * The EpisodeRecognizer container for an EpisodeRecognizer implementation. It
 * can contain exactly one kind of EpisodeRecognizer. If more than one
 * EpisodeRecognizer must be used in an ECGLab experimentation, each one
 * requires a new EpisodeRecognizerIntermediateModule instance configured with a
 * different EpisodeRecognizer kind. See the documentation in
 * module.properties.xml for the module's configuaration.
 */
public class EpisodeRecognizerIntermediateModule extends IntermediateModule {

    /**
     * Java package which includes the recognizers.
     */
    private static String BASE_RECOGNIZER_DIR = 
        "org.electrocodeogram.module.intermediate.implementation.recognizers.";
    /**
     * Log instance.
     */
    private static Logger logger = LogHelper
            .createLogger(EpisodeRecognizerIntermediateModule.class.getName());

    /**
     * Generally useful time format used in ECG.
     */
    public static SimpleDateFormat dateFormat = new SimpleDateFormat(
            WellFormedEventPacket.DATE_FORMAT_PATTERN);

    /**
     * Manager to hold a set of similiar EpisodeRecognizers.
     */
    private EpisodeRecognizerManager manager = null;

    /**
     * Minimal allowed duration of an episode.
     */
    private long minDuration = 0;

    /**
     * Standard IntermediateModule constructor.
     * 
     * @param arg0
     * @param arg1
     */
    public EpisodeRecognizerIntermediateModule(String arg0, String arg1) {
        super(arg0, arg1);
    }

    /**
     * @see org.electrocodeogram.module.intermediate.implementation.IntermediateModule#analyse(org.electrocodeogram.event.ValidEventPacket)
     */
    @Override
    public Collection<ValidEventPacket> analyse(ValidEventPacket packet) {

        return manager.analyse(packet);

    }

    /**
     * Returns minimal duration.
     * 
     * @return minimal allowed duration of an episode
     */
    public final long getMinDuration() {
        return minDuration;
    }

    /**
     * @see org.electrocodeogram.module.intermediate.implementation.IntermediateModule#propertyCahnged() 
     * @param propertyName
     * @param propertyValue
     */
    @Override
    public final void propertyChanged(final ModuleProperty moduleProperty)
            throws ModulePropertyException {

        logger.entering(this.getClass().getName(), "propertyChanged",
                new Object[] { moduleProperty });

        if (moduleProperty.getName().equals("Recognizer")) {

            logger.log(Level.INFO, "Request to set the property: "
                    + moduleProperty.getName());

            if (moduleProperty.getValue() == null) {
                logger.log(Level.WARNING, "The property value is null for: "
                        + moduleProperty.getName());

                logger.exiting(this.getClass().getName(), "propertyChanged");

                throw new ModulePropertyException(
                        "The property value is null.", this.getName(), this
                                .getId(), moduleProperty.getName(),
                        moduleProperty.getValue());
            }

            // Creates manager for this recognizer
            String recognizerClassName = 
                BASE_RECOGNIZER_DIR + moduleProperty.getValue();
                
            this.manager = new EpisodeRecognizerManager(this, recognizerClassName);

        } else if (moduleProperty.getName().equals("MinDuration")) {

            logger.log(Level.INFO, "Request to set the property: "
                    + moduleProperty.getName());

            if (moduleProperty.getValue() == null) {
                logger.log(Level.WARNING, "The property value is null for: "
                        + moduleProperty.getName());

                logger.exiting(this.getClass().getName(), "propertyChanged");

                throw new ModulePropertyException(
                        "The property value is null.", this.getName(), this
                                .getId(), moduleProperty.getName(),
                        moduleProperty.getValue());
            }

            this.minDuration = Long.parseLong(moduleProperty.getValue());

        } else {
            logger.log(Level.WARNING,
                    "The module does not support a property with the given name: "
                            + moduleProperty.getName());

            logger.exiting(this.getClass().getName(), "propertyChanged");

            throw new ModulePropertyException(
                    "The module does not support this property.", this
                            .getName(), this.getId(), moduleProperty.getName(),
                    moduleProperty.getValue());

        }

        logger.exiting(this.getClass().getName(), "propertyChanged");
    }

    /**
     * @see org.electrocodeogram.module.Module#analyseCoreNotification()
     */
    @Override
    public void update() {

    }

    /**
     * @see org.electrocodeogram.module.intermediate.implementation.IntermediateModule#initialize()
     */
    @Override
    public void initialize() {

        this.setProcessingMode(ProcessingMode.ANNOTATOR);

    }

}
