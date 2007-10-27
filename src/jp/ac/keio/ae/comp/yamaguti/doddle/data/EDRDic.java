package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import java.io.*;
import java.util.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

import org.apache.log4j.*;

/**
 * @author takeshi morita
 */
public class EDRDic {

    private static DBManager edrDBManager;
    private static DBManager edrtDBManager;

    private static String TREE_DATA_FILE = "tree.data";
    private static String WORD_DATA_FILE = "word.data";
    private static String CONCEPT_DATA_FILE = "concept.data";
    private static String RELATION_DATA_FILE = "relation.data";

    private static String TREE_INDEX_FILE = "tree.index";
    private static String WORD_INDEX_FILE = "word.index";
    private static String CONCEPT_INDEX_FILE = "concept.index";
    private static String RELATION_INDEX_FILE = "relation.index";

    private static RandomAccessFile edrTreeDataFile;
    private static RandomAccessFile edrWordDataFile;
    private static RandomAccessFile edrConceptDataFile;
    private static RandomAccessFile edrRelationDataFile;

    private static RandomAccessFile edrTreeIndexFile;
    private static RandomAccessFile edrWordIndexFile;
    private static RandomAccessFile edrConceptIndexFile;
    private static RandomAccessFile edrRelationIndexFile;

    private static RandomAccessFile edrtTreeDataFile;
    private static RandomAccessFile edrtWordDataFile;
    private static RandomAccessFile edrtConceptDataFile;

    private static RandomAccessFile edrtTreeIndexFile;
    private static RandomAccessFile edrtWordIndexFile;
    private static RandomAccessFile edrtConceptIndexFile;

    private static Map<String, Concept> edrURIConceptMap; // キャッシュ用
    private static Map<String, Concept> edrtURIConceptMap; // キャッシュ用
    private static Map<String, Set<String>> edrWordIDSetMap; // キャッシュ用
    private static Map<String, Set<String>> edrtWordIDSetMap; // キャッシュ用

