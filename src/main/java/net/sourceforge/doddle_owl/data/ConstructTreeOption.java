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

import net.sourceforge.doddle_owl.ui.*;

/**
 * @author Takeshi Morita
 */
public class ConstructTreeOption {

    private Concept c;
    private String option;
    private boolean isReplaceSubConcepts;

    public ConstructTreeOption(Concept c) {
        this.c = c;
        if (OptionDialog.isCompoundWordSetSameConcept()) {
            option = "SAME";
        } else {
            option = "SUB";
        }
    }

    public ConstructTreeOption(Concept c, String opt) {
        this.c = c;
        option = opt;
    }

    public void setConcept(Concept c) {
        this.c = c;
    }

    public Concept getConcept() {
        return c;
    }

    public void setOption(String opt) {
        option = opt;
    }

    public String getOption() {
        return option;
    }

    public boolean isReplaceSubConcepts() {
        return isReplaceSubConcepts;
    }

    public void setIsReplaceSubConcepts(boolean t) {
        isReplaceSubConcepts = t;
    }
    
    public String toString() {
        return "option: "+option+" concept: "+c+" is Replace Sub Concept: "+isReplaceSubConcepts;
    }
}
