/*
 * Class: NoTestDataException
 * Version: 1.0
 * Date: 19.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.test;

/**
 * If a pseudorandom String is requested by a line number that is not available or if the requested string length is to higher then available
 * this exception is thrown.
 *
 */
public class NoTestDataException extends Exception {

    /**
     * The <em>Serialization</em> id.
     */
    private static final long serialVersionUID = -2177108636519661021L;

}
