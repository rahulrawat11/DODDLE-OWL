/*
 * @(#)  2006/02/08
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.actions;

import java.awt.event.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class ConstructNounAndVerbTreeAction extends AbstractAction {

    public ConstructNounAndVerbTreeAction() {
        super(Translator.getTerm("ClassAndPropertyTreeConstructionAction"));
    }

    public void actionPerformed(ActionEvent e) {
        SwingWorker<String, String> worker = new SwingWorker<String, String>() {
            public String doInBackground() {
                InputConceptSelectionPanel inputConceptSelectionPanel = DODDLE.getCurrentProject().getInputConceptSelectionPanel();
                inputConceptSelectionPanel.makeEDRTree();
                new ConstructTreeAction(true, DODDLE.getCurrentProject()).constructTree();
                DODDLE.getCurrentProject().addLog("ClassAndPropertyTreeConstructionAction");
                return "done";
            }
        };
        DODDLE.STATUS_BAR.setSwingWorker(worker);
        worker.execute();
    }
}
