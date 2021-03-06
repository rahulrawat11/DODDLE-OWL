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
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.Map.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import net.infonode.docking.*;
import net.infonode.docking.util.*;
import net.sourceforge.doddle_owl.*;
import net.sourceforge.doddle_owl.data.*;
import net.sourceforge.doddle_owl.utils.*;

/**
 * @author shigeta
 * @author Takeshi Morita
 */
public class ConceptDriftManagementPanel extends JPanel implements ActionListener,
		ListSelectionListener {

	private String conceptTreeType;
	private JList mraJList;
	private JButton checkMRAButton;
	private List<List<ConceptTreeNode>> mraList;
	private JList traJList;
	private JTree trimmedNodeTree;
	private List<ConceptTreeNode> traList;
	private JButton traButton;
	private JButton checkTRAButton;
	private RemoveMultipleInheritancePanel rmMultipleInheritancePanel;

	private JTree conceptTree;

	private JTextField trimmedNumField;
	private ConceptTreeMaker maker = ConceptTreeMaker.getInstance();

	private View[] mainViews;
	private RootWindow rootWindow;

	private DODDLEProject project;

	public ConceptDriftManagementPanel(String type, JTree tree, DODDLEProject p) {
		project = p;
		conceptTree = tree;
		conceptTreeType = type;
		mraList = new ArrayList<List<ConceptTreeNode>>();
		mraJList = new JList();
		mraJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mraJList.addListSelectionListener(this);
		checkMRAButton = new JButton(Translator.getTerm("CheckMRAButton"));
		checkMRAButton.addActionListener(this);
		traList = new ArrayList<ConceptTreeNode>();
		traJList = new JList();
		traJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		traJList.addListSelectionListener(this);
		trimmedNodeTree = new JTree();
		trimmedNodeTree.setCellRenderer(new ConceptTreeCellRenderer(type));
		trimmedNodeTree.setModel(new DefaultTreeModel(null));
		trimmedNodeTree.setEditable(false);
		traButton = new JButton(Translator.getTerm("TrimmedResultAnalysisButton"));
		traButton.addActionListener(this);
		checkTRAButton = new JButton(Translator.getTerm("CheckTRAButton"));
		checkTRAButton.addActionListener(this);
		trimmedNumField = new JTextField(5);
		trimmedNumField.setText("3");

		mainViews = new View[3];
		ViewMap viewMap = new ViewMap();

		mainViews[0] = new View(Translator.getTerm("MatchedResultAnalysisResultBorder"), null,
				getMRAPanel());
		mainViews[1] = new View(Translator.getTerm("TrimmedResultAnalysisResultBorder"), null,
				getTRAPanel());
		rmMultipleInheritancePanel = new RemoveMultipleInheritancePanel();
		mainViews[2] = new View(Translator.getTerm("RemoveMultipleInheritanceBorder"), null,
				rmMultipleInheritancePanel);

		for (int i = 0; i < mainViews.length; i++) {
			viewMap.addView(i, mainViews[i]);
		}
		rootWindow = Utils.createDODDLERootWindow(viewMap);
		setLayout(new BorderLayout());
		add(rootWindow, BorderLayout.CENTER);
	}

	public void setXGALayout() {
		rootWindow.setWindow(new TabWindow(new DockingWindow[] { mainViews[0], mainViews[1],
				mainViews[2] }));
		mainViews[0].restoreFocus();
	}

	public void setUXGALayout() {
		SplitWindow sw1 = new SplitWindow(true, mainViews[0], mainViews[1]);
		SplitWindow sw2 = new SplitWindow(true, 0.66f, sw1, mainViews[2]);
		rootWindow.setWindow(sw2);
		mainViews[0].restoreFocus();
	}

	public List<ConceptTreeNode> getTRAResult() {
		return traList;
	}

	public void addTRANode(ConceptTreeNode node) {
		traList.add(node);

	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == traJList) {
			traAction();
		} else if (e.getSource() == mraJList) {
			mraAction();
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == traButton) {
			resetTrimmedResultAnalysis();
		} else if (e.getSource() == checkMRAButton) {
			if (mraJList.getSelectedIndex() < 0) {
				return;
			}
			List<ConceptTreeNode> nodeList = mraList.get(mraJList.getSelectedIndex());
			for (ConceptTreeNode node : nodeList) {
				node.setIsUserConcept(true);
				DODDLE_OWL.getCurrentProject().getInputConceptSelectionPanel()
						.addInputConcept(node.getConcept());
				DODDLE_OWL.getCurrentProject().getInputConceptSelectionPanel()
						.deleteSystemAddedConcept(node.getConcept());
			}
			resetMatchedResultAnalysis();
		} else if (e.getSource() == checkTRAButton) {
			if (traJList.getSelectedIndex() < 0) {
				return;
			}
			ConceptTreeNode node = traList.get(traJList.getSelectedIndex());
			node.initTrimmedConceptList();
			resetTrimmedResultAnalysis();
		}
	}

	public void resetConceptDriftManagementResult(ConceptTreeNode rootNode) {
		resetMultipleInheritanceConceptSet(rootNode);
		resetTrimmedResultAnalysis();
		resetMatchedResultAnalysis();
	}

	public void resetMatchedResultAnalysis() {
		maker.resetMRA();
		maker.matchedResultAnalysis((ConceptTreeNode) conceptTree.getModel().getRoot());
		mraList = new ArrayList<List<ConceptTreeNode>>(maker.getMRAresult());
		setMRADefaultValue();
	}

	public void resetTrimmedResultAnalysis() {
		int trimmedNum = 3;
		try {
			trimmedNum = Integer.parseInt(trimmedNumField.getText());
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(this, nfe.getMessage(), "Number Format Exception",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		maker.resetTRA();
		maker.trimmedResultAnalysis((ConceptTreeNode) conceptTree.getModel().getRoot(), trimmedNum);
		traList = new ArrayList<ConceptTreeNode>(maker.getTRAresult());
		setTRADefaultValue();
	}

	public void resetMultipleInheritanceConceptSet(ConceptTreeNode rootNode) {
		rmMultipleInheritancePanel.setMultipleInheritanceConceptSet(maker
				.getMulipleInheritanceConceptSet(rootNode));
		rmMultipleInheritancePanel.resetMutipleInheritanceConceptJListTitle();
	}

	/**
	 * 各言語のルート概念にあたるラベルを比較する必要がある
	 * 
	 * @return
	 */
	private boolean isClass() {
		String rootLabel = conceptTree.getModel().getRoot().toString();
		return rootLabel.equals("名詞的概念") || rootLabel.equals("Root Class")
				|| rootLabel.equals("名詞的概念 (Is-a)") || rootLabel.equals("Is-a Root Class");
	}

	/**
	 * 
	 * リストで選択されているTRAで分析された箇所の選択，グループ化を行う
	 * 
	 */
	private void traAction() {
		if (traList != null && !traList.isEmpty()) {
			int index = traJList.getSelectedIndex();
			if (index == -1) {
				return;
			}
			ConceptTreeNode traNode = traList.get(index);
			if (DODDLE_OWL.doddlePlugin != null) {
				List<ConceptTreeNode> set = new ArrayList<ConceptTreeNode>();
				set.add(traNode);
				if (traNode.getParent() != null) {
					set.add((ConceptTreeNode) traNode.getParent());
				}
				if (isClass()) {
					DODDLE_OWL.doddlePlugin.selectClasses(changeToURISet(set));
				} else {
					DODDLE_OWL.doddlePlugin.selectProperties(changeToURISet(set));
				}
			}
			DefaultTreeModel treeModel = (DefaultTreeModel) conceptTree.getModel();
			TreeNode[] nodes = treeModel.getPathToRoot(traNode);
			TreePath path = new TreePath(nodes);
			conceptTree.scrollPathToVisible(path);
			conceptTree.setSelectionPath(path);
			traAction(traNode);
		}
	}

	public void traAction(ConceptTreeNode traNode) {
		if (traNode.getParent() == null) {
			trimmedNodeTree.setModel(new DefaultTreeModel(null));
			return;
		}
		Concept trimmedTreeRootConcept = ((ConceptTreeNode) traNode.getParent()).getConcept();
		TreeModel trimmedTreeModel = getTrimmedTreeModel(trimmedTreeRootConcept,
				traNode.getTrimmedConceptList());
		trimmedNodeTree.setModel(trimmedTreeModel);
		for (int i = 0; i < trimmedNodeTree.getRowCount(); i++) {
			trimmedNodeTree.expandPath(trimmedNodeTree.getPathForRow(i));
		}
	}

	private TreeModel getTrimmedTreeModel(Concept trimmedTreeRootConcept,
			List<List<Concept>> trimmedConceptList) {
		ConceptTreeNode rootNode = new ConceptTreeNode(trimmedTreeRootConcept, project);
		TreeModel trimmedTreeModel = new DefaultTreeModel(rootNode);
		for (List<Concept> list : trimmedConceptList) {
			DefaultMutableTreeNode parentNode = rootNode;
			for (Concept tc : list) {
				ConceptTreeNode childNode = new ConceptTreeNode(tc, project);
				parentNode.insert(childNode, 0);
				parentNode = childNode;
			}
		}
		if (rootNode.getChildCount() == 0) {
			trimmedTreeModel = new DefaultTreeModel(null);
		}
		return trimmedTreeModel;
	}

	/**
	 * 
	 * リストで選択されているMRAで分析された箇所の選択，グループ化を行う
	 * 
	 */
	private void mraAction() {
		if (mraList != null && !mraList.isEmpty()) {
			int index = mraJList.getSelectedIndex();

			if (index == -1) {
				return;
			}
			List<ConceptTreeNode> stm = mraList.get(index);
			if (DODDLE_OWL.doddlePlugin != null) {
				if (isClass()) {
					DODDLE_OWL.doddlePlugin.selectClasses(changeToURISet(stm));
				} else {
					DODDLE_OWL.doddlePlugin.selectProperties(changeToURISet(stm));
				}
			}
			TreePath[] paths = new TreePath[stm.size()];
			for (int i = 0; i < paths.length; i++) {
				paths[i] = new TreePath(stm.get(i).getPath());
			}
			conceptTree.scrollPathToVisible(paths[0]);
			conceptTree.setSelectionPaths(paths);
		}
	}

	/**
	 * 名前空間を付与する
	 */
	private Set<String> changeToURISet(List<ConceptTreeNode> stm) {
		Set<String> uriSet = new HashSet<String>();
		for (ConceptTreeNode node : stm) {
			Concept c = node.getConcept();
			uriSet.add(c.getURI());
		}
		return uriSet;
	}

	/**
	 * matched result analysis を制御するパネルを作る
	 */
	private JPanel getMRAPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(new JScrollPane(mraJList), BorderLayout.CENTER);
		panel.add(Utils.createEastPanel(checkMRAButton), BorderLayout.SOUTH);
		return panel;
	}

	/**
	 * trimmed result analysis を制御するパネルを作る
	 */
	private JPanel getTRAPanel() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		JPanel southPanel = new JPanel();
		southPanel.add(new JSeparator());
		southPanel.add(trimmedNumField);
		southPanel.add(traButton);
		southPanel.add(checkTRAButton);
		JPanel listPanel = new JPanel();
		listPanel.setLayout(new GridLayout(2, 1));
		listPanel.add(new JScrollPane(traJList));
		JScrollPane trimmedNodeJListScroll = new JScrollPane(trimmedNodeTree);
		trimmedNodeJListScroll.setBorder(BorderFactory.createTitledBorder(Translator
				.getTerm("TrimmedConceptList")));
		listPanel.add(trimmedNodeJListScroll);
		mainPanel.add(listPanel, BorderLayout.CENTER);
		mainPanel.add(Utils.createEastPanel(southPanel), BorderLayout.SOUTH);
		return mainPanel;
	}

	/**
	 * 初期値をセットする
	 */
	public void setConceptDriftManagementResult() {
		mraList = new ArrayList<List<ConceptTreeNode>>(maker.getMRAresult());
		traList = new ArrayList<ConceptTreeNode>(maker.getTRAresult());
		trimmedNodeTree.setModel(new DefaultTreeModel(null));
		rmMultipleInheritancePanel.setMultipleInheritanceConceptSet(maker
				.getMulipleInheritanceConceptSet());
		setMRADefaultValue();
		setTRADefaultValue();
		mainViews[2].getViewProperties().setTitle(
				Translator.getTerm("RemoveMultipleInheritanceBorder") + " ("
						+ maker.getMulipleInheritanceConceptSet().size() + ")");
	}

	private void setMRADefaultValue() {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < mraList.size(); i++) {
			List<ConceptTreeNode> nodeList = mraList.get(i);
			ConceptTreeNode sinNode = nodeList.get(0); // 0番目にSINノードが格納されている
			list.add(i + 1 + ": " + sinNode.getConcept());
		}
		mraJList.setListData(list.toArray());
		mainViews[0].getViewProperties().setTitle(
				Translator.getTerm("MatchedResultAnalysisResultBorder") + " (" + list.size() + ")");
	}

	public void setTRADefaultValue() {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < traList.size(); i++) {
			ConceptTreeNode traNode = traList.get(i);
			list.add(i + 1 + ": " + traNode.getConcept() + " (" + traNode.getTrimmedCountList()
					+ ")");
		}
		traJList.setListData(list.toArray());
		mainViews[1].getViewProperties().setTitle(
				Translator.getTerm("TrimmedResultAnalysisResultBorder") + " (" + traList.size()
						+ ")");
	}

	class RemoveMultipleInheritancePanel extends JPanel implements ListSelectionListener,
			ActionListener {

		private JList multipleInheritanceConceptJList;
		private JList multipleInheritanceUpperConceptJList;
		private JButton removeUpperConceptLinkButton;

		public RemoveMultipleInheritancePanel() {
			multipleInheritanceConceptJList = new JList();
			multipleInheritanceConceptJList.addListSelectionListener(this);
			JScrollPane multipleInheritanceConceptJListScroll = new JScrollPane(
					multipleInheritanceConceptJList);
			multipleInheritanceConceptJListScroll.setBorder(BorderFactory
					.createTitledBorder(Translator.getTerm("MultipleInheritanceConceptList")));
			multipleInheritanceUpperConceptJList = new JList();
			multipleInheritanceUpperConceptJList.addListSelectionListener(this);
			JScrollPane multipleInheritanceUpperConceptJListScroll = new JScrollPane(
					multipleInheritanceUpperConceptJList);
			multipleInheritanceUpperConceptJListScroll.setBorder(BorderFactory
					.createTitledBorder(Translator.getTerm("UpperConceptList")));
			removeUpperConceptLinkButton = new JButton(
					Translator.getTerm("RemoveLinkToUpperConceptButton"));
			removeUpperConceptLinkButton.addActionListener(this);

			JPanel listPanel = new JPanel();
			listPanel.setLayout(new GridLayout(2, 1));
			listPanel.add(multipleInheritanceConceptJListScroll);
			listPanel.add(multipleInheritanceUpperConceptJListScroll);

			JPanel eastPanel = new JPanel();
			eastPanel.setLayout(new BorderLayout());
			eastPanel.add(removeUpperConceptLinkButton, BorderLayout.EAST);

			setLayout(new BorderLayout());
			add(listPanel, BorderLayout.CENTER);
			add(eastPanel, BorderLayout.SOUTH);
		}

		public void setMultipleInheritanceConceptSet(Set<Concept> multipleInheritanceConceptSet) {
			DefaultListModel listModel = new DefaultListModel();
			for (Concept c : multipleInheritanceConceptSet) {
				listModel.addElement(c);
			}
			multipleInheritanceConceptJList.setModel(listModel);
			multipleInheritanceUpperConceptJList.setModel(new DefaultListModel());
		}

		public void valueChanged(ListSelectionEvent e) {
			ConstructConceptTreePanel conceptTreePanel = getConceptTreePanel();
			DefaultTreeModel model = (DefaultTreeModel) conceptTreePanel.getConceptTreeModel();
			Concept c = (Concept) multipleInheritanceConceptJList.getSelectedValue();
			if (e.getSource() == multipleInheritanceConceptJList) {
				if (c != null) {
					Set<ConceptTreeNode> sameConceptTreeNodeSet = getSameConceptTreeNodeSet(
							conceptTreePanel, model, c);
					DefaultListModel listModel = new DefaultListModel();
					Set<Concept> sameUpperConceptSet = new HashSet<Concept>();
					for (ConceptTreeNode node : sameConceptTreeNodeSet) {
						ConceptTreeNode upperConcept = (ConceptTreeNode) node.getParent();
						if (!sameUpperConceptSet.contains(upperConcept.getConcept())) {
							listModel.addElement(upperConcept);
							sameUpperConceptSet.add(upperConcept.getConcept());
						}
					}
					multipleInheritanceUpperConceptJList.setModel(listModel);
				}
			} else if (e.getSource() == multipleInheritanceUpperConceptJList) {
				ConceptTreeNode upperTreeNode = (ConceptTreeNode) multipleInheritanceUpperConceptJList
						.getSelectedValue();
				if (upperTreeNode != null) {
					Set<ConceptTreeNode> multipleInheritanceNodeSet = getSameConceptTreeNodeSet(
							conceptTreePanel, model, c);
					for (ConceptTreeNode multipleInheritanceNode : multipleInheritanceNodeSet) {
						if (multipleInheritanceNode.getParent().equals(upperTreeNode)) {
							TreeNode[] nodes = model.getPathToRoot(multipleInheritanceNode);
							TreePath path = new TreePath(nodes);
							conceptTree.scrollPathToVisible(path);
							conceptTree.setSelectionPath(path);
							if (DODDLE_OWL.doddlePlugin != null) {
								List<ConceptTreeNode> set = new ArrayList<ConceptTreeNode>();
								set.add(multipleInheritanceNode);
								set.add(upperTreeNode);
								if (isClass()) {
									DODDLE_OWL.doddlePlugin.selectClasses(changeToURISet(set));
								} else {
									DODDLE_OWL.doddlePlugin.selectProperties(changeToURISet(set));
								}
							}
							break;
						}
					}
				}
			}
		}

		/**
		 * @param conceptTreePanel
		 * @param model
		 * @param c
		 * @return
		 */
		private Set<ConceptTreeNode> getSameConceptTreeNodeSet(
				ConstructConceptTreePanel conceptTreePanel, DefaultTreeModel model, Concept c) {
			ConceptTreeNode rootNode = (ConceptTreeNode) model.getRoot();
			Set<ConceptTreeNode> sameConceptTreeNodeSet = new HashSet<ConceptTreeNode>();
			conceptTreePanel.searchSameConceptTreeNode(c, rootNode, sameConceptTreeNodeSet);
			return sameConceptTreeNodeSet;
		}

		public void actionPerformed(ActionEvent e) {
			Concept c = (Concept) multipleInheritanceConceptJList.getSelectedValue();
			if (e.getSource() == removeUpperConceptLinkButton) {
				ConceptTreeNode upperConceptTreeNode = (ConceptTreeNode) multipleInheritanceUpperConceptJList
						.getSelectedValue();
				ConstructConceptTreePanel conceptTreePanel = getConceptTreePanel();
				DefaultTreeModel model = (DefaultTreeModel) conceptTreePanel.getConceptTreeModel();
				Set<ConceptTreeNode> sameConceptTreeNodeSet = getSameConceptTreeNodeSet(
						conceptTreePanel, model, c);
				for (ConceptTreeNode removeNode : sameConceptTreeNodeSet) {
					if (removeNode.getParent().equals(upperConceptTreeNode)) {
						conceptTreePanel.deleteLinkToUpperConcept(removeNode);
						selectTargetMultipleInheritanceConcept(c);
						break;
					}
				}
			}
		}

		public void selectTargetMultipleInheritanceConcept(Concept targetConcept) {
			ListModel listModel = multipleInheritanceConceptJList.getModel();
			for (int i = 0; i < listModel.getSize(); i++) {
				Concept c = (Concept) listModel.getElementAt(i);
				if (targetConcept.equals(c)) {
					multipleInheritanceConceptJList.setSelectedIndex(i);
					break;
				}
			}
		}

		public void resetMutipleInheritanceConceptJListTitle() {
			DefaultListModel listModel = (DefaultListModel) multipleInheritanceConceptJList
					.getModel();
			mainViews[2].getViewProperties().setTitle(
					Translator.getTerm("RemoveMultipleInheritanceBorder") + " (" + listModel.size()
							+ ")");
			rootWindow.repaint();
		}

		/**
		 * @return
		 */
		private ConstructConceptTreePanel getConceptTreePanel() {
			ConstructConceptTreePanel conceptTreePanel = null;
			if (conceptTreeType.equals(ConceptTreeCellRenderer.NOUN_CONCEPT_TREE)) {
				conceptTreePanel = project.getConstructClassPanel();
			} else if (conceptTreeType.equals(ConceptTreeCellRenderer.VERB_CONCEPT_TREE)) {
				conceptTreePanel = project.getConstructPropertyPanel();
			}
			return conceptTreePanel;
		}
	}

	public void saveTrimmedResultAnalysis(File file) {
		BufferedWriter writer = null;
		try {
			FileOutputStream fos = new FileOutputStream(file);
			writer = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
			StringBuilder builder = new StringBuilder();

			List<ConceptTreeNode> conceptTreeNodeList = getTRAResult();
			for (ConceptTreeNode traNode : conceptTreeNodeList) {
				builder.append(traNode.getConcept().getURI());
				builder.append(",");
				ConceptTreeNode parentNode = (ConceptTreeNode) traNode.getParent();
				builder.append(parentNode.getConcept().getURI());
				builder.append(",");
				List<List<Concept>> trimmedConceptList = traNode.getTrimmedConceptList();
				for (List<Concept> list : trimmedConceptList) {
					builder.append("|");
					for (Concept tc : list) {
						if (tc == null) {
							continue;
						}
						builder.append(tc.getURI());
						builder.append(",");
					}
				}
				builder.append("\n");
			}
			writer.write(builder.toString());
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException ioe2) {
					ioe2.printStackTrace();
				}
			}
		}
	}

	public void saveTrimmedResultAnalysis(int projectID, Statement stmt,
			String trimmedAnalysisTable, String trimmedConceptListTable) {
		try {
			List<ConceptTreeNode> conceptTreeNodeList = getTRAResult();
			int conceptListID = 1;
			for (ConceptTreeNode traNode : conceptTreeNodeList) {
				ConceptTreeNode parentNode = (ConceptTreeNode) traNode.getParent();
				String sql = "INSERT INTO " + trimmedAnalysisTable
						+ " (Project_ID,Concept_List_ID,Target_Concept,Target_Parent_Concept) "
						+ "VALUES(" + projectID + "," + conceptListID + ",'"
						+ traNode.getConcept().getURI() + "','" + parentNode.getConcept().getURI()
						+ "')";
				stmt.executeUpdate(sql);

				List<List<Concept>> trimmedConceptList = traNode.getTrimmedConceptList();
				for (List<Concept> list : trimmedConceptList) {
					for (Concept tc : list) {
						if (tc == null) {
							continue;
						}
						sql = "INSERT INTO " + trimmedConceptListTable
								+ " (Project_ID,Concept_List_ID,Concept) " + "VALUES(" + projectID
								+ ",'" + conceptListID + "','" + tc.getURI() + "')";
						stmt.executeUpdate(sql);
					}
				}
				conceptListID++;
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}

	public void loadTrimmedResultAnalysis(File file) {
		if (!file.exists()) {
			return;
		}
		BufferedReader reader = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
			Map<String, List<List<Concept>>> idTrimmedConceptListMap = new HashMap<String, List<List<Concept>>>();
			while (reader.ready()) {
				String line = reader.readLine();
				String[] lines = line.split("\\|");
				String[] concepts = lines[0].split(",");
				List<List<Concept>> trimmedConceptList = new ArrayList<List<Concept>>();
				for (int i = 1; i < lines.length; i++) {
					String[] conceptStrs = lines[i].split(",");
					List<Concept> list = new ArrayList<Concept>();
					for (int j = 0; j < conceptStrs.length; j++) {
						list.add(DODDLEDic.getConcept(conceptStrs[j]));
					}
					trimmedConceptList.add(list);
				}
				idTrimmedConceptListMap.put(concepts[0] + concepts[1], trimmedConceptList);
			}
			TreeModel treeModel = conceptTree.getModel();
			ConceptTreeNode rootNode = (ConceptTreeNode) treeModel.getRoot();
			loadTrimmedResultAnalysis(rootNode, idTrimmedConceptListMap);
			setTRADefaultValue();
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException ioe2) {
				ioe2.printStackTrace();
			}
		}
	}

	public void loadTrimmedResultAnalysis(int projectID, Statement stmt,
			String trimmedAnalysisTable, String trimmedConceptListTable) {
		try {
			Map<Integer, String> conceptListIDConceptMap = new HashMap<Integer, String>();
			String sql = "SELECT * from " + trimmedAnalysisTable + " where Project_ID=" + projectID;
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				int conceptListID = rs.getInt("Concept_List_ID");
				String targetConcept = rs.getString("Target_Concept");
				String targetParentConcept = rs.getString("Target_Parent_Concept");
				conceptListIDConceptMap.put(conceptListID, targetConcept + targetParentConcept);
			}
			Map<String, List<List<Concept>>> idTrimmedConceptListMap = new HashMap<String, List<List<Concept>>>();
			for (Entry<Integer, String> entry : conceptListIDConceptMap.entrySet()) {
				int conceptListID = entry.getKey();
				String conceptID = entry.getValue();
				List<List<Concept>> trimmedConceptList = null;
				if (idTrimmedConceptListMap.get(conceptID) != null) {
					trimmedConceptList = idTrimmedConceptListMap.get(conceptID);
				} else {
					trimmedConceptList = new ArrayList<List<Concept>>();
				}
				sql = "SELECT * from " + trimmedConceptListTable + " where Project_ID=" + projectID
						+ " and Concept_List_ID=" + conceptListID;
				rs = stmt.executeQuery(sql);
				List<Concept> list = new ArrayList<Concept>();
				while (rs.next()) {
					String concept = rs.getString("Concept");
					list.add(DODDLEDic.getConcept(concept));
				}
				trimmedConceptList.add(list);
				idTrimmedConceptListMap.put(conceptID, trimmedConceptList);
			}

			TreeModel treeModel = conceptTree.getModel();
			ConceptTreeNode rootNode = (ConceptTreeNode) treeModel.getRoot();
			loadTrimmedResultAnalysis(rootNode, idTrimmedConceptListMap);
			setTRADefaultValue();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void loadTrimmedResultAnalysis(ConceptTreeNode node,
			Map<String, List<List<Concept>>> idTrimmedConceptListMap) {
		for (int i = 0; i < node.getChildCount(); i++) {
			ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
			String id = childNode.getConcept().getURI() + node.getConcept().getURI();
			List<List<Concept>> trimmedConceptList = idTrimmedConceptListMap.get(id);
			if (trimmedConceptList != null && 0 < trimmedConceptList.size()) {
				childNode.setTrimmedConceptList(trimmedConceptList);
				addTRANode(childNode);
			}
			loadTrimmedResultAnalysis(childNode, idTrimmedConceptListMap);
		}
	}
}