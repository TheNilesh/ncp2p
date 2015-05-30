package peer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.PeerUI;

public class PeerConsoleUI implements PeerUI{

	PeerImpl p;
	PeerConsoleUI() throws Exception{
		p=new PeerImpl();
		consoleListener();
	}
	
	void consoleListener() throws IOException{
		 BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while(true){
			String cmd=br.readLine();
			String[]param= cmd.split(" ");
			String maincmd=param[0].toLowerCase();
			switch(maincmd){
			case "search":
				p.searchFile(param[1]);
				break;
			case "down":
				p.downloadFile(param[1], param[2]);
				break;
			default:
				System.out.println("UNKNOWN command");
				break;
			}
		}//while
	}
	public static void main(String args[]) throws Exception{
		new PeerConsoleUI();
	}
	
	
}
