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
    
    @Override
    public boolean equals(Object obj) {
        NonTaxonomicRelation nonTaxRel = (NonTaxonomicRelation) obj;
        return nonTaxRel.getDomain().equals(domain) && nonTaxRel.getRange().equals(range);
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public int hashCode() {
        return 0; // 常に同じ値を返す
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
        return new Object[] {new Boolean(isMetaProperty), domain, relation, range};
    }
    
    public Object[] getWrongTableData() {
        return new Object[] {domain, range};
    }
    
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(isMetaProperty);
        builder.append("\t");
        builder.append(domain);
        builder.append("\t");
        builder.append(relation);
        builder.append("\t");
        builder.append(range);
        return builder.toString();
    }
}
