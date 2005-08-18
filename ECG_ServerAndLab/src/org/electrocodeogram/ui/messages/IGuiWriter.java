/**
 * 
 */
package org.electrocodeogram.ui.messages;

import java.util.Observer;

/**
 *
 */
public interface IGuiWriter extends Observer
{

    /**
     * @param frame
     */
    void setTarget(MessagesFrame frame);

}
