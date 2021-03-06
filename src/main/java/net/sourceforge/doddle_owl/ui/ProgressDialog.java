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

package net.sourceforge.doddle_owl.ui;

import java.awt.*;

import javax.swing.*;

import net.sourceforge.doddle_owl.*;

/**
 * @author Takeshi Morita
 */
public class ProgressDialog extends JDialog {

    private int division;
    private int maxValue;
    private int currentValue;
    private int progressCountSize;
    private JTextField messageField;
    private JProgressBar progressBar;

    public ProgressDialog(String title, int max) {
        super(DODDLE_OWL.rootFrame, title, false);
        messageField = new JTextField(50);
        progressBar = new JProgressBar();
        initProgressBar(max);
        getContentPane().add(messageField, BorderLayout.CENTER);
        getContentPane().add(progressBar, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(DODDLE_OWL.rootFrame);
        setVisible(true);
    }

    private void initProgressBar(int max) {
        currentValue = 0;
        maxValue = max;
        progressCountSize = 50;
        progressBar.setIndeterminate(false);
        progressBar.setMinimum(0);
        if (maxValue < progressCountSize) {
            progressBar.setMaximum(maxValue);
        } else {
            division = maxValue / progressCountSize;
            progressBar.setMaximum(progressCountSize);
        }
        progressBar.setValue(0);
    }

    public void setMessage(String msg) {
        currentValue++;
        if (maxValue < progressCountSize) {
            progressBar.setValue(currentValue);
        } else if (currentValue % division == 0) {
            progressBar.setValue(currentValue);
        }
        messageField.setText(msg);
    }
}
