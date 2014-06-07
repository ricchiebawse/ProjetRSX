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

public class Player extends Thread {
	
	//Classe joueur qui sert de se connecter a un serveur de jeu en TCP
	//TODO : L'utilisation d'un Thread est-elle necessaire ?
	
	private String domain;//Domain auquel on se connecte pour joueur : IP du serveur
	private int port;//Port du serveur
	private String name;
	private Socket soc;
	BufferedReader entree;
	PrintWriter sortie;
	int gameTurn;
	
	/*TODO: A modifier pour gérer plusieurs adversaires (liste) (OU PAS ?)*/
	String nameOpponent;//Nom de l'adversaire
	String ipOpponent="";//IP de l'adversaire
	int portOpponent;//Port de l'adversaire
	
	
	private void connectToReferee(String domain, int port)
	{
		//Connexion a l'arbitrePFCLS
		
		//TODO : Ici on se connecte a l'arbitre directement avec le meme domain et port que le joueur
		//car on joue UNIQUEMENT en local pour l'instant. Mais on est censŽ pouvoir jouer en reseau,
		//donc il faudra s'occuper de a.
		
		
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
		
		sortie.println(name); // Envoie du nom du joueur a l'arbitre afin qu'il le diffuse a l'autre joueur
		StaticMethods.consolePrintln("recherche de connexion avec l'autre joueur...");
		
		//EN ATTENTE : attente de reception du nom du joueur2 ( qu'une fois que ce dernier se sera connectŽ ˆ l'arbitre).
		nameOpponent = StaticMethods.receiveString(entree); 
		
	}

	public void run() {
		try {	
			connectToReferee(this.domain, this.port);
			if(soc==null)
			{
				StaticMethods.consolePrintln("Connexion a échouée");
			}
			else
			{
				StaticMethods.consolePrintln("Connextion effectuée avec " + nameOpponent);
				StaticMethods.consolePrintln("Bon match !");
				
				//Chat : classe permettant de chatter avec son adversaire
				Chat chat = new Chat(soc.getLocalPort(), nameOpponent);
				gameTurn=1;
				while(true) {
					StaticMethods.consolePrintln("Menu : 1) jouer (par defaut)\n2) chatter");
					String choice = StaticMethods.getKeyboarding();
					if(choice.equals("2"))//Le joueur decide de chatter avec son adversaire
					{
						
						if(ipOpponent.equals("")){//Si on a pas encore les donnees de l'adversaire.
							StaticMethods.sendString("info", sortie);
							ipOpponent = StaticMethods.receiveString(entree);
							portOpponent = Integer.parseInt(StaticMethods.receiveString(entree));
							//StaticMethods.consolePrintln("Données de l'adversaire reçu.");
						}
						try {
							chat.sendMsg(ipOpponent, portOpponent);
						} catch (SocketException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}		
						
					}
					else
					{//Le joueur decide de jouer son tour
						
						//Deroulement d'un tour de jeu.
						
						StaticMethods.consolePrintln("Tour n° "+ gameTurn);
						StaticMethods.consolePrintln("Veuillez entrer soit :\npierre\nfeuille\nciseau\nlezard\nspoke");
						//demande a l'utilisateur la saisie de son choix
						String texte = StaticMethods.getKeyboarding();
						StaticMethods.sendString(texte, sortie);
						String turnRslt = StaticMethods.receiveString(entree);
						
						StaticMethods.consolePrintln("Mon Choix --> " + texte );
						StaticMethods.consolePrintln("\t Résultat --> " + turnRslt);

						gameTurn++;

						if(isGameOver(turnRslt))
							break;
								
				}
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
				StaticMethods.consolePrintln("Vous avez abandonné la partie");
			else if((rep.equals("defaite")))
				StaticMethods.consolePrintln("Vous avez perdu la partie");
			else
				StaticMethods.consolePrintln("Vous avez gagné la partie");	
			
			return true;
		}
		return false;
	}
	
	public Player() {
		StaticMethods.consolePrintln("Quelle est votre nom?");
		String name = StaticMethods.getKeyboarding();
		StaticMethods.consolePrintln("Bienvenue "+ name +" !");
		this.name=name;
		this.domain="localhost";
		this.port=8080;
	}
	
	public Player(String domain, int port) {
		StaticMethods.consolePrintln("Quelle est votre nom?");
		String name = StaticMethods.getKeyboarding();
		StaticMethods.consolePrintln("Bienvenue "+ name +" !");
		this.name=name;
		this.domain=domain;
		this.port=port;
	}

	public static void main(String[] args){
		Player cli = new Player("localhost", 8080);
		cli.start();
	}
}