/*
 * @(#)  2007/09/08
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.actions;

import java.awt.event.*;
import java.io.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class OpenRecentProjectAction extends OpenProjectAction {

        private File projectFile;

        public OpenRecentProjectAction(String project, DODDLE ddl) {
            this.title = Translator.getTerm("OpenProjectAction");
            projectFile = new File(project);
            doddle = ddl;
        }
        
        public void  actionPerformed(ActionEvent e) {
            openFile = projectFile;
            newProject = new DODDLEProject(openFile.getAbsolutePath(), 32);
            OpenProjectWorker worker = new OpenProjectWorker(11);
            DODDLE.STATUS_BAR.setSwingWorker(worker);
            worker.execute();
        }
}
