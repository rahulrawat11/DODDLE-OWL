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

package net.sourceforge.doddle_owl.actions;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import net.sourceforge.doddle_owl.*;
import net.sourceforge.doddle_owl.data.*;
import net.sourceforge.doddle_owl.ui.*;
import net.sourceforge.doddle_owl.utils.*;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * @author Takeshi Morita
 */
public class LoadOntologyAction extends AbstractAction {

	private String conversionType;
	private FileFilter owlFileFilter;
	private FileFilter freeMindFileFilter;
	public static final String OWL_ONTOLOGY = "OWL";
	public static final String FREEMIND_ONTOLOGY = "FREEMIND";

	public LoadOntologyAction(String title, String type) {
		super(title);
		conversionType = type;
		owlFileFilter = new OWLFileFilter();
		freeMindFileFilter = new FreeMindFileFilter();
	}

	public void loadFreeMindOntology(DODDLEProject currentProject, File file) {
		InputConceptSelectionPanel inputConceptSelectionPanel = currentProject
				.getInputConceptSelectionPanel();
		ConstructClassPanel constructClassPanel = currentProject.getConstructClassPanel();
		ConstructPropertyPanel constructPropertyPanel = currentProject.getConstructPropertyPanel();

		if (!file.exists()) {
			return;
		}
		constructClassPanel.init();
		constructPropertyPanel.init();
		currentProject.resetURIConceptMap();
		ConceptTreeMaker.getInstance().setInputConceptSet(
				inputConceptSelectionPanel.getInputConceptSet());

		Element docElement = FreeMindModelMaker.getDocumentElement(file);
		Element rootNode = null;
		Element nounRootNode = null;
		Element verbRootNode = null;
		NodeList rootNodeList = docElement.getChildNodes();
		for (int i = 0; i < rootNodeList.getLength(); i++) {
			if (rootNodeList.item(i).getNodeName().equals("node")) {
				rootNode = (Element) rootNodeList.item(i);
			}
		}
		rootNodeList = rootNode.getChildNodes();
		for (int i = 0; i < rootNodeList.getLength(); i++) {
			if (rootNodeList.item(i).getNodeName().equals("node")) {
				rootNode = (Element) rootNodeList.item(i);
				if (rootNode.getAttribute("ID").equals(ConceptTreeMaker.DODDLE_CLASS_ROOT_URI)) {
					nounRootNode = rootNode;
				} else if (rootNode.getAttribute("ID").equals(
						ConceptTreeMaker.DODDLE_PROPERTY_ROOT_URI)) {
					verbRootNode = rootNode;
				}
			}
		}

		Concept rootNounConcept = new VerbConcept(ConceptTreeMaker.DODDLE_CLASS_ROOT_URI, "");
		rootNounConcept.addLabel(new DODDLELiteral("ja", "名詞的概念 (Is-a)"));
		rootNounConcept.addLabel(new DODDLELiteral("en", "Is-a Root Class"));
		ConceptTreeNode rootTreeNode = new ConceptTreeNode(rootNounConcept,
				DODDLE_OWL.getCurrentProject());
		FreeMindModelMaker.setConceptTreeModel(rootTreeNode, nounRootNode);

		currentProject.initUserIDCount();
		DefaultTreeModel treeModel = new DefaultTreeModel(rootTreeNode);
		treeModel = constructClassPanel.setConceptTreeModel(treeModel);
		constructClassPanel.setVisibleIsaTree(true);
		currentProject.setUserIDCount(currentProject.getUserIDCount() + 1);
		ConceptTreeMaker.getInstance().conceptDriftManagement(treeModel);
		constructClassPanel.setConceptDriftManagementResult();
		treeModel.reload();

		currentProject.setUserIDCount(currentProject.getUserIDCount());
		VerbConcept rootVerbConcept = new VerbConcept(ConceptTreeMaker.DODDLE_PROPERTY_ROOT_URI, "");
		rootVerbConcept.addLabel(new DODDLELiteral("ja", "動詞的概念"));
		rootVerbConcept.addLabel(new DODDLELiteral("en", "Root Property"));
		rootTreeNode = new ConceptTreeNode(rootVerbConcept, DODDLE_OWL.getCurrentProject());
		FreeMindModelMaker.setConceptTreeModel(rootTreeNode, verbRootNode);
		treeModel = new DefaultTreeModel(rootTreeNode);
		treeModel = constructPropertyPanel.setConceptTreeModel(treeModel);
		constructPropertyPanel.setVisibleIsaTree(true);
		ConceptTreeMaker.getInstance().conceptDriftManagement(treeModel);
		constructPropertyPanel.setConceptDriftManagementResult();
		treeModel.reload();
		currentProject.setUserIDCount(currentProject.getUserIDCount() + 1);
		expandTrees(currentProject);
	}

