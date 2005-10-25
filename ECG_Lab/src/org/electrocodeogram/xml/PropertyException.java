/*
 * Class: PropertyException
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.xml;

import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * I
 *
 */
public class PropertyException extends Exception {

    private static Logger _logger = LogHelper
        .createLogger(PropertyException.class.getName());

    private static final long serialVersionUID = 4841697530729167223L;

    public PropertyException(String string) {

    // _logger.log(Level.WARNIGN, "An PropertyException occured");
    //	
    // _logger.log(Level.WARNING, this.getMessage());
    //		
    }
}