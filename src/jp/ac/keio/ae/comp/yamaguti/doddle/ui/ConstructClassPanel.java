package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

/*
 * 2005/03/01
 *  
 */

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.tree.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

import org.apache.log4j.*;

/**
 * @author takeshi morita
 * 
 */
public class ConstructClassPanel extends ConstructConceptTreePanel {

    public ConstructClassPanel(DODDLEProject p) {
        project = p;
        undefinedWordListPanel = new UndefinedWordListPanel();
        conceptTreePanel = new ConceptTreePanel(Translator.getString("ClassTreePanel.ConceptTree"),
                undefinedWordListPanel, p);
        conceptDriftManagementPanel = new ConceptDriftManagementPanel(ConceptTreeCellRenderer.NOUN_CONCEPT_TREE,
                conceptTreePanel.getConceptTree(), project);

        conceptInfoPanel = new ConceptInformationPanel(conceptTreePanel.getConceptTree(), new ConceptTreeCellRenderer(
                ConceptTreeCellRenderer.NOUN_CONCEPT_TREE), conceptDriftManagementPanel);
        JSplitPane eastSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, conceptInfoPanel,
                conceptDriftManagementPanel);
        eastSplitPane.setDividerSize(DODDLE.DIVIDER_SIZE);
        eastSplitPane.setOneTouchExpandable(true);

        JSplitPane westPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, undefinedWordListPanel, conceptTreePanel);
        westPane.setOneTouchExpandable(true);
        westPane.setDividerSize(10);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, westPane, eastSplitPane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerSize(DODDLE.DIVIDER_SIZE);
        this.setLayout(new BorderLayout());
        this.add(splitPane, BorderLayout.CENTER);
    }

    public TreeModel getTreeModel(Set<Concept> conceptSet) {
        Set pathSet = treeMaker.getPathList(conceptSet);
        trimmedConceptNum = 0;
        TreeModel model = treeMaker.getTrimmedTreeModel(pathSet, project, ConceptTreeMaker.DODDLE_CLASS_ROOT_ID);
        trimmedConceptNum = treeMaker.getTrimmedConceptNum();
        beforeTrimmingConceptNum = treeMaker.getBeforeTrimmingConceptNum();
        addedSINNum = beforeTrimmingConceptNum - conceptSet.size();
        DODDLE.getLogger().log(Level.INFO, "クラス階層構築における追加SIN数: " + addedSINNum);
        DODDLE.getLogger().log(Level.INFO, "剪定前クラス数: " + beforeTrimmingConceptNum);
        DODDLE.getLogger().log(Level.INFO, "剪定クラス数: " + trimmedConceptNum);
        DODDLE.getLogger().log(Level.INFO, "剪定後クラス数: " + getAfterTrimmingConceptNum());
        conceptTreePanel.checkAllMultipleInheritanceNode(model);
        treeMaker.conceptDriftManagement(model);
        setConceptDriftManagementResult();
        return model;
    }

    public void addComplexWordConcept(Map matchedWordIDMap, Map abstractNodeLabelMap, TreeNode rootNode) {
        DefaultTreeModel model = (DefaultTreeModel) conceptTreePanel.getConceptTree().getModel();
        ConceptTreeNode conceptTreeRootNode = (ConceptTreeNode) model.getRoot();
        conceptTreePanel.addComplexWordConcept(matchedWordIDMap, rootNode, conceptTreeRootNode, abstractNodeLabelMap);
        DODDLE.getLogger().log(Level.INFO, "追加した抽象中間ノード数: " + conceptTreePanel.getAbstractNodeCnt());
        addedAbstractComplexConceptCnt = conceptTreePanel.getAbstractConceptCnt();
        DODDLE.getLogger().log(Level.INFO, "追加した抽象中間クラス数: " + addedAbstractComplexConceptCnt);
        if (addedAbstractComplexConceptCnt == 0) {
            averageAbstracComplexConceptGroupSiblingConceptCnt = 0;
        } else {
            averageAbstracComplexConceptGroupSiblingConceptCnt = conceptTreePanel
                    .getTotalAbstractNodeGroupSiblingNodeCnt()
                    / addedAbstractComplexConceptCnt;
        }
        DODDLE.getLogger().log(Level.INFO,
                "抽象中間クラスの平均兄弟クラスグループ化数: " + averageAbstracComplexConceptGroupSiblingConceptCnt);
    }

}