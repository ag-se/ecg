/*
 * Class: MessageEvent
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.event;

/**
 * Represents a message from a module that shall be displayed in the
 * GUI of the ECG Lab. It is used in the
 * {@link org.electrocodeogram.module.Module.GuiNotificator#fireMessageNotification(MessageEvent)}.
 */
public class MessageEvent {

    /**
     * The type of the message. The names are self-speaking.
     */
    public enum MessageType {
        ERROR, WARNING, INFO, QUESTION
    }

    /**
     * The name of themodule that generated this message.
     */
    private String moduleName;

    /**
     * The id of themodule that generated this message.
     */
    private int moduleId;

    /**
     * The message to display.
     */
    private String message;

    /**
     * The type of the message.
     */
    private MessageType messageType;

    /**
     * Creates a new message for the GUI to be displayed.
     * @param msg
     *            Is the message
     * @param type
     *            Is the message type
     * @param name
     *            Is the name of the module generating the message
     * @param id
     *            Is the id of the module generating the message
     */
    public MessageEvent(final String msg, final MessageType type,
        final String name, final int id) {
        this.message = msg;

        this.messageType = type;

        this.moduleName = name;

        this.moduleId = id;
    }

    /**
     * Gets the message.
     * @return the message
     */
    public final String getMessage() {
        return this.message;
    }

    /**
     * Gets the message type.
     * @return The type of the message
     */
    public final MessageType getMessageType() {
        return this.messageType;
    }

    /**
     * The id of the module generating this message.
     * @return id of the module generating this message
     */
    public final int getModuleId() {
        return this.moduleId;
    }

    /**
     * The name of the module generating this message.
     * @return The name of the module generating this message
     */
    public final String getModuleName() {
        return this.moduleName;
    }

}
