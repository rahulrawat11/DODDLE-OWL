package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.Map.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.actions.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

import org.apache.log4j.*;

/*
 * Created on 2004/08/22
 *
 */

/**
 * @author takeshi morita
 * 
 */
public class DisambiguationPanel extends JPanel implements ListSelectionListener, ActionListener, TreeSelectionListener {

    private Set<Concept> inputConceptSet; // 入力概念のセット
    private Set<Concept> inputNounConceptSet; // 入力名詞的概念のセット
    private Set<Concept> inputVerbConceptSet; // 入力動詞的概念のセット

    private Set<InputWordModel> inputWordModelSet; // 入力単語モデルのセット
    private Map<String, Set<Concept>> wordConceptSetMap; // 入力単語と入力単語を見出しを含むIDのマッピング
    private Map<String, Concept> wordConceptMap; // 入力単語と適切に対応するIDのマッピング
    private Map<String, Set<EvalConcept>> wordEvalConceptSetMap;
    private Map<InputWordModel, ConstructTreeOption> complexConstructTreeOptionMap;

    private TitledBorder perfectMatchedWordJListTitle;
    private TitledBorder partialMatchedWordJListTitle;

    private JTabbedPane wordListTabbedPane;

    private JTextField searchWordField;
    private JButton searchWordButton;

    private JList perfectMatchedWordJList; // 完全照合した単語リスト
    private Set<InputWordModel> perfectMatchedWordModelSet;
    private JList partialMatchedWordJList; // 部分照合した単語リスト
    private Set<InputWordModel> partialMatchedWordModelSet;
    private JList conceptSetJList;
    private UndefinedWordListPanel undefinedWordListPanel;

    private DefaultListModel undefinedWordListModel;

    private JCheckBox perfectMatchedAmbiguityCntCheckBox;
    private JCheckBox perfectMatchedShowOnlyRelatedComplexWordsCheckBox;
    private JCheckBox perfectMatchedIsSyncCheckBox;

    private JCheckBox partialMatchedComplexWordCheckBox;
    private JCheckBox partialMatchedMatchedWordBox;
    private JCheckBox partialMatchedAmbiguityCntCheckBox;

    private JList jpWordList;
    private JList enWordList;
    private JTextArea jpExplanationArea;
    private JTextArea enExplanationArea;

    private JRadioButton addAsSubConceptRadioButton;
    private JRadioButton addAsSameConceptRadioButton;

    private JList hilightPartJList;
    // private JEditorPane documentArea;
    private JTextArea documentArea;
    private JCheckBox viewHilightCheckBox;
    private JCheckBox showAroundConceptTreeCheckBox;
    private JTree aroundConceptTree;
    private TreeModel aroundConceptTreeModel;

    private InputModule inputModule;
    private ConstructClassPanel constructClassPanel;
    private ConstructPropertyPanel constructPropertyPanel;

    private JButton automaticDisAmbiguationButton;
    private JButton constructNounTreeButton;
    private JButton constructNounAndVerbTreeButton;
    private JButton showConceptDescriptionButton;

    private AutomaticDisAmbiguationAction automaticDisAmbiguationAction;
    private ConstructTreeAction constructNounTreeAction;
    private ConstructTreeAction constructNounAndVerbTreeAction;

    private Action saveCompleteMatchWordAction;
    private Action saveCompleteMatchWordWithComplexWordAcion;

    private ConceptDescriptionFrame conceptDescriptionFrame;

    private DocumentSelectionPanel docSelectionPanel;

    private DODDLEProject project;

    public DisambiguationPanel(ConstructClassPanel tp, ConstructPropertyPanel pp, DODDLEProject p) {
        project = p;
        constructClassPanel = tp;
        constructPropertyPanel = pp;
        inputModule = new InputModule(project);
        wordConceptMap = new HashMap<String, Concept>();
        complexConstructTreeOptionMap = new HashMap<InputWordModel, ConstructTreeOption>();

        conceptDescriptionFrame = new ConceptDescriptionFrame();

        conceptSetJList = new JList();
        conceptSetJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        conceptSetJList.addListSelectionListener(this);
        JScrollPane conceptJListScroll = new JScrollPane(conceptSetJList);
        conceptJListScroll.setBorder(BorderFactory.createTitledBorder(Translator
                .getString("DisambiguationPanel.ConceptList")));

        jpWordList = new JList();
        JScrollPane jpWordsAreaScroll = new JScrollPane(jpWordList);
        jpWordsAreaScroll.setBorder(BorderFactory.createTitledBorder(Translator
                .getString("DisambiguationPanel.JapaneseWords")));
        enWordList = new JList();
        JScrollPane enWordsAreaScroll = new JScrollPane(enWordList);
        enWordsAreaScroll.setBorder(BorderFactory.createTitledBorder(Translator
                .getString("DisambiguationPanel.EnglishWords")));
        jpExplanationArea = new JTextArea();
        jpExplanationArea.setLineWrap(true);
        JScrollPane jpExplanationAreaScroll = new JScrollPane(jpExplanationArea);
        jpExplanationAreaScroll.setBorder(BorderFactory.createTitledBorder(Translator
                .getString("DisambiguationPanel.JapaneseConceptExplanation")));
        enExplanationArea = new JTextArea();
        enExplanationArea.setLineWrap(true);
        JScrollPane enExplanationAreaScroll = new JScrollPane(enExplanationArea);
        enExplanationAreaScroll.setBorder(BorderFactory.createTitledBorder(Translator
                .getString("DisambiguationPanel.EnglishConceptExplanation")));

        addAsSameConceptRadioButton = new JRadioButton(Translator.getString("DisambiguationPanel.SameConcept"), true);
        addAsSameConceptRadioButton.addActionListener(this);
        addAsSubConceptRadioButton = new JRadioButton(Translator.getString("DisambiguationPanel.SubConcept"));
        addAsSubConceptRadioButton.addActionListener(this);
        ButtonGroup group = new ButtonGroup();
        group.add(addAsSameConceptRadioButton);
        group.add(addAsSubConceptRadioButton);
        JPanel constructTreeOptionPanel = new JPanel();
        constructTreeOptionPanel.setLayout(new GridLayout(1, 2));
        constructTreeOptionPanel.setBorder(BorderFactory.createTitledBorder(Translator
                .getString("DisambiguationPanel.ConstructTreeOption")));
        constructTreeOptionPanel.add(addAsSameConceptRadioButton);
        constructTreeOptionPanel.add(addAsSubConceptRadioButton);

        JPanel explanationPanel = new JPanel();
        explanationPanel.setLayout(new GridLayout(5, 1, 5, 5));
        explanationPanel.add(jpWordsAreaScroll);
        explanationPanel.add(enWordsAreaScroll);
        explanationPanel.add(jpExplanationAreaScroll);
        explanationPanel.add(enExplanationAreaScroll);
        explanationPanel.add(constructTreeOptionPanel);

        undefinedWordListPanel = new UndefinedWordListPanel();

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new GridLayout(1, 3, 5, 5));
        listPanel.add(conceptJListScroll);
        listPanel.add(explanationPanel);
        listPanel.add(undefinedWordListPanel);

