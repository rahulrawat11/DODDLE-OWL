/*
 * @(#)  2006/02/08
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.actions;

import java.awt.event.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class ConstructNounTreeAction extends AbstractAction {

    public ConstructNounTreeAction() {
        super(Translator.getTerm("ClassTreeConstructionAction"));
    }

    public void actionPerformed(ActionEvent e) {
        SwingWorker<String, String> worker = new SwingWorker<String, String>() {
            public String doInBackground() {
                DODDLE.STATUS_BAR.initNormal(9);
                DODDLE.STATUS_BAR.startTime();
                DODDLE.STATUS_BAR.setLastMessage(Translator.getTerm("ClassTreeConstructionAction"));
                new ConstructTreeAction(false, DODDLE.getCurrentProject()).constructTree();
                DODDLE.getCurrentProject().addLog("ClassTreeConstructionAction");
                return "done";
            }
        };
        DODDLE.STATUS_BAR.setSwingWorker(worker);
        worker.execute();
    }
}