    public static boolean initEDRDic() {
        if (edrURIConceptMap != null) { return true; }
        edrURIConceptMap = new HashMap<String, Concept>();
        if (DODDLEConstants.IS_USING_DB) {
            try {
                edrDBManager = new DBManager(true, DODDLEConstants.EDR_HOME);
                DODDLE.getLogger().log(Level.INFO, "init EDR Concept Classification Dictionary on DB");
            } catch (Exception e) {
                // If an exception reaches this point, the last transaction did
                // not
                // complete. If the exception is RunRecoveryException, follow
                // the Berkeley DB recovery procedures before running again.
                edrURIConceptMap = null;
                DODDLE.getLogger().log(Level.INFO, "cannot open EDR Dic");
                return false;
            }
        } else {
            edrWordIDSetMap = new HashMap<String, Set<String>>();
            try {
                edrTreeDataFile = new RandomAccessFile(DODDLEConstants.EDR_HOME + TREE_DATA_FILE, "r");
                edrWordDataFile = new RandomAccessFile(DODDLEConstants.EDR_HOME + WORD_DATA_FILE, "r");
                edrConceptDataFile = new RandomAccessFile(DODDLEConstants.EDR_HOME + CONCEPT_DATA_FILE, "r");
                edrRelationDataFile = new RandomAccessFile(DODDLEConstants.EDR_HOME + RELATION_DATA_FILE, "r");

                edrTreeIndexFile = new RandomAccessFile(DODDLEConstants.EDR_HOME + TREE_INDEX_FILE, "r");
                edrWordIndexFile = new RandomAccessFile(DODDLEConstants.EDR_HOME + WORD_INDEX_FILE, "r");
                edrConceptIndexFile = new RandomAccessFile(DODDLEConstants.EDR_HOME + CONCEPT_INDEX_FILE, "r");
                edrRelationIndexFile = new RandomAccessFile(DODDLEConstants.EDR_HOME + RELATION_INDEX_FILE, "r");
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static boolean initEDRTDic() {
        if (edrtURIConceptMap != null) { return true; }
        edrtURIConceptMap = new HashMap<String, Concept>();
        if (DODDLEConstants.IS_USING_DB) {
            try {
                edrtDBManager = new DBManager(true, DODDLEConstants.EDRT_HOME);
            } catch (Exception e) {
                edrtURIConceptMap = null;
                DODDLE.getLogger().log(Level.INFO, "cannot open EDRT Dic");
                return false;
            }
        } else {
            edrtWordIDSetMap = new HashMap<String, Set<String>>();
            try {
                edrtTreeDataFile = new RandomAccessFile(DODDLEConstants.EDRT_HOME + TREE_DATA_FILE, "r");
                edrtWordDataFile = new RandomAccessFile(DODDLEConstants.EDRT_HOME + WORD_DATA_FILE, "r");
                edrtConceptDataFile = new RandomAccessFile(DODDLEConstants.EDRT_HOME + CONCEPT_DATA_FILE, "r");

                edrtTreeIndexFile = new RandomAccessFile(DODDLEConstants.EDRT_HOME + TREE_INDEX_FILE, "r");
                edrtWordIndexFile = new RandomAccessFile(DODDLEConstants.EDRT_HOME + WORD_INDEX_FILE, "r");
                edrtConceptIndexFile = new RandomAccessFile(DODDLEConstants.EDRT_HOME + CONCEPT_INDEX_FILE, "r");
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private static long getIndexFpListSize(boolean isSpecial) {
        RandomAccessFile indexFpListFile = null;
        if (isSpecial) {
            indexFpListFile = edrtWordIndexFile;
        } else {
            indexFpListFile = edrWordIndexFile;
        }
        try {
            return indexFpListFile.length() / 10;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return -1;
    }

    private static long getIndexFp(long fp, boolean isSpecial) {
        RandomAccessFile indexFpListFile = null;
        if (isSpecial) {
            indexFpListFile = edrtWordIndexFile;
        } else {
            indexFpListFile = edrWordIndexFile;
        }
        try {
            indexFpListFile.seek(fp);
            String fpStr = indexFpListFile.readLine();
            if (fpStr == null) { return -1; }
            return Long.valueOf(fpStr);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return -1;
    }

    private static long getIndexFileSize(RandomAccessFile indexFile) {
        try {
            return indexFile.length() / 10;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return -1;
    }

    private static long getConceptIndexFileSize(boolean isSpecial) {
        RandomAccessFile indexFile = null;
        if (isSpecial) {
            indexFile = edrtConceptIndexFile;
        } else {
            indexFile = edrConceptIndexFile;
        }
        return getIndexFileSize(indexFile);
    }

    private static long getTreeIndexFileSize(boolean isSpecial) {
        RandomAccessFile indexFile = null;
        if (isSpecial) {
            indexFile = edrtTreeIndexFile;
        } else {
            indexFile = edrTreeIndexFile;
        }
        return getIndexFileSize(indexFile);
    }

    private static long getRelationIndexFileSize() {
        return getIndexFileSize(edrRelationIndexFile);
    }

    private static long getDataFp(long fp, RandomAccessFile indexFile) {
        try {
            indexFile.seek(fp);
            return Long.valueOf(indexFile.readLine());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return -1;
    }

    private static long getConceptDataFp(long fp, boolean isSpecial) {
        RandomAccessFile indexFile = null;
        if (isSpecial) {
            indexFile = edrtConceptIndexFile;
        } else {
            indexFile = edrConceptIndexFile;
        }
        return getDataFp(fp, indexFile);
    }

    private static long getTreeDataFp(long fp, boolean isSpecial) {
        RandomAccessFile indexFile = null;
        if (isSpecial) {
            indexFile = edrtTreeIndexFile;
        } else {
            indexFile = edrTreeIndexFile;
        }
        return getDataFp(fp, indexFile);
    }

    private static long getRelationDataFp(long fp) {
        return getDataFp(fp, edrRelationIndexFile);
    }

    private static String getTermAndIndexFpSet(long ifp, boolean isSpecial) {
        RandomAccessFile indexFile = null;
        if (isSpecial) {
            indexFile = edrtWordDataFile;
        } else {
            indexFile = edrWordDataFile;
        }
        try {
            // System.out.println("ifp: " + ifp);
            indexFile.seek(ifp);
            return new String(indexFile.readLine().getBytes("ISO8859_1"), "UTF-8");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

    private static String getData(long dfp, RandomAccessFile dataFile, String encoding) {
        try {
            // System.out.println("dfp: " + dfp);
            dataFile.seek(dfp);
            return new String(dataFile.readLine().getBytes("ISO8859_1"), encoding);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

    private static String getConceptData(long dfp, boolean isSpecial) {
        RandomAccessFile dataFile = null;
        if (isSpecial) {
            dataFile = edrtConceptDataFile;
        } else {
            dataFile = edrConceptDataFile;
        }
        return getData(dfp, dataFile, "UTF-8");
    }

    private static String getTreeData(long dfp, boolean isSpecial) {
        RandomAccessFile dataFile = null;
        if (isSpecial) {
            dataFile = edrtTreeDataFile;
        } else {
            dataFile = edrTreeDataFile;
        }
        return getData(dfp, dataFile, "ISO8859_1");
    }

    private static String getRelationData(long dfp) {
        return getData(dfp, edrRelationDataFile, "ISO8859_1");
    }

    public static String getConceptData(boolean isSpecial, String id) {
        long low = 0;
        long conceptIndexFileSize = getConceptIndexFileSize(isSpecial);
        long high = conceptIndexFileSize;
        while (low <= high) {
            long mid = (low + high) / 2;
            if (conceptIndexFileSize - 1 <= mid) { return null; }
            long conceptDataFP = getConceptDataFp(mid * 10, isSpecial);
            if (conceptDataFP == -1) { return null; }
            // System.out.println("mid: " + mid);
            String conceptData = getConceptData(conceptDataFP, isSpecial);
            if (conceptData == null) { return null; }
            String[] lines = conceptData.split("\t");
            String searchedID = lines[0];
            // System.out.println(searchedID.compareTo(id));
            if (searchedID.compareTo(id) == 0) {
                // System.out.println(conceptData);
                return conceptData;
            } else if (0 < searchedID.compareTo(id)) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return null;
    }

    public static String getTreeData(boolean isSpecial, String id) {
        long low = 0;
        long treeIndexFileSize = getTreeIndexFileSize(isSpecial);
        long high = treeIndexFileSize;
        while (low <= high) {
            long mid = (low + high) / 2;
            // System.out.println("mid: " + mid);
            if (treeIndexFileSize - 1 <= mid) { return null; }
            long treeDataFP = getTreeDataFp(mid * 10, isSpecial);
            if (treeDataFP == -1) { return null; }
            String treeData = getTreeData(treeDataFP, isSpecial);
            if (treeData == null) { return null; }
            String[] lines = treeData.split("\t");
            String searchedID = lines[0];
            // System.out.println(searchedID.compareTo(id));
            if (searchedID.compareTo(id) == 0) {
                // System.out.println(conceptData);
                return treeData;
            } else if (0 < searchedID.compareTo(id)) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return null;
    }

    public static String getRelationData(String id) {
        long low = 0;
        long relationIndexFileSize = getRelationIndexFileSize();
        long high = relationIndexFileSize;

        while (low <= high) {
            long mid = (low + high) / 2;
            if (relationIndexFileSize - 1 <= mid) { return null; }
            long relationDataFP = getRelationDataFp(mid * 10);
            if (relationDataFP == -1) { return null; }
            String relationData = getRelationData(relationDataFP);
            if (relationData == null) { return null; }
            String[] lines = relationData.split("\t");
            String searchedID = lines[0];
            // System.out.println(searchedID.compareTo(id));
            if (searchedID.compareTo(id) == 0) {
                // System.out.println(conceptData);
                return relationData;
            } else if (0 < searchedID.compareTo(id)) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return null;
    }

    private static Concept getConcept(long dfp, boolean isSpecial) {
        RandomAccessFile dataFile = null;
        try {
            if (isSpecial) {
                dataFile = edrtConceptDataFile;
            } else {
                dataFile = edrConceptDataFile;
            }
            dataFile.seek(dfp);
            String data = new String(dataFile.readLine().getBytes("ISO8859_1"), "UTF-8");
            // System.out.println(data);
            String[] dataArray = data.split("\\^");
            String[] conceptData = new String[4];
            String id = dataArray[0].replaceAll("\t", "");
            System.arraycopy(dataArray, 1, conceptData, 0, conceptData.length);

            String uri = "";
            Concept c = null;
            if (isSpecial) {
                uri = DODDLEConstants.EDRT_URI + id;
                c = new Concept(uri, conceptData);
                edrtURIConceptMap.put(uri, c);
            } else {
                uri = DODDLEConstants.EDR_URI + id;
                c = new Concept(uri, conceptData);
                edrURIConceptMap.put(uri, c);
            }
            return c;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

    private static Set<Long> getdataFpSet(boolean isSpecial, long high, String term) {
        long low = 0;
        Set<Long> dataFpSet = new HashSet<Long>();
        while (low <= high) {
            long mid = (low + high) / 2;
            // System.out.println("mid: " + mid);
            long indexFP = getIndexFp(mid * 10, isSpecial);
            if (indexFP == -1) { return dataFpSet; }
            String line = getTermAndIndexFpSet(indexFP, isSpecial);
            String[] lines = line.split("\t");
            String searchedTerm = lines[0];
            // System.out.println(searchedTerm.compareTo(term));
            if (searchedTerm.compareTo(term) == 0) {
                for (int i = 1; i < lines.length; i++) {
                    dataFpSet.add(Long.valueOf(lines[i]));
                }
                // System.out.println(searchedTerm);
                return dataFpSet;
            } else if (0 < searchedTerm.compareTo(term)) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return dataFpSet;
    }

    public static Set<String> getIDSet(String word, boolean isSpecial) {
        DBManager dbManager = null;
        Map<String, Set<String>> wordIDSetMap = null;
        if (isSpecial) {
            wordIDSetMap = edrtWordIDSetMap;
            dbManager = edrtDBManager;
        } else {
            wordIDSetMap = edrWordIDSetMap;
            dbManager = edrDBManager;
        }

        if (DODDLEConstants.IS_USING_DB) {
            if (dbManager.getEDRIDSet(word) == null) { return null; }
            return dbManager.getEDRIDSet(word);
        }

        if (wordIDSetMap.get(word) != null) { return wordIDSetMap.get(word); }
        Set<Long> dataFpSet = getdataFpSet(isSpecial, getIndexFpListSize(isSpecial), word);
        // System.out.println(dataFpSet);
        Set<String> idSet = new HashSet<String>();
        for (Long dfp : dataFpSet) {
            // System.out.println(dfp);
            Concept c = getConcept(dfp, isSpecial);
            // System.out.println(c.getLocalName());
            idSet.add(c.getLocalName());
        }
        wordIDSetMap.put(word, idSet);

        return idSet;
    }

    public static Set<String> getEDRTIDSet(String word) {
        return getIDSet(word, true);
    }

    public static Set<String> getEDRIDSet(String word) {
        return getIDSet(word, false);
    }

    private static void addURISet(String data, String relation, Set<String> uriSet) {
        String[] idSet = data.split("\\|" + relation)[1].split("\t");
        for (int i = 0; i < idSet.length; i++) {
            String id = idSet[i];
            if (id.indexOf("|") != -1) {
                break;
            }
            if (!id.equals("")) {
                uriSet.add(DODDLEConstants.EDR_URI + id);
            }
        }
    }

    /**
     * 
     * 入力概念集合を入力として，その中から動詞的概念の集合を返す
     * 
     */
    public static Set<Concept> getVerbConceptSet(Set<Concept> inputConceptSet) {
        Set<Concept> verbConceptSet = new HashSet<Concept>();
        for (Concept c : inputConceptSet) {
            String id = c.getLocalName();
            String data = getRelationData(id);
            if (data != null && (data.indexOf("|agent") != -1 || data.indexOf("|object") != -1)) { // agentとobjectの場合のみを考慮
                verbConceptSet.add(c);
            }
        }
        return verbConceptSet;
    }

    /**
     * idを受け取り，そのIDの下位に存在するURIのセットを返す
     * 
     * @param id
     */
    public static Set<String> getSubURISet(String id) {
        Set<String> uriSet = new HashSet<String>();
        for (Set<String> subIDSet : EDRTree.getEDRTree().getSubURISet(id)) {
            uriSet.addAll(subIDSet);
        }
        return uriSet;
    }

    public static Set<String> getRelationValueSet(String relation, String vid, List<List<Concept>> trimmedConceptList) {
        Set<String> uriSet = new HashSet<String>();
        String data = getRelationData(vid);
        if (data != null) {
            if (data.indexOf("|" + relation) == -1) { return uriSet; }
            addURISet(data, relation, uriSet);
        }
        for (List<Concept> conceptList : trimmedConceptList) {
            for (Concept c : conceptList) {
                String tid = c.getLocalName();
                data = getRelationData(tid);
                if (data == null) {
                    continue;
                }
                if (data.indexOf("|" + relation) == -1) {
                    continue;
                }
                addURISet(data, relation, uriSet);
            }
        }
        return uriSet;
    }

    public static Concept getConcept(String id, boolean isSpecial) {
        String ns = "";
        DBManager dbManager = null;
        Map<String, Concept> uriConceptMap = null;
        if (isSpecial) {
            ns = DODDLEConstants.EDRT_URI;
            dbManager = edrtDBManager;
            uriConceptMap = edrtURIConceptMap;
        } else {
            ns = DODDLEConstants.EDR_URI;
            dbManager = edrDBManager;
            uriConceptMap = edrURIConceptMap;
        }

        if (DODDLEConstants.IS_USING_DB) {
            String uri = ns + id;
            if (uriConceptMap.get(uri) != null) { return uriConceptMap.get(uri); }
            if (dbManager == null) { return null; }
            dbManager.setEDRConcept(uri);
            Concept c = dbManager.getEDRConcept();
            uriConceptMap.put(uri, c);
            return c;
        }
        String uri = ns + id;
        if (uriConceptMap.get(uri) != null) { return uriConceptMap.get(uri); }
        String data = getConceptData(isSpecial, id);
        // System.out.println(data);
        String[] dataArray = data.split("\\^");
        String[] conceptData = new String[4];
        System.arraycopy(dataArray, 1, conceptData, 0, conceptData.length);

        Concept c = new Concept(uri, conceptData);
        uriConceptMap.put(uri, c);
        return c;
    }

    public static Concept getEDRTConcept(String id) {
        return getConcept(id, true);
    }

    public static Concept getEDRConcept(String id) {
        return getConcept(id, false);
    }

    private static void closeDB(DBManager dbManager, String msg) {
        if (dbManager != null) {
            try {
                // Always attempt to close the database cleanly.
                dbManager.close();
                DODDLE.getLogger().log(Level.INFO, "Close " + msg);
            } catch (Exception e) {
                System.err.println("Exception during database close:");
                e.printStackTrace();
            }
        }
    }

    public static void closeDB() {
        closeDB(edrDBManager, "EDR DB");
        closeDB(edrtDBManager, "EDRT DB");
    }

    public static DBManager getEDRDBManager() {
        return edrDBManager;
    }

    public static DBManager getEDRTDBManager() {
        return edrtDBManager;
    }
}
