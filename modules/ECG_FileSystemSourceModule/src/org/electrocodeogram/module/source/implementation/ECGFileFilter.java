/**
 * 
 */
package org.electrocodeogram.module.source.implementation;

import java.io.File;
import java.io.FileFilter;

public class ECGFileFilter implements FileFilter {

    public boolean accept(File f) {
        if (f.isDirectory())
            return false;
        if (f.isHidden())
            return false;

        String extension = getExtension(f);
        if (extension != null) {
            if (extension.equals("log") ||
                extension.equals("ecg") ||
                extension.equals("events") ||
                extension.equals("out")) {
                    return true;
            } else {
                return false;
            }
        }

        return false;
    }

    public String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }        
}