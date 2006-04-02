/*
 * @(#)  2006/04/02
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class DODDLEDic {

    public static Concept getConcept(String id) {
        String[] identity = id.split(":");
        if (identity.length == 2) {
            if (identity[0].equals("edr")) {
                return EDRDic.getEDRConcept(identity[1]);
            } else if (identity[0].equals("edrt")) {
                return EDRDic.getEDRTConcept(identity[1]);
            } else if (identity[0].equals("wn")) { return WordNetDic.getWNConcept(identity[1]); }
        } else {
            return EDRDic.getEDRConcept(id);
        }
        return null;
    }
}
