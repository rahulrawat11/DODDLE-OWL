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

package net.sourceforge.doddle_owl.data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.SwingWorker;

import net.java.sen.SenFactory;
import net.java.sen.StringTagger;
import net.java.sen.dictionary.Token;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.Synset;
import net.sourceforge.doddle_owl.*;
import net.sourceforge.doddle_owl.taskanalyzer.*;
import net.sourceforge.doddle_owl.utils.*;

/**
 *
 * @author Takeshi Morita
 *
 */
public class InputModule {

	private boolean isLoadInputTermSet;
	private Set<String> undefinedTermSet;
	private Set<InputTermModel> inputTermModelSet;
	private Map<String, Set<Concept>> termConceptSetMap;

	public static int INIT_PROGRESS_VALUE = 887253;
	private DODDLEProject project;

	public InputModule(DODDLEProject p) {
		project = p;
		isLoadInputTermSet = false;
		undefinedTermSet = new TreeSet<String>();
		inputTermModelSet = new TreeSet<InputTermModel>();
		termConceptSetMap = new HashMap<String, Set<Concept>>();
	}

	static class WordIDsLinesComparator implements Comparator<String> {
		public int compare(String o1, String o2) {
			String w1 = o1.split("\t")[0];
			String w2 = o2.split("\t")[0];
			return w1.compareTo(w2);
		}
	}

	static class IDDefinitionLinesComparator implements Comparator<String> {
		public int compare(String o1, String o2) {
			return o1.compareTo(o2);
		}
	}

	private void clearData() {
		isLoadInputTermSet = false;
		inputTermModelSet.clear();
		undefinedTermSet.clear();
		termConceptSetMap.clear();
	}

	public boolean isLoadInputTermSet() {
		return isLoadInputTermSet;
	}

	public void setIsLoadInputTermSet() {
		isLoadInputTermSet = true;
	}

	/**
	 *
	 * 複合語の先頭の形態素から除いていき照合を行う．
	 */
	public InputTermModel makeInputTermModel(String inputTerm) {
		if (inputTerm.length() == 0) {
			return null;
		}
		List<Morpheme> morphemeList = getMorphemeList(inputTerm);
		StringBuilder subInputTermBuilder = null;
		Set<Concept> conceptSet = null;
		boolean isEnglish = isEnglish(inputTerm);
		int matchedPoint = 0;

		for (int i = 0; i < morphemeList.size(); i++) {
			List<Morpheme> subList = morphemeList.subList(i, morphemeList.size());
			subInputTermBuilder = new StringBuilder();
			for (Morpheme morpheme : subList) {
				String basicForm = morpheme.getBasic();
				if (isEnglish) {
					subInputTermBuilder.append(basicForm + " ");
				} else {
					subInputTermBuilder.append(basicForm);
				}
			}
			if (isEnglish) {
				subInputTermBuilder.deleteCharAt(subInputTermBuilder.length() - 1);
			}
			conceptSet = getConceptSet(subInputTermBuilder.toString());
			if (0 < conceptSet.size()) {
				matchedPoint = i;
				break;
			}
		}
		if (conceptSet.size() == 0) {
			return null;
		}
		InputTermModel itModel = new InputTermModel(inputTerm, morphemeList,
				subInputTermBuilder.toString(), conceptSet.size(), matchedPoint, project);
		if (termConceptSetMap.get(itModel.getMatchedTerm()) == null) {
			termConceptSetMap.put(itModel.getMatchedTerm(), conceptSet);
		}
		return itModel;
	}

	private Set<Concept> getConceptSet(String subInputTerm) {
		Set<Concept> conceptSet = new HashSet<Concept>();
		setEDRConceptSet(subInputTerm, conceptSet);
		setEDRTConceptSet(subInputTerm, conceptSet);
		setWordNetConceptSet(subInputTerm, conceptSet); // スペースを_に置き換えるとマッチしなくなる
		setJpnWordNetConceptSet(subInputTerm, conceptSet);
		OWLOntologyManager.setOWLConceptSet(subInputTerm, conceptSet);
		return conceptSet;
	}

	private void setWordNetConceptSet(String subIW, Set<Concept> conceptSet) {
		if (!project.getOntologySelectionPanel().isWordNetEnable() || !isEnglish(subIW)) {
			return;
		}
		IndexWord indexWord = WordNetDic.getInstance().getNounIndexWord(subIW);
		if (indexWord == null) {
			return;
		}
		for (Synset synset : indexWord.getSenses()) {
			if (synset.containsWord(subIW)) {
				Concept c = WordNetDic.getWNConcept(new Long(synset.getOffset()).toString());
				conceptSet.add(c);
			}
		}
	}

	private void setJpnWordNetConceptSet(String subIW, Set<Concept> conceptSet) {
		if (!project.getOntologySelectionPanel().isJpnWordNetEnable()) {
			return;
		}
		Set<String> idSet = JpnWordNetDic.getJPNWNSynsetSet(subIW);
		if (idSet == null) {
			return;
		}
		for (String id : idSet) {
			Concept c = JpnWordNetDic.getJPNWNConcept(id);
			if (c != null) {
				conceptSet.add(c);
			}
		}
	}

	private void setEDRConceptSet(String subInputTerm, Set<Concept> conceptSet) {
		if (!project.getOntologySelectionPanel().isEDREnable()) {
			return;
		}
		Set<String> idSet = EDRDic.getEDRIDSet(subInputTerm);
		// System.out.println(idSet);
		if (idSet == null) {
			return;
		}
		for (String id : idSet) {
			Concept c = EDRDic.getEDRConcept(id);
			if (c != null) {
				conceptSet.add(c);
			}
		}
	}

