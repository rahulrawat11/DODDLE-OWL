/*
 * @(#)  2007/11/02
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.data;

/**
 * @author takeshi morita
 */
public class WrongPair {
    private String domain;
    private String range;

    public WrongPair(String d, String r) {
        domain = d;
        range = r;
    }

    @Override
    public boolean equals(Object obj) {
        WrongPair wp = (WrongPair) obj;
        return wp.getDomain().equals(domain) && wp.getRange().equals(range);
    }

    @Override
    public int hashCode() {
        return domain.hashCode() + range.hashCode();
    }

    public String getDomain() {
        return domain;
    }

    public String getRange() {
        return range;
    }
}
