package speer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class SPController implements ActionListener{

	

	public static void main(String args[]) throws IOException{
		SPController c=new SPController();
		SPView v=new SPView();
			SuperPeerImpl p=new SuperPeerImpl(4689);
			c.setModel(p);
			p.setView(v);
			c.setView(v);
			v.setController(c);
	}

	private void setView(SPView v) {
		this.view=v;
		
	}

	private SuperPeerImpl model;
	private SPView view;
	
	private void setModel(SuperPeerImpl p) {
		this.model=p;
		
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
	
		int a=Integer.parseInt(view.txtMaxPeers.getText());
		model.setMaxPeers(a);
		
	}

}