        hilightPartJList = new JList();
        hilightPartJList.addListSelectionListener(this);
        JScrollPane hilightPartJListScroll = new JScrollPane(hilightPartJList);
        hilightPartJListScroll.setBorder(BorderFactory.createTitledBorder("行番号"));
        hilightPartJListScroll.setPreferredSize(new Dimension(100, 100));
        // documentArea = new JEditorPane("text/html", "");
        documentArea = new JTextArea();
        documentArea.setEditable(false);
        documentArea.setLineWrap(true);
        JScrollPane documentAreaScroll = new JScrollPane(documentArea);
        documentAreaScroll.setBorder(BorderFactory.createTitledBorder(Translator
                .getString("DisambiguationPanel.InputDocument")));
        viewHilightCheckBox = new JCheckBox(Translator.getString("DisambiguationPanel.HighlightInputWords"), false);

        showAroundConceptTreeCheckBox = new JCheckBox(Translator.getString("DisambiguationPanel.ShowConceptTree"),
                false);

        aroundConceptTreeModel = new DefaultTreeModel(null);
        aroundConceptTree = new JTree(aroundConceptTreeModel);
        aroundConceptTree.addTreeSelectionListener(this);
        aroundConceptTree.setEditable(false);
        aroundConceptTree.setCellRenderer(new AroundTreeCellRenderer());
        JScrollPane aroundConceptTreeScroll = new JScrollPane(aroundConceptTree);
        aroundConceptTreeScroll.setBorder(BorderFactory.createTitledBorder(Translator
                .getString("DisambiguationPanel.ConceptTree")));

        JPanel treePanel = new JPanel();
        treePanel.setLayout(new BorderLayout());
        treePanel.add(aroundConceptTreeScroll, BorderLayout.CENTER);
        treePanel.add(showAroundConceptTreeCheckBox, BorderLayout.SOUTH);

        JPanel documentPanel = new JPanel();
        documentPanel.setLayout(new BorderLayout());
        documentPanel.add(documentAreaScroll, BorderLayout.CENTER);
        documentPanel.add(viewHilightCheckBox, BorderLayout.SOUTH);
        // documentPanel.add(hilightPartJListScroll, BorderLayout.WEST);

        JPanel referencePanel = new JPanel();
        referencePanel.setLayout(new GridLayout(1, 2));
        referencePanel.add(documentPanel);
        referencePanel.add(treePanel);

        automaticDisAmbiguationAction = new AutomaticDisAmbiguationAction(Translator
                .getString("DisambiguationPanel.AutomaticDisambiguation"));
        automaticDisAmbiguationButton = new JButton(automaticDisAmbiguationAction);
        // showConceptDescriptionButton = new JButton(new
        // ShowConceptDescriptionAction("概念記述を表示"));

        JPanel p1 = new JPanel();
        p1.add(automaticDisAmbiguationButton);
        // p1.add(showConceptDescriptionButton);

        constructNounTreeButton = new JButton(new ConstructNounTreeAction());
        constructNounAndVerbTreeButton = new JButton(new ConstructNounAndVerbTreeAction());
        JPanel p2 = new JPanel();
        p2.add(constructNounTreeButton);
        p2.add(constructNounAndVerbTreeButton);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(p1, BorderLayout.WEST);
        buttonPanel.add(p2, BorderLayout.EAST);

