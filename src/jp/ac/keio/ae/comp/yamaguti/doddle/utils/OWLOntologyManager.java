/*
 * @(#)  2006/12/16
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.utils;

import java.util.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;

/**
 * @author takeshi morita
 */
public class OWLOntologyManager {

    private static Map<String, ReferenceOWLOntology> refOntMap = new HashMap<String, ReferenceOWLOntology>();

    public static void addRefOntology(String uri, ReferenceOWLOntology ontInfo) {
        refOntMap.put(uri, ontInfo);
    }

    public static void removeRefOntology(String uri) {
        refOntMap.remove(uri);
    }

    public static Collection<ReferenceOWLOntology> getRefOntologySet() {
        return refOntMap.values();
    }

    public static Set<List<Concept>> getPathToRootSet(String uri) {
        Set<List<Concept>> pathToRootSet = new HashSet<List<Concept>>();
        for (ReferenceOWLOntology refOnt : getRefOntologySet()) {
            Set<List<Concept>> set = refOnt.getPathToRootSet(uri);
            if (set != null) {
                pathToRootSet.addAll(set);
            }
        }
        return pathToRootSet;
    }

    public static void setOWLConceptSet(String word, Set<Concept> conceptSet) {
        for (ReferenceOWLOntology refOnt : getRefOntologySet()) {
            Set<String> uriSet = refOnt.getURISet(word);
            if (uriSet == null) {
                continue;
            }
            for (String uri : uriSet) {
                conceptSet.add(refOnt.getConcept(uri));
            }
        }
    }
}
