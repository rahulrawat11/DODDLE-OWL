/*
 * @(#)  2007/01/26
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.data;

/**
 * @author takeshi morita
 */
public class NonTaxonomicRelation {
    private String domain;
    private String range;
    private Concept relation;
    private boolean isMetaProperty;

    public NonTaxonomicRelation(String d, String r) {
        domain = d;
        range = r;
    }

    public NonTaxonomicRelation(String d, Concept rel, String r) {
        domain = d;
        range = r;
        relation = rel;
    }

    public boolean isValid() {
        return !(domain.equals("") || range.equals(""));
    }

    public boolean isSameRelation(Concept rel) {
        if (rel == null || relation == null) { return false; }
        return rel.equals(relation);
    }

    @Override
    public boolean equals(Object obj) {
        NonTaxonomicRelation nonTaxRel = (NonTaxonomicRelation) obj;
        return nonTaxRel.getDomain().equals(domain) && nonTaxRel.getRange().equals(range)
                && isSameRelation(nonTaxRel.getRelation());
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public int hashCode() {
        int hashCode = domain.hashCode() + range.hashCode();
        if (relation != null) {
            hashCode += relation.hashCode();
        }
        return hashCode;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public Concept getRelation() {
        return relation;
    }

    public void setRelation(Concept relation) {
        this.relation = relation;
    }

    public boolean isMetaProperty() {
        return isMetaProperty;
    }

    public void setMetaProperty(boolean isMetaProperty) {
        this.isMetaProperty = isMetaProperty;
    }

    public Object[] getAcceptedTableData() {
        return new Object[] { new Boolean(isMetaProperty), domain, relation, range};
    }

    public Object[] getWrongTableData() {
        return new Object[] { domain, range};
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(isMetaProperty);
        builder.append("\t");
        builder.append(domain);
        builder.append("\t");
        if (relation != null) {
            builder.append(relation.getURI());
            builder.append("\t");
        }
        builder.append(range);
        return builder.toString();
    }
}
