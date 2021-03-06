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

package net.sourceforge.doddle_owl.taskanalyzer;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import javax.xml.parsers.*;

import net.sourceforge.doddle_owl.data.Document;

import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * @author Takeshi Morita
 */
public class CabochaDocument {

    private String docName;
    private Document document;
    private List<Sentence> sentenceList;
    private Set<Segment> segmentSet;
    private Map<String, Integer> compoundWordCountMap;
    private Map<String, Integer> compoundWordWithNokakuCountMap;
    private Map<Segment, Set<Segment>> segmentMap;
    private Process cabochaProcess;
    public static String CHARSET = "UTF-8";

    public CabochaDocument(Process cp) {
        cabochaProcess = cp;
        sentenceList = new ArrayList<Sentence>();
        segmentSet = new HashSet<Segment>();
        compoundWordCountMap = new HashMap<String, Integer>();
        compoundWordWithNokakuCountMap = new HashMap<String, Integer>();
        segmentMap = new HashMap<Segment, Set<Segment>>();
    }

    public CabochaDocument(Document doc, Process cp) {
        this(cp);
        document = doc;
        cabochaDocReader();
    }

    private void setMorpheme(NodeList tokElementList, Segment segment) {
        for (int i = 0; i < tokElementList.getLength(); i++) {
            Element tokElement = (Element) tokElementList.item(i);
            String surface = tokElement.getTextContent();
            String[] elems = tokElement.getAttribute("feature").split(",");
            String pos = elems[0];
            for (int j = 1; j < 3; j++) {
                if (!elems[j].equals("*")) {
                    pos += "-" + elems[j];
                }
            }
            String basic = surface;
            String kana = surface;
            if (elems.length == 9) {
                basic = elems[6];
                kana = elems[7];
            }
            Morpheme morpheme = new Morpheme(surface, kana, basic, pos);
            segment.addMorpheme(morpheme);
        }
    }

    private void setChunk(NodeList chunkElementList, Segment segment, Sentence sentence) {
        for (int i = 0; i < chunkElementList.getLength(); i++) {
            Element chunkElement = (Element) chunkElementList.item(i);
            int link = Integer.parseInt(chunkElement.getAttribute("link"));
            segment = new Segment(link);
            sentence.addSegment(segment);
            NodeList tokElementList = chunkElement.getElementsByTagName("tok");
            setMorpheme(tokElementList, segment);
        }
    }

    private void cabochaReader(File outputFile) {
        Segment segment = null;
        Sentence sentence = new Sentence();
        try {
            DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbfactory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(outputFile);
            Element root = doc.getDocumentElement();
            NodeList sentenceElementList = root.getElementsByTagName("sentence");
            for (int i = 0; i < sentenceElementList.getLength(); i++) {
                Element sentenceElement = (Element) sentenceElementList.item(i);
                NodeList chunkElementList = sentenceElement.getElementsByTagName("chunk");
                setChunk(chunkElementList, segment, sentence);
                sentence.mergeSegments();
                setSegmentMap(sentence);
                setCompoundWordCountMap(sentence);
                setCompoundWordWithNokakuCountMap(sentence);
                segmentSet.addAll(sentence.getSegmentList());
                sentenceList.add(sentence);
                sentence = new Sentence();
            }
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException saxe) {
            saxe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private File saveCabochaOutput() {
        File tmpFile = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(cabochaProcess.getInputStream(), CHARSET));
            tmpFile = File.createTempFile("cabochaOutputTemp", null);
            BufferedWriter tmpWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpFile),
                    CabochaDocument.CHARSET));
            tmpWriter.write("<?xml version=\"1.0\" encoding=\"" + CHARSET + "\" ?>");
            tmpWriter.write("<root>");
            String line = "";
            while ((line = reader.readLine()) != null) {
                tmpWriter.write(line);
            }
            tmpWriter.write("</root>");
            tmpWriter.flush();
            tmpWriter.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
        }
        return tmpFile;
    }

    private void cabochaDocReader() {
        File outputFile = saveCabochaOutput();
        cabochaReader(outputFile);
    }

    public String getDocName() {
        return docName;
    }

    public Set<Segment> getSegmentSet() {
        return segmentSet;
    }

    private void setSegmentMap(Sentence sentence) {
        Map<Segment, Set<Segment>> sentenceMap = sentence.getSegmentMap();
        for (Entry<Segment, Set<Segment>> entry : sentenceMap.entrySet()) {
            if (segmentMap.get(entry.getKey()) != null) {
                Set<Segment> segSet = segmentMap.get(entry.getKey());
                segSet.addAll(entry.getValue());
            } else {
                segmentMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private void setCompoundWordCountMap(Sentence sentence) {
        Map<String, Integer> sentenceMap = sentence.getCompoundWordCountMap();
        for (Entry<String, Integer> entry : sentenceMap.entrySet()) {
            if (compoundWordCountMap.get(entry.getKey()) != null) {
                compoundWordCountMap.put(entry.getKey(), entry.getValue() + compoundWordCountMap.get(entry.getKey()));
            } else {
                compoundWordCountMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private void setCompoundWordWithNokakuCountMap(Sentence sentence) {
        Map<String, Integer> sentenceMap = sentence.getCompoundWordWithNokakuCountMap();
        for (Entry<String, Integer> entry : sentenceMap.entrySet()) {
            if (compoundWordWithNokakuCountMap.get(entry.getKey()) != null) {
                compoundWordWithNokakuCountMap.put(entry.getKey(), entry.getValue()
                        + compoundWordWithNokakuCountMap.get(entry.getKey()));
            } else {
                compoundWordWithNokakuCountMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public List<Sentence> getSentenceList() {
        return sentenceList;
    }

    public Map<Segment, Set<Segment>> getSegmentMap() {
        return segmentMap;
    }

    public Set<String> getCompoundWordSet() {
        return compoundWordCountMap.keySet();
    }

    public Map<String, Integer> getCompoundWordCountMap() {
        return compoundWordCountMap;
    }

    public Set<String> getCompoundWordWithNokakuSet() {
        return compoundWordWithNokakuCountMap.keySet();
    }

    public Map<String, Integer> getCompoundWordWithNokakuCountMap() {
        return compoundWordWithNokakuCountMap;
    }

    public List<PrimitiveTask> getPrimitiveTaskList() {
        List<PrimitiveTask> primitiveTaskList = new ArrayList<PrimitiveTask>();
        for (Sentence sentence : sentenceList) {
            primitiveTaskList.addAll(sentence.getTaskDescriptionSet());
        }
        return primitiveTaskList;
    }

    public void printTaskDescriptions() {
        for (Sentence sentence : sentenceList) {
            System.out.println("(文): " + sentence);
            for (PrimitiveTask taskDescription : sentence.getTaskDescriptionSet()) {
                System.out.println(taskDescription);
            }
            System.out.println("");
        }
    }

    public String toString() {
        return document.getFile().getName() + " sentence size: " + sentenceList.size() + " segment size: "
                + segmentSet.size();
    }
}
