package peer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JTable;

public class Controller extends MouseAdapter implements ActionListener {

	PeerImpl model;
	View view;
	public Controller(){

	}
	public void setView(View v){
		this.view=v;
	}
	public void setModel(PeerImpl p){
		this.model=p;
	}
	
	@Override
	 public void actionPerformed(ActionEvent arg0) {
	  
		System.out.println("Action Performed" + arg0.getID());
		
		if(view==null){
			return;
		}
	   //retrieve the input from View

		Object obj=arg0.getSource();
		JButton btn=(JButton)obj;
		
		btn.setEnabled(false);
		if(btn.equals(view.btnSearch)){
			String[][] tmp1= model.searchFile(view.getSearchText());
			if(tmp1.length!=0){
				view.setSearchResult(tmp1);
			}else{
				view.showMessage("No Files matching search pattern.");
			}
		}
		
		
		btn.setEnabled(true);
	  }
	
	//For jTable
	@Override
	public void mousePressed(MouseEvent arg0) {
		if (arg0.getClickCount() == 2) {
			JTable target = (JTable)arg0.getSource();
			int row = target.getSelectedRow();
			int column = target.getSelectedColumn();
			if(column==0){
				String md5=(String)target.getValueAt(row, 4);
				String localName=view.getInputString("Enter File Name to save as :");
				if(localName!=null){
					model.downloadFile(md5, localName);
				}
			}
		}
	}
	
	public static void main(String args[]){
		Controller c=new Controller();
		View v=new View();
		PeerImpl p=new PeerImpl(v,"C:\\conf.xml");
		c.setView(v);
		c.setModel(p);
		v.setController(c);
	}
	
}
