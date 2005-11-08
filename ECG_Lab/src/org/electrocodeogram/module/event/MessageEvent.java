/**
 * 
 */
package org.electrocodeogram.module.event;

/**
 *
 */
public class MessageEvent {

    public enum MessageType {
        ERROR, WARNING, INFO, QUESTION
    }

    private String moduleName;

    private int moduleId;

    private String message;

    private MessageType messageType;

    public MessageEvent(String msg, MessageType type, String name, int id) {
        this.message = msg;

        this.messageType = type;

        this.moduleName = name;

        this.moduleId = id;
    }

    public String getMessage() {
        return this.message;
    }

    public MessageType getMessageType() {
        return this.messageType;
    }

    
    public int getModuleId() {
        return this.moduleId;
    }

    
    public String getModuleName() {
        return this.moduleName;
    }

}
