package org.electrocodeogram.module.intermediate;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.Module;

/**
 * This abstract class shall be subclassed for every new intermediate module.
 * The abstract method analyse is to be implemented to do the actual analysis that is
 * required by the module.
 */
public abstract class IntermediateModule extends Module implements IIntermediateModule
{

	private static Logger _logger = LogHelper.createLogger(IntermediateModule.class.getName());

	/**
	 * If an intermediate module is operating in annotation mode, the AnnotationStyle
	 * tells when the module will send its annotation events.
	 *
	 */
	public enum AnnotationStyle
	{
		/**
		 * The module will first send an annotation event(s) and then send the original event(s). 
		 */
		PRE_ANNOTATION,

		/**
		 * The module will first send the original event(s) and then send the an annotation event(s). 
		 */
		POST_ANNOTATION
	}

	/**
	 * 
	 * The ProcessingMode defines whether the module operates as an annotator or as a filter.
	 *
	 */
	public enum ProcessingMode
	{
		/**
		 * The module operates as an annotator.
		 */
		ANNOTATOR,

		/**
		 * The module operates as a filter.
		 */
		FILTER
	}

	private String _separator = "";

	private ProcessingMode _processingMode;

	private AnnotationStyle _annotationStyle;

	/**
	 * This creates a new IntermediateModule with the given processing mode.
	 * @param moduleClassId Is the id of the module class as registered with the ModuleRegistry
	 * @param name The name given to this moduule instance
	 */
	public IntermediateModule(String moduleClassId, String name)
	{
		super(ModuleType.INTERMEDIATE_MODULE, moduleClassId, name);

		_logger.entering(this.getClass().getName(), "IntermediateModule");

		this._processingMode = ProcessingMode.ANNOTATOR;

		this._annotationStyle = AnnotationStyle.POST_ANNOTATION;

		initialize();

		_logger.exiting(this.getClass().getName(), "IntermediateModule");

	}

	/**
	 * This method returns the annotation style that is set for the module.
	 * @return The annotation style
	 */
	public AnnotationStyle getAnnnotationStyle()
	{
		_logger.entering(this.getClass().getName(), "getAnnnotationStyle");

		_logger.exiting(this.getClass().getName(), "getAnnnotationStyle");

		return this._annotationStyle;
	}

	/**
	 * This method is used to set the annotation style of the module to the given annotation style.
	 * @param annotationStyle Is the new annotation style of the module 
	 */
	public void setAnnnotationStyle(AnnotationStyle annotationStyle)
	{
		_logger.entering(this.getClass().getName(), "setAnnnotationStyle");

		if (annotationStyle == null)
		{
			_logger.log(Level.WARNING, "annotationStyle is null");

			return;
		}

		this._annotationStyle = annotationStyle;

		_logger.exiting(this.getClass().getName(), "setAnnnotationStyle");
	}

	/**
	 * This returns the processing mode the module is operating in.
	 * @return The processing mode
	 */
	public ProcessingMode getProcessingMode()
	{
		_logger.entering(this.getClass().getName(), "getProcessingMode");

		_logger.exiting(this.getClass().getName(), "getProcessingMode");

		return this._processingMode;

	}

	/**
	 * This sets the processing mode of the module to the given mode.
	 * @param processingMode Is the new processing mode of the module.
	 */
	public void setProcessingMode(ProcessingMode processingMode)
	{
		_logger.entering(this.getClass().getName(), "setProcessingMode");

		if (processingMode == null)
		{
			_logger.log(Level.WARNING, "processingMode is null");

			return;
		}

		this._processingMode = processingMode;

		_logger.exiting(this.getClass().getName(), "setProcessingMode");

	}

	/**
	 * This method returns the separator string that this module uses.
	 * @return The separator string
	 */
	public String getSeparator()
	{
		_logger.entering(this.getClass().getName(), "getSeparator");

		_logger.exiting(this.getClass().getName(), "getSeparator");

		return this._separator;
	}

	/**
	 * This sets the separator string of the module to the given string value.
	 * @param separator Is the string value to use a the new separator string
	 */
	public void setSeparator(String separator)
	{
		_logger.entering(this.getClass().getName(), "setSeparator");

		if (separator == null)
		{
			_logger.log(Level.WARNING, "separator is null");

			return;
		}

		this._separator = separator;

		_logger.exiting(this.getClass().getName(), "setSeparator");
	}

	/**
	 * @see org.electrocodeogram.module.Module#receiveEventPacket(org.electrocodeogram.event.ValidEventPacket)
	 * In addition to its superclass method this method gets the analysis result events of the module
	 * and sends them according to the processing mode and annotation style of the module.
	 */
	@Override
	public final void receiveEventPacket(ValidEventPacket eventPacket)
	{

		_logger.entering(this.getClass().getName(), "receiveEventPacket");

		if (eventPacket == null)
		{
			_logger.log(Level.WARNING, "eventPacket is null");

			return;
		}

		if (this._processingMode == ProcessingMode.ANNOTATOR)
		{
			ValidEventPacket resultPacket = getAnalysisResult(eventPacket);

			if (this._annotationStyle == AnnotationStyle.PRE_ANNOTATION)
			{
				sendEventPacket(resultPacket);
				sendEventPacket(eventPacket);
			}
			else
			{
				sendEventPacket(eventPacket);
				sendEventPacket(resultPacket);
			}
		}
		else
		{
			ValidEventPacket resultPacket = getAnalysisResult(eventPacket);

			sendEventPacket(resultPacket);
		}

		_logger.exiting(this.getClass().getName(), "receiveEventPacket");
	}

	private ValidEventPacket getAnalysisResult(ValidEventPacket eventPacket)
	{
		_logger.entering(this.getClass().getName(), "getAnalysisResult");

		_logger.exiting(this.getClass().getName(), "getAnalysisResult");

		return analyse(eventPacket);
	}

	/**
	 * This method is to be implemented by all subclassing intermediate modules.
	 * For any given input event it shall compute and return an output event.
	 * That is the analysis result.
	 * @param eventPacket Is the original incoming event data
	 * @return The data of an event that is a result of the analysis
	 */
	public abstract ValidEventPacket analyse(ValidEventPacket eventPacket);

	/**
	 * @see org.electrocodeogram.module.Module#initialize()
	 */
	@Override
	public abstract void initialize();
}