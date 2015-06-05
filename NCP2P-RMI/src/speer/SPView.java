package speer;

import java.awt.EventQueue;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class SPView extends JFrame {

	private static final long serialVersionUID = 1L;
	private DefaultListModel<String> lsmPeers;
	private JPanel contentPane;
	private JTable table;
	private JButton btnSave;
	JTextField txtMaxPeers;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SPView frame = new SPView();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public SPView() {
		setTitle("ncp2p SuperPeer");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 524, 345);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(10, 11, 485, 285);
		contentPane.add(tabbedPane);
		
		JPanel pnlPeers = new JPanel();
		tabbedPane.addTab("Peers", null, pnlPeers, null);
		pnlPeers.setLayout(null);
		
		JScrollPane scrPeers = new JScrollPane();
		scrPeers.setBounds(10, 11, 204, 190);
		pnlPeers.add(scrPeers);
		
		lsmPeers=new DefaultListModel<String>();
		JList<String> lstPeers = new JList<String>(lsmPeers);
		scrPeers.setViewportView(lstPeers);
		
		JLabel lblMaxPeers = new JLabel("Max Peers :");
		lblMaxPeers.setBounds(238, 70, 81, 14);
		pnlPeers.add(lblMaxPeers);
		
		txtMaxPeers = new JTextField("3");
		txtMaxPeers.setBounds(329, 65, 86, 20);
		pnlPeers.add(txtMaxPeers);
		txtMaxPeers.setColumns(10);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(238, 125, 232, 2);
		pnlPeers.add(separator);
		
		btnSave = new JButton("Save");
		btnSave.setBounds(326, 96, 89, 23);
		pnlPeers.add(btnSave);
		
		JPanel pnlFiles = new JPanel();
		tabbedPane.addTab("Files", null, pnlFiles, null);
		pnlFiles.setLayout(null);
		
		JScrollPane scrFiles = new JScrollPane();
		scrFiles.setBounds(10, 11, 460, 235);
		pnlFiles.add(scrFiles);
		
		table = new JTable();
		scrFiles.setViewportView(table);
		setVisible(true);
	}
	
	public void addPeer(String nick){
		lsmPeers.addElement(nick);
	}
	
	public void removePeer(String nick){
		lsmPeers.removeElement(nick);
	}

	public void setController(SPController c) {
		btnSave.addActionListener(c);
		
	}

}
