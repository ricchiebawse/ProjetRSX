package projet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Player{
	
	//Classe joueur qui sert de se connecter a un serveur de jeu en TCP
	
	private String domainServer;//Domain auquel on se connecte pour joueur : IP du serveur
	private int portServer;//Port du serveur
	private String myName;
	private Socket soc;
	BufferedReader entree;
	PrintWriter sortie;
	int gameTurn=1;
	
	String nameOpponent;//Nom de l'adversaire
	String ipOpponent="";//IP de l'adversaire
	int portOpponent;//Port de l'adversaire
	
	
	private void connectToReferee(String domain, int port)
	{
		//Connexion a l'arbitrePFCLS
		
		//TODO : Ici on se connecte a l'arbitre directement avec le meme domain et port que le joueur
		//car on joue UNIQUEMENT en local pour l'instant. Mais on est censé pouvoir jouer en reseau,
		//donc il faudra s'occuper de ça.
		
		
		soc = null;
		try {
			soc = new Socket(domain, port);
			entree = new BufferedReader(new InputStreamReader(soc.getInputStream()));
			sortie = new PrintWriter(soc.getOutputStream(),true);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sortie.println(myName); // Envoie du nom du joueur a l'arbitre afin qu'il le diffuse a l'autre joueur
		StaticMethods.consolePrintln("recherche de connexion avec l'autre joueur...");
		
		//EN ATTENTE : attente de reception du nom du joueur2 ( qu'une fois que ce dernier se sera connect� � l'arbitre).
		nameOpponent = StaticMethods.receiveString(entree); 
		
	}

	public void startGame() {
		try {	
			connectToReferee(this.domainServer, this.portServer);
			if(soc==null)
			{
				StaticMethods.consolePrintln("Connexion a échouée");
			}
			else
			{
				StaticMethods.consolePrintln("Connextion effectuée avec " + nameOpponent);
				StaticMethods.consolePrintln("Bon match !");
				
				//On recupère les informations de son adversaires : IP + PORT : Pour discuter avec lui par UDP
				if(ipOpponent.equals("")){//Si on a pas encore les donnees de l'adversaire.
					StaticMethods.sendString("info", sortie);
					ipOpponent = StaticMethods.receiveString(entree);
					portOpponent = Integer.parseInt(StaticMethods.receiveString(entree));
				}
				
				//Instancier cette classe permet de chatter avec son adversaire (ipOpponent+portOpponent+nameOpponent) par UDP, via une fenêtre IHM
				ChatUDP chat = new ChatUDP(soc.getLocalPort(), nameOpponent, ipOpponent, portOpponent, myName);		
				
				while(true) {//UN parcours de cette boucle corresponds à UN tour de Jeu.
											
						//Deroulement d'un tour de jeu.
						
						StaticMethods.consolePrintln("Tour "+ gameTurn);
						StaticMethods.consolePrintln("Veuillez entrer soit :\npierre\nfeuille\nciseau\nlezard\nspoke");
						String texte = StaticMethods.getKeyboarding();
						StaticMethods.sendString(texte, sortie);
						String turnRslt = StaticMethods.receiveString(entree);
						
						StaticMethods.consolePrintln("Mon Choix --> " + texte );
						StaticMethods.consolePrintln("\t Résultat --> " + turnRslt);

						gameTurn++;

						if(isGameOver(turnRslt))
							break;
								
				}
				
			entree.close();
			sortie.close();
			soc.close();
			}

		} catch (IOException e) {
			e.getMessage();
		}
	}
	
	private boolean isGameOver(String rep){
		//Verification de la fin de jeu (Game Over).
		
		if(rep.equals("defaite")||rep.equals("victoire")||rep.equals("abandon")){
			if((rep.equals("abandon")))
				StaticMethods.consolePrintln("Vous avez abandonn� la partie");
			else if((rep.equals("defaite")))
				StaticMethods.consolePrintln("Vous avez perdu la partie");
			else
				StaticMethods.consolePrintln("Vous avez gagn� la partie");	
			
			return true;
		}
		return false;
	}
	
	public Player() {
		StaticMethods.consolePrintln("Quelle est votre nom?");
		String name = StaticMethods.getKeyboarding();
		StaticMethods.consolePrintln("Bienvenue "+ name +" !");
		this.myName=name;
		this.domainServer="localhost";
		this.portServer=8080;
	}
	
	public Player(String domain, int port) {
		StaticMethods.consolePrintln("Quelle est votre nom?");
		String name = StaticMethods.getKeyboarding();
		StaticMethods.consolePrintln("Bienvenue "+ name +" !");
		this.myName=name;
		this.domainServer=domain;
		this.portServer=port;
	}

	public static void main(String[] args){
		Player cli = new Player("localhost", 8080);
		cli.startGame();
	}
}