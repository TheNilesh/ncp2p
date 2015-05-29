package com;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

public class MainForm extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;  //  @jve:decl-index=0:visual-constraint="23,38"
	private JScrollPane m_scrollPane = null;
	private JTable m_table = null;
	private Hashtable<String, String> m_hash = null;
	private JButton m_btnAdd = null;
	File f=new File("C:\\abc.txt");

	/**
	 * This is the default constructor
	 */
	public MainForm() {
		super();
		initialize();
		System.out.println(f.length());
		m_hash = new Hashtable<String, String>();
		m_hash.put("Dog", "Bow");
	}

	private void onButtonPressed() {
		m_hash.put("Cow", "Moo");
		m_table.revalidate();
		System.out.println("NOW:" + f.length());
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(409, 290);
		this.setTitle("JFrame");
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.setSize(new Dimension(500, 500));
			jContentPane.setPreferredSize(new Dimension(500, 500));
			jContentPane.add(getM_scrollPane(), BorderLayout.NORTH);
			jContentPane.add(getM_btnAdd(), BorderLayout.SOUTH);
		}
		return jContentPane;
	}

	/**
	 * This method initializes m_scrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getM_scrollPane() {
		if (m_scrollPane == null) {
			m_scrollPane = new JScrollPane();
			m_scrollPane.setViewportView(getM_table());
		}
		return m_scrollPane;
	}

	/**
	 * This method initializes m_table	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getM_table() {
		if (m_table == null) {
			m_table = new JTable();
			m_table.setModel(new AbstractTableModel(){
	private static final long serialVersionUID = 1L;

	public int getColumnCount() {
		return 2;
	}

	public int getRowCount() {
		return m_hash.size();
	}

	public String getColumnName(int column) {
		if (column == 0) {
			return "Animal";
		} else {
			return "Sound";
		}
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return getKey(rowIndex);
		} else {
			return m_hash.get(getKey(rowIndex));
		} // if-else

	}

	private String getKey(int a_index) {
		String retval = "";
		Enumeration<String> e = m_hash.keys();
		for (int i = 0; i < a_index + 1; i++) {
			retval = e.nextElement();
		} // for

		return retval;
	}

			});
		}
		return m_table;
	}

	/**
	 * This method initializes m_btnAdd	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getM_btnAdd() {
		if (m_btnAdd == null) {
			m_btnAdd = new JButton();
			m_btnAdd.setPreferredSize(new Dimension(34, 30));
			m_btnAdd.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					onButtonPressed();
				}
			});
		}
		return m_btnAdd;
	}

	public static void main(String[] args) {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MainForm frame = new MainForm();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setSize(500, 500);
				frame.setVisible(true);
			}
		});
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"