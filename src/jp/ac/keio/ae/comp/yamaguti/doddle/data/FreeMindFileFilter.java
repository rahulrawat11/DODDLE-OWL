/*
 * @(#)  2007/11/23
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import java.io.*;

import javax.swing.filechooser.FileFilter;

/**
 * @author takeshi morita
 */
public class FreeMindFileFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) return true;
        return f.getName().toLowerCase().endsWith(".mm");
    }
    @Override
    public String getDescription() {
        return "FreeMind (.mm)";
    }
}
