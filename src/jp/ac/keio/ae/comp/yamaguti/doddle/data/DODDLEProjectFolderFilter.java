/*
 * @(#)  2007/11/23
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import java.io.*;

import javax.swing.filechooser.FileFilter;

import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class DODDLEProjectFolderFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
        return f.isDirectory();
    }

    @Override
    public String getDescription() {
        return Translator.getTerm("DODDLEProjectFolderFilter");
    }

}
