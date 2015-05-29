package peer;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class PeerUI {

	PeerImpl p;
	private JFrame appWindow;
	private JTextField txtCmd;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PeerUI window = new PeerUI();
					window.appWindow.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public PeerUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		appWindow = new JFrame();
		appWindow.setTitle("Peer");
		appWindow.setBounds(100, 100, 450, 300);
		appWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		appWindow.getContentPane().setLayout(null);
		
		JLabel lblCommand = new JLabel("Command :");
		lblCommand.setBounds(23, 39, 70, 14);
		appWindow.getContentPane().add(lblCommand);
		
		txtCmd = new JTextField();
		txtCmd.setBounds(95, 36, 230, 20);
		appWindow.getContentPane().add(txtCmd);
		txtCmd.setColumns(10);
		
		JButton btnSubmit = new JButton("Submit");
		
		p=new PeerImpl();
		btnSubmit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String[] cmd = txtCmd.getText().split(" ");
				switch(cmd[0]){
				case "REG":
					
					break;
				case "FCHANGE":
					p.fileChanged(new File(cmd[1]), Integer.parseInt(cmd[2]));
					break;
				case "DOWNLOAD":
					p.downloadFile(cmd[1]);
					break;
				case "SEARCH":
					p.searchFile(cmd[1]);
					break;
				default:
					break;
				}
			}
		});
		btnSubmit.setBounds(335, 35, 89, 23);
		appWindow.getContentPane().add(btnSubmit);
		
		JFormattedTextField ftxtLog = new JFormattedTextField();
		ftxtLog.setBounds(23, 92, 386, 142);
		appWindow.getContentPane().add(ftxtLog);
	}
}
