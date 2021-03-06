/*
 * Project Name: DODDLE-OWL (a Domain Ontology rapiD DeveLopment Environment - OWL extension)
 * Project Website: http://doddle-owl.sourceforge.net/
 * 
 * Copyright (C) 2004-2015 Yamaguchi Laboratory, Keio University. All rights reserved. 
 * 
 * This file is part of DODDLE-OWL.
 * 
 * DODDLE-OWL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DODDLE-OWL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with DODDLE-OWL.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package net.sourceforge.doddle_owl.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import net.sourceforge.doddle_owl.*;
import net.sourceforge.doddle_owl.data.*;
import net.sourceforge.doddle_owl.utils.*;

/**
 * @author Takeshi Morita
 */
public class ConceptSelectionDialog extends JDialog implements ActionListener, ListSelectionListener,
        TreeSelectionListener {

    private JList predefinedRelationJList;

    private Set selectedConceptSet;
    private JTree conceptTree;
    private JButton expandButton;
    private JButton applyButton;
    private JButton cancelButton;

    public static Concept agentConcept = new Concept(DODDLEConstants.BASE_URI+"DID0", "agent");
    public static Concept objectConcept = new Concept(DODDLEConstants.BASE_URI+"DID1", "object");

    public ConceptSelectionDialog(String type, String title) {
        selectedConceptSet = new HashSet();

        conceptTree = new JTree(new DefaultTreeModel(null));
        conceptTree.addTreeSelectionListener(this);
        conceptTree.setCellRenderer(new ConceptTreeCellRenderer(type));
        JScrollPane conceptTreeScroll = new JScrollPane(conceptTree);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(conceptTreeScroll, BorderLayout.CENTER);
        if (type.equals(ConceptTreeCellRenderer.VERB_CONCEPT_TREE)) {
            agentConcept = new Concept(DODDLEConstants.BASE_URI+"DID0", "agent");
            objectConcept = new Concept(DODDLEConstants.BASE_URI+"DID1", "object");
            predefinedRelationJList = new JList(new Concept[] { agentConcept, objectConcept});
            predefinedRelationJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            predefinedRelationJList.addListSelectionListener(this);
            predefinedRelationJList.setPreferredSize(new Dimension(150, 150));
            predefinedRelationJList.setBorder(BorderFactory.createTitledBorder("Predfined Relation"));
            mainPanel.add(predefinedRelationJList, BorderLayout.EAST);
        }

        expandButton = new JButton(Translator.getTerm("ExpandConceptTreeButton"));
        expandButton.addActionListener(this);
        applyButton = new JButton(Translator.getTerm("OKButton"));
        applyButton.addActionListener(this);
        cancelButton = new JButton(Translator.getTerm("CancelButton"));
        cancelButton.addActionListener(this);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2));
        buttonPanel.add(expandButton);
        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);
        JPanel eastButtonPanel = new JPanel();
        eastButtonPanel.setLayout(new BorderLayout());
        eastButtonPanel.add(buttonPanel, BorderLayout.EAST);

        getContentPane().add(mainPanel, BorderLayout.CENTER);
        getContentPane().add(eastButtonPanel, BorderLayout.SOUTH);

        setTitle(title);
        setSize(800, 600);
        setModal(true);
        setLocationRelativeTo(DODDLE_OWL.rootPane);
    }

    public void setSingleSelection() {
        conceptTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    }

    public void setTreeModel(TreeModel model) {
        conceptTree.setModel(model);
        expandAllPath();
    }

    private void expandAllPath() {
        for (int i = 0; i < conceptTree.getRowCount(); i++) {
            conceptTree.expandPath(conceptTree.getPathForRow(i));
        }
    }

    public void setVisible(boolean t) {
        conceptTree.clearSelection();
        super.setVisible(t);
    }

    public Set getConceptSet() {
        return selectedConceptSet;
    }

    public Concept getConcept() {
        if (selectedConceptSet.size() == 1) { return (Concept) selectedConceptSet.toArray()[0]; }
        return null;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == expandButton) {
            expandAllPath();
        } else if (e.getSource() == applyButton) {
            selectedConceptSet.clear();
            if (predefinedRelationJList != null && predefinedRelationJList.getSelectedValue() != null) {
                selectedConceptSet.add(predefinedRelationJList.getSelectedValue());
            } else {
                TreePath[] paths = conceptTree.getSelectionPaths();
                for (int i = 0; i < paths.length; i++) {
                    ConceptTreeNode node = (ConceptTreeNode) paths[i].getLastPathComponent();
                    selectedConceptSet.add(node.getConcept());
                }
            }
            setVisible(false);
        } else if (e.getSource() == cancelButton) {
            selectedConceptSet.clear();
            setVisible(false);
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() == predefinedRelationJList) {
            conceptTree.clearSelection();
        }
    }

    public void valueChanged(TreeSelectionEvent e) {
        if (e.getSource() == conceptTree && predefinedRelationJList != null) {
            predefinedRelationJList.clearSelection();
        }
    }
}
