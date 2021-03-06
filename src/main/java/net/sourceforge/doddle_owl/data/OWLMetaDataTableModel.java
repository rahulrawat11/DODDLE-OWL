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

import javax.swing.table.*;

import net.sourceforge.doddle_owl.ui.*;
import net.sourceforge.doddle_owl.utils.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author Takeshi Morita
 */
public class OWLMetaDataTableModel extends DefaultTableModel {

    private NameSpaceTable nsTable;
    
    public OWLMetaDataTableModel(NameSpaceTable nstbl, Object[] columnNames, int rowCount) {
        super(columnNames, rowCount);
        nsTable = nstbl;
    }

    public void addRow(Object[] rowData) {
        super.addRow(rowData);

    }

    public void refreshTableModel() {
        for (int i = 0; i < getRowCount(); i++) {
            setValueAt(getValueAt(i, 0), i, 0);
            setValueAt(getValueAt(i, 1), i, 1);
        }
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Object newValue = null;
        String resourceStr = "";
        Resource resource = null;
        if (aValue instanceof Resource) {
            resource = (Resource) aValue;
            resourceStr = resource.getURI();
        } else {
            resourceStr = aValue.toString();
        }
        if (getColumnName(columnIndex).equals("Property") && resourceStr.indexOf("http:") != -1) {
            if (resource == null) {
                resource = ResourceFactory.createResource(resourceStr);
            }
            String prefix = nsTable.getPrefix(Utils.getNameSpace(resource));
            if (prefix != null) {
                String localName = Utils.getLocalName(resource);
                newValue = prefix + ":" + localName;
            } else {
                newValue = resource.getURI();
            }
        } else {
            newValue = aValue;
        }
        super.setValueAt(newValue, rowIndex, columnIndex);
    }
}
