/*
 * @(#)  2006/03/01
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class OntologySelectionPanel extends JPanel implements ActionListener {

    private JButton nextTabButton;
    private NameSpaceTable nsTable;

    private GeneralOntologySelectionPanel generalOntologySelectionPanel;
    private OWLOntologySelectionPanel owlOntologySelectionPanel;

    public OntologySelectionPanel() {
        generalOntologySelectionPanel = new GeneralOntologySelectionPanel();
        nsTable = new NameSpaceTable();
        owlOntologySelectionPanel = new OWLOntologySelectionPanel(nsTable);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add(generalOntologySelectionPanel, Translator
                .getString("OntologySelectionPanel.RefGenericOntologySelection"));
        tabbedPane.add(owlOntologySelectionPanel, Translator.getString("OntologySelectionPanel.OWLOntologySelection"));
        tabbedPane.add(nsTable, Translator.getString("OntologySelectionPanel.NameSpaceTable"));

        nextTabButton = new JButton(Translator.getString("OntologySelectionPanel.DocumentSelection"));
        nextTabButton.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(nextTabButton, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    public String getPrefix(String ns) {
        return nsTable.getPrefix(ns);
    }

    public void actionPerformed(ActionEvent e) {
        DODDLE.setSelectedIndex(DODDLE.DOCUMENT_SELECTION_PANEL);
    }

    public void saveOWLOntologySet(File saveFile) {
        owlOntologySelectionPanel.saveOWLOntologySet(saveFile);
    }

    public void loadOWLOntologySet(File loadFile) {
        owlOntologySelectionPanel.loadOWLOntologySet(loadFile);
    }

    public void saveGeneralOntologyInfo(File saveFile) {
        generalOntologySelectionPanel.saveGeneralOntologyInfo(saveFile);
    }

    public void loadGeneralOntologyInfo(File loadFile) {
        generalOntologySelectionPanel.loadGeneralOntologyInfo(loadFile);
    }

    public String getEnableDicList() {
        return generalOntologySelectionPanel.getEnableDicList();
    }
    
    public boolean isEDREnable() {
        return generalOntologySelectionPanel.isEDREnable();
    }

    public boolean isEDRTEnable() {
        return generalOntologySelectionPanel.isEDRTEnable();
    }

    public boolean isWordNetEnable() {
        return generalOntologySelectionPanel.isWordNetEnable();
    }
}

