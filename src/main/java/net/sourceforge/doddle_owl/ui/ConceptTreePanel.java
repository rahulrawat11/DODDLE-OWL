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
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import net.sourceforge.doddle_owl.*;
import net.sourceforge.doddle_owl.data.*;
import net.sourceforge.doddle_owl.utils.*;

import org.apache.log4j.*;

/**
 * @author Takeshi Morita
 */
public class ConceptTreePanel extends JPanel {
    private DODDLEProject project;

    private JTextField searchConceptField;
    private TitledBorder searchConceptFieldBorder;
    private JButton searchButton;
    private JButton searchPreviousButton;
    private JButton searchNextButton;
    private JCheckBox perfectlyMatchedSearchOptionCheckBox;
    private JCheckBox searchURICheckBox;
    private JCheckBox caseSensitivityCheckBox;
    private JList labelLangJList;
    private JList descriptionLangJList;

    private JTree conceptTree;
    private ConceptTreeNode targetConceptTreeNode;

    private AddConceptAction addSubConceptAction;
    private AddConceptAction addSibConceptAction;
    private CopyConceptAction copyConceptAction;
    private CutConceptAction cutConceptAction;
    private PasteConceptAction pasteConceptAction;
    private DeleteInternalConceptAction deleteInternalConceptAction;
    private DeleteLinkToUpperConceptAction deleteLinkToUpperConceptAction;
    private DeleteConceptAction deleteConceptAction;
    private AddUndefinedTermListAction addUndefinedTermListAction;
    private MoveUndefinedTermListAction moveUndefinedTermListAction;
    private UndoAction undoAction;
    private RedoAction redoAction;
    private JCheckBox showPrefixCheckBox;

    private ImageIcon addSubConceptIcon = Utils.getImageIcon("add_sub_concept.png");
    private ImageIcon addSibConceptIcon = Utils.getImageIcon("add_sib_concept.png");
    // private ImageIcon expandTreeIcon = Utils.getImageIcon("expand_tree.png");
    // private ImageIcon addUndefWordIcon =
    // Utils.getImageIcon("add_undef_word.png");
    // private ImageIcon undefIcon = Utils.getImageIcon("undef.png");

    private ImageIcon copyIcon = Utils.getImageIcon("page_white_copy.png");
    private ImageIcon cutIcon = Utils.getImageIcon("cut.png");
    private ImageIcon pasteIcon = Utils.getImageIcon("page_white_paste.png");
    private ImageIcon removeConceptIcon = Utils.getImageIcon("remove_concept.png");
    private ImageIcon removeUpperLinkIcon = Utils.getImageIcon("remove_upper_link.png");
    private ImageIcon removeInternalConceptIcon = Utils.getImageIcon("remove_internal_concept.png");

    private ImageIcon undoIcon = Utils.getImageIcon("arrow_undo.png");
    private ImageIcon redoIcon = Utils.getImageIcon("arrow_redo.png");

    private UndefinedTermListPanel undefinedTermListPanel;
    private ConceptDriftManagementPanel conceptDriftManagementPanel;

    private Map<String, Concept> idConceptMap;
    private Map<Concept, Set<ConceptTreeNode>> conceptSameConceptTreeNodeMap;
    private Map<String, Concept> compoundWordConceptMap; // 複合語と対応する概念のマッピング
    private static final int LANG_SIZE = 60;

    private ConceptSelectionDialog conceptSelectionDialog;

    private String treeType;

    private JTree hasaTree;

    public static final String CLASS_ISA_TREE = "clas is-a Tree";
    public static final String PROPERTY_ISA_TREE = "property is-a Tree";
    public static final String CLASS_HASA_TREE = "clas has-a Tree";
    public static final String PROPERTY_HASA_TREE = "property has-a Tree";

    public ConceptTreePanel(String title, String type, UndefinedTermListPanel undefPanel, DODDLEProject p) {
        project = p;
        treeType = type;
        if (type == CLASS_HASA_TREE) {
            conceptSelectionDialog = new ConceptSelectionDialog(ConceptTreeCellRenderer.NOUN_CONCEPT_TREE,
                    "Class Is-a Selection Dialog");
        } else if (type == PROPERTY_HASA_TREE) {
            conceptSelectionDialog = new ConceptSelectionDialog(ConceptTreeCellRenderer.VERB_CONCEPT_TREE,
                    "Property Is-a Selection Dialog");
        }
        undefinedTermListPanel = undefPanel;
        abstractLabelSet = new HashSet<String>();
        idConceptMap = new HashMap<String, Concept>();
        conceptSameConceptTreeNodeMap = new HashMap<Concept, Set<ConceptTreeNode>>();
        compoundWordConceptMap = new HashMap<String, Concept>();
        Action searchAction = new SearchAction();
        searchConceptField = new JTextField(15);
        searchConceptField.addActionListener(searchAction);
        searchConceptFieldBorder = BorderFactory.createTitledBorder(Translator.getTerm("SearchConceptTextField")
                + " (0/0)");
        searchConceptField.setBorder(searchConceptFieldBorder);
        searchButton = new JButton(Translator.getTerm("SearchButton"));
        searchButton.addActionListener(searchAction);
        searchPreviousButton = new JButton(Translator.getTerm("SearchPreviousButton"));
        searchPreviousButton.addActionListener(searchAction);
        searchNextButton = new JButton(Translator.getTerm("SearchNextButton"));
        searchNextButton.addActionListener(searchAction);

        perfectlyMatchedSearchOptionCheckBox = new JCheckBox(Translator.getTerm("PerfectlyMatchSearchOptionCheckBox"));
        searchURICheckBox = new JCheckBox(Translator.getTerm("SearchURICheckBox"));
        caseSensitivityCheckBox = new JCheckBox(Translator.getTerm("CaseSensitivityCheckBox"));
        JPanel searchCheckBoxPanel = new JPanel();
        searchCheckBoxPanel.add(perfectlyMatchedSearchOptionCheckBox);
        searchCheckBoxPanel.add(searchURICheckBox);
        searchCheckBoxPanel.add(caseSensitivityCheckBox);

        labelLangJList = new JList(new String[] { "en", "ja", "ALL", "NULL"});
        labelLangJList.setSelectedValue("ALL", true);
        JScrollPane labelLangJListScroll = new JScrollPane(labelLangJList);
        labelLangJListScroll.setPreferredSize(new Dimension(LANG_SIZE, 70));
        labelLangJListScroll.setMinimumSize(new Dimension(LANG_SIZE, 70));
        labelLangJListScroll.setBorder(BorderFactory.createTitledBorder(Translator.getTerm("LabelLangList")));
        descriptionLangJList = new JList(new String[] { "en", "ja", "ALL", "NULL"});
        JScrollPane descriptionLangJListScroll = new JScrollPane(descriptionLangJList);
        descriptionLangJListScroll.setBorder(BorderFactory
                .createTitledBorder(Translator.getTerm("DescriptionLangList")));
        descriptionLangJListScroll.setPreferredSize(new Dimension(LANG_SIZE, 70));
        descriptionLangJListScroll.setMinimumSize(new Dimension(LANG_SIZE, 70));

        JPanel searchRangePanel = new JPanel();
        searchRangePanel.setBorder(BorderFactory.createTitledBorder(Translator.getTerm("SearchOptionBorder")));
        searchRangePanel.setLayout(new GridLayout(1, 2));
        searchRangePanel.add(labelLangJListScroll);
        searchRangePanel.add(descriptionLangJListScroll);

        JPanel searchOptionPanel = new JPanel();
        searchOptionPanel.setLayout(new BorderLayout());
        searchOptionPanel.add(searchCheckBoxPanel, BorderLayout.NORTH);
        searchOptionPanel.add(searchRangePanel, BorderLayout.CENTER);

        JPanel searchDirectionPanel = new JPanel();
        searchDirectionPanel.setLayout(new GridLayout(1, 2));
        searchDirectionPanel.add(searchPreviousButton);
        searchDirectionPanel.add(searchNextButton);
        JPanel searchButtonPanel = new JPanel();
        searchButtonPanel.setLayout(new GridLayout(2, 1));
        searchButtonPanel.add(searchButton);
        searchButtonPanel.add(searchDirectionPanel);

        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BorderLayout());
        searchPanel.add(searchConceptField, BorderLayout.CENTER);
        searchPanel.add(searchButtonPanel, BorderLayout.EAST);
        searchPanel.add(searchOptionPanel, BorderLayout.SOUTH);