	public void loadOWLOntology(DODDLEProject currentProject, File file) {
		if (!file.exists()) {
			return;
		}
		BufferedReader reader = null;
		try {
			InputStream is = new FileInputStream(file);
			reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			Model model = ModelFactory.createDefaultModel();
			model.read(reader, DODDLEConstants.BASE_URI, "RDF/XML");
			loadOWLOntology(currentProject, model);
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

	public void loadOWLOntology(DODDLEProject currentProject, Model model) {
		InputConceptSelectionPanel inputConceptSelectionPanel = currentProject
				.getInputConceptSelectionPanel();
		ConstructClassPanel constructClassPanel = currentProject.getConstructClassPanel();
		ConstructPropertyPanel constructPropertyPanel = currentProject.getConstructPropertyPanel();

		constructClassPanel.init();
		constructPropertyPanel.init();
		currentProject.resetURIConceptMap();
		ConceptTreeMaker.getInstance().setInputConceptSet(
				inputConceptSelectionPanel.getInputConceptSet());

		currentProject.initUserIDCount();
		TreeNode rootNode = ConceptTreeMaker.getInstance().getConceptTreeRoot(currentProject,
				model, ResourceFactory.createResource(ConceptTreeMaker.DODDLE_CLASS_ROOT_URI),
				ConceptTreePanel.CLASS_ISA_TREE);
		DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
		treeModel = constructClassPanel.setConceptTreeModel(treeModel);
		constructClassPanel.setVisibleIsaTree(true);
		currentProject.setUserIDCount(currentProject.getUserIDCount() + 1);
		ConceptTreeMaker.getInstance().conceptDriftManagement(treeModel);
		constructClassPanel.setConceptDriftManagementResult();
		treeModel.reload();

		rootNode = ConceptTreeMaker.getInstance().getConceptTreeRoot(currentProject, model,
				ResourceFactory.createResource(ConceptTreeMaker.DODDLE_CLASS_HASA_ROOT_URI),
				ConceptTreePanel.CLASS_HASA_TREE);
		treeModel = new DefaultTreeModel(rootNode);
		constructClassPanel.setHasaTreeModel(treeModel);
		constructClassPanel.setVisibleHasaTree(true);
		treeModel.reload();

		currentProject.setUserIDCount(currentProject.getUserIDCount());
		rootNode = ConceptTreeMaker.getInstance().getPropertyTreeRoot(currentProject, model,
				ResourceFactory.createResource(ConceptTreeMaker.DODDLE_PROPERTY_ROOT_URI),
				ConceptTreePanel.PROPERTY_ISA_TREE);
		treeModel = new DefaultTreeModel(rootNode);
		treeModel = constructPropertyPanel.setConceptTreeModel(treeModel);
		constructPropertyPanel.setVisibleIsaTree(true);
		ConceptTreeMaker.getInstance().conceptDriftManagement(treeModel);
		constructPropertyPanel.setConceptDriftManagementResult();
		treeModel.reload();

		rootNode = ConceptTreeMaker.getInstance().getPropertyTreeRoot(currentProject, model,
				ResourceFactory.createResource(ConceptTreeMaker.DODDLE_PROPERTY_HASA_ROOT_URI),
				ConceptTreePanel.PROPERTY_HASA_TREE);
		treeModel = new DefaultTreeModel(rootNode);
		constructPropertyPanel.setHasaTreeModel(treeModel);
		constructPropertyPanel.setVisibleHasaTree(true);
		treeModel.reload();

		currentProject.setUserIDCount(currentProject.getUserIDCount() + 1);
		expandTrees(currentProject);
	}

	private void expandTrees(DODDLEProject currentProject) {
		currentProject.getConstructClassPanel().expandIsaTree();
		currentProject.getConstructClassPanel().expandHasaTree();
		currentProject.getConstructPropertyPanel().expandIsaTree();
		currentProject.getConstructPropertyPanel().expandHasaTree();
	}

	public void actionPerformed(ActionEvent e) {
		JFileChooser chooser = new JFileChooser(DODDLEConstants.PROJECT_HOME);
		if (conversionType.equals(OWL_ONTOLOGY)) {
			chooser.addChoosableFileFilter(owlFileFilter);
		} else if (conversionType.equals(FREEMIND_ONTOLOGY)) {
			chooser.addChoosableFileFilter(freeMindFileFilter);
		}
		int retval = chooser.showOpenDialog(DODDLE_OWL.rootPane);
		if (retval == JFileChooser.APPROVE_OPTION) {
			DODDLEProject currentProject = DODDLE_OWL.getCurrentProject();
			if (conversionType.equals(OWL_ONTOLOGY)) {
				loadOWLOntology(currentProject, chooser.getSelectedFile());
				DODDLE_OWL.STATUS_BAR.setText(Translator.getTerm("OpenOWLOntologyAction"));
			} else if (conversionType.equals(FREEMIND_ONTOLOGY)) {
				loadFreeMindOntology(currentProject, chooser.getSelectedFile());
				DODDLE_OWL.STATUS_BAR.setText(Translator.getTerm("OpenFreeMindOntologyAction"));
			}
		}
	}
}
