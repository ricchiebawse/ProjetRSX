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
	
	public static final String YES="oui";
	public static final String NO="non";




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
				StaticMethods.consolePrintln("Connexion effectuée avec " + nameOpponent);
				StaticMethods.consolePrintln("Bon match !");
				
				//On recupère les informations de son adversaires : IP + PORT : Pour discuter avec lui par UDP
				if(ipOpponent.equals("")){//Si on a pas encore les donnees de l'adversaire.
					StaticMethods.sendString(Consts.MSG_DATA, sortie);
					ipOpponent = StaticMethods.receiveString(entree);
					portOpponent = Integer.parseInt(StaticMethods.receiveString(entree));
				}
				
				StaticMethods.consolePrintln("Afficher les règles du jeu ? oui (par défaut) ou non");
				String reponse = StaticMethods.getKeyboarding();
				if(reponse.equals(NO))
				{
					//On envoie au serveur que le joueur ne souhaite pas récuperer les règles du jeu
					StaticMethods.sendString(Consts.MSG_0, sortie);
				}
				else
				{
					StaticMethods.sendString(Consts.MSG_1, sortie);
					
					//On demande au serveur les regles du jeu
					String rules="Règles du jeu :";
					StaticMethods.consolePrintln(rules);
					
					//Le serveur nous envoies plusieurs string (séparé par des sauts de ligne) avec les regles du jeu, on affiche tant que le message n'est pas terminé
					while(!(rules.equals(Consts.MSG_0)))
					{
						rules = StaticMethods.receiveString(entree);
						if(!(rules.equals(Consts.MSG_0)))
							StaticMethods.consolePrintln(rules);
					}
				}
				
				//Instancier cette classe permet de chatter avec son adversaire (ipOpponent+portOpponent+nameOpponent) par UDP, via une fenêtre IHM
				ChatUDP chat = new ChatUDP(soc.getLocalPort(), nameOpponent, ipOpponent, portOpponent, myName);		
				
				//Fonction jeu
				turnGame();
				
				//Ferme la fenetre de chat
				chat.endChat();
				
				//Fermeture des sockets instanciées
				entree.close();
				sortie.close();
				soc.close();
				
			}
			

		} catch (IOException e) {
			e.getMessage();
		}
		
	}
	
	private void turnGame()
	{
		do
		{//UN parcours de cette boucle corresponds à UN tour de Jeu.
			
			//Deroulement d'un tour de jeu.
			
			//Permet de gérer le while (sinon parfois pas le temps de finir le isGameOver avant d'entamer une nouvelle boucle) A TESTER !
			try {
				Thread.currentThread();
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			StaticMethods.consolePrintln("Tour "+ gameTurn);
			String grid =StaticMethods.receiveString(entree);
			StaticMethods.consolePrintln(grid);
			String texteValid;
			do{
				String texte = StaticMethods.getKeyboarding();
				StaticMethods.sendString(texte, sortie);
				texteValid = StaticMethods.receiveString(entree);
				if(texteValid.equals(Consts.MSG_KO))
				{
					String error = StaticMethods.receiveString(entree);
					StaticMethods.consolePrintln(error);
				}
			}while(texteValid.equals(Consts.MSG_KO));
			
			String turnRslt = StaticMethods.receiveString(entree);
			StaticMethods.consolePrintln("\t Résultat --> " + turnRslt);

			gameTurn++;
					
		}while(!isGameOver());
	}
	
	
	private boolean isGameOver(){
		//Verification de la fin de jeu (Game Over).
		String endRslt = StaticMethods.receiveString(entree);
		
		if(endRslt.equals(Consts.DEFEAT)||endRslt.equals(Consts.WIN)||endRslt.equals(Consts.DEFEAT_BY_SURRENDER) ||endRslt.equals(Consts.WIN_BY_SURRENDER) ){
			if((endRslt.equals(Consts.DEFEAT_BY_SURRENDER)))
				StaticMethods.consolePrintln("Vous avez abandonné la partie");
			else if((endRslt.equals(Consts.DEFEAT)))
				StaticMethods.consolePrintln("Vous avez perdu la partie");
			else if((endRslt.equals(Consts.WIN_BY_SURRENDER)))
				StaticMethods.consolePrintln("Vous avez gagné la partie sur abandon");
			else
				StaticMethods.consolePrintln("Vous avez gagné la partie");	
			return newGame();
		}
		return false;
	}
	
	private boolean newGame(){
		//Demande au joueur si il souhaite un new game, puis envoie au serveur, si deux joueurs ok, le serveur renvoie ok.
		
		StaticMethods.consolePrintln("Voulez vous rejouer face au même adversaire? oui ou non (par défaut)");
		String texte = StaticMethods.getKeyboarding();
		if(texte.equals(YES))
		{
			StaticMethods.sendString(Consts.MSG_1, sortie);
		}
		else
		{
			StaticMethods.sendString(Consts.MSG_0, sortie);
		}
		
		String reponseServer = StaticMethods.receiveString(entree);
		
		if(reponseServer.equals(Consts.MSG_1))
		{
			gameTurn=1;
			String manche = StaticMethods.receiveString(entree);
			StaticMethods.consolePrintln("Partie n°: "+manche);
			turnGame();
			return false;
		}
		else
		{
			/* Affichage fin de partie*/
			StaticMethods.consolePrintln("Fin du jeu !");
			if(reponseServer.equals(Consts.MSG_2))
			{
				StaticMethods.consolePrintln("Vous n'avez pas souhaité rejouer de partie");
			}
			else if(reponseServer.equals(Consts.MSG_0))
			{
				StaticMethods.consolePrintln("L'adversaire n'a pas souhaité rejouer la partie");
			}
			else
			{
				StaticMethods.consolePrintln("Aucun d'entre vous n'a souhaité rejouer la partie");
			}
			return true;
		}
		
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
		//Les arguments d'exécution sont : l'IP du serveur distant, le port du serveur distant.
		String servIP=Consts.DEFAULT_IP;
		int servPort=Consts.DEFAULT_PORT;
		
		if(args.length>0){
			if(args[0]!=null){ servIP=args[0]; }
			if(args[1]!=null){ servPort=Integer.parseInt(args[1]); }
		}

		Player cli = new Player(servIP, servPort);
		cli.startGame();
	}
}