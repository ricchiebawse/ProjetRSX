package projet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServeurMultiThread extends Thread {
	
	private ServerSocket s;
	private Socket soc;
	private BufferedReader entree;

	public void run() {
		try {
			BufferedReader entree = new BufferedReader (new InputStreamReader (soc.getInputStream()));
			PrintWriter sortie = new PrintWriter (soc.getOutputStream(), true);

			while(true) {
				String str = entree.readLine();
				if(str.equals("bye")){
					String newstr = str.toUpperCase();
					sortie.println(newstr);
					break;
				}
				String newstr = str.toUpperCase();
				sortie.println(newstr);
				//System.out.println("Entrée : " + str);
				
				//System.out.println("\t Réponse : " + "C'est entendu !");
			}

			entree.close();
			sortie.close();
			soc.close();
		} catch (IOException e) {
			e.getMessage();
		}
	}	
	
	public ServeurMultiThread(Socket soc) {
		this.soc = soc;
	}

	public static void main(String[] args){
		System.out.println("Serveur !");
		int i = 0;
		try {		
			ServerSocket s = new ServerSocket(8080);
			
			while(i < 100) {
				Socket soc = s.accept();
				ServeurMultiThread smt = new ServeurMultiThread(soc);
				smt.start();
				++i;
			}
			
			s.close();
		} catch (IOException e) {
			e.getMessage();
		}
	}
}
