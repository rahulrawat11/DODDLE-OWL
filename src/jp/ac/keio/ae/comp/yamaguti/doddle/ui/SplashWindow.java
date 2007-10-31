/*
 * @(#)  2005/09/08
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;
import java.net.*;

import javax.swing.*;
import javax.swing.event.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class SplashWindow extends JWindow implements HyperlinkListener {

    public SplashWindow(Frame rootFrame) {
        ImageIcon logo = Utils.getImageIcon("doddle_splash.png");
        JLabel logoLabel = new JLabel(logo);
        JEditorPane editor = new JEditorPane("text/html", "");
        editor.addHyperlinkListener(this);
        editor.setEditable(false);
        StringBuilder builder = new StringBuilder();
        builder.append("<font face=TimesNewRoman>");
        builder.append("Beta Version: " + DODDLEConstants.VERSION + "<br>");
        builder.append("Copyright (C) 2004-2007 DODDLE Project<br>");
        builder
                .append("<a href=http://www.yamaguchi.comp.ae.keio.ac.jp/mmm/doddle/>http://www.yamaguchi.comp.ae.keio.ac.jp/mmm/doddle/</a>");
        builder.append("</font>");
        editor.setText(builder.toString());

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.black));
        panel.add(logoLabel, BorderLayout.CENTER);
        panel.add(editor, BorderLayout.SOUTH);
        getContentPane().add(panel);
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                dispose();
            }
        });
        pack();
        setLocationRelativeTo(rootFrame);
        setVisible(true);
    }

    public void hyperlinkUpdate(HyperlinkEvent ae) {
        try {
            if (ae.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                Desktop.getDesktop().browse(new URI("http://www.yamaguchi.comp.ae.keio.ac.jp/mmm/doddle/"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new SplashWindow(null);
    }
}