	private void setEDRTConceptSet(String subIW, Set<Concept> conceptSet) {
		if (!project.getOntologySelectionPanel().isEDRTEnable()) {
			return;
		}
		Set<String> idSet = EDRDic.getEDRTIDSet(subIW);
		if (idSet == null) {
			return;
		}
		for (String id : idSet) {
			Concept c = EDRDic.getEDRTConcept(id);
			if (c != null) {
				conceptSet.add(c);
			}
		}
	}

	private boolean isEnglish(String iw) {
		return iw.matches("(\\w|\\s|-|_)*"); // ping-pong ball, digital_clockを考慮
	}

	private List<Morpheme> getMorphemeList(String iw) {
		if (isEnglish(iw)) {
			return getEnMorphemeList(iw);
		}
		return getJaMorphemeList(iw);
	}

	private List<Morpheme> getEnMorphemeList(String iw) {
		List<Morpheme> morphemeList = new ArrayList<Morpheme>();
		if (iw.indexOf(" ") == -1) {
			Morpheme m = new Morpheme(iw, iw);
			morphemeList.add(m);
		}
		for (String w : iw.split(" ")) {
			Morpheme m = new Morpheme(w, w);
			morphemeList.add(m);
		}
		return morphemeList;
	}

	/**
	 * @param iw
	 */
	private List<Morpheme> getJaMorphemeList(String iw) {
		List<Morpheme> morphemeList = new ArrayList<Morpheme>();
		try {
			StringTagger tagger = SenFactory.getStringTagger(null);
			List<Token> tokenList = new ArrayList<Token>();
			tagger.analyze(iw, tokenList);
			for (Token t : tokenList) {
				String basicForm = t.getMorpheme().getBasicForm();
				if (basicForm.equals("*")) {
					basicForm = t.getSurface();
				}
				morphemeList.add(new Morpheme(t.getSurface(), basicForm));
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return morphemeList;
	}

	public class InputTermModelWorker extends SwingWorker<String, String> implements
			PropertyChangeListener {

		private int division;
		private int initialTaskCnt;
		private int currentTaskCnt;
		private Set<String> termSet;

		public InputTermModelWorker(Set<String> termSet, int taskCnt) {
			initialTaskCnt = taskCnt;
			this.termSet = termSet;
			if (initialTaskCnt == 0) {
				addPropertyChangeListener(this);
				currentTaskCnt = taskCnt;
				int progressCountSize = 50;
				if (termSet.size() < progressCountSize) {
					division = 1;
				} else {
					division = termSet.size() / progressCountSize;
				}
				DODDLE_OWL.STATUS_BAR.setLastMessage(Translator.getTerm("SetInputTermSetButton"));
				DODDLE_OWL.STATUS_BAR.startTime();
				DODDLE_OWL.STATUS_BAR.initNormal(progressCountSize);
				DODDLE_OWL.STATUS_BAR.lock();
			}
		}

		public String doInBackground() {
			try {
				clearData();
				Set<String> matchedTermSet = new HashSet<String>();
				int i = 0;
				for (String term : termSet) {
					i++;
					DODDLE_OWL.STATUS_BAR.setLastMessage(Translator.getTerm("SetInputTermSetButton")
							+ ": " + i + "/" + termSet.size());
					if (initialTaskCnt == 0 && i % division == 0) {
						setProgress(currentTaskCnt++);
					}
					InputTermModel itModel = makeInputTermModel(term);
					if (itModel != null) {
						inputTermModelSet.add(itModel);
						// 部分照合した複合語中で，完全照合単語リストに含まれない照合した単語を完全照合単語として追加
						String matchedTerm = itModel.getMatchedTerm();
						if (!(term.equals(matchedTerm) || matchedTermSet.contains(matchedTerm) || termSet
								.contains(matchedTerm))) {
							itModel = makeInputTermModel(matchedTerm);
							if (itModel != null) {
								matchedTermSet.add(matchedTerm);
								itModel.setIsSystemAdded(true);
								inputTermModelSet.add(itModel);
							}
						}
					} else {
						if (0 < term.length()) {
							undefinedTermSet.add(term);
						}
					}
				}
				project.getInputConceptSelectionPanel().initTermList();
				project.getConceptDefinitionPanel().setInputConceptSet();
				DODDLE_OWL.setSelectedIndex(DODDLEConstants.DISAMBIGUATION_PANEL);
			} finally {
				if (initialTaskCnt == 0) {
					DODDLE_OWL.STATUS_BAR.unLock();
					DODDLE_OWL.STATUS_BAR.hideProgressBar();
				}
				isLoadInputTermSet = true;
				DODDLE_OWL.STATUS_BAR.setLastMessage(Translator.getTerm("SetInputTermSetDoneMessage"));
			}
			return "done";
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getNewValue() instanceof Integer) {
				DODDLE_OWL.STATUS_BAR.setValue(currentTaskCnt);
			}
		}
	}

	public void initData(Set<String> termSet, int taskCnt) {
		InputTermModelWorker worker = new InputTermModelWorker(termSet, taskCnt);
		DODDLE_OWL.STATUS_BAR.setSwingWorker(worker);
		worker.execute();
	}

	public Set<InputTermModel> getInputTermModelSet() {
		return inputTermModelSet;
	}

	public Map<String, Set<Concept>> getTermConceptSetMap() {
		return termConceptSetMap;
	}

	public Set<String> getUndefinedTermSet() {
		return undefinedTermSet;
	}
}