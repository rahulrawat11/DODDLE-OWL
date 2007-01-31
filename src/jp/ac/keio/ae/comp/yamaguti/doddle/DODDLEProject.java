package jp.ac.keio.ae.comp.yamaguti.doddle;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class DODDLEProject extends JInternalFrame implements ActionListener {

    private JTabbedPane tabbedPane;
    private OntologySelectionPanel ontSelectionPanel;
    private DocumentSelectionPanel docSelectionPanel;
    private InputWordSelectionPanel inputWordSelectinPanel;
    private DisambiguationPanel disambiguationPanel;
    private ConstructClassPanel constructClassPanel;
    private ConstructPropertyPanel constructPropertyPanel;
    private ConceptDefinitionPanel conceptDefinitionPanel;
    private VisualizationPanel visualizationPanel;

    private int userIDCount;
    private Map<String, Concept> uriConceptMap;

    private JMenu projectMenu;
    private JCheckBoxMenuItem projectMenuItem;

    public DODDLEProject(String title, JMenu pm) {
        super(title, true, true, true, true);
        projectMenu = pm;
        projectMenuItem = new JCheckBoxMenuItem(title);
        projectMenuItem.addActionListener(this);
        projectMenu.add(projectMenuItem);

        userIDCount = 0;
        uriConceptMap = new HashMap<String, Concept>();
        constructClassPanel = new ConstructClassPanel(this);
        ontSelectionPanel = new OntologySelectionPanel();
        constructPropertyPanel = new ConstructPropertyPanel(this);
        disambiguationPanel = new DisambiguationPanel(constructClassPanel, constructPropertyPanel, this);
        inputWordSelectinPanel = new InputWordSelectionPanel(disambiguationPanel);
        docSelectionPanel = new DocumentSelectionPanel(inputWordSelectinPanel, this);
        conceptDefinitionPanel = new ConceptDefinitionPanel(this);
        if (DODDLE.getDODDLEPlugin() != null) {
            visualizationPanel = new VisualizationPanel(this);
        }
        disambiguationPanel.setDocumentSelectionPanel(docSelectionPanel);
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(Translator.getString("OntologySelectionPanel.Text"), Utils.getImageIcon("ontology.png"),
                ontSelectionPanel);
        tabbedPane.addTab(Translator.getString("DocumentSelectionPanel.Text"), Utils.getImageIcon("open_doc.gif"),
                docSelectionPanel);
        tabbedPane.addTab(Translator.getString("InputWordSelectionPanel.Text"), Utils.getImageIcon("input_words.png"),
                inputWordSelectinPanel);
        tabbedPane.addTab(Translator.getString("DisambiguationPanel.Text"), Utils.getImageIcon("disambiguation.png"),
                disambiguationPanel);
        tabbedPane.addTab(Translator.getString("ClassTreePanel.Text"), Utils.getImageIcon("class_tree.png"),
                constructClassPanel);
        tabbedPane.addTab(Translator.getString("PropertyTreePanel.Text"), Utils.getImageIcon("property_tree.png"),
                constructPropertyPanel);
        tabbedPane.addTab(Translator.getString("ConceptDefinitionPanel.Text"), Utils.getImageIcon("non-taxonomic.png"),
                conceptDefinitionPanel);
        if (DODDLE.getDODDLEPlugin() != null) {
            tabbedPane.addTab(Translator.getString("VisualizationPanel.Text"), Utils.getImageIcon("mr3_logo.png"),
                    visualizationPanel);
        }
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosing(InternalFrameEvent e) {
                int messageType = JOptionPane.showConfirmDialog(tabbedPane, getTitle() + "\nプロジェクトを終了しますか？");
                if (messageType == JOptionPane.YES_OPTION) {
                    projectMenu.remove(projectMenuItem);
                    dispose();
                }
            }
        });
        setSize(600, 500);
    }

    public void setProjectName(String name) {
        projectMenuItem.setText(name);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == projectMenuItem) {
            for (int i = 0; i < projectMenu.getItemCount(); i++) {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem) projectMenu.getItem(i);
                item.setSelected(false);
            }
            projectMenuItem.setSelected(true);
            toFront();
            try {
                setSelected(true);
            } catch (PropertyVetoException pve) {
                pve.printStackTrace();
            }
        }
    }

    public void resetURIConceptMap() {
        uriConceptMap.clear();
    }

    public Set getAllConcept() {
        return uriConceptMap.keySet();
    }

    public void putConcept(String uri, Concept c) {
        uriConceptMap.put(uri, c);
    }

    public Concept getConcept(String uri) {
        return uriConceptMap.get(uri);
    }

    public void initUserIDCount() {
        userIDCount = 0;
    }

    public int getUserIDCount() {
        return userIDCount;
    }

    public String getUserIDStr() {
        return "UID" + Integer.toString(userIDCount++);
    }

    public void setUserIDCount(int id) {
        if (userIDCount < id) {
            userIDCount = id;
        }
    }

    public OntologySelectionPanel getOntologySelectionPanel() {
        return ontSelectionPanel;
    }

    public DocumentSelectionPanel getDocumentSelectionPanel() {
        return docSelectionPanel;
    }

    public InputWordSelectionPanel getInputWordSelectionPanel() {
        return inputWordSelectinPanel;
    }

    public DisambiguationPanel getDisambiguationPanel() {
        return disambiguationPanel;
    }

    public InputWordModel makeInputWordModel(String iw, Map<String, Set<Concept>> wcSetMap) {
        return disambiguationPanel.makeInputWordModel(iw, wcSetMap);
    }

    public ConstructPropertyPanel getConstructPropertyPanel() {
        return constructPropertyPanel;
    }

    public ConstructClassPanel getConstructClassPanel() {
        return constructClassPanel;
    }

    public ConceptDefinitionPanel getConceptDefinitionPanel() {
        return conceptDefinitionPanel;
    }

    public void setSelectedIndex(int i) {
        tabbedPane.setSelectedIndex(i);
    }

    public boolean isPerfectMatchedAmbiguityCntCheckBox() {
        return disambiguationPanel.isPerfectMatchedAmbiguityCntCheckBox();
    }
    
    public boolean isPerfectMatchedSystemAddedWordCheckBox() {
        return disambiguationPanel.isPerfectMatchedSystemAddedWordCheckBox();
    }

    public boolean isPartialMatchedAmbiguityCntCheckBox() {
        return disambiguationPanel.isPartialMatchedAmbiguityCntCheckBox();
    }

    public boolean isPartialMatchedComplexWordCheckBox() {
        return disambiguationPanel.isPartialMatchedComplexWordCheckBox();
    }

    public boolean isPartialMatchedMatchedWordBox() {
        return disambiguationPanel.isPartialMatchedMatchedWordBox();
    }

}
