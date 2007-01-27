/*
 * @(#)  2006/02/08
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.actions;

import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class OpenProjectAction extends AbstractAction {

    private File openDir;
    private String title;
    private DODDLE doddle;

    public OpenProjectAction(String title, DODDLE ddl) {
        super(title, Utils.getImageIcon("open.gif"));
        this.title = title;
        doddle = ddl;
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
    }

    public String getTitle() {
        return title;
    }

    public void openProject() {
        try {
            DODDLE.STATUS_BAR.setLastMessage(Translator.getString("StatusBar.Message.OpenProjectDone"));
            DODDLE.STATUS_BAR.startTime();
            DODDLE.STATUS_BAR.initNormal(22);
            DODDLE.STATUS_BAR.lock();

            DODDLEProject project = new DODDLEProject(openDir.getAbsolutePath(), DODDLE.projectMenu);
            DODDLE.desktop.add(project);
            project.toFront();
            DODDLE.desktop.setSelectedFrame(project);
            DODDLE.STATUS_BAR.addProjectValue();
            DODDLEProject currentProject = (DODDLEProject) DODDLE.desktop.getSelectedFrame();
            OntologySelectionPanel ontSelectionPanel = currentProject.getOntologySelectionPanel();
            DisambiguationPanel disambiguationPanel = currentProject.getDisambiguationPanel();
            DocumentSelectionPanel docSelectionPanelI = currentProject.getDocumentSelectionPanel();
            InputWordSelectionPanel inputWordSelectionPanel = currentProject.getInputWordSelectionPanel();
            ConceptDefinitionPanel conceptDefinitionPanel = currentProject.getConceptDefinitionPanel();
            openDir.mkdir();
            DODDLE.STATUS_BAR.addProjectValue();
            doddle.loadBaseURI(new File(openDir, "projectInfo.txt"));
            DODDLE.STATUS_BAR.addProjectValue();
            doddle.getOptionDialog().loadOption(new File(openDir, "option.txt"),
                    doddle.getCurrentProject().getDisambiguationPanel());
            DODDLE.STATUS_BAR.addProjectValue();
            docSelectionPanelI.loadDocuments(openDir);
            DODDLE.STATUS_BAR.addProjectValue();
            ontSelectionPanel.loadOntologyInfo(new File(openDir, "OntologyInfo.txt"));
            DODDLE.STATUS_BAR.addProjectValue();
            inputWordSelectionPanel.loadWordInfoTable(new File(openDir, "WordInfoTable.txt"), new File(openDir,
                    "RemovedWordInfoTable.txt"));
            DODDLE.STATUS_BAR.addProjectValue();
            File inputWordSetFile = new File(openDir, "InputWordSet.txt");
            disambiguationPanel.loadInputWordSet(inputWordSetFile);
            DODDLE.STATUS_BAR.addProjectValue();
            disambiguationPanel.loadWordEvalConceptSet(new File(openDir, "wordEvalConceptSet.txt"));
            DODDLE.STATUS_BAR.addProjectValue();
            if (inputWordSetFile.exists()) {
                disambiguationPanel.loadWordCorrespondConceptSetMap(new File(openDir, "InputWord_ID.txt"));
                DODDLE.STATUS_BAR.addProjectValue();
            }
            disambiguationPanel.loadConstructTreeOptionSet(new File(openDir, "InputWord_ConstructTreeOption.txt"));
            DODDLE.STATUS_BAR.addProjectValue();
            disambiguationPanel.loadInputConceptSet(new File(openDir, "InputIDSet.txt"));
            DODDLE.STATUS_BAR.addProjectValue();
            disambiguationPanel.loadUndefinedWordSet(new File(openDir, "UndefinedWordSet.txt"));
            DODDLE.STATUS_BAR.addProjectValue();
            doddle.loadOntology(currentProject, new File(openDir, "Ontology.owl"));
            DODDLE.STATUS_BAR.addProjectValue();
            doddle.loadIDTypicalWord(currentProject, new File(openDir, "ID_TypicalWord.txt"));
            DODDLE.STATUS_BAR.addProjectValue();

            ConstructClassPanel constructClassPanel = currentProject.getConstructClassPanel();
            ConstructPropertyPanel constructPropertyPanel = currentProject.getConstructPropertyPanel();
            constructClassPanel.loadTrimmedResultAnalysis(currentProject, new File(openDir,
                    "ClassTrimmedResultAnalysis.txt"));
            constructPropertyPanel.loadTrimmedResultAnalysis(currentProject, new File(openDir,
                    "PropertyTrimmedResultAnalysis.txt"));

            conceptDefinitionPanel.setInputDocList();
            conceptDefinitionPanel
                    .loadConceptDefinitionParameters(new File(openDir, "ConceptDefinitionParameters.txt"));
            DODDLE.STATUS_BAR.addProjectValue();
            File conceptDefinitionResultDir = new File(openDir, "WordSpaceResults");
            conceptDefinitionResultDir.mkdir();
            conceptDefinitionPanel.loadWordSpaceResult(conceptDefinitionResultDir);
            DODDLE.STATUS_BAR.addProjectValue();
            conceptDefinitionResultDir = new File(openDir, "AprioriResults");
            conceptDefinitionResultDir.mkdir();
            conceptDefinitionPanel.loadAprioriResult(conceptDefinitionResultDir);
            DODDLE.STATUS_BAR.addProjectValue();

            conceptDefinitionPanel.loadConceptDefinition(new File(openDir, "ConceptDefinition.txt"));
            DODDLE.STATUS_BAR.addProjectValue();
            conceptDefinitionPanel.loadWrongPairSet(new File(openDir, "WrongPairSet.txt"));
            DODDLE.STATUS_BAR.addProjectValue();
            
            disambiguationPanel.selectTopList();
            project.setVisible(true);
            project.setMaximum(true);
            constructClassPanel.expandTree();
            constructPropertyPanel.expandTree();
            DODDLE.STATUS_BAR.addProjectValue();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DODDLE.STATUS_BAR.unLock();
            DODDLE.STATUS_BAR.hideProgressBar();
        }
    }

    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser(DODDLE.PROJECT_DIR);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int retval = chooser.showOpenDialog(DODDLE.rootPane);
        if (retval != JFileChooser.APPROVE_OPTION) { return; }
        openDir = chooser.getSelectedFile();
        SwingWorker worker = new SwingWorker<String, String>() {
            public String doInBackground() {
                openProject();
                return "done";
            }
        };
        DODDLE.STATUS_BAR.setSwingWorker(worker);
        worker.execute();
    }
}
