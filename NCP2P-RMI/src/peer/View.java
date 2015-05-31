package peer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class View {

	private Controller c;
	private JFrame frame;
	
	JButton btnSearch;
	private JTextField txtSearch;
	private DefaultTableModel dftSearch;
	JTable tblSearch;
	
	private JTable tblTasks;

	public View() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			initialize();
			frame.setVisible(true);
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}
	
	public void setController(Controller c){
		this.c=c;
		btnSearch.addActionListener(c);
		tblSearch.addMouseListener(c);
		tblTasks.addMouseListener(c);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 633, 388);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblPeerName = new JLabel("Peer Name :");
		lblPeerName.setBounds(10, 11, 98, 14);
		frame.getContentPane().add(lblPeerName);
		
		JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
		tabs.setBounds(10, 46, 589, 303);
		frame.getContentPane().add(tabs);
		
		JPanel pnlSearch = new JPanel();
		tabs.addTab("Search", null, pnlSearch, null);
		pnlSearch.setLayout(null);
		
		JLabel lblSearch = new JLabel("Search Query :");
		lblSearch.setBounds(12, 11, 97, 14);
		pnlSearch.add(lblSearch);
		
		txtSearch=new JTextField();
		txtSearch.setBounds(102, 7, 349, 23);
		pnlSearch.add(txtSearch);
			
		btnSearch = new JButton("Search");
		btnSearch.setBounds(463, 7, 91, 23);
		pnlSearch.add(btnSearch);
		
		JScrollPane scrSearch = new JScrollPane();
		scrSearch.setBounds(29, 56, 525, 207);
		pnlSearch.add(scrSearch);
		
		dftSearch = new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			@Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
			@Override
		     public Class<?> getColumnClass(int column) {
		        if (getRowCount() > 0) {
		           Object value = getValueAt(0, column);
		           if (value != null) {
		              return getValueAt(0, column).getClass(); 
		           }
		        }

		        return super.getColumnClass(column);
		     }
        };
		dftSearch.addColumn("File Name");
		dftSearch.addColumn("Tags");
		dftSearch.addColumn("Size(KB)");
		dftSearch.addColumn("Seeders");
		dftSearch.addColumn("MD5");
       
		tblSearch = new JTable(dftSearch);
		tblSearch.setShowVerticalLines(false);
		scrSearch.setViewportView(tblSearch);
		tblSearch.setRowHeight(30);
		TableColumn tc=tblSearch.getColumnModel().getColumn(2);
		tc.setMinWidth(50);
		tc.setMaxWidth(50);
		tc.setPreferredWidth(50);
		
		JPanel pnlTasks = new JPanel();
		tabs.addTab("Tasks", null, pnlTasks, null);
		pnlTasks.setLayout(null);
		
		JScrollPane scrTasks = new JScrollPane();
		scrTasks.setBounds(10, 11, 564, 252);
		pnlTasks.add(scrTasks);
		
		tblTasks = new JTable();
		scrTasks.setViewportView(tblTasks);
		
		JPanel pnlSetting = new JPanel();
		tabs.addTab("Settings", null, pnlSetting, null);
		pnlSetting.setLayout(null);
		
		JPanel pnlLogs = new JPanel();
		tabs.addTab("Logs", null, pnlLogs, null);
		
		JPanel pnlAbout = new JPanel();
		pnlAbout.setToolTipText("");
		tabs.addTab("About", null, pnlAbout, null);
		pnlAbout.setLayout(null);
	}

	public String getSearchText() {
		return txtSearch.getText();
	}

	public void setSearchResult(Object[][] data) {
		dftSearch.setRowCount(0);
		
		for(int i=0;i<data.length;i++){
			dftSearch.addRow(data[i]);
		}
		//ImageIcon ic=new ImageIcon("d:\\nilesh\\temp\\download.png");
		//dftSearch.setValueAt(ic, 0, 0);
	}

	public void showMessage(String string) {
		JOptionPane.showMessageDialog(frame, string,  "Result", JOptionPane.WARNING_MESSAGE);
	}

	public String getInputString(String string) {
		return JOptionPane.showInputDialog(string);
	}
}