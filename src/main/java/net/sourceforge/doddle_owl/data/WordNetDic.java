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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.PointerUtils;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.data.list.PointerTargetNode;
import net.sf.extjwnl.data.list.PointerTargetNodeList;
import net.sf.extjwnl.data.list.PointerTargetTree;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sourceforge.doddle_owl.*;
import net.sourceforge.doddle_owl.utils.*;

import org.apache.log4j.Level;

/**
 * @author shigeta
 * @author Takeshi Morita
 * 
 */
public class WordNetDic {

	private static WordNetDic wordnetDic;
	private static Dictionary dictionary;

	public WordNetDic() {
		try {
			dictionary = Dictionary.getFileBackedInstance(Utils.getENWNFile().getAbsolutePath());
		} catch (Exception ex) {
			ex.printStackTrace();
			DODDLE_OWL.getLogger().log(Level.INFO, Translator.getTerm("WordNetLoadErrorMessage"));
			JOptionPane.showMessageDialog(null, Translator.getTerm("WordNetLoadErrorMessage"));
		}
	}

	public static void resetWordNet() {
		try {
			dictionary = Dictionary.getFileBackedInstance(Utils.getENWNFile().getAbsolutePath());
		} catch (JWNLException e) {
			e.printStackTrace();
			DODDLE_OWL.getLogger().log(Level.INFO, Translator.getTerm("WordNetLoadErrorMessage"));
			JOptionPane.showMessageDialog(null, Translator.getTerm("WordNetLoadErrorMessage"));
		}
	}

	public static WordNetDic getInstance() {
		if (wordnetDic == null) {
			wordnetDic = new WordNetDic();
		}
		return wordnetDic;
	}

	public IndexWord getIndexWord(POS pos, String word) {
		IndexWord indexWord = null;
		try {
			indexWord = dictionary.lookupIndexWord(pos, word);
		} catch (JWNLException e) {
			e.printStackTrace();
		}
		return indexWord;
	}

	private Set<String> adverbConceptSet = new HashSet<String>();

	private boolean isAdverb(String concept) {
		if (adverbConceptSet.contains(concept)) {
			return true;
		}
		IndexWord retiw;
		try {
			retiw = dictionary.lookupIndexWord(POS.ADVERB, concept);
		} catch (JWNLException je) {
			retiw = null;
		}
		if (retiw != null) {
			adverbConceptSet.add(concept);
		}
		return (retiw != null);
	}