        showPrefixCheckBox = new JCheckBox(Translator.getTerm("ShowQNameAction"), false);

        conceptTree = new JTree();
        conceptTree.setEditable(false);
        conceptTree.setDragEnabled(true);
        new DropTarget(conceptTree, DnDConstants.ACTION_COPY_OR_MOVE, new ConceptTreeDropTargetAdapter());
        conceptTree.setScrollsOnExpand(true);
        JScrollPane conceptTreeScroll = new JScrollPane(conceptTree);
        conceptTreeScroll.setPreferredSize(new Dimension(250, 100));
        conceptTreeScroll.setBorder(BorderFactory.createTitledBorder(title));

        initActions(); // conceptTreeよりも後に初期化する必要あり
        initKeyActions();
        // POPUPMenuを作るので，initActions()より後に実行
        conceptTree.addMouseListener(new ConceptTreeMouseAdapter());
        JToolBar toolBar = new JToolBar();
        initToolBar(toolBar);

        JPanel treePanel = new JPanel();
        treePanel.setLayout(new BorderLayout());
        treePanel.add(toolBar, BorderLayout.NORTH);
        treePanel.add(conceptTreeScroll, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(searchPanel, BorderLayout.NORTH);
        add(treePanel, BorderLayout.CENTER);
    }

    private void initActions() {
        addSubConceptAction = new AddConceptAction(Translator.getTerm("AddSubConceptAction"), addSubConceptIcon, true);
        addSibConceptAction = new AddConceptAction(Translator.getTerm("AddSibConceptAction"), addSibConceptIcon, false);
        copyConceptAction = new CopyConceptAction(Translator.getTerm("CopyConceptAction"));
        cutConceptAction = new CutConceptAction(Translator.getTerm("CutConceptAction"));
        pasteConceptAction = new PasteConceptAction(Translator.getTerm("PasteConceptAction"));
        deleteInternalConceptAction = new DeleteInternalConceptAction(Translator.getTerm("DeleteInternalConceptAction"));
        deleteLinkToUpperConceptAction = new DeleteLinkToUpperConceptAction(Translator
                .getTerm("DeleteLinkToUpperConceptAction"));
        deleteConceptAction = new DeleteConceptAction(Translator.getTerm("DeleteConceptAction"));
        addUndefinedTermListAction = new AddUndefinedTermListAction(Translator.getTerm("AddUndefinedTermAction"));
        moveUndefinedTermListAction = new MoveUndefinedTermListAction(Translator.getTerm("UndefineTermAction"));
        undoAction = new UndoAction(Translator.getTerm("UndoAction"), undoIcon);
        redoAction = new RedoAction(Translator.getTerm("RedoAction"), redoIcon);
    }

    private void initKeyActions() {
        ActionMap actionMap = conceptTree.getActionMap();
        actionMap.put(TransferHandler.getCopyAction().getValue(Action.NAME), copyConceptAction);
        actionMap.put(TransferHandler.getCutAction().getValue(Action.NAME), cutConceptAction);
        actionMap.put(TransferHandler.getPasteAction().getValue(Action.NAME), pasteConceptAction);
        actionMap.put(Translator.getTerm("AddSubConceptAction"), addSubConceptAction);
        actionMap.put(Translator.getTerm("AddSibConceptAction"), addSibConceptAction);
        actionMap.put(Translator.getTerm("DeleteInternalConceptAction"), deleteInternalConceptAction);
        actionMap.put(Translator.getTerm("DeleteLinkToUpperConceptAction"), deleteLinkToUpperConceptAction);
        actionMap.put(Translator.getTerm("DeleteConceptAction"), deleteConceptAction);
        actionMap.put(Translator.getTerm("UndoAction"), undoAction);
        actionMap.put(Translator.getTerm("RedoAction"), redoAction);
    }

    public void initToolBar(JToolBar toolBar) {
        toolBar.add(addSubConceptAction).setToolTipText(addSubConceptAction.getTitle());
        toolBar.add(addSibConceptAction).setToolTipText(addSibConceptAction.getTitle());
        toolBar.add(copyConceptAction).setToolTipText(copyConceptAction.getTitle());
        toolBar.add(cutConceptAction).setToolTipText(cutConceptAction.getTitle());
        toolBar.add(pasteConceptAction).setToolTipText(pasteConceptAction.getTitle());
        toolBar.add(deleteLinkToUpperConceptAction).setToolTipText(deleteLinkToUpperConceptAction.getTitle());
        toolBar.add(deleteInternalConceptAction).setToolTipText(deleteInternalConceptAction.getTitle());
        if (treeType == CLASS_ISA_TREE || treeType == PROPERTY_ISA_TREE) {
            toolBar.add(deleteConceptAction).setToolTipText(deleteConceptAction.getTitle());
        }
        toolBar.add(undoAction);
        toolBar.add(redoAction);
    }

    public void initUndo() {
        undoAction.setEnabled(false);
        redoAction.setEnabled(false);
    }

    public void setHasaTree(JTree hasaTree) {
        this.hasaTree = hasaTree;
    }

    public void setConceptDriftManagementPanel(ConceptDriftManagementPanel cdmp) {
        conceptDriftManagementPanel = cdmp;
    }

    public boolean isShowPrefix() {
        return showPrefixCheckBox.isSelected();
    }

    public void loadDescriptions(Map<String, DODDLELiteral> wordDescriptionMap) {
        ConceptTreeNode rootNode = (ConceptTreeNode) conceptTree.getModel().getRoot();
        setDescriptions(rootNode, wordDescriptionMap);
    }

    public void setDescriptions(ConceptTreeNode treeNode, Map<String, DODDLELiteral> wordDescriptionMap) {
        for (int i = 0; i < treeNode.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) treeNode.getChildAt(i);
            for (String lang : childNode.getLangLabelLiteralListMap().keySet()) {
                for (DODDLELiteral label : childNode.getLangLabelLiteralListMap().get(lang)) {
                    if (wordDescriptionMap.get(label.getString()) != null) {
                        DODDLELiteral description = wordDescriptionMap.get(label.getString());
                        childNode.getConcept().addDescription(description);
                    }
                }
            }
            setDescriptions(childNode, wordDescriptionMap);
        }
    }

    class ConceptTreeMouseAdapter extends MouseAdapter {

        private JPopupMenu popupMenu;

        ConceptTreeMouseAdapter() {
            popupMenu = new JPopupMenu();
            popupMenu.add(addSubConceptAction);
            popupMenu.add(addSibConceptAction);
            popupMenu.add(copyConceptAction);
            popupMenu.add(cutConceptAction);
            popupMenu.add(pasteConceptAction);
            JMenu deleteMenu = new JMenu(Translator.getTerm("RemoveButton"));
            popupMenu.add(deleteMenu);
            deleteMenu.add(deleteLinkToUpperConceptAction);
            deleteMenu.add(deleteInternalConceptAction);
            if (treeType == CLASS_ISA_TREE || treeType == PROPERTY_ISA_TREE) {
                deleteMenu.add(deleteConceptAction);
                popupMenu.add(addUndefinedTermListAction);
                // popupMenu.add(moveUndefinedTermListAction);
            }
            popupMenu.add(undoAction);
            popupMenu.add(redoAction);
            popupMenu.add(new ExpandSelectedPathAction(Translator.getTerm("ExpandConceptTreeAction")));
        }

        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                if (!(conceptTree.getSelectionCount() == 1)) { return; }
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    public JTree getConceptTree() {
        return conceptTree;
    }

    public boolean isConceptContains(Concept c) {
        if (!(conceptTree.getModel().getRoot() instanceof ConceptTreeNode)) { return false; }
        ConceptTreeNode rootNode = (ConceptTreeNode) conceptTree.getModel().getRoot();
        return isConceptContains(c, rootNode);
    }

    private boolean isConceptContains(Concept c, ConceptTreeNode node) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            if (c == childNode.getConcept()) { return true; }
            return isConceptContains(c, childNode);
        }
        return false;
    }

    class SearchAction extends AbstractAction {

        private int index;
        private List searchNodeList;
        private String searchKeyWord;

        public SearchAction() {
            index = 0;
            searchKeyWord = "";
            searchNodeList = new ArrayList();
        }

        private boolean isSearchURI(Concept c) {
            if (searchURICheckBox.isSelected()) {
                String searchString = c.getURI();
                if (!caseSensitivityCheckBox.isSelected()) {
                    searchString = searchString.toLowerCase();
                    searchKeyWord = searchKeyWord.toLowerCase();
                }
                if (perfectlyMatchedSearchOptionCheckBox.isSelected() && c.getURI().equals(searchKeyWord)) {
                    return true;
                } else if (c.getURI().matches(searchKeyWord)) { return true; }
            }
            return false;
        }

        private boolean isSearchLabel(Concept c) {
            boolean checkAllLabel = false;
            boolean checkNullLabel = false;

            Object[] selectedLabelLangList = labelLangJList.getSelectedValues();
            for (int i = 0; i < selectedLabelLangList.length; i++) {
                if (selectedLabelLangList[i].equals("ALL")) {
                    checkAllLabel = true;
                }
                if (selectedLabelLangList[i].equals("NULL")) {
                    checkNullLabel = true;
                }
            }

            if (!checkNullLabel) {
                Map<String, List<DODDLELiteral>> langLabelListMap = c.getLangLabelListMap();
                if (checkAllLabel) {
                    for (List<DODDLELiteral> labelList : langLabelListMap.values()) {
                        for (DODDLELiteral label : labelList) {
                            String targetString = label.getString();
                            if (!caseSensitivityCheckBox.isSelected()) {
                                targetString = targetString.toLowerCase();
                                searchKeyWord = searchKeyWord.toLowerCase();
                            }
                            if (perfectlyMatchedSearchOptionCheckBox.isSelected()) { return targetString
                                    .matches(searchKeyWord); }
                            if (targetString.indexOf(searchKeyWord) != -1) { return true; }
                        }
                    }
                } else {
                    for (int i = 0; i < selectedLabelLangList.length; i++) {
                        if (langLabelListMap.get(selectedLabelLangList[i]) == null) {
                            continue;
                        }
                        for (DODDLELiteral label : langLabelListMap.get(selectedLabelLangList[i])) {
                            String targetString = label.getString();
                            if (!caseSensitivityCheckBox.isSelected()) {
                                targetString = targetString.toLowerCase();
                                searchKeyWord = searchKeyWord.toLowerCase();
                            }
                            if (perfectlyMatchedSearchOptionCheckBox.isSelected()) { return targetString
                                    .matches(searchKeyWord); }
                            if (targetString.indexOf(searchKeyWord) != -1) { return true; }
                        }
                    }
                }
            }
            return false;
        }

        private boolean isSearchDescription(Concept c) {
            boolean checkAllDescription = false;
            boolean checkNullDescription = false;

            Object[] selectedDescriptionLangList = descriptionLangJList.getSelectedValues();
            for (int i = 0; i < selectedDescriptionLangList.length; i++) {
                if (selectedDescriptionLangList[i].equals("ALL")) {
                    checkAllDescription = true;
                }
                if (selectedDescriptionLangList[i].equals("NULL")) {
                    checkNullDescription = true;
                }
            }
            if (!checkNullDescription) {
                Map<String, List<DODDLELiteral>> langDescriptionListMap = c.getLangDescriptionListMap();
                if (checkAllDescription) {
                    for (List<DODDLELiteral> descriptionList : langDescriptionListMap.values()) {
                        for (DODDLELiteral description : descriptionList) {
                            String targetString = description.getString();
                            if (!caseSensitivityCheckBox.isSelected()) {
                                targetString = targetString.toLowerCase();
                                searchKeyWord = searchKeyWord.toLowerCase();
                            }
                            if (perfectlyMatchedSearchOptionCheckBox.isSelected()) { return targetString
                                    .matches(searchKeyWord); }
                            if (targetString.indexOf(searchKeyWord) != -1) { return true; }
                        }
                    }
                } else {
                    for (int i = 0; i < selectedDescriptionLangList.length; i++) {
                        if (langDescriptionListMap.get(selectedDescriptionLangList[i]) == null) {
                            continue;
                        }
                        for (DODDLELiteral description : langDescriptionListMap.get(selectedDescriptionLangList[i])) {
                            String targetString = description.getString();
                            if (!caseSensitivityCheckBox.isSelected()) {
                                targetString = targetString.toLowerCase();
                                searchKeyWord = searchKeyWord.toLowerCase();
                            }
                            if (perfectlyMatchedSearchOptionCheckBox.isSelected()) { return targetString
                                    .matches(searchKeyWord); }
                            if (targetString.indexOf(searchKeyWord) != -1) { return true; }
                        }
                    }
                }
            }
            return false;
        }

        private boolean isSearchConcept(Concept c) {
            if (isSearchURI(c)) { return true; }
            if (isSearchLabel(c)) { return true; }
            if (isSearchDescription(c)) { return true; }
            return false;
        }

        private void searchConcept(ConceptTreeNode node) {
            for (int i = 0; i < node.getChildCount(); i++) {
                ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
                try {
                    if (isSearchConcept(childNode.getConcept())) {
                        searchNodeList.add(childNode);
                    }
                } catch (PatternSyntaxException e) {
                    JOptionPane.showMessageDialog(null, e.getMessage(), "PatternSyntaxException",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                searchConcept(childNode);
            }
        }

        private void setSearchFieldTitle() {
            if (searchNodeList.size() == 0) {
                searchConceptFieldBorder.setTitle(Translator.getTerm("SearchConceptTextField") + " (0/0)");
            } else {
                searchConceptFieldBorder.setTitle(Translator.getTerm("SearchConceptTextField") + " (" + (index + 1)
                        + "/" + searchNodeList.size() + ")");
            }
            searchConceptField.repaint();
        }

        private void selectSearchNode() {
            setSearchFieldTitle();
            ConceptTreeNode node = (ConceptTreeNode) searchNodeList.get(index);
            DefaultTreeModel model = (DefaultTreeModel) conceptTree.getModel();
            TreeNode[] nodes = model.getPathToRoot(node);
            TreePath path = new TreePath(nodes);
            conceptTree.scrollPathToVisible(path);
            conceptTree.setSelectionPath(path);
        }

        private void searchPrevious() {
            if (searchNodeList.size() == 0) { return; }
            if (0 <= index - 1 && index - 1 < searchNodeList.size()) {
                index--;
            } else {
                index = searchNodeList.size() - 1;
            }
            selectSearchNode();
        }

        private void searchNext() {
            if (searchNodeList.size() == 0) { return; }
            if (0 <= index + 1 && index + 1 < searchNodeList.size()) {
                index++;
            } else {
                index = 0;
            }
            selectSearchNode();
        }

        private void search() {
            index = 0;
            searchNodeList.clear();
            searchKeyWord = searchConceptField.getText();
            DefaultTreeModel model = (DefaultTreeModel) conceptTree.getModel();
            if (!(model.getRoot() instanceof ConceptTreeNode)) { return; }
            ConceptTreeNode rootNode = (ConceptTreeNode) model.getRoot();
            searchConcept(rootNode);
            setSearchFieldTitle();
            if (searchNodeList.size() == 0) { return; }
            selectSearchNode();
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == searchConceptField && searchKeyWord.equals(searchConceptField.getText())
                    && 0 < searchNodeList.size()) {
                // キーワードが変化しておらず，検索結果が１以上で，検索フィールドでエンターボタンを押した場合
                searchNext();
            } else if (e.getSource() == searchButton || e.getSource() == searchConceptField) {
                search();
            } else if (e.getSource() == searchPreviousButton) {
                searchPrevious();
            } else if (e.getSource() == searchNextButton) {
                searchNext();
            }
        }
    }

    class ConceptTreeDropTargetAdapter extends DropTargetAdapter {

        private TreePath[] dragPaths;

        public void dragEnter(DropTargetDragEvent dtde) {
            dragPaths = conceptTree.getSelectionPaths();
        }

        private ConceptTreeNode getSelectedNode(Point p) {
            TreePath path = conceptTree.getPathForLocation(p.x, p.y);
            if (path == null || path.getLastPathComponent() == null) { return null; }
            return (ConceptTreeNode) path.getLastPathComponent();
        }

        public void dragOver(DropTargetDragEvent dtde) {
            Point point = dtde.getLocation();
            ConceptTreeNode node = getSelectedNode(point);
            if (node != null) {
                int row = conceptTree.getRowForLocation(point.x, point.y);
                conceptTree.setSelectionRow(row);
            }
        }

        public void drop(DropTargetDropEvent dtde) {
            Point dropPoint = dtde.getLocation();
            ConceptTreeNode dropNode = getSelectedNode(dropPoint);

            if (dropNode == null || dragPaths == null) {
                dragPaths = null;
                return;
            } else if (UndefinedTermListPanel.isDragUndefinedList) {
                DefaultListModel listModel = undefinedTermListPanel.getModel();
                DefaultListModel viewListModel = undefinedTermListPanel.getViewModel();
                for (Object selectedValue : undefinedTermListPanel.getSelectedValues()) {
                    ConceptTreeNode parent = (ConceptTreeNode) conceptTree.getLastSelectedPathComponent();
                    insertNewConceptTreeNode(selectedValue.toString(), parent.getConcept());
                    listModel.removeElement(selectedValue);
                    if (viewListModel != null) {
                        viewListModel.removeElement(selectedValue);
                    }
                }
                undefinedTermListPanel.setTitleWithSize();
                UndefinedTermListPanel.isDragUndefinedList = false;
            } else {
                DefaultTreeModel treeModel = (DefaultTreeModel) conceptTree.getModel();
                for (int i = 0; i < dragPaths.length; i++) {
                    DefaultMutableTreeNode movedNode = (DefaultMutableTreeNode) dragPaths[i].getLastPathComponent();
                    if (movedNode == dropNode) {
                        continue;
                    }
                    addCommand();
                    if (movedNode.getParent() != null) {
                        treeModel.removeNodeFromParent(movedNode);
                    }
                    treeModel.insertNodeInto(movedNode, dropNode, 0);
                    checkMultipleInheritanceNode(((ConceptTreeNode) movedNode).getConcept());
                    expandSubTree((ConceptTreeNode) movedNode);
                }
            }
            dragPaths = null;
        }
    }

    private boolean isSameConcept(Concept concept, ConceptTreeNode node, Set sameConceptSet) {
        return concept.getURI().equals(node.getConcept().getURI());
    }

    public void searchSameConceptTreeNode(Concept concept, ConceptTreeNode node, Set sameConceptSet) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            if (isSameConcept(concept, childNode, sameConceptSet)) {
                sameConceptSet.add(childNode);
            }
            searchSameConceptTreeNode(concept, childNode, sameConceptSet);
        }
    }

    private boolean hasMultipleParent(Set sameConceptTreeNodeSet) {
        ConceptTreeNode parentTreeNode = null;
        for (Iterator i = sameConceptTreeNodeSet.iterator(); i.hasNext();) {
            ConceptTreeNode node = (ConceptTreeNode) i.next();
            if (parentTreeNode == null) {
                parentTreeNode = (ConceptTreeNode) node.getParent();
            } else {
                if (parentTreeNode.getURI().equals(((ConceptTreeNode) node.getParent()).getURI())) {
                    continue;
                }
                return true;
            }
        }
        return false;
    }

    private void makeIDParentIDSetMap(Map idParentIDSetMap, ConceptTreeNode node) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            if (idParentIDSetMap.get(childNode.getURI()) != null) {
                Set parentIDSet = (Set) idParentIDSetMap.get(childNode.getURI());
                parentIDSet.add(node.getURI());
                idParentIDSetMap.put(childNode.getURI(), parentIDSet);
            } else {
                Set parentIDSet = new HashSet();
                parentIDSet.add(node.getURI());
                idParentIDSetMap.put(childNode.getURI(), parentIDSet);
            }
            makeIDParentIDSetMap(idParentIDSetMap, childNode);
        }
    }

    private void checkAllMultipleInheritanceNode(Map idParentSetMap, ConceptTreeNode node) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            Set parentIDSet = (Set) idParentSetMap.get(childNode.getURI());
            if (1 < parentIDSet.size()) {
                childNode.setIsMultipleInheritance(true);
            }
            checkAllMultipleInheritanceNode(idParentSetMap, childNode);
        }
    }

    public void checkAllMultipleInheritanceNode(TreeModel treeModel) {
        Map idParentIDSetMap = new HashMap();
        ConceptTreeNode rootNode = (ConceptTreeNode) treeModel.getRoot();
        makeIDParentIDSetMap(idParentIDSetMap, rootNode);
        checkAllMultipleInheritanceNode(idParentIDSetMap, rootNode);
    }

    public void checkMultipleInheritanceNode(Concept c) {
        ConceptTreeNode rootNode = (ConceptTreeNode) conceptTree.getModel().getRoot();
        Set sameConceptTreeNodeSet = new HashSet();
        searchSameConceptTreeNode(c, rootNode, sameConceptTreeNodeSet);
        if (hasMultipleParent(sameConceptTreeNodeSet)) {
            for (Iterator j = sameConceptTreeNodeSet.iterator(); j.hasNext();) {
                ConceptTreeNode node = (ConceptTreeNode) j.next();
                node.setIsMultipleInheritance(true);
            }
        } else {
            for (Iterator j = sameConceptTreeNodeSet.iterator(); j.hasNext();) {
                ConceptTreeNode node = (ConceptTreeNode) j.next();
                node.setIsMultipleInheritance(false);
            }
        }
    }

    public Set getAllConcept() {
        Set conceptSet = new HashSet();
        TreeModel treeModel = conceptTree.getModel();
        ConceptTreeNode rootNode = (ConceptTreeNode) treeModel.getRoot();
        conceptSet.add(rootNode.getConcept());
        getAllConcept(rootNode, conceptSet);
        return conceptSet;
    }

    private void getAllConcept(TreeNode node, Set conceptSet) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            conceptSet.add(childNode.getConcept());
            getAllConcept(childNode, conceptSet);
        }
    }

    public Set<String> getAllConceptURI() {
        Set<String> uriSet = new HashSet<String>();
        TreeModel treeModel = conceptTree.getModel();
        if (treeModel.getRoot() instanceof ConceptTreeNode) {
            ConceptTreeNode rootNode = (ConceptTreeNode) treeModel.getRoot();
            uriSet.add(rootNode.getConcept().getURI());
            getAllConceptURI(rootNode, uriSet);
        }
        return uriSet;
    }

    private void getAllConceptURI(TreeNode node, Set conceptSet) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            conceptSet.add(childNode.getConcept().getURI());
            getAllConceptURI(childNode, conceptSet);
        }
    }

    private void deleteConcept(DefaultTreeModel model, ConceptTreeNode deleteNode) {
        ConceptTreeNode rootNode = (ConceptTreeNode) model.getRoot();
        Set<ConceptTreeNode> sameConceptSet = new HashSet<ConceptTreeNode>();
        searchSameConceptTreeNode(deleteNode.getConcept(), rootNode, sameConceptSet);
        for (ConceptTreeNode delNode : sameConceptSet) {
            if (delNode.getParent() != null) {
                model.removeNodeFromParent(delNode);
            }
        }
    }

    /**
     * 概念を削除（上位リンクの削除ではなく，概念そのものを削除）
     */
    private void deleteConcept() {
        if (conceptTree.getSelectionCount() == 1) {
            DefaultTreeModel model = (DefaultTreeModel) conceptTree.getModel();
            ConceptTreeNode deleteNode = (ConceptTreeNode) conceptTree.getLastSelectedPathComponent();
            deleteConcept(model, deleteNode);
            ConceptTreeNode rootNode = (ConceptTreeNode) model.getRoot();
            conceptDriftManagementPanel.resetConceptDriftManagementResult(rootNode);
            if (hasaTree != null) {
                model = (DefaultTreeModel) hasaTree.getModel();
                deleteConcept(model, deleteNode);
            }
        }
    }

    private Concept searchedConcept; // 領域オントロジーのノードに対応づけられたConcept
    private Set<Concept> supConceptSet; // 上位概念のセットを保存

    public Set getSupConceptSet(String id) {
        DefaultTreeModel model = (DefaultTreeModel) conceptTree.getModel();
        supConceptSet = new HashSet<Concept>();
        if (model.getRoot() instanceof ConceptTreeNode) {
            ConceptTreeNode rootNode = (ConceptTreeNode) model.getRoot();
            searchSupConcept(id, rootNode);
        }
        return supConceptSet;
    }

    /**
     * 引数で与えたidの上位概念を検索する. （EDR全体に定義されているConceptではない)
     */
    private void searchSupConcept(String id, ConceptTreeNode node) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            if (childNode.getConcept() != null && childNode.getConcept().getLocalName().equals(id)) {
                supConceptSet.add(node.getConcept());
            }
            searchSupConcept(id, childNode);
        }
    }

    /**
     * ノードに対応づけられた概念を検索する．（EDR全体に定義されているConceptではない)
     */
    private void searchConcept(String identity, ConceptTreeNode node) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            if (childNode.getConcept().getURI().equals(identity)) {
                searchedConcept = childNode.getConcept();
            }
            searchConcept(identity, childNode);
        }
    }

    public void addJPWord(String identity, String word) {
        DefaultTreeModel model = (DefaultTreeModel) conceptTree.getModel();
        ConceptTreeNode rootNode = (ConceptTreeNode) model.getRoot();
        searchedConcept = null;
        searchConcept(identity, rootNode);
        if (searchedConcept != null) {
            DODDLELiteral label = new DODDLELiteral("ja", word);
            searchedConcept.addLabel(label);
            searchedConcept.setInputLabel(label);
        }
    }

    public void addSubConcept(String identity, String word) {
        DefaultTreeModel model = (DefaultTreeModel) conceptTree.getModel();
        ConceptTreeNode rootNode = (ConceptTreeNode) model.getRoot();
        searchedConcept = null;
        searchConcept(identity, rootNode);
        if (searchedConcept != null) {
            insertNewConceptTreeNode(word, searchedConcept);
        }
    }

    private Concept insertCompoundConceptTreeNode(String identity, String newWord, ConceptTreeNode conceptTreeRootNode) {
        searchedConcept = null;
        if (idConceptMap.get(identity) == null) {
            searchConcept(identity, conceptTreeRootNode);
            idConceptMap.put(identity, searchedConcept);
        } else {
            searchedConcept = idConceptMap.get(identity);
        }
        if (searchedConcept == null) {
            searchedConcept = conceptTreeRootNode.getConcept();
        }
        if (searchedConcept != null) {
            if (compoundWordConceptMap.get(newWord) != null) {
                Concept c = compoundWordConceptMap.get(newWord);
                insertConceptTreeNode(c, searchedConcept, false, true);
                return c;
            }
            Concept c = insertNewConceptTreeNode(newWord, searchedConcept);
            compoundWordConceptMap.put(newWord, c);
            return c;
        }
        return null;
    }

    private String getChildWordWithoutTopWord(DefaultMutableTreeNode node) {
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
            if (childNode.getUserObject() instanceof String) {
                String compoundWord = (String) childNode.getUserObject();
                InputTermModel iwModel = project.getInputConceptSelectionPanel().makeInputTermModel(compoundWord);
                return iwModel.getBasicWordWithoutTopWord();
            }
        }
        return null;
    }

    public void addCompoundWordConcept(String identity, TreeNode node, ConceptTreeNode conceptTreeRootNode,
            Map<DefaultMutableTreeNode, String> abstractNodeLabelMap) {
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
            String newWord = "";
            if (childNode.getUserObject() instanceof Concept) {
                Concept c = (Concept) childNode.getUserObject();
                searchedConcept = null;
                searchConcept(identity, conceptTreeRootNode);
                newWord = abstractNodeLabelMap.get(childNode);
                if (newWord == null) {
                    newWord = c.getWord() + getChildWordWithoutTopWord(childNode);
                    newWord = newWord.replaceAll("\\s*", "");
                }
                newWord = "[A] " + newWord;
                abstractNodeCnt++;
                abstractLabelSet.add(newWord);
                totalAbstractNodeGroupSiblingNodeCnt += childNode.getChildCount();
                DODDLE_OWL.getLogger().log(Level.DEBUG,
                        "[" + abstractNodeCnt + "] 抽象概念(兄弟数)： " + newWord + " (" + childNode.getChildCount() + ")");
            } else {
                newWord = childNode.toString();
            }
            Concept childConcept = insertCompoundConceptTreeNode(identity, newWord, conceptTreeRootNode);
            if (childConcept != null) {
                addCompoundWordConcept(childConcept.getURI(), childNode, conceptTreeRootNode, abstractNodeLabelMap);
            }
        }
    }

    public Map<String, Concept> getCompoundWordConceptMap() {
        return compoundWordConceptMap;
    }

    private int abstractNodeCnt;
    private Set<String> abstractLabelSet;
    private int totalAbstractNodeGroupSiblingNodeCnt;

    public int getAbstractNodeCnt() {
        return abstractNodeCnt;
    }

    public int getAbstractConceptCnt() {
        return abstractLabelSet.size();
    }

    public int getTotalAbstractNodeGroupSiblingNodeCnt() {
        return totalAbstractNodeGroupSiblingNodeCnt;
    }

    public void addCompoundWordConcept(Map matchedTermIDMap, TreeNode node, ConceptTreeNode conceptTreeRootNode,
            Map abstractNodeLabelMap) {
        idConceptMap.clear();
        abstractLabelSet.clear();
        conceptSameConceptTreeNodeMap.clear();
        compoundWordConceptMap.clear();
        abstractNodeCnt = 0;
        totalAbstractNodeGroupSiblingNodeCnt = 0;
        for (int i = 0; i < node.getChildCount(); i++) {
            TreeNode childNode = node.getChildAt(i);
            String identity = childNode.toString();
            addCompoundWordConcept(identity, childNode, conceptTreeRootNode, abstractNodeLabelMap);
        }
    }

    private void copyConceptTreeNode(ConceptTreeNode targetNode) {
        DefaultTreeModel model = (DefaultTreeModel) conceptTree.getModel();
        ConceptTreeNode insertNode = new ConceptTreeNode(targetNode, project);
        model.insertNodeInto(insertNode, targetNode, 0);
    }

    private Concept insertNewConceptTreeNode(String word, Concept parentConcept) {
        Concept newConcept = new VerbConcept(DODDLEConstants.BASE_URI + project.getUserIDStr(), word);
        newConcept.setInputLabel(new DODDLELiteral("", word));
        project.getInputConceptSelectionPanel().addInputConcept(newConcept);
        insertConceptTreeNode(newConcept, parentConcept, false, true);
        return newConcept;
    }

    private Set<ConceptTreeNode> insertConceptTreeNode(Concept insertConcept, Concept parentConcept,
            boolean isInputConcept, boolean isUserConcept) {
        DefaultTreeModel model = (DefaultTreeModel) conceptTree.getModel();
        ConceptTreeNode rootNode = (ConceptTreeNode) model.getRoot();
        Set<ConceptTreeNode> sameConceptTreeNodeSet = new HashSet<ConceptTreeNode>();
        if (parentConcept.getURI().equals(rootNode.getConcept().getURI())) {
            sameConceptTreeNodeSet.add(rootNode);
        } else {
            if (conceptSameConceptTreeNodeMap.get(parentConcept) != null) {
                sameConceptTreeNodeSet = conceptSameConceptTreeNodeMap.get(parentConcept);
            } else {
                searchSameConceptTreeNode(parentConcept, rootNode, sameConceptTreeNodeSet);
                conceptSameConceptTreeNodeMap.put(parentConcept, sameConceptTreeNodeSet);
            }
        }
        Set<ConceptTreeNode> insertedConceptTreeNodeSet = new HashSet<ConceptTreeNode>();
        for (ConceptTreeNode parentNode : sameConceptTreeNodeSet) {
            if (!hasSameChildNode(parentNode, insertConcept)) {
                ConceptTreeNode insertNode = new ConceptTreeNode(insertConcept, project);
                insertNode.setIsInputConcept(isInputConcept);
                insertNode.setIsUserConcept(isUserConcept);
                // 兄弟概念数が多い場合は，先頭に挿入した方がよいので
                model.insertNodeInto(insertNode, parentNode, 0);
                if (parentNode == rootNode) {
                    conceptTree.expandPath(conceptTree.getSelectionPath());
                }
                insertedConceptTreeNodeSet.add(insertNode);
            }
        }
        return insertedConceptTreeNodeSet;
    }

    private ConceptTreeNode insertConceptTreeNode(ConceptTreeNode parentNode, ConceptTreeNode pasteConceptTreeNode) {
        Concept insertConcept = pasteConceptTreeNode.getConcept();
        if (!hasSameChildNode(parentNode, insertConcept)) {
            ConceptTreeNode insertNode = new ConceptTreeNode(pasteConceptTreeNode, project);
            DefaultTreeModel model = (DefaultTreeModel) conceptTree.getModel();
            ConceptTreeNode rootNode = (ConceptTreeNode) model.getRoot();
            // 兄弟概念数が多い場合は，先頭に挿入した方がよいので
            model.insertNodeInto(insertNode, parentNode, 0);
            if (parentNode == rootNode) {
                conceptTree.expandPath(conceptTree.getSelectionPath());
            }
            return insertNode;
        }
        return null;
    }

    private boolean hasSameChildNode(ConceptTreeNode parentNode, Concept insertNodeConcept) {
        for (int i = 0; i < parentNode.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) parentNode.getChildAt(i);
            if (childNode.getConcept().equals(insertNodeConcept)) { return true; }
        }
        return false;
    }

    public void copyTargetTreeNode(ConceptTreeNode copyTreeNode, ConceptTreeNode targetTreeNode) {
        for (int i = 0; i < targetTreeNode.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) targetTreeNode.getChildAt(i);
            ConceptTreeNode copyChildNode = new ConceptTreeNode(childNode, project);
            copyTreeNode.add(copyChildNode);
            copyTargetTreeNode(copyChildNode, childNode);
        }
    }

    class CopyConceptAction extends AbstractAction {

        private String title;

        public String getTitle() {
            return title;
        }

        CopyConceptAction(String title) {
            super(title, copyIcon);
            this.title = title;
            setToolTipText(title);
            KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK);
            putValue(ACCELERATOR_KEY, keyStroke);
        }

        public void actionPerformed(ActionEvent e) {
            if (conceptTree.getSelectionCount() == 1) {
                ConceptTreeNode targetTreeNode = (ConceptTreeNode) conceptTree.getLastSelectedPathComponent();
                targetConceptTreeNode = new ConceptTreeNode(targetTreeNode, project);
                copyTargetTreeNode(targetConceptTreeNode, targetTreeNode);
                DODDLE_OWL.STATUS_BAR.setValue(title + ": " + targetConceptTreeNode.getConcept());
                project.addLog(getTitle());
            }
        }
    }

    class CutConceptAction extends AbstractAction {

        private String title;

        public String getTitle() {
            return title;
        }

        CutConceptAction(String title) {
            super(title, cutIcon);
            this.title = title;
            setToolTipText(title);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            if (conceptTree.getSelectionCount() == 1) {
                addCommand();
                ConceptTreeNode targetTreeNode = (ConceptTreeNode) conceptTree.getLastSelectedPathComponent();
                targetConceptTreeNode = new ConceptTreeNode(targetTreeNode, project);
                copyTargetTreeNode(targetConceptTreeNode, targetTreeNode);
                deleteLinkToUpperConcept();
                DODDLE_OWL.STATUS_BAR.setValue(title + ": " + targetConceptTreeNode.getConcept());
                project.addLog(getTitle());
            }
        }

    }

    private void pasteConcept2(ConceptTreeNode insertedConceptTreeNode, ConceptTreeNode pasteConceptTreeNode) {
        for (int i = 0; i < pasteConceptTreeNode.getChildCount(); i++) {
            ConceptTreeNode childPasteConceptTreeNode = (ConceptTreeNode) pasteConceptTreeNode.getChildAt(i);
            ConceptTreeNode childInsertedConceptTreeNode = insertConceptTreeNode(insertedConceptTreeNode,
                    childPasteConceptTreeNode);
            pasteConcept2(childInsertedConceptTreeNode, childPasteConceptTreeNode);
            conceptTree.expandPath(new TreePath(childInsertedConceptTreeNode.getPath()));
        }
    }

    private Concept pasteConcept(ConceptTreeNode pasteTargetNode, ConceptTreeNode pasteConceptTreeNode) {
        Concept pasteConcept = pasteConceptTreeNode.getConcept();
        if (pasteTargetNode != null) {
            Set<ConceptTreeNode> insertedConceptTreeNodeSet = insertConceptTreeNode(pasteConcept, pasteTargetNode
                    .getConcept(), pasteConceptTreeNode.isInputConcept(), pasteConceptTreeNode.isUserConcept());
            for (ConceptTreeNode insertedConceptTreeNode : insertedConceptTreeNodeSet) {
                pasteConcept2(insertedConceptTreeNode, pasteConceptTreeNode);
                conceptTree.expandPath(new TreePath(insertedConceptTreeNode.getPath()));
            }
            checkMultipleInheritanceNode(pasteConcept);
        }
        return pasteConcept;
    }

    class PasteConceptAction extends AbstractAction {

        private String title;

        public String getTitle() {
            return title;
        }

        PasteConceptAction(String title) {
            super(title, pasteIcon);
            this.title = title;
            setToolTipText(title);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            if (conceptTree.getSelectionCount() == 1) {
                addCommand();
                ConceptTreeNode pasteTargetNode = (ConceptTreeNode) conceptTree.getLastSelectedPathComponent();
                Concept pasteConcept = pasteConcept(pasteTargetNode, targetConceptTreeNode);
                DODDLE_OWL.STATUS_BAR.setValue(title + ": " + pasteConcept);
                project.addLog(getTitle());
            }
        }

    }

    class UndoAction extends AbstractAction {

        public UndoAction(String title, Icon icon) {
            super(title, icon);
            setToolTipText(title);
            setEnabled(false);
            InputMap inputMap = conceptTree.getInputMap();
            KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_MASK);
            putValue(ACCELERATOR_KEY, keyStroke);
            inputMap.put(keyStroke, title);
        }

        public void actionPerformed(ActionEvent e) {
            project.undo();
            setEnabled(project.canUndo());
            redoAction.setEnabled(project.canRedo());
            if (treeType.equals(CLASS_ISA_TREE) || treeType.equals(PROPERTY_ISA_TREE)) {
                TreeModel model = conceptTree.getModel();
                ConceptTreeNode rootNode = (ConceptTreeNode) model.getRoot();
                conceptDriftManagementPanel.resetConceptDriftManagementResult(rootNode);
            }
        }
    }

    class RedoAction extends AbstractAction {

        public RedoAction(String title, Icon icon) {
            super(title, icon);
            setToolTipText(title);
            setEnabled(false);
            InputMap inputMap = conceptTree.getInputMap();
            KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_MASK);
            putValue(ACCELERATOR_KEY, keyStroke);
            inputMap.put(keyStroke, title);
        }

        public void actionPerformed(ActionEvent e) {
            project.redo();
            undoAction.setEnabled(project.canUndo());
            setEnabled(project.canRedo());
            if (treeType.equals(CLASS_ISA_TREE) || treeType.equals(PROPERTY_ISA_TREE)) {
                TreeModel model = conceptTree.getModel();
                ConceptTreeNode rootNode = (ConceptTreeNode) model.getRoot();
                conceptDriftManagementPanel.resetConceptDriftManagementResult(rootNode);
            }
        }
    }

    class AddConceptAction extends AbstractAction {

        private String title;
        private boolean isAddSubConcept;

        AddConceptAction(String title, Icon icon, boolean t) {
            super(title, icon);
            this.title = title;
            isAddSubConcept = t;
            setToolTipText(title);
            InputMap inputMap = conceptTree.getInputMap();
            if (t) {
                KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
                putValue(ACCELERATOR_KEY, keyStroke);
                inputMap.put(keyStroke, title);
            } else {
                KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_MASK);
                putValue(ACCELERATOR_KEY, keyStroke);
                inputMap.put(keyStroke, title);
            }
        }

        public String getTitle() {
            return title;
        }

        private void insertIsaTreeConcept() {
            TreeModel isaTreeModel = null;
            if (treeType == CLASS_HASA_TREE) {
                isaTreeModel = project.getConstructClassPanel().getIsaTree().getModel();
            } else if (treeType == PROPERTY_HASA_TREE) {
                isaTreeModel = project.getConstructPropertyPanel().getIsaTree().getModel();
            }
            conceptSelectionDialog.setTreeModel(isaTreeModel);
            conceptSelectionDialog.setVisible(true);
            Set<Concept> addConceptSet = conceptSelectionDialog.getConceptSet();
            if (addConceptSet.size() == 0) { return; }
            ConceptTreeNode parentNode = (ConceptTreeNode) conceptTree.getLastSelectedPathComponent();
            if (parentNode != null) {
                ConceptTreeNode rootNode = (ConceptTreeNode) isaTreeModel.getRoot();
                for (Concept isaConcept : addConceptSet) {
                    Set<ConceptTreeNode> sameConceptTreeNodeSet = new HashSet<ConceptTreeNode>();
                    searchSameConceptTreeNode(isaConcept, rootNode, sameConceptTreeNodeSet);
                    ConceptTreeNode treeNode = (ConceptTreeNode) sameConceptTreeNodeSet.toArray()[0];
                    if (!isAddSubConcept && treeNode != rootNode) {
                        treeNode = (ConceptTreeNode) treeNode.getParent();
                        parentNode = (ConceptTreeNode) parentNode.getParent();
                    }
                    if (parentNode != null) {
                        insertConceptTreeNode(isaConcept, parentNode.getConcept(), treeNode.isInputConcept(), treeNode
                                .isUserConcept());
                    }
                    checkMultipleInheritanceNode(isaConcept);
                    conceptTree.expandPath(conceptTree.getSelectionPath());
                }
            }
        }

        private void insertNewConcept() {
            ConceptTreeNode parent = (ConceptTreeNode) conceptTree.getLastSelectedPathComponent();
            if (parent != null) {
                if (isAddSubConcept) {
                    insertNewConceptTreeNode(Translator.getTerm("NewConceptLabel") + project.getUserIDCount(), parent
                            .getConcept());
                    conceptTree.expandPath(conceptTree.getSelectionPath());
                } else {
                    parent = (ConceptTreeNode) parent.getParent();
                    if (parent != null) {
                        insertNewConceptTreeNode(Translator.getTerm("NewConceptLabel") + project.getUserIDCount(),
                                parent.getConcept());
                    }
                }
            }
        }

        public void actionPerformed(ActionEvent e) {
            if (treeType == CLASS_ISA_TREE || treeType == PROPERTY_ISA_TREE) {
                addCommand();
                insertNewConcept();
                project.addLog(getTitle(), treeType);
            } else if (treeType == CLASS_HASA_TREE || treeType == PROPERTY_HASA_TREE) {
                addCommand();
                insertIsaTreeConcept();
                project.addLog(getTitle(), treeType);
            }
        }
    }

    private void deleteLinkToUpperConcept() {
        ConceptTreeNode targetDeleteNode = (ConceptTreeNode) conceptTree.getLastSelectedPathComponent();
        deleteLinkToUpperConcept(targetDeleteNode);
    }

    private void deleteInternalConcept() {
        ConceptTreeNode targetDeleteNode = (ConceptTreeNode) conceptTree.getLastSelectedPathComponent();
        deleteInternalConcept(targetDeleteNode);
    }

    public void addCommand() {
        ConceptTreeNode targetNode = (ConceptTreeNode) conceptTree.getLastSelectedPathComponent();
        Concept targetConcept = null;
        Concept parentConcept = null;
        if (targetNode != null) {
            targetConcept = targetNode.getConcept();
            ConceptTreeNode parentNode = null;
            if (targetNode.getParent() != null) {
                parentNode = (ConceptTreeNode) targetNode.getParent();
                parentConcept = parentNode.getConcept();
            }
        }
        project.addCommand(parentConcept, targetConcept, treeType);
        undoAction.setEnabled(project.canUndo());
        redoAction.setEnabled(project.canRedo());
    }

    /**
     * @param targetDeleteNode
     */
    public void deleteInternalConcept(ConceptTreeNode targetDeleteNode) {
        DefaultTreeModel model = (DefaultTreeModel) conceptTree.getModel();
        if (targetDeleteNode != null && targetDeleteNode.getParent() != null) {
            ConceptTreeNode targetDeleteNodeParent = (ConceptTreeNode) targetDeleteNode.getParent();

            Set<ConceptTreeNode> targetDeleteNodeChildren = new TreeSet<ConceptTreeNode>();
            for (int i = 0; i < targetDeleteNode.getChildCount(); i++) {
                targetDeleteNodeChildren.add((ConceptTreeNode) targetDeleteNode.getChildAt(i));
            }

            if (targetDeleteNodeParent != null) {
                for (ConceptTreeNode targetDeleteNodeChild : targetDeleteNodeChildren) {
                    model.insertNodeInto(targetDeleteNodeChild, targetDeleteNodeParent, 0);
                }
            }
            model.removeNodeFromParent(targetDeleteNode);
            if (treeType == CLASS_ISA_TREE || treeType == PROPERTY_ISA_TREE) {
                checkMultipleInheritanceNode(targetDeleteNode.getConcept());
                conceptDriftManagementPanel.resetConceptDriftManagementResult((ConceptTreeNode) model.getRoot());
            }
        }
    }

    /**
     * @param targetDeleteNode
     */
    public void deleteLinkToUpperConcept(ConceptTreeNode targetDeleteNode) {
        DefaultTreeModel model = (DefaultTreeModel) conceptTree.getModel();
        if (targetDeleteNode != null && targetDeleteNode.getParent() != null) {
            ConceptTreeNode targetDeleteNodeParent = (ConceptTreeNode) targetDeleteNode.getParent();
            ConceptTreeNode rootNode = (ConceptTreeNode) model.getRoot();
            Set<ConceptTreeNode> deleteTreeNodeSet = new HashSet<ConceptTreeNode>();
            searchSameConceptTreeNode(targetDeleteNode.getConcept(), rootNode, deleteTreeNodeSet);
            for (ConceptTreeNode deleteTreeNode : deleteTreeNodeSet) {
                ConceptTreeNode deleteTreeNodeParent = (ConceptTreeNode) deleteTreeNode.getParent();
                if (deleteTreeNodeParent != null
                        && targetDeleteNodeParent.getURI().equals(deleteTreeNodeParent.getURI())) {
                    model.removeNodeFromParent(deleteTreeNode);
                }
            }
            if (treeType == CLASS_ISA_TREE || treeType == PROPERTY_ISA_TREE) {
                checkMultipleInheritanceNode(targetDeleteNode.getConcept());
                conceptDriftManagementPanel.resetConceptDriftManagementResult(rootNode);
            }
        }
    }

    class DeleteInternalConceptAction extends AbstractAction {

        private String title;

        public String getTitle() {
            return title;
        }

        DeleteInternalConceptAction(String title) {
            super(title, removeInternalConceptIcon);
            this.title = title;
            setToolTipText(title);
            InputMap inputMap = conceptTree.getInputMap();
            KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, KeyEvent.SHIFT_MASK);
            putValue(ACCELERATOR_KEY, keyStroke);
            inputMap.put(keyStroke, title);
        }

        public void actionPerformed(ActionEvent e) {
            DODDLE_OWL.STATUS_BAR.setValue(title + ": " + conceptTree.getLastSelectedPathComponent());
            addCommand();
            deleteInternalConcept();
            project.addLog(getTitle());
        }
    }

    class DeleteLinkToUpperConceptAction extends AbstractAction {

        private String title;

        public String getTitle() {
            return title;
        }

        DeleteLinkToUpperConceptAction(String title) {
            super(title, removeUpperLinkIcon);
            this.title = title;
            setToolTipText(title);
            InputMap inputMap = conceptTree.getInputMap();
            KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
            putValue(ACCELERATOR_KEY, keyStroke);
            inputMap.put(keyStroke, title);
        }

        public void actionPerformed(ActionEvent e) {
            DODDLE_OWL.STATUS_BAR.setValue(title + ": " + conceptTree.getLastSelectedPathComponent());
            addCommand();
            deleteLinkToUpperConcept();
            project.addLog(getTitle());
        }
    }

    class DeleteConceptAction extends AbstractAction {

        private String title;

        DeleteConceptAction(String title) {
            super(title, removeConceptIcon);
            this.title = title;
            setToolTipText(title);
            InputMap inputMap = conceptTree.getInputMap();
            KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, KeyEvent.CTRL_MASK);
            putValue(ACCELERATOR_KEY, keyStroke);
            inputMap.put(keyStroke, title);
        }

        public String getTitle() {
            return title;
        }

        public void actionPerformed(ActionEvent e) {
            DODDLE_OWL.STATUS_BAR.setValue(title + ": " + conceptTree.getLastSelectedPathComponent());
            addCommand();
            deleteConcept();
            project.addLog(getTitle());
        }
    }

    class AddUndefinedTermListAction extends AbstractAction {

        private String title;

        public String getTitle() {
            return title;
        }

        AddUndefinedTermListAction(String title) {
            super(title);
            this.title = title;
            setToolTipText(title);
        }

        public void actionPerformed(ActionEvent e) {
            DefaultListModel listModel = undefinedTermListPanel.getModel();
            DefaultListModel viewListModel = undefinedTermListPanel.getViewModel();
            for (Object selectedValue : undefinedTermListPanel.getSelectedValues()) {
                ConceptTreeNode parent = (ConceptTreeNode) conceptTree.getLastSelectedPathComponent();
                if (parent != null) {
                    insertNewConceptTreeNode(selectedValue.toString(), parent.getConcept());
                    listModel.removeElement(selectedValue);
                    if (viewListModel != null) {
                        viewListModel.removeElement(selectedValue);
                    }
                }
            }
            undefinedTermListPanel.setTitleWithSize();
        }
    }

    class MoveUndefinedTermListAction extends AbstractAction {

        private String title;

        public String getTitle() {
            return title;
        }

        MoveUndefinedTermListAction(String title) {
            super(title);
            this.title = title;
            setToolTipText(title);
        }

        public void actionPerformed(ActionEvent e) {
            TreePath path = conceptTree.getSelectionPath();
            if (path.getLastPathComponent() != null) {
                ConceptTreeNode conceptTreeNode = (ConceptTreeNode) path.getLastPathComponent();
                // もう少し，実装をつめる必要あり
                DefaultListModel listModel = undefinedTermListPanel.getModel();
                DefaultListModel viewListModel = undefinedTermListPanel.getViewModel();
                listModel.addElement(conceptTreeNode.toString());
                if (viewListModel != null
                        && conceptTreeNode.toString().matches(undefinedTermListPanel.getSearchRegex())) {
                    viewListModel.addElement(conceptTreeNode.toString());
                }
                deleteConcept();
                undefinedTermListPanel.setTitleWithSize();
            }
        }
    }

    public Map getConceptPreferentialTermMap() {
        Map conceptPreferentialTermMap = new HashMap();
        DefaultTreeModel model = (DefaultTreeModel) conceptTree.getModel();
        if (model.getRoot() instanceof ConceptTreeNode) {
            ConceptTreeNode rootNode = (ConceptTreeNode) model.getRoot();
            saveConceptPreferentialTerm(rootNode, conceptPreferentialTermMap);
        }
        return conceptPreferentialTermMap;
    }

    private void saveConceptPreferentialTerm(ConceptTreeNode node, Map conceptPreferentialTermMap) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            if (childNode.getInputWord() != null) {
                conceptPreferentialTermMap.put(childNode.getURI(), childNode.getInputWord());
            }
            saveConceptPreferentialTerm(childNode, conceptPreferentialTermMap);
        }
    }

    public void loadConceptPreferentialTerm(Map idPreferentialTermMap) {
        DefaultTreeModel model = (DefaultTreeModel) conceptTree.getModel();
        if (model.getRoot() instanceof ConceptTreeNode) {
            ConceptTreeNode rootNode = (ConceptTreeNode) model.getRoot();
            loadConceptPreferentialTerm(rootNode, idPreferentialTermMap);
        }
    }

    private void loadConceptPreferentialTerm(ConceptTreeNode node, Map conceptPreferentialTermMap) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            if (conceptPreferentialTermMap.get(childNode.getURI()) != null) {
                String inputWord = (String) conceptPreferentialTermMap.get(childNode.getURI());
                Concept concept = childNode.getConcept();
                concept.setInputLabel(new DODDLELiteral("", inputWord));
                childNode.setConcept(concept);
                ((DefaultTreeModel) conceptTree.getModel()).reload(childNode);
            }
            loadConceptPreferentialTerm(childNode, conceptPreferentialTermMap);
        }
    }

    public void expandSubTree(ConceptTreeNode node) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            conceptTree.expandPath(new TreePath(childNode.getPath()));
            expandSubTree(childNode);
        }
    }

    class ExpandSelectedPathAction extends AbstractAction {

        public ExpandSelectedPathAction(String title) {
            super(title);
        }

        public void actionPerformed(ActionEvent e) {
            expandSubTree((ConceptTreeNode) conceptTree.getLastSelectedPathComponent());
            // conceptTree.expandPath(conceptTree.getSelectionPath());
        }
    }

    class ExpandAllPathAction extends AbstractAction {

        public ExpandAllPathAction(String title) {
            super(title);
        }

        public void actionPerformed(ActionEvent e) {
            for (int i = 0; i < conceptTree.getRowCount(); i++) {
                conceptTree.expandPath(conceptTree.getPathForRow(i));
            }
        }
    }

    public void selectConceptTreeNode(ConceptTreeNode node, Concept targetConcept, Concept parentConcept) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            if (parentConcept == null && childNode.getConcept().equals(targetConcept)) {
                conceptTree.setSelectionPath(new TreePath(childNode.getPath()));
                return;
            } else if (node.getConcept().equals(parentConcept) && childNode.getConcept().equals(targetConcept)) {
                conceptTree.setSelectionPath(new TreePath(childNode.getPath()));
                return;
            }
            selectConceptTreeNode(childNode, targetConcept, parentConcept);
        }
    }

}
