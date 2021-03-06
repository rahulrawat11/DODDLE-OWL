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
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.Map.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import net.sourceforge.doddle_owl.*;
import net.sourceforge.doddle_owl.data.*;
import net.sourceforge.doddle_owl.utils.*;

/**
 * @author Takeshi Morita
 */
public class TermInfoTablePanel extends JPanel implements ActionListener, KeyListener,
		ListSelectionListener {

	private int docNum;
	private Map<String, TermInfo> termInfoMap;

	private JTextField searchTermField;
	private JTextField searchPOSField;
	private JEditorPane docArea;
	private JTable termInfoTable;
	private TableRowSorter<TableModel> rowSorter;
	private TermInfoTableModel termInfoTableModel;

	private boolean isDeletingTableItems;

	public TermInfoTablePanel() {
		searchTermField = new JTextField(20);
		searchTermField.addActionListener(this);
		searchTermField.addKeyListener(this);
		searchTermField.setBorder(BorderFactory.createTitledBorder(Translator
				.getTerm("TermFilterTextField")));
		searchPOSField = new JTextField(20);
		searchPOSField.addActionListener(this);
		searchPOSField.addKeyListener(this);
		searchPOSField.setBorder(BorderFactory.createTitledBorder(Translator
				.getTerm("POSFilterTextField")));
		JPanel searchPanel = new JPanel();
		searchPanel.add(searchTermField);
		searchPanel.add(searchPOSField);

		termInfoTable = new JTable();
		termInfoTable.getSelectionModel().addListSelectionListener(this);
		JScrollPane termInfoTableScroll = new JScrollPane(termInfoTable);
		setTermInfoTableModel(null, 0);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(searchPanel, BorderLayout.NORTH);
		mainPanel.add(termInfoTableScroll, BorderLayout.CENTER);

		docArea = new JEditorPane("text/html", "");
		docArea.setEditable(false);
		JScrollPane docAreaScroll = new JScrollPane(docArea);
		docAreaScroll.setBorder(BorderFactory.createTitledBorder(Translator
				.getTerm("InputDocumentArea")));

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mainPanel, docAreaScroll);
		splitPane.setDividerLocation(300);
		splitPane.setDividerSize(10);
		splitPane.setOneTouchExpandable(true);

		setLayout(new BorderLayout());
		add(splitPane, BorderLayout.CENTER);
	}

	private int getColumnNamePosition(JTable table, String columnName) {
		for (int i = 0; i < table.getColumnCount(); i++) {
			if (table.getColumnName(i).equals(columnName)) {
				return i;
			}
		}
		return 0;
	}

	public void setIsDeletingTableItems(boolean t) {
		isDeletingTableItems = t;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (isDeletingTableItems) {
			return;
		}
		int row = termInfoTable.getSelectedRow();
		if (0 <= row) {
			String term = (String) termInfoTable.getValueAt(row,
					getColumnNamePosition(termInfoTable, Translator.getTerm("TermLabel")));
			String targetLines = DODDLE_OWL.getCurrentProject().getDocumentSelectionPanel()
					.getTargetHtmlLines(term);
			docArea.setText(targetLines);
		}
	}

	public TableModel getTableModel() {
		return termInfoTable.getModel();
	}

	public JTable getTable() {
		return termInfoTable;
	}

	public int getTableSize() {
		return termInfoMap.size();
	}

	public TermInfo getTermInfo(String term) {
		return termInfoMap.get(term);
	}

	public void addTermInfoMapKey(String addTerm, TermInfo info) {
		termInfoMap.put(addTerm, info);
	}

	public void removeTermInfoMapKey(String deleteTerm) {
		termInfoMap.remove(deleteTerm);
	}

	public JTable getTermInfoTable() {
		return termInfoTable;
	}

	public Collection<TermInfo> getTermInfoSet() {
		return termInfoMap.values();
	}

	public void setTermInfoTableModel(Map<String, TermInfo> wiMap, int dn) {
		docNum = dn;
		String TERM = Translator.getTerm("TermLabel");
		String POS = Translator.getTerm("POSLabel");
		String TF = Translator.getTerm("TFLabel");
		String IDF = Translator.getTerm("IDFLabel");
		String TFIDF = Translator.getTerm("TFIDFLabel");
		// String INPUT_DOCUMENT = Translator.getTerm("InputDocumentLabel");
		String UPPER_CONCEPT = Translator.getTerm("UpperConceptLabel");
		// Object[] titles = new Object[] { TERM, POS, TF, IDF, TFIDF,
		// INPUT_DOCUMENT, UPPER_CONCEPT};
		Object[] titles = new Object[] { TERM, POS, TF, IDF, TFIDF, UPPER_CONCEPT };

		termInfoTableModel = new TermInfoTableModel(null, titles);
		termInfoTableModel.getColumnClass(0);
		rowSorter = new TableRowSorter<TableModel>(termInfoTableModel);
		rowSorter.setMaxSortKeys(5);

		termInfoTable.setRowSorter(rowSorter);
		termInfoTable.setModel(termInfoTableModel);
		termInfoTable.getTableHeader().setToolTipText("sorted by column");
		termInfoMap = wiMap;
		if (termInfoMap == null) {
			return;
		}
		Collection<TermInfo> termInfoSet = termInfoMap.values();
		for (TermInfo info : termInfoSet) {
			Vector rowData = info.getRowData();
			termInfoTableModel.addRow(rowData);
		}
	}

	public void loadTermInfoTable(File loadFile) {
		termInfoMap = new HashMap<String, TermInfo>();
		if (!loadFile.exists()) {
			return;
		}
		BufferedReader reader = null;
		try {
			FileInputStream fis = new FileInputStream(loadFile);
			reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
			String line = reader.readLine();
			docNum = new Integer(line.split("=")[1]).intValue();
			while ((line = reader.readLine()) != null) {
				String[] items = line.split("\t");
				String term = items[0];
				TermInfo info = new TermInfo(term, docNum);
				String[] posSet = items[1].split(":");
				for (int i = 0; i < posSet.length; i++) {
					info.addPos(posSet[i]);
				}
				try {
					// 以下，docsを処理する場合には，5, 6, 7を一つずつインクリメントする必要あり
					if (5 < items.length) {
						String[] inputDocSet = items[5].split(":");
						for (int i = 0; i < inputDocSet.length; i++) {
							String inputDoc = inputDocSet[i].split("=")[0];
							Integer num = new Integer(inputDocSet[i].split("=")[1]);
							info.putInputDoc(new File(inputDoc), num);
						}
					}
					if (items.length == 7) {
						String[] upperConceptSet = items[6].split(":");
						for (int i = 0; i < upperConceptSet.length; i++) {
							info.addUpperConcept(upperConceptSet[i]);
						}
					}
				} catch (ArrayIndexOutOfBoundsException aiobe) {
					// 無視
				}
				termInfoMap.put(term, info);
			}
		} catch (IOException ioex) {
			ioex.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException ioe2) {
				ioe2.printStackTrace();
			}
		}
		setTermInfoTableModel(termInfoMap, docNum);
	}

	public void loadTermInfoTable(int projectID, Statement stmt, String termTable, int docNum) {
		String posTable = termTable + "_pos_list";
		String docTable = termTable + "_doc_list";
		termInfoMap = new HashMap<String, TermInfo>();
		Map<Integer, TermInfo> posListIDTermInfoMap = new HashMap<Integer, TermInfo>();
		Map<Integer, TermInfo> docListIDTermInfoMap = new HashMap<Integer, TermInfo>();
		try {
			String sql = "SELECT * from " + termTable + " where Project_ID=" + projectID;
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String term = URLDecoder.decode(rs.getString("Term"), "UTF8");
				int posListID = rs.getInt("POS_List_ID");
				int tf = rs.getInt("TF");
				double idf = rs.getDouble("IDF");
				double tfidf = rs.getDouble("TF_IDF");
				int docListID = rs.getInt("DOC_List_ID");
				this.docNum = docNum;
				TermInfo info = new TermInfo(term, docNum);

				posListIDTermInfoMap.put(posListID, info);
				docListIDTermInfoMap.put(docListID, info);

				termInfoMap.put(term, info);
			}

			for (Entry<Integer, TermInfo> entry : posListIDTermInfoMap.entrySet()) {
				int posListID = entry.getKey();
				TermInfo info = entry.getValue();
				sql = "SELECT * from " + posTable + " where Project_ID=" + projectID
						+ " and POS_List_ID=" + posListID;
				ResultSet rs1 = stmt.executeQuery(sql);
				while (rs1.next()) {
					String pos = rs1.getString("POS");
					info.addPos(pos);
				}
			}

			for (Entry<Integer, TermInfo> entry : posListIDTermInfoMap.entrySet()) {
				int docListID = entry.getKey();
				TermInfo info = entry.getValue();
				sql = "SELECT * from " + docTable + " where Project_ID=" + projectID
						+ " and Doc_List_ID=" + docListID;
				ResultSet rs2 = stmt.executeQuery(sql);
				while (rs2.next()) {
					String doc = URLDecoder.decode(rs2.getString("Doc"), "UTF8");
					int docTF = rs2.getInt("TF");
					info.putInputDoc(new File(doc), docTF);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		setTermInfoTableModel(termInfoMap, docNum);
	}

	public void saveTermInfoTable(File saveFile) {
		if (termInfoMap == null) {
			return;
		}
		BufferedWriter writer = null;
		try {
			FileOutputStream fos = new FileOutputStream(saveFile);
			writer = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
			writer.write("docNum=" + docNum + "\n");
			for (TermInfo info : termInfoMap.values()) {
				writer.write(info.toString() + "\n");
			}
		} catch (IOException ioex) {
			ioex.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException ioe2) {
					ioe2.printStackTrace();
				}
			}
		}
	}

	public void saveTermInfoTable(int projectID, Statement stmt, String termTable) {
		String posTable = termTable + "_pos_list";
		String docTable = termTable + "_doc_list";
		if (termInfoMap == null) {
			return;
		}
		try {
			int posAndDocListID = 1;
			for (TermInfo info : termInfoMap.values()) {
				String term = URLEncoder.encode(info.getTerm(), "UTF8");
				Set<String> posSet = info.getPosSet();
				int tf = info.getTF();
				double idf = info.getIDF();
				double tfidf = info.getTFIDF();
				Set<File> docSet = info.getInputDocumentSet();

				String sql = "INSERT INTO " + termTable
						+ " (Project_ID,Term,POS_List_ID,TF,IDF,TF_IDF,Doc_List_ID) " + "VALUES("
						+ projectID + ",'" + term + "'," + posAndDocListID + "," + tf + "," + idf
						+ "," + tfidf + "," + posAndDocListID + ")";
				stmt.executeUpdate(sql);
				for (String pos : posSet) {
					sql = "INSERT INTO " + posTable + " (Project_ID,POS_List_ID,POS) VALUES("
							+ projectID + "," + posAndDocListID + ",'" + pos + "')";
					stmt.executeUpdate(sql);
				}
				for (File doc : docSet) {
					int inputDocTF = info.getInputDocumentTF(doc);
					String docPath = URLEncoder.encode(doc.getAbsolutePath(), "UTF8");
					sql = "INSERT INTO " + docTable + " (Project_ID,Doc_List_ID,Doc,TF) VALUES("
							+ projectID + "," + posAndDocListID + ",'" + docPath + "',"
							+ inputDocTF + ")";
					stmt.executeUpdate(sql);
				}
				posAndDocListID++;
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	class TermInfoTableModel extends DefaultTableModel {

		TermInfoTableModel(Object[][] data, Object[] columnNames) {
			super(data, columnNames);
		}

		public Class<?> getColumnClass(int columnIndex) {
			String columnName = getColumnName(columnIndex);
			if (columnName.equals("TF")) {
				return Integer.class;
			} else if (columnName.equals("IDF") || columnName.equals("TF-IDF")) {
				return Double.class;
			} else {
				return String.class;
			}
		}
	}

	public void loadTermInfoTable() {
		JFileChooser chooser = new JFileChooser(".");
		int retval = chooser.showOpenDialog(DODDLE_OWL.rootPane);
		if (retval == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			loadTermInfoTable(file);
		}
	}

	public void saveTermInfoTable() {
		JFileChooser chooser = new JFileChooser(".");
		int retval = chooser.showSaveDialog(DODDLE_OWL.rootPane);
		if (retval == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			saveTermInfoTable(file);
		}
	}

	public void actionPerformed(ActionEvent e) {
		searchTermOrPOS(e);
	}

	/**
	 * @param e
	 */
	private void searchTermOrPOS(EventObject e) {
		if (e.getSource() == searchTermField || e.getSource() == searchPOSField) {
			try {
				if (searchTermField.getText().length() == 0
						&& searchPOSField.getText().length() == 0) {
					rowSorter.setRowFilter(RowFilter.regexFilter(".*", new int[] { 0 }));
				}
				if (searchTermField.getText().length() != 0) {
					rowSorter.setRowFilter(RowFilter.regexFilter(searchTermField.getText(),
							new int[] { 0 }));
				}
				if (searchPOSField.getText().length() != 0) {
					rowSorter.setRowFilter(RowFilter.regexFilter(searchPOSField.getText(),
							new int[] { 1 }));
				}
			} catch (PatternSyntaxException pse) {
				JOptionPane.showMessageDialog(this, pse.getMessage(), "PatternSyntaxException",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void keyPressed(KeyEvent e) {
		searchTermOrPOS(e);
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}
}
