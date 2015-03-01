/*
 * Project Name: DODDLE (a Domain Ontology rapiD DeveLopment Environment)
 * Project Website: http://doddle-owl.sourceforge.net/
 * 
 * Copyright (C) 2004-2008 Yamaguchi Laboratory, Keio University. All rights reserved. 
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

package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class EvalConcept implements Comparable {

    private Concept concept;
    private double evalValue;

    public EvalConcept(Concept c, double v) {
        concept = c;
        evalValue = v;
    }

    public void setEvalValue(double ev) {
        evalValue = ev;
    }

    public double getEvalValue() {
        return evalValue;
    }

    public Concept getConcept() {
        return concept;
    }

    public int compareTo(Object o) {
        double ev = ((EvalConcept) o).getEvalValue();
        EvalConcept c = (EvalConcept) o;
        if (evalValue < ev) {
            return 1;
        } else if (evalValue > ev) {
            return -1;
        } else {
            if (concept == null) {
                return 1;
            } else if (c == null) { return -1; }
            return concept.getURI().compareTo(c.getConcept().getURI());
        }
    }

    public String toString() {
        if (concept == null) { return Translator.getTerm("NotAvailableLabel"); }
        return "[" + String.format("%.3f", evalValue) + "]" + "[" + concept.getQName() + "]" + "[" + concept.getWord()
                + "]";
    }
}