        setLayout(new BorderLayout());
        JSplitPane verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, listPanel, referencePanel);
        verticalSplitPane.setOneTouchExpandable(true);
        verticalSplitPane.setDividerSize(DODDLE.DIVIDER_SIZE);
        JSplitPane horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getWordListPanel(),
                verticalSplitPane);
        horizontalSplitPane.setOneTouchExpandable(true);
        horizontalSplitPane.setDividerSize(DODDLE.DIVIDER_SIZE);
        add(horizontalSplitPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    public Map<InputWordModel, ConstructTreeOption> getComplexConstructTreeOptionMap() {
        return complexConstructTreeOptionMap;
    }

    private JPanel getWordListPanel() {
        JPanel perfectMatchedWordListPanel = getPerfectMatchedWordListPanel();
        JPanel partialMatchedWordListPanel = getPartialMatchedWordListPanel();

        wordListTabbedPane = new JTabbedPane();
        wordListTabbedPane.add(Translator.getString("DisambiguationPanel.PerfectMatch"), perfectMatchedWordListPanel);
        wordListTabbedPane.add(Translator.getString("DisambiguationPanel.PartialMatch"), partialMatchedWordListPanel);

        JPanel wordListPanel = new JPanel();
        wordListPanel.setLayout(new BorderLayout());
        wordListPanel.add(getSearchWordPanel(), BorderLayout.NORTH);
        wordListPanel.add(wordListTabbedPane, BorderLayout.CENTER);

        return wordListPanel;
    }

    private JPanel getSearchWordPanel() {
        searchWordField = new JTextField();
        searchWordField.addActionListener(this);
        searchWordButton = new JButton(Translator.getString("DisambiguationPanel.Search"));
        searchWordButton.addActionListener(this);
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BorderLayout());
        searchPanel.add(searchWordField, BorderLayout.CENTER);
        searchPanel.add(searchWordButton, BorderLayout.EAST);
        return searchPanel;
    }

    private JPanel getPerfectMatchedWordListPanel() {
        perfectMatchedWordJList = new JList();
        perfectMatchedWordJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        perfectMatchedWordJList.addListSelectionListener(this);
        JScrollPane perfectMatchedWordListScroll = new JScrollPane(perfectMatchedWordJList);
        perfectMatchedWordJListTitle = BorderFactory.createTitledBorder(Translator
                .getString("DisambiguationPanel.PerfectMatchWordList"));
        perfectMatchedWordListScroll.setBorder(perfectMatchedWordJListTitle);

        perfectMatchedAmbiguityCntCheckBox = new JCheckBox(Translator
                .getString("DisambiguationPanel.DisambiguationCount"), true);
        perfectMatchedAmbiguityCntCheckBox.addActionListener(this);
        perfectMatchedShowOnlyRelatedComplexWordsCheckBox = new JCheckBox(Translator
                .getString("DisambiguationPanel.ShowOnlyCorrespondComplexWords"), false);
        perfectMatchedShowOnlyRelatedComplexWordsCheckBox.addActionListener(this);
        perfectMatchedIsSyncCheckBox = new JCheckBox(Translator
                .getString("DisambiguationPanel.ApplyDisambiguationResultToCorrenpondancePartialMatchedWords"), true);
        JPanel perfectMatchedFilterPanel = new JPanel();
        perfectMatchedFilterPanel.setLayout(new GridLayout(3, 1));
        perfectMatchedFilterPanel.add(perfectMatchedAmbiguityCntCheckBox);
        perfectMatchedFilterPanel.add(perfectMatchedShowOnlyRelatedComplexWordsCheckBox);
        perfectMatchedFilterPanel.add(perfectMatchedIsSyncCheckBox);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(perfectMatchedWordListScroll, BorderLayout.CENTER);
        panel.add(perfectMatchedFilterPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel getPartialMatchedWordListPanel() {
        partialMatchedWordJList = new JList();
        partialMatchedWordJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        partialMatchedWordJList.addListSelectionListener(this);
        JScrollPane partialMatchedWordListScroll = new JScrollPane(partialMatchedWordJList);
        partialMatchedWordJListTitle = BorderFactory.createTitledBorder(Translator
                .getString("DisambiguationPanel.PartialMatchWordList"));
        partialMatchedWordListScroll.setBorder(partialMatchedWordJListTitle);

        partialMatchedComplexWordCheckBox = new JCheckBox(Translator.getString("DisambiguationPanel.ComplexWord"), true);
        partialMatchedComplexWordCheckBox.addActionListener(this);
        partialMatchedMatchedWordBox = new JCheckBox(Translator.getString("DisambiguationPanel.MatchResult"), true);
        partialMatchedMatchedWordBox.addActionListener(this);
        partialMatchedAmbiguityCntCheckBox = new JCheckBox(Translator
                .getString("DisambiguationPanel.DisambiguationCount"), true);
        partialMatchedAmbiguityCntCheckBox.addActionListener(this);
        JPanel partialMatchedFilterPanel = new JPanel();
        partialMatchedFilterPanel.setLayout(new GridLayout(2, 2));
        partialMatchedFilterPanel.add(partialMatchedComplexWordCheckBox);
        partialMatchedFilterPanel.add(partialMatchedMatchedWordBox);
        partialMatchedFilterPanel.add(partialMatchedAmbiguityCntCheckBox);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(partialMatchedWordListScroll, BorderLayout.CENTER);
        panel.add(partialMatchedFilterPanel, BorderLayout.SOUTH);

        return panel;
    }

    public int getPartialMatchedWordCnt() {
        return partialMatchedWordModelSet.size();
    }

    public int getPerfectMatchedWordCnt() {
        return perfectMatchedWordModelSet.size();
    }

    public int getMatchedWordCnt() {
        return getPartialMatchedWordCnt() + getPerfectMatchedWordCnt();
    }

    public int getUndefinedWordCnt() {
        return undefinedWordListPanel.getModel().getSize();
    }

    public UndefinedWordListPanel getUndefinedWordListPanel() {
        return undefinedWordListPanel;
    }

    public void setDocumentSelectionPanel(DocumentSelectionPanel p) {
        docSelectionPanel = p;
    }

    public void selectTopList() {
        if (perfectMatchedWordJList.getModel().getSize() != 0) {
            perfectMatchedWordJList.setSelectedIndex(0);
        } else {
            enWordList.setListData(new Object[0]);
            jpWordList.setListData(new Object[0]);
            enExplanationArea.setText("");
            jpExplanationArea.setText("");
            addAsSameConceptRadioButton.setEnabled(false);
            addAsSubConceptRadioButton.setEnabled(false);
        }
    }

    public void valueChanged(TreeSelectionEvent e) {
        TreePath path = aroundConceptTree.getSelectionPath();
        if (path != null) {
            ConceptTreeNode node = (ConceptTreeNode) path.getLastPathComponent();
            enWordList.setListData(node.getEnWords());
            jpWordList.setListData(node.getJpWords());
            enExplanationArea.setText(node.getEnExplanation());
            jpExplanationArea.setText(node.getJpExplanation());
        }
    }

    private void saveCompoundOption(String option) {
        JList wordJList = getTargetWordJList();
        InputWordModel iwModel = (InputWordModel) wordJList.getSelectedValue();
        if (iwModel != null && iwModel.isPartialMatchWord()) {
            ConstructTreeOption ctOption = complexConstructTreeOptionMap.get(iwModel);
            ctOption.setOption(option);
            complexConstructTreeOptionMap.put(iwModel, ctOption);
        }
    }

    private void showOnlyRelatedComplexWords() {
        if (perfectMatchedShowOnlyRelatedComplexWordsCheckBox.isSelected()) {
            InputWordModel targetIWModel = (InputWordModel) perfectMatchedWordJList.getSelectedValue();
            if (targetIWModel == null) { return; }
            Set searchedPartialMatchedWordModelSet = new TreeSet();
            for (Iterator i = partialMatchedWordModelSet.iterator(); i.hasNext();) {
                InputWordModel iwModel = (InputWordModel) i.next();
                if (iwModel.getMatchedWord().equals(targetIWModel.getMatchedWord())) {
                    searchedPartialMatchedWordModelSet.add(iwModel);
                }
            }
            partialMatchedWordJList.setListData(searchedPartialMatchedWordModelSet.toArray());
            partialMatchedWordJListTitle.setTitle(Translator.getString("DisambiguationPanel.PartialMatchWordList")
                    + " (" + searchedPartialMatchedWordModelSet.size() + "/" + partialMatchedWordModelSet.size() + ")");
        } else {
            partialMatchedWordJList.setListData(partialMatchedWordModelSet.toArray());
            partialMatchedWordJListTitle.setTitle(Translator.getString("DisambiguationPanel.PartialMatchWordList")
                    + " (" + partialMatchedWordModelSet.size() + ")");
        }
        perfectMatchedWordJList.repaint();
        partialMatchedWordJList.repaint();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == perfectMatchedWordJList || e.getSource() == partialMatchedWordJList) {
            perfectMatchedWordJList.repaint();
            partialMatchedWordJList.repaint();
        } else if (e.getSource() == addAsSameConceptRadioButton) {
            saveCompoundOption("SAME");
        } else if (e.getSource() == addAsSubConceptRadioButton) {
            saveCompoundOption("SUB");
        } else if (e.getSource() == perfectMatchedAmbiguityCntCheckBox
                || e.getSource() == partialMatchedAmbiguityCntCheckBox
                || e.getSource() == partialMatchedComplexWordCheckBox || e.getSource() == partialMatchedMatchedWordBox) {
            perfectMatchedWordJList.repaint();
            partialMatchedWordJList.repaint();
        } else if (e.getSource() == perfectMatchedShowOnlyRelatedComplexWordsCheckBox) {
            showOnlyRelatedComplexWords();
        } else if (e.getSource() == searchWordButton || e.getSource() == searchWordField) {
            String keyWord = searchWordField.getText();
            if (keyWord.length() == 0) {
                perfectMatchedWordJList.setListData(perfectMatchedWordModelSet.toArray());
                perfectMatchedWordJListTitle.setTitle(Translator.getString("DisambiguationPanel.PerfectMatchWordList")
                        + " (" + perfectMatchedWordModelSet.size() + ")");
                partialMatchedWordJList.setListData(partialMatchedWordModelSet.toArray());
                partialMatchedWordJListTitle.setTitle(Translator.getString("DisambiguationPanel.PartialMatchWordList")
                        + " (" + partialMatchedWordModelSet.size() + ")");
            } else {
                Set searchedPerfectMatchedWordModelSet = new TreeSet();
                Set searchedPartialMatchedWordModelSet = new TreeSet();
                for (Iterator i = perfectMatchedWordModelSet.iterator(); i.hasNext();) {
                    InputWordModel iwModel = (InputWordModel) i.next();
                    if (iwModel.getWord().indexOf(keyWord) != -1) {
                        searchedPerfectMatchedWordModelSet.add(iwModel);
                    }
                }
                InputWordModel targetIWModel = (InputWordModel) perfectMatchedWordJList.getSelectedValue();
                for (Iterator i = partialMatchedWordModelSet.iterator(); i.hasNext();) {
                    InputWordModel iwModel = (InputWordModel) i.next();
                    if (iwModel.getWord().indexOf(keyWord) != -1) {
                        if (perfectMatchedShowOnlyRelatedComplexWordsCheckBox.isSelected()) {
                            if (iwModel.getMatchedWord().equals(targetIWModel.getMatchedWord())) {
                                searchedPartialMatchedWordModelSet.add(iwModel);
                            }
                        } else {
                            searchedPartialMatchedWordModelSet.add(iwModel);
                        }
                    }
                }
                perfectMatchedWordJList.setListData(searchedPerfectMatchedWordModelSet.toArray());
                perfectMatchedWordJListTitle.setTitle(Translator.getString("DisambiguationPanel.PerfectMatchWordList")
                        + " (" + searchedPerfectMatchedWordModelSet.size() + "/" + perfectMatchedWordModelSet.size()
                        + ")");
                partialMatchedWordJList.setListData(searchedPartialMatchedWordModelSet.toArray());
                partialMatchedWordJListTitle.setTitle(Translator.getString("DisambiguationPanel.PartialMatchWordList")
                        + " (" + searchedPartialMatchedWordModelSet.size() + "/" + partialMatchedWordModelSet.size()
                        + ")");
            }
            wordListTabbedPane.repaint();
        }
    }

    public boolean isPerfectMatchedAmbiguityCntCheckBox() {
        return perfectMatchedAmbiguityCntCheckBox.isSelected();
    }

    public boolean isPartialMatchedAmbiguityCntCheckBox() {
        return partialMatchedAmbiguityCntCheckBox.isSelected();
    }

    public boolean isPartialMatchedComplexWordCheckBox() {
        return partialMatchedComplexWordCheckBox.isSelected();
    }

    public boolean isPartialMatchedMatchedWordBox() {
        return partialMatchedMatchedWordBox.isSelected();
    }

    public ConstructTreeAction getConstructNounTreeAction() {
        return constructNounTreeAction;
    }

    public ConstructTreeAction getConstructNounAndVerbTreeAction() {
        return constructNounAndVerbTreeAction;
    }

    public Action getSaveCompleteMatchWordAction() {
        return saveCompleteMatchWordAction;
    }

    public Action getSaveCompleteMatchWordWithComplexWordAction() {
        return saveCompleteMatchWordWithComplexWordAcion;
    }

    public Map<String, Concept> getWordConceptMap() {
        return wordConceptMap;
    }

    public Map<String, Set<Concept>> getWordConceptSetMap() {
        return wordConceptSetMap;
    }

    public void loadWordConceptMap() {
        JFileChooser chooser = new JFileChooser(".");
        int retval = chooser.showOpenDialog(DODDLE.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            loadWordConceptMap(chooser.getSelectedFile());
        }
    }

    public void loadWordConceptMap(File file) {
        if (!file.exists()) { return; }
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "JISAutoDetect"));
            Set<String> inputWordSet = new HashSet<String>();
            while (inputWordModelSet == null) {
                try {
                    Thread.sleep(1000);
                    // System.out.println("sleep");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            for (InputWordModel iwModel : inputWordModelSet) {
                inputWordSet.add(iwModel.getWord());
            }
            String line = "";
            while ((line = reader.readLine()) != null) {
                String[] wordID = line.replaceAll("\n", "").split(",");
                if (0 < wordID[0].length()) {
                    String word = wordID[0];
                    String id = wordID[1];
                    InputWordModel iwModel = inputModule.makeInputWordModel(word);
                    if (iwModel != null && inputWordSet.contains(iwModel.getWord())) {
                        Concept c = DODDLEDic.getConcept(id);
                        if (c != null) {
                            wordConceptMap.put(iwModel.getWord(), c);
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public Set<Concept> setInputConceptSet() {
        inputConceptSet = new HashSet<Concept>();
        for (InputWordModel iwModel : inputWordModelSet) {
            Concept c = wordConceptMap.get(iwModel.getWord());
            if (c == null) {
                if (iwModel.isPartialMatchWord()) {
                    c = wordConceptMap.get(iwModel.getMatchedWord());
                    wordConceptMap.put(iwModel.getWord(), c);
                }
                if (c == null) {
                    Set<Concept> conceptSet = wordConceptSetMap.get(iwModel.getMatchedWord());
                    if (conceptSet != null) {
                        c = (Concept) conceptSet.toArray()[0];
                        wordConceptMap.put(iwModel.getWord(), c);
                    }
                }
            }
            if (c.equals(nullConcept)) {
                complexConstructTreeOptionMap.remove(iwModel);
                continue;
            }
            if (iwModel.isPartialMatchWord()) {
                ConstructTreeOption ctOption = new ConstructTreeOption(c);
                complexConstructTreeOptionMap.put(iwModel, ctOption);
            }
            c.setInputWord(iwModel.getMatchedWord()); // メインとなる見出しを設定する
            inputConceptSet.add(c);
        }
        return inputConceptSet;
    }

    public static Concept nullConcept = new Concept("null", "該当なし");
    public static EvalConcept nullEvalConcept = new EvalConcept(null, -1);

    private void selectAmbiguousConcept(JList wordJList) {
        if (!wordJList.isSelectionEmpty()) {
            String orgWord = ((InputWordModel) wordJList.getSelectedValue()).getWord();
            String selectedWord = ((InputWordModel) wordJList.getSelectedValue()).getMatchedWord();
            Set<Concept> conceptSet = wordConceptSetMap.get(selectedWord);

            Set evalConceptSet = null;

            if (!(wordEvalConceptSetMap == null || wordEvalConceptSetMap.get(selectedWord) == null)) {
                evalConceptSet = wordEvalConceptSetMap.get(selectedWord);
            } else {
                evalConceptSet = getEvalConceptSet(conceptSet);
                evalConceptSet.add(nullEvalConcept);
            }

            conceptSetJList.setListData(evalConceptSet.toArray());
            Concept correctConcept = wordConceptMap.get(orgWord);
            if (correctConcept != null) {
                if (correctConcept.equals(nullConcept)) {
                    conceptSetJList.setSelectedValue(nullEvalConcept, true);
                    addAsSameConceptRadioButton.setEnabled(false);
                    addAsSubConceptRadioButton.setEnabled(false);
                    return;
                }
                for (Iterator i = evalConceptSet.iterator(); i.hasNext();) {
                    EvalConcept evalConcept = (EvalConcept) i.next();
                    if (evalConcept.getConcept() == null) {
                        continue;
                    }
                    if (correctConcept.equals(evalConcept.getConcept())) {
                        conceptSetJList.setSelectedValue(evalConcept, true);
                    }
                }
            } else {
                conceptSetJList.setSelectedIndex(0);
                EvalConcept evalConcept = (EvalConcept) conceptSetJList.getSelectedValue();
                wordConceptMap.put(orgWord, evalConcept.getConcept());
            }
            // hilightSelectedWord(selectedWord);
            hilightSelectedWord(orgWord);

            // 完全照合の場合は，階層構築オプションパネルを無効にする
            InputWordModel iwModel = (InputWordModel) wordJList.getSelectedValue();
            if (iwModel.isPartialMatchWord()) {
                addAsSameConceptRadioButton.setEnabled(true);
                addAsSubConceptRadioButton.setEnabled(true);
                ConstructTreeOption ctOption = complexConstructTreeOptionMap.get(iwModel);
                if (ctOption != null) {
                    if (ctOption.getOption().equals("SAME")) {
                        addAsSameConceptRadioButton.setSelected(true);
                    } else {
                        addAsSubConceptRadioButton.setSelected(true);
                    }
                } else {
                    EvalConcept evalConcept = (EvalConcept) conceptSetJList.getSelectedValue();
                    if (evalConcept != null) {
                        ctOption = new ConstructTreeOption(evalConcept.getConcept());
                        complexConstructTreeOptionMap.put(iwModel, ctOption);
                        addAsSameConceptRadioButton.setSelected(true);
                    }
                }
            } else {
                addAsSameConceptRadioButton.setEnabled(false);
                addAsSubConceptRadioButton.setEnabled(false);
            }
        }
    }

    private void hilightSelectedWord(String word) {
        if (viewHilightCheckBox.isSelected()) {
            String targetLines = docSelectionPanel.getTargetTextLines(word);
            // String targetLines = docSelectionPanel.getTargetHtmlLines(word);
            documentArea.setText(targetLines);
        }
    }

    /**
     * @param conceptSet
     * @return
     */
    private Set getEvalConceptSet(Set<Concept> conceptSet) {
        Set<EvalConcept> evalConceptSet = new TreeSet<EvalConcept>();
        for (Concept c : conceptSet) {
            evalConceptSet.add(new EvalConcept(c, 0));
        }
        return evalConceptSet;
    }

    private JList getTargetWordJList() {
        int selectedIndex = wordListTabbedPane.getSelectedIndex();
        if (selectedIndex == 0) {
            return perfectMatchedWordJList;
        } else if (selectedIndex == 1) { return partialMatchedWordJList; }
        return null;
    }

    private void syncPartialMatchedAmbiguousConceptSet(String orgWord, Concept c) {
        if (!perfectMatchedIsSyncCheckBox.isSelected()) { return; }
        for (Iterator i = partialMatchedWordModelSet.iterator(); i.hasNext();) {
            InputWordModel iwModel = (InputWordModel) i.next();
            if (iwModel.getMatchedWord().equals(orgWord)) {
                wordConceptMap.put(iwModel.getWord(), c);
            }
        }
    }

    private void selectCorrectConcept(JList wordJList) {
        if (!wordJList.isSelectionEmpty() && !conceptSetJList.isSelectionEmpty()) {
            InputWordModel iwModel = (InputWordModel) wordJList.getSelectedValue();
            EvalConcept evalConcept = (EvalConcept) conceptSetJList.getSelectedValue();
            String word = iwModel.getWord();
            if (evalConcept == nullEvalConcept) {
                wordConceptMap.put(word, nullConcept);
                syncPartialMatchedAmbiguousConceptSet(word, nullConcept);
                jpWordList.setListData(new Object[0]);
                enWordList.setListData(new Object[0]);
                jpExplanationArea.setText("");
                enExplanationArea.setText("");
                aroundConceptTree.setModel(new DefaultTreeModel(null));
                return;
            }
            Concept c = evalConcept.getConcept();
            wordConceptMap.put(word, c);
            syncPartialMatchedAmbiguousConceptSet(word, c);
            Concept edrConcept = evalConcept.getConcept();
            jpWordList.setListData(edrConcept.getJaWords());
            enWordList.setListData(edrConcept.getEnWords());
            jpExplanationArea.setText(edrConcept.getJaExplanation());
            enExplanationArea.setText(edrConcept.getEnExplanation());

            if (showAroundConceptTreeCheckBox.isSelected()) {
                EvalConcept ec = (EvalConcept) conceptSetJList.getSelectedValue();
                Set pathSet = null;
                if (ec.getConcept().getPrefix().equals("edr")) {
                    pathSet = EDRTree.getEDRTree().getPathToRootSet(ec.getConcept().getId());
                } else if (ec.getConcept().getPrefix().equals("edrt")) {
                    pathSet = EDRTree.getEDRTTree().getPathToRootSet(ec.getConcept().getId());
                } else if (ec.getConcept().getPrefix().equals("wn")) {
                    pathSet = WordNetDic.getPathToRootSet(new Long(ec.getConcept().getId()));
                }
                TreeModel model = constructClassPanel.getDefaultConceptTreeModel(pathSet);
                aroundConceptTree.setModel(model);
                for (int i = 0; i < aroundConceptTree.getRowCount(); i++) {
                    aroundConceptTree.expandPath(aroundConceptTree.getPathForRow(i));
                }
            } else {
                aroundConceptTree.setModel(new DefaultTreeModel(null));
            }

            ConstructTreeOption ctOption = complexConstructTreeOptionMap.get(iwModel);
            if (ctOption != null) {
                ctOption.setConcept(evalConcept.getConcept());
                complexConstructTreeOptionMap.put(iwModel, ctOption);
            }
        }
    }

    private final ImageIcon bestMatchIcon = Utils.getImageIcon("class_best_match_icon.png");
    private final ImageIcon ConceptNodeIcon = Utils.getImageIcon("class_sin_icon.png");

    public class AroundTreeCellRenderer extends DefaultTreeCellRenderer {

        public AroundTreeCellRenderer() {
            setOpaque(true);
        }

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {

            Component component = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row,
                    hasFocus);

            setText(value.toString());

            if (selected) {
                setBackground(new Color(0, 0, 128));
                setForeground(Color.white);
            } else {
                setBackground(Color.white);
                setForeground(Color.black);
            }

            if (value.getClass().equals(ConceptTreeNode.class)) {
                ConceptTreeNode node = (ConceptTreeNode) value;
                if (node.isLeaf()) {
                    setIcon(bestMatchIcon);
                } else {
                    setIcon(ConceptNodeIcon);
                }
            }
            return component;
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() == perfectMatchedWordJList) {
            selectAmbiguousConcept(perfectMatchedWordJList);
            showOnlyRelatedComplexWords();
        } else if (e.getSource() == partialMatchedWordJList) {
            selectAmbiguousConcept(partialMatchedWordJList);
        } else if (e.getSource() == conceptSetJList) {
            selectCorrectConcept(getTargetWordJList());
        } else if (e.getSource() == hilightPartJList) {
            jumpHilightPart();
        }
    }

    private void jumpHilightPart() {
        Integer lineNum = (Integer) hilightPartJList.getSelectedValue();
        Rectangle rect = documentArea.getVisibleRect();
        rect.y = 0;
        documentArea.scrollRectToVisible(rect);
        int lineHeight = documentArea.getFontMetrics(documentArea.getFont()).getHeight();
        // System.out.println(lineHeight);
        rect.y = (lineNum.intValue() + 1) * lineHeight;
        documentArea.scrollRectToVisible(rect);
    }

    private File inputFile;

    private void initWordList() {
        inputWordModelSet = inputModule.getInputWordModelSet();
        perfectMatchedWordModelSet = new TreeSet<InputWordModel>();
        partialMatchedWordModelSet = new TreeSet<InputWordModel>();

        for (InputWordModel iwModel : inputWordModelSet) {
            if (iwModel.isPartialMatchWord()) {
                partialMatchedWordModelSet.add(iwModel);
            } else {
                perfectMatchedWordModelSet.add(iwModel);
            }
        }
        perfectMatchedWordJList.setListData(perfectMatchedWordModelSet.toArray());
        perfectMatchedWordJListTitle.setTitle(Translator.getString("DisambiguationPanel.PerfectMatchWordList") + " ("
                + perfectMatchedWordModelSet.size() + ")");
        partialMatchedWordJList.setListData(partialMatchedWordModelSet.toArray());
        partialMatchedWordJListTitle.setTitle(Translator.getString("DisambiguationPanel.PartialMatchWordList") + " ("
                + partialMatchedWordModelSet.size() + ")");

        wordConceptSetMap = inputModule.getWordConceptSetMap();

        Set undefinedSet = inputModule.getUndefinedWordSet();
        DefaultListModel listModel = undefinedWordListPanel.getModel();
        listModel.clear();
        for (Iterator i = undefinedSet.iterator(); i.hasNext();) {
            listModel.addElement(i.next());
        }
        undefinedWordListPanel.setTitleWithSize();
        repaint(); // titledBorderのタイトルを再表示させるため
    }

    public void loadInputWordSet(File file) {
        if (!file.exists()) { return; }
        inputFile = file;
        Set<String> wordSet = new HashSet<String>();
        try {
            FileInputStream fis = new FileInputStream(inputFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "JISAutoDetect"));
            String line = "";
            while ((line = reader.readLine()) != null) {
                String word = line.replaceAll("\n", "");
                wordSet.add(word);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadInputWordSet(wordSet);
    }

    private Set<String> wordSet;

    public void loadInputWordSet(Set<String> ws) {
        wordSet = ws;
        perfectMatchedWordJList.clearSelection();
        partialMatchedWordJList.clearSelection();
        undefinedWordListPanel.clearSelection();
        complexConstructTreeOptionMap.clear();

        if (DODDLE.IS_USING_DB) {
            inputModule.initDataWithDB(wordSet);
        } else {
            inputModule.initDataWithMem(wordSet);
        }
        initWordList();
    }

    public Set<Concept> getInputConceptSet() {
        return inputConceptSet;
    }

    public Set<Concept> getInputNounConceptSet() {
        return inputNounConceptSet;
    }

    public Set<Concept> getInputVerbConceptSet() {
        return inputVerbConceptSet;
    }

    public Set<InputWordModel> getInputWordModelSet() {
        return inputModule.getInputWordModelSet();
    }

    public AutomaticDisAmbiguationAction getAutomaticDisAmbiguationAction() {
        return automaticDisAmbiguationAction;
    }

    public class ShowConceptDescriptionAction extends AbstractAction {

        public ShowConceptDescriptionAction(String title) {
            super(title);
        }

        public void actionPerformed(ActionEvent e) {
            EvalConcept evalConcept = (EvalConcept) conceptSetJList.getSelectedValue();
            if (evalConcept != null) {
                conceptDescriptionFrame.setConcept(evalConcept.getConcept().getId());
                setInputConceptSet();
                conceptDescriptionFrame.setInputConceptSet();
                conceptDescriptionFrame.setVisible(true);
            }
        }
    }

    class ConceptDescriptionFrame extends JFrame {

        private ConceptDescriptionUI conceptDescrptionPanel;

        ConceptDescriptionFrame() {
            setBounds(50, 50, 800, 600);
            conceptDescrptionPanel = new ConceptDescriptionUI();
            Container contentPane = getContentPane();
            contentPane.add(conceptDescrptionPanel);
        }

        public void setConcept(String id) {
            conceptDescrptionPanel.setConcept(id);
        }

        public void setInputConceptSet() {
            conceptDescrptionPanel.setInputConceptSet(inputConceptSet);
        }
    }

    public class AutomaticDisAmbiguationAction extends AbstractAction {

        private Set<String> wordSet;

        public AutomaticDisAmbiguationAction(String title) {
            super(title);
        }

        /**
         * 
         * 多義性のある概念リストと入力語彙を入力として，評価値つき概念リストを返すメソッド
         * 
         */
        public void setWordEvalConceptSetMap(Set<InputWordModel> inputWordSet) {
            wordSet = new HashSet<String>();
            for (InputWordModel iwModel : inputWordModelSet) {
                wordSet.add(iwModel.getMatchedWord());
            }
            wordEvalConceptSetMap = new HashMap<String, Set<EvalConcept>>();

            DODDLE.STATUS_BAR.startTime();
            DODDLE.STATUS_BAR.initNormal(wordSet.size());
            for (String inputWord : wordSet) {
                Set<Concept> conceptSet = wordConceptSetMap.get(inputWord);
                Set<EvalConcept> evalConceptSet = new TreeSet<EvalConcept>();
                for (Concept c : conceptSet) {
                    int evalValue = 0;
                    if (OptionDialog.isCheckSupConcepts()) {
                        evalValue += cntRelevantSupConcepts(c);
                    }
                    if (OptionDialog.isCheckSubConcepts()) {
                        evalValue += cntRelevantSubConcepts(c);
                    }
                    if (OptionDialog.isCheckSiblingConcepts()) {
                        evalValue += cntRelevantSiblingConcepts(c);
                    }
                    evalConceptSet.add(new EvalConcept(c, evalValue));
                }
                evalConceptSet.add(nullEvalConcept);
                wordEvalConceptSetMap.put(inputWord, evalConceptSet);
                DODDLE.STATUS_BAR.addValue();
            }
            DODDLE.STATUS_BAR.hideProgressBar();
        }

        private Set getSiblingConceptSet(Concept c) {
            Set siblingConceptSet = null;
            if (c.getPrefix().equals("edr")) {
                siblingConceptSet = EDRTree.getEDRTree().getSiblingConceptSet(c.getId());
            } else if (c.getPrefix().equals("edrt")) {
                siblingConceptSet = EDRTree.getEDRTTree().getSiblingConceptSet(c.getId());
            } else if (c.getPrefix().equals("wn")) {
                siblingConceptSet = WordNetDic.getSiblingConceptSet(new Long(c.getId()));
            }
            return siblingConceptSet;
        }

        private Set getSubConceptSet(Concept c) {
            Set subConceptSet = null;
            if (c.getPrefix().equals("edr")) {
                subConceptSet = EDRTree.getEDRTree().getSubConceptSet(c.getId());
            } else if (c.getPrefix().equals("edrt")) {
                subConceptSet = EDRTree.getEDRTTree().getSubConceptSet(c.getId());
            } else if (c.getPrefix().equals("wn")) {
                subConceptSet = WordNetDic.getSubConceptSet(new Long(c.getId()));
            }
            return subConceptSet;
        }

        private Set getPathToRootSet(Concept c) {
            Set pathSet = null;
            if (c.getPrefix().equals("edr")) {
                pathSet = EDRTree.getEDRTree().getPathToRootSet(c.getId());
            } else if (c.getPrefix().equals("edrt")) {
                pathSet = EDRTree.getEDRTTree().getPathToRootSet(c.getId());
            } else if (c.getPrefix().equals("wn")) {
                pathSet = WordNetDic.getPathToRootSet(new Long(c.getId()));
            }
            return pathSet;
        }

        private int getMaxEvalValue(Set<Collection> pathSet, String identity) {
            if (pathSet == null) { return 0; }
            int maxEvalValue = 0;
            for (Collection<Concept> path : pathSet) {
                int evalValue = 0;
                for (Concept sc : path) {
                    if (sc == null) {
                        continue;
                    }
                    if (sc.getIdentity().equals(identity)) {
                        continue;
                    }
                    Concept c = DODDLEDic.getConcept(sc.getIdentity());
                    if (isIncludeInputWords(wordSet, c)) {
                        evalValue++;
                    }
                }
                if (maxEvalValue < evalValue) {
                    maxEvalValue = evalValue;
                }
            }
            return maxEvalValue;
        }

        private int cntRelevantSiblingConcepts(Concept c) {
            Set pathSet = getSiblingConceptSet(c);
            return getMaxEvalValue(pathSet, c.getIdentity());
        }

        private int cntRelevantSupConcepts(Concept c) {
            Set pathSet = getPathToRootSet(c);
            return getMaxEvalValue(pathSet, c.getIdentity());
        }

        private int cntRelevantSubConcepts(Concept c) {
            Set pathSet = getSubConceptSet(c);
            return getMaxEvalValue(pathSet, c.getIdentity());
        }

        private boolean isIncludeInputWords(Set wordSet, Concept c) {
            if (c == null) { return false; }
            String[] jpWords = c.getJaWords();
            for (int j = 0; j < jpWords.length; j++) {
                if (wordSet.contains(jpWords[j])) { return true; }
            }
            String[] enWords = c.getEnWords();
            for (int j = 0; j < enWords.length; j++) {
                if (wordSet.contains(enWords[j])) { return true; }
            }
            if (wordSet.contains(c.getJaExplanation())) { return true; }
            if (wordSet.contains(c.getEnExplanation())) { return true; }
            return false;
        }

        public void doDisAmbiguation() {
            new Thread() {
                public void run() {
                    setWordEvalConceptSetMap(getInputWordModelSet());
                }
            }.start();
        }

        public void actionPerformed(ActionEvent e) {
            doDisAmbiguation();
        }
    }

    public void showAllWords() {
        JFrame frame = new JFrame();
        Set allWordSet = new TreeSet();
        if (inputWordModelSet != null) {
            for (InputWordModel iwModel : inputWordModelSet) {
                allWordSet.add(iwModel.getWord());
            }
            allWordSet.addAll(inputModule.getUndefinedWordSet());
            JList list = new JList();
            list.setBorder(BorderFactory.createTitledBorder("入力されたすべての単語(" + allWordSet.size() + ")"));
            list.setListData(allWordSet.toArray());
            JScrollPane listScroll = new JScrollPane(list);
            frame.getContentPane().add(listScroll);
            frame.setBounds(50, 50, 200, 600);
            frame.setVisible(true);
        }
    }

    public void saveConstructTreeOptionSet(File file) {
        if (complexConstructTreeOptionMap == null) { return; }
        try {
            OutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "SJIS"));
            StringBuffer buf = new StringBuffer();
            for (Iterator i = complexConstructTreeOptionMap.keySet().iterator(); i.hasNext();) {
                InputWordModel iwModel = (InputWordModel) i.next();
                ConstructTreeOption ctOption = complexConstructTreeOptionMap.get(iwModel);
                buf.append(iwModel.getWord() + "\t" + ctOption.getConcept().getIdentity() + "\t" + ctOption.getOption()
                        + "\n");
            }
            writer.write(buf.toString());
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void loadConstructTreeOptionSet(File file) {
        if (!file.exists()) { return; }
        complexConstructTreeOptionMap = new HashMap<InputWordModel, ConstructTreeOption>();
        try {
            InputStream is = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "SJIS"));
            String line = "";
            while ((line = reader.readLine()) != null) {
                String[] strs = line.split("\t");
                String iw = strs[0];
                String id = strs[1];
                String opt = strs[2];
                if (0 < iw.length()) {
                    InputWordModel iwModel = inputModule.makeInputWordModel(iw);
                    complexConstructTreeOptionMap.put(iwModel, new ConstructTreeOption(DODDLEDic.getConcept(id), opt));
                }
            }
            reader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public InputWordModel makeInputWordModel(String iw) {
        return inputModule.makeInputWordModel(iw);
    }

    public InputWordModel makeInputWordModel(String iw, Map<String, Set<Concept>> wcSetMap) {
        return inputModule.makeInputWordModel(iw, wcSetMap);
    }

    public void saveInputWordSet(File file) {
        if (inputWordModelSet == null) { return; }
        try {
            OutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "SJIS"));
            StringBuffer buf = new StringBuffer();
            for (InputWordModel iwModel : inputWordModelSet) {
                buf.append(iwModel.getWord() + "\n");
            }
            for (Iterator i = inputModule.getUndefinedWordSet().iterator(); i.hasNext();) {
                buf.append(i.next() + "\n");
            }
            writer.write(buf.toString());
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void loadUndefinedWordSet(File file) {
        if (!file.exists()) { return; }
        undefinedWordListModel = new DefaultListModel();
        try {
            InputStream is = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "SJIS"));
            String line = "";
            while ((line = reader.readLine()) != null) {
                undefinedWordListModel.addElement(line);
            }
            reader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        constructClassPanel.setUndefinedWordListModel(undefinedWordListModel);
        if (OptionDialog.isNounAndVerbConceptHierarchyConstructionMode()) {
            constructPropertyPanel.setUndefinedWordListModel(undefinedWordListModel);
        }
    }

    public void loadInputConceptSet(File file) {
        if (!file.exists()) { return; }
        inputConceptSet = new HashSet<Concept>();
        try {
            InputStream is = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "SJIS"));
            String id = "";
            while ((id = reader.readLine()) != null) {
                inputConceptSet.add(DODDLEDic.getConcept(id));
            }
            reader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void saveUndefinedWordSet(File file) {
        if (undefinedWordListModel == null) { return; }
        try {
            OutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "SJIS"));
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < undefinedWordListModel.getSize(); i++) {
                buf.append(undefinedWordListModel.getElementAt(i) + "\n");
            }
            writer.write(buf.toString());
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void saveInputConceptSet(File file) {
        if (inputConceptSet == null) { return; }
        try {
            OutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "SJIS"));
            StringBuffer buf = new StringBuffer();
            for (Concept c : inputConceptSet) {
                buf.append(c.getIdentity() + "\n");
            }
            writer.write(buf.toString());
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void saveWordConceptMap(File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "SJIS"));

            StringBuffer buf = new StringBuffer();
            for (Iterator i = getWordConceptMap().entrySet().iterator(); i.hasNext();) {
                Map.Entry entry = (Map.Entry) i.next();
                String word = (String) entry.getKey();
                Concept concept = (Concept) entry.getValue();
                buf.append(word + "," + concept.getIdentity() + "\n");
            }
            writer.write(buf.toString());
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void saveWordConceptMap() {
        JFileChooser chooser = new JFileChooser(".");
        int retval = chooser.showSaveDialog(DODDLE.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            saveWordConceptMap(file);
        }
    }

    public void saveWordEvalConceptSet(File file) {
        if (wordEvalConceptSetMap == null || inputWordModelSet == null) { return; }
        try {
            OutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "SJIS"));

            StringBuffer buf = new StringBuffer();
            for (InputWordModel iwModel : inputWordModelSet) {
                Set<EvalConcept> evalConceptSet = wordEvalConceptSetMap.get(iwModel.getMatchedWord());
                if (evalConceptSet == null) {
                    continue;
                }
                buf.append(iwModel.getWord());
                int evalValue = -1;
                for (EvalConcept ec : evalConceptSet) {
                    if (evalValue == ec.getEvalValue()) {
                        buf.append("\t" + ec.getConcept().getIdentity());
                    } else {
                        if (ec.getConcept() != null) {
                            buf.append("||" + ec.getEvalValue() + "\t" + ec.getConcept().getIdentity());
                            evalValue = ec.getEvalValue();
                        }
                    }
                }
                buf.append("\n");
            }
            writer.write(buf.toString());
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void saveCompleteMatchWord() {
        JFileChooser chooser = new JFileChooser(".");
        int retval = chooser.showSaveDialog(DODDLE.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            saveCompleteMatchWord(file);
        }
    }

    private void saveCompleteMatchWord(File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "SJIS"));

            Set completeWordSet = new TreeSet();
            for (Iterator i = inputWordModelSet.iterator(); i.hasNext();) {
                InputWordModel iwModel = (InputWordModel) i.next();
                if (iwModel.isPartialMatchWord()) {
                    completeWordSet.add(iwModel.getMatchedWord());
                } else {
                    completeWordSet.add(iwModel.getWord());
                }
            }
            for (Iterator i = completeWordSet.iterator(); i.hasNext();) {
                writer.write(i.next() + "\n");
            }
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void saveCompleteMatchWordWithComplexWord() {
        JFileChooser chooser = new JFileChooser(".");
        int retval = chooser.showSaveDialog(DODDLE.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            saveCompleteMatchWordWithComplexWord(file);
        }
    }

    private void saveCompleteMatchWordWithComplexWord(File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "SJIS"));

            Map completeWordComplexWordMap = new TreeMap();
            for (Iterator i = inputWordModelSet.iterator(); i.hasNext();) {
                InputWordModel iwModel = (InputWordModel) i.next();
                if (completeWordComplexWordMap.get(iwModel.getMatchedWord()) != null) {
                    Set complexWordSet = (Set) completeWordComplexWordMap.get(iwModel.getMatchedWord());
                    complexWordSet.add(iwModel.getWord());
                    completeWordComplexWordMap.put(iwModel.getMatchedWord(), complexWordSet);
                } else {
                    Set complexWordSet = new TreeSet();
                    complexWordSet.add(iwModel.getWord());
                    completeWordComplexWordMap.put(iwModel.getMatchedWord(), complexWordSet);
                }
            }

            StringBuffer buf = new StringBuffer();
            for (Iterator i = completeWordComplexWordMap.keySet().iterator(); i.hasNext();) {
                String matchedWord = (String) i.next();
                buf.append(matchedWord + "=>");
                Set complexWordSet = (Set) completeWordComplexWordMap.get(matchedWord);
                for (Iterator j = complexWordSet.iterator(); j.hasNext();) {
                    String complexWord = (String) j.next();
                    buf.append(complexWord + ",");
                }
                buf.append("\n");
            }
            writer.write(buf.toString());
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void saveWordEvalConceptSet() {
        JFileChooser chooser = new JFileChooser(".");
        int retval = chooser.showSaveDialog(DODDLE.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            saveWordEvalConceptSet(file);
        }
    }

    public void loadWordEvalConceptSet() {
        JFileChooser chooser = new JFileChooser(".");
        int retval = chooser.showOpenDialog(DODDLE.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            loadWordEvalConceptSet(file);
        }
    }

    public void loadWordEvalConceptSet(File file) {
        if (!file.exists()) { return; }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "SJIS"));
            String line = "";
            wordEvalConceptSetMap = new HashMap<String, Set<EvalConcept>>();
            while ((line = reader.readLine()) != null) {
                String[] wordAndResults = line.split("\\|\\|");
                String word = wordAndResults[0];
                Set<EvalConcept> evalConceptSet = new TreeSet<EvalConcept>();
                for (int i = 1; i < wordAndResults.length; i++) {
                    String[] valueAndIDs = wordAndResults[i].split("\t");
                    int value = Integer.parseInt(valueAndIDs[0]);
                    for (int j = 1; j < valueAndIDs.length; j++) {
                        String id = valueAndIDs[j];
                        Concept c = DODDLEDic.getConcept(id);
                        evalConceptSet.add(new EvalConcept(c, value));
                    }
                }
                wordEvalConceptSetMap.put(word, evalConceptSet);
            }
            reader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}