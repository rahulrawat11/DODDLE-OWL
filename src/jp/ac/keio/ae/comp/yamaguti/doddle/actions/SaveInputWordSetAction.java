/*
 * @(#)  2007/06/01
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.actions;

import java.awt.event.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class SaveInputWordSetAction extends AbstractAction {

    public SaveInputWordSetAction(String title) {
        super(title);
    }

    public void actionPerformed(ActionEvent e) {
        DODDLEProject currentProject = DODDLE.getCurrentProject();
        DisambiguationPanel disambiguationPanel = currentProject.getDisambiguationPanel();

        JFileChooser chooser = new JFileChooser(DODDLEConstants.PROJECT_HOME);
        int retval = chooser.showSaveDialog(DODDLE.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            disambiguationPanel.saveInputWordSet(chooser.getSelectedFile());
            DODDLE.STATUS_BAR.setText(Translator.getTerm("SaveInputWordListAction"));
        }
    }
    
}