	private boolean isNumber(String concept) {
		if (concept.startsWith("1") || concept.startsWith("2") || concept.startsWith("3")
				|| concept.startsWith("4") || concept.startsWith("5") || concept.startsWith("6")
				|| concept.startsWith("7") || concept.startsWith("8") || concept.startsWith("9")
				|| concept.startsWith("0")) {
			return true;
		} else if (concept.equals("one") || concept.equals("two") || concept.equals("three")
				|| concept.equals("four") || concept.equals("five") || concept.equals("six")
				|| concept.equals("seven") || concept.equals("eight") || concept.equals("nine")
				|| concept.equals("ten") || concept.equals("eleven") || concept.equals("twelve")
				|| concept.equals("thirteen") || concept.equals("fourteen")
				|| concept.equals("fifteen") || concept.equals("sixteen")
				|| concept.equals("seventeen") || concept.equals("eighteen")
				|| concept.equals("nineteen") || concept.equals("twenty")
				|| concept.equals("thirty") || concept.equals("forty") || concept.equals("fifty")
				|| concept.equals("sixty") || concept.equals("seventy") || concept.equals("eighty")
				|| concept.equals("ninety") || concept.equals("hundred")
				|| concept.equals("thousand")) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isVerb(String concept) {
		return (concept.equals("be") || concept.equals("are") || concept.equals("take")
				|| concept.equals("make") || concept.equals("do") || concept.equals("have")
				|| concept.equals("give") || concept.equals("given"));
	}

	private boolean isNogood(String concept) {
		return (concept.equals("ha") || concept.equals("or") || concept.equals("he")
				|| concept.equals("at") || concept.equals("who") || concept.equals("wa")
				|| concept.equals("ii") || concept.equals("iii"));
	}

	public boolean isInvalid(String concept) {
		return (isVerb(concept) || isAdverb(concept) || isNumber(concept) || isNogood(concept));
	}

	public static Set<Set<String>> getSiblingConceptSet(Long offset) {
		Set<Set<String>> siblingIDSet = new HashSet<Set<String>>();
		try {
			Synset synset = dictionary.getSynsetAt(POS.NOUN, offset.longValue());
			PointerTargetNodeList siblingNodeList = PointerUtils.getCoordinateTerms(synset);
			Set<String> idList = new HashSet<String>();
			for (PointerTargetNode node : siblingNodeList) {
				idList.add(new Long(node.getSynset().getOffset()).toString());
			}
			siblingIDSet.add(idList);
		} catch (JWNLException jwnle) {
			jwnle.printStackTrace();
		}
		return siblingIDSet;
	}

	public static Set<Set<String>> getSubIDSet(Long offset) {
		Set<Set<String>> subIDSet = new HashSet<Set<String>>();
		try {
			Synset synset = dictionary.getSynsetAt(POS.NOUN, offset.longValue());
			PointerTargetTree hyponymTree = PointerUtils.getHyponymTree(synset);
			// 多重継承を許すようにしている
			List<PointerTargetNodeList> treeNodeLists = hyponymTree.reverse();
			for (PointerTargetNodeList nodeList : treeNodeLists) {
				Set<String> idList = new HashSet<String>();
				for (PointerTargetNode node : nodeList) {
					idList.add(new Long(node.getSynset().getOffset()).toString());
				}
				subIDSet.add(idList);
			}
		} catch (JWNLException jwnle) {
			jwnle.printStackTrace();
		}
		return subIDSet;
	}

	public static Set<List<Concept>> getPathToRootSet(Long offset) {
		Set<List<Concept>> pathToRootSet = new HashSet<List<Concept>>();
		try {
			Synset synset = dictionary.getSynsetAt(POS.NOUN, offset.longValue());
			PointerTargetTree hypernymTree = PointerUtils.getHypernymTree(synset);
			// 多重継承を許すようにしている
			List<PointerTargetNodeList> treeNodeLists = hypernymTree.reverse();
			for (PointerTargetNodeList nodeList : treeNodeLists) {
				List<Concept> conceptList = new ArrayList<Concept>();
				for (PointerTargetNode node : nodeList) {
					Concept c = getWNConcept(new Long(node.getSynset().getOffset()).toString());
					conceptList.add(c);
				}
				pathToRootSet.add(conceptList);
			}
		} catch (JWNLException jwnle) {
			jwnle.printStackTrace();
		}
		return pathToRootSet;
	}

	private static boolean isEnglish(String iw) {
		return iw.matches("(\\w|\\s)*");
	}

	public static Set<String> getURISet(String word) {
		Set<String> uriSet = new HashSet<String>();
		if (!DODDLE_OWL.getCurrentProject().getOntologySelectionPanel().isWordNetEnable()) {
			return uriSet;
		}
		if (!isEnglish(word)) {
			return uriSet;
		}
		IndexWord indexWord = WordNetDic.getInstance().getNounIndexWord(word);
		if (indexWord == null) {
			return uriSet;
		}
		for (Synset synset : indexWord.getSenses()) {
			if (synset.containsWord(word)) {
				uriSet.add(DODDLEConstants.WN_URI + new Long(synset.getOffset()).toString());
			}
		}
		return uriSet;
	}

	public static Set<List<String>> getURIPathToRootSet(Long offset) {
		Set<List<String>> pathToRootSet = new HashSet<List<String>>();
		try {
			Synset synset = dictionary.getSynsetAt(POS.NOUN, offset.longValue());
			PointerTargetTree hypernymTree = PointerUtils.getHypernymTree(synset);
			// 多重継承を許すようにしている
			List<PointerTargetNodeList> treeNodeLists = hypernymTree.reverse();
			for (PointerTargetNodeList nodeList : treeNodeLists) {
				List<String> uriList = new ArrayList<String>();
				for (PointerTargetNode node : nodeList) {
					String uri = DODDLEConstants.WN_URI
							+ new Long(node.getSynset().getOffset()).toString();
					uriList.add(uri);
				}
				pathToRootSet.add(uriList);
			}
		} catch (JWNLException jwnle) {
			jwnle.printStackTrace();
		}
		return pathToRootSet;
	}

	private static Map<String, Concept> idConceptMap = new HashMap<String, Concept>();

	public static Concept getWNConcept(String id) {
		if (dictionary == null) {
			return null;
		}
		String uri = DODDLEConstants.WN_URI + id;
		try {
			if (idConceptMap.get(uri) != null) {
				return idConceptMap.get(uri);
			}
			Synset synset = dictionary.getSynsetAt(POS.NOUN, new Long(id).longValue());
			Concept c = new Concept(uri, "");
			for (Word word : synset.getWords()) {
				c.addLabel(new DODDLELiteral("en", word.getLemma()));
			}
			c.addDescription(new DODDLELiteral("en", synset.getGloss()));
			idConceptMap.put(uri, c);
			return c;
		} catch (JWNLException jwnle) {
			jwnle.printStackTrace();
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		}
		return null;
	}

	public IndexWord getNounIndexWord(String word) {
		try {
			return dictionary.lookupIndexWord(POS.NOUN, word);
		} catch (JWNLException e) {
			return null;
		}
	}

	public IndexWord getVerbIndexWord(String word) {
		try {
			return dictionary.lookupIndexWord(POS.VERB, word);
		} catch (JWNLException e) {
			return null;
		}
	}

	public static void main(String[] args) throws Exception {
		Dictionary dictionary = null;
		try {
			dictionary = Dictionary
					.getFileBackedInstance("/Users/t_morita/DODDLE-OWL/WordNet-3.0/dict");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println(dictionary.lookupIndexWord(POS.NOUN, "digital clock"));
		String word = "ping-pong ball";
		IndexWord indexWord = dictionary.lookupIndexWord(POS.NOUN, word);
		System.out.println(indexWord);
		for (Synset synset : indexWord.getSenses()) {
			System.out.println(synset.containsWord(word) + ": " + synset.getWords());
		}

		System.out.println(dictionary.lookupIndexWord(POS.NOUN, "dog"));
		System.out.println(dictionary.lookupIndexWord(POS.NOUN, "cat"));
	}
}