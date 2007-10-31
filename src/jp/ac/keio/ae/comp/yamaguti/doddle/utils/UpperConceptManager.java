package jp.ac.keio.ae.comp.yamaguti.doddle.utils;

import java.io.*;
import java.util.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;

/**
 * @author takeshi morita
 */
public class UpperConceptManager {

    private static Map<String, Set<String>> upperConceptLabelIDMap;
    public static String UPPER_CONCEPT_LIST = "./upperConceptList.txt";

    public static Set<String> getUpperConceptLabelSet() {
        return upperConceptLabelIDMap.keySet();
    }

    public static Set<String> getWordSet(String ucLabel) {
        return upperConceptLabelIDMap.get(ucLabel);
    }

    public static void makeUpperOntologyList() {
        if (upperConceptLabelIDMap != null) { return; }
        upperConceptLabelIDMap = new TreeMap<String, Set<String>>();

        File file = new File(UPPER_CONCEPT_LIST);
        if (file.exists()) {
            BufferedReader reader = null;
            try {
                FileInputStream fis = new FileInputStream(file);
                reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
                while (reader.ready()) {
                    String line = reader.readLine();
                    String[] labelAndID = line.replaceAll("\n", "").split(",");
                    if (labelAndID[0].indexOf('#') == -1) {
                        System.out.println(labelAndID[0] + ":" + labelAndID[1]);
                        upperConceptLabelIDMap.put(labelAndID[0], getSubWordSet(labelAndID[1]));
                    }
                }
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
    }

    private static Set<String> getSubWordSet(String upperID) {
        Set<String> uriSet = new HashSet<String>();
        Set<Set<String>> subURISet = EDRTree.getEDRTree().getSubURISet(DODDLEConstants.EDR_URI + upperID);
        for (Set<String> set : subURISet) {
            uriSet.addAll(set);
        }
        uriSet.add(upperID);
        Set<String> wordSet = new HashSet<String>();
        for (String uri : uriSet) {
            Concept c = EDRDic.getEDRConcept(Utils.getLocalName(uri));
            if (c == null) {
                continue;
            }
            Map<String, List<DODDLELiteral>> langLabelListMap = c.getLangLabelListMap();
            for (List<DODDLELiteral> labelList : langLabelListMap.values()) {
                for (DODDLELiteral label : labelList) {
                    wordSet.add(label.getString());
                }
            }
        }
        return wordSet;
    }

}
