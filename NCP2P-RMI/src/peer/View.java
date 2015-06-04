package peer;

import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class View {

	@SuppressWarnings("unused")
	private Controller c;
	private JFrame frmPeerToPeer;
	
	private JLabel lblPeerName;
	private JLabel lblIp;
	private JLabel lblDmStatus;
	JTextField txtPeerName;
	JTextField txtShare;
	private JTextField txtSuperPeer;
	private JLabel lblStatus;
	
	
	JButton btnSearch;
	private JTextField txtSearch;
	private DefaultTableModel dftSearch;
	private JTable tblSearch;
	private DefaultTableModel dftTasks;
	private JTable tblTasks;
	JButton btnSave;

	public View() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			initialize();
			frmPeerToPeer.setVisible(true);
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}
	
	public void setController(Controller c){
		this.c=c;
		btnSearch.addActionListener(c);
		tblSearch.addMouseListener(c);
		//tblTasks.addMouseListener(c);
		btnSave.addActionListener(c);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmPeerToPeer = new JFrame();
		frmPeerToPeer.setIconImage(Toolkit.getDefaultToolkit().getImage(View.class.getResource("/res/p2p3d.png")));
		frmPeerToPeer.setTitle("Peer to Peer File Sharing");
		frmPeerToPeer.setBounds(100, 100, 651, 508);
		frmPeerToPeer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmPeerToPeer.getContentPane().setLayout(null);
		
		lblPeerName= new JLabel("Peer Name :");
		lblPeerName.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblPeerName.setBounds(125, 11, 309, 24);
		frmPeerToPeer.getContentPane().add(lblPeerName);
		
		JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
		tabs.setBounds(30, 87, 589, 379);
		frmPeerToPeer.getContentPane().add(tabs);
		
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
		scrSearch.setBounds(29, 56, 525, 282);
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
		//tblSearch.setShowVerticalLines(false);
		tblSearch.setShowGrid(false);
		scrSearch.setViewportView(tblSearch);
		tblSearch.setRowHeight(25);
		TableColumn tc=tblSearch.getColumnModel().getColumn(2);
		tc.setMinWidth(50);
		tc.setMaxWidth(50);
		tc.setPreferredWidth(50);
		
		JPanel pnlTasks = new JPanel();
		tabs.addTab("Tasks", null, pnlTasks, null);
		pnlTasks.setLayout(null);
		
		JScrollPane scrTasks = new JScrollPane();
		scrTasks.setBounds(10, 35, 564, 331);
		pnlTasks.add(scrTasks);
		
		dftTasks = new DefaultTableModel(){
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
		dftTasks.addColumn("TYPE");
		dftTasks.addColumn("File Name");
		dftTasks.addColumn("Progress");
		dftTasks.addColumn("sessionID");
		
		tblTasks = new JTable(dftTasks);
		scrTasks.setViewportView(tblTasks);
		
		tblTasks.setShowGrid(false);
		tblTasks.setRowHeight(25);
		TableColumn tc2=tblTasks.getColumnModel().getColumn(0);
		tc2.setMinWidth(50);
		tc2.setMaxWidth(50);
		tc2.setPreferredWidth(50);
		
		JLabel lblBackgroundTaskLike = new JLabel("Background task, like Download, Upload will be shown here.");
		lblBackgroundTaskLike.setBounds(10, 11, 564, 14);
		pnlTasks.add(lblBackgroundTaskLike);
		
		JPanel pnlSetting = new JPanel();
		tabs.addTab("Settings", null, pnlSetting, null);
		pnlSetting.setLayout(null);
		
		JLabel lblPeerName_1 = new JLabel("Peer Name:");
		lblPeerName_1.setBounds(104, 40, 114, 16);
		pnlSetting.add(lblPeerName_1);
		
		txtPeerName = new JTextField();
		txtPeerName.setBounds(220, 38, 202, 20);
		pnlSetting.add(txtPeerName);
		txtPeerName.setColumns(10);
		
		JLabel lblSharedDirectory = new JLabel("Shared Directory:");
		lblSharedDirectory.setBounds(104, 68, 114, 14);
		pnlSetting.add(lblSharedDirectory);
		
		txtShare = new JTextField();
		txtShare.setBounds(220, 70, 202, 20);
		pnlSetting.add(txtShare);
		txtShare.setColumns(10);
		
		JLabel lblSuperpeer = new JLabel("SuperPeer:");
		lblSuperpeer.setBounds(104, 104, 114, 14);
		pnlSetting.add(lblSuperpeer);
		
		txtSuperPeer = new JTextField();
		txtSuperPeer.setEditable(false);
		txtSuperPeer.setBounds(220, 101, 202, 20);
		pnlSetting.add(txtSuperPeer);
		txtSuperPeer.setColumns(10);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(104, 139, 318, 2);
		pnlSetting.add(separator);
		
		btnSave = new JButton("Save");
		btnSave.setBounds(331, 152, 91, 23);
		pnlSetting.add(btnSave);
		
		JPanel pnlLogs = new JPanel();
		tabs.addTab("Logs", null, pnlLogs, null);
		pnlLogs.setLayout(null);
		
		JLabel lblLogs = new JLabel("Background logs :");
		lblLogs.setBounds(23, 11, 179, 14);
		pnlLogs.add(lblLogs);
		
		JPanel pnlAbout = new JPanel();
		pnlAbout.setToolTipText("");
		tabs.addTab("About", null, pnlAbout, null);
		pnlAbout.setLayout(null);
		
		JLabel lblHead = new JLabel("Peer to Peer File Sharing using Network Coding");
		lblHead.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblHead.setBounds(76, 39, 433, 27);
		pnlAbout.add(lblHead);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(56, 72, 442, 2);
		pnlAbout.add(separator_1);
		
		JLabel lblUrl = new JLabel("<html> <i>https://www.github.com/TheNilesh/ncp2p</i></html>");
		lblUrl.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblUrl.setBounds(139, 85, 241, 14);
		pnlAbout.add(lblUrl);
		
		JLabel lblInfo = new JLabel("<html>Programmers: <br> <b>Nilesh Akhade</b> [akhadenilesh93@gmail.com]<br> <b>Ajay Choudhary</b> [koolacac@gmail.com]<html>");
		lblInfo.setBounds(76, 123, 304, 73);
		pnlAbout.add(lblInfo);
		
		JLabel lblIcon = new JLabel("");
		lblIcon.setIcon(new ImageIcon(View.class.getResource("/res/p2p.png")));
		lblIcon.setBounds(30, 11, 85, 65);
		frmPeerToPeer.getContentPane().add(lblIcon);
		
		lblStatus = new JLabel("Offline");
		lblStatus.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblStatus.setIcon(new ImageIcon(View.class.getResource("/res/offline.png")));
		lblStatus.setBounds(125, 46, 309, 30);
		frmPeerToPeer.getContentPane().add(lblStatus);
		
		lblDmStatus = new JLabel("DM Status: IDLE");
		lblDmStatus.setBounds(454, 10, 165, 30);
		frmPeerToPeer.getContentPane().add(lblDmStatus);
		
		JSeparator sprtr2 = new JSeparator();
		sprtr2.setBounds(125, 36, 309, 2);
		frmPeerToPeer.getContentPane().add(sprtr2);
		
		lblIp = new JLabel("0.0.0.0 : 0000");
		lblIp.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblIp.setBounds(454, 46, 165, 30);
		frmPeerToPeer.getContentPane().add(lblIp);
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
		JOptionPane.showMessageDialog(frmPeerToPeer, string,  "Result", JOptionPane.WARNING_MESSAGE);
	}

	public String getInputString(String string) {
		return JOptionPane.showInputDialog(string);
	}
	
	public String getInputString(String title,String question,String defaultAnswer){
		String input = (String)JOptionPane.showInputDialog(null, question,
				title, JOptionPane.QUESTION_MESSAGE,null,null,defaultAnswer);
		return input;
	}



	public void addTask(String type, String name, int progress, int sessionID) {
		Object[] rowData={type,name,new Integer(progress),new Integer(sessionID)};		
		dftTasks.addRow(rowData);
	}
	
	public void updateProgress(int sessionID, int progress) {
		int i = getRow(sessionID);
		if(i!=-1){
			dftTasks.setValueAt(new Integer(progress), i, 3); //3 column=Progress
		}
	}
	
	private int getRow(int sessionID){
		Integer id=new Integer(sessionID);
		for(int i=0;i<dftTasks.getRowCount();i++){
			if(dftTasks.getValueAt(i, 3).equals(id)){
				return i;
			}
		}
		return -1;
	}

	public void setInfo(String key, String value) {
		switch(key){
		case "PNAME":
			lblPeerName.setText("Peer Name: " + value);
			txtPeerName.setText(value);
			break;
		case "STAT":
			lblStatus.setText(value);
			if(value.equalsIgnoreCase("online")){
				lblStatus.setIcon(new ImageIcon(View.class.getResource("/res/online.png")));
			}else if(value.equalsIgnoreCase("offline")){
				lblStatus.setIcon(new ImageIcon(View.class.getResource("/res/offline.png")));
			}
			break;
		case "SP":
			txtSuperPeer.setText(value);
			break;
		case "DMSTAT":
			lblDmStatus.setText("DM Status: " + value);
			break;
		case "EXIP":
			lblIp.setText(value);
			break;
		case "SHARE":
			txtShare.setText(value);
			break;
		default:
			System.out.println("VIEW:::: Unsupported Property");
			break;
		}	
	}
}