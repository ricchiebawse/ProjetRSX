package projet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;


public class RefereePFCLS extends Thread {
	
	public static final int SCORE_TO_WIN=1;
	public static final int MAX_SIMULTANEOUS_GAME=5;

	//Classe servant de serveur de jeu de pierre feuille ciseau lezard spoke, message en TCP
	
	private ServerSocket s;
	//Sockets de service avec les deux DERNIERS joueurs connect?s.
	private static Socket socLastJoueur1=null;
	private static Socket socLastJoueur2=null;
	
	private static String nameJoueur1;  // Nom joueur 1 de la derniere partie commenc?e
	private static String nameJoueur2; // Nom joueur 2 de la derniere partie commenc?e

	private static ArrayList<String> moveAllowed;
	private static String rules;
	
	private boolean isGameOver(int p1, int p2, PrintWriter sortie1, PrintWriter sortie2)
	{
		//Verification de fin de jeu (Game Over) et diffusion des resultats aux joueurs.
		if(p2>=SCORE_TO_WIN)
		{
			StaticMethods.sendString(Consts.WIN,sortie2);
			StaticMethods.sendString(Consts.DEFEAT,sortie1);
			StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie2);
			StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie1);
			
			return true;
		}
		if(p1>=SCORE_TO_WIN){
			StaticMethods.sendString(Consts.WIN,sortie1);
			StaticMethods.sendString(Consts.DEFEAT,sortie2);
			StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie2);
			StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie1);
			return true;
		}
		else
		{
			StaticMethods.sendString(Consts.CONTINUE,sortie1);
			StaticMethods.sendString(Consts.CONTINUE,sortie2);
			StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie2);
			StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie1);
			
			//Pas de grille donc on envoie pas full
			StaticMethods.sendString(Consts.MSG_GRID_NOT_FULL,sortie1);
			StaticMethods.sendString(Consts.MSG_GRID_NOT_FULL,sortie2);
			return false;
		}
		
	}
	
	private void sendOpponentData(String str, PrintWriter sortie, Socket soc)
	{
		//Envoie de l'IP et du port d'un joueur à l'autre joueur.
		if(str.equals(Consts.MSG_DATA))
		{
			sortie.println(soc.getInetAddress().toString());
			sortie.println(soc.getPort());
		}
	}

	private boolean isAbandon(String str1, String str2, PrintWriter sortie1, PrintWriter sortie2)
	{
		//Envoie des resultats du match si Abandon.
				if(str1.equals(Consts.SURRENDER)){
					StaticMethods.sendString(Consts.WIN_BY_SURRENDER,sortie2);
					StaticMethods.sendString(Consts.DEFEAT_BY_SURRENDER,sortie1);
					
					return true;
				}
				if(str2.equals(Consts.SURRENDER)){
					StaticMethods.sendString(Consts.WIN_BY_SURRENDER,sortie1);
					StaticMethods.sendString(Consts.DEFEAT_BY_SURRENDER,sortie2);
					
					return true;
				}
				return false;
	}
	
	private void spreadTurnResults(int result,int p1, int p2, PrintWriter sortie1, PrintWriter sortie2, String choiceJ1, String choiceJ2)
	{
		//Envoie aux joueurs les resultats du tour terminé.
		String msgJ1="";
		String msgJ2="";
		
		if(result==3)
		{
			msgJ1+="Abandon ";
			msgJ1+="Abandon ";
		}
		if(result==2)
		{
			msgJ1+="Perdu ";
			msgJ2+="Gagne ";
		}
		if(result==1)
		{
			msgJ1+="Gagne ";
			msgJ2+="Perdu ";
		}
		if(result==0)
		{
			msgJ1+="Nul ";
			msgJ2+="Nul ";
		}
		
		msgJ1+="| Choix : "+choiceJ1+" - Points : "+p1+" | Choix adversaire : "+choiceJ2+" - Points adversaire : "+p2;
		msgJ2+="| Choix : "+choiceJ2+" - Points : "+p2+" | Choix adversaire : "+choiceJ1+" - Points adversaire : "+p1;
		
		StaticMethods.sendString(msgJ1,sortie1);
		StaticMethods.sendString(msgJ2,sortie2);

	}


	
	public void run() {//Thread de gestion d'UNE partie de PFCLS entre DEUX joueurs.
		try {
			Socket socJoueur1=socLastJoueur1;
			Socket socJoueur2=socLastJoueur2;
			
			BufferedReader entreeJoueur1 = new BufferedReader (new InputStreamReader (socJoueur1.getInputStream()));
			PrintWriter sortieJoueur1 = new PrintWriter (socJoueur1.getOutputStream(), true);
			BufferedReader entreeJoueur2 = new BufferedReader (new InputStreamReader (socJoueur2.getInputStream()));
			PrintWriter sortieJoueur2 = new PrintWriter (socJoueur2.getOutputStream(), true);
			
			//Ré-initialisation à NULL des deux attributs socLastJoueurX necessaire a la creation d'autres parties
			socLastJoueur1=null;
			socLastJoueur2=null; 
			
			/*Point de joueur1 et joueur2*/
			int pointsJ1=0;
			int pointsJ2=0;
			
			/*Num joueur*/
			int numPlayer1=1;
			int numPlayer2=2;
			
			/*Nombre de partie disputées entre ces deux joueurs*/
			int partie=1;
			
			String choiceJ1 = StaticMethods.receiveString(entreeJoueur1);
			String choiceJ2 = StaticMethods.receiveString(entreeJoueur2);
			
			//Envoie les donnees de l'autre
			sendOpponentData(choiceJ1,sortieJoueur1,socJoueur2);
			sendOpponentData(choiceJ2,sortieJoueur2,socJoueur1);	
			
			//Reponse à la demande des joueurs d'afficher les régles
			choiceJ1 = StaticMethods.receiveString(entreeJoueur1);
			choiceJ2 = StaticMethods.receiveString(entreeJoueur2);
			
			//Envoie les regles du jeu aux joueurs qui le veulent
			
			//Envoie à J1 s'il le désire
			if(choiceJ1.equals(Consts.MSG_ACK))
			{
				StaticMethods.sendString(rules,sortieJoueur1);
				//Envoie de l'indicateur de fin de fin de message
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortieJoueur1);
			}
			
			//Envoie à J2 s'il le désire
			if(choiceJ2.equals(Consts.MSG_ACK))
			{
				StaticMethods.sendString(rules,sortieJoueur2);
				//Envoie de l'indicateur de fin de fin de message
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortieJoueur2);
			}
			
			StaticMethods.sendString(String.valueOf(partie),sortieJoueur1);
			StaticMethods.sendString(String.valueOf(partie),sortieJoueur2);
			StaticMethods.sendString(String.valueOf(numPlayer1),sortieJoueur1);
			StaticMethods.sendString(String.valueOf(numPlayer2),sortieJoueur2);
			
			
			while(true) {//UN tour de cette boucle corresponds a UN tour de jeu.
				
				StaticMethods.sendString("Veuillez entrer soit : | pierre | feuille | ciseau | lezard | spoke |",sortieJoueur1);
				StaticMethods.sendString("Veuillez entrer soit : | pierre | feuille | ciseau | lezard | spoke |",sortieJoueur2);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortieJoueur1);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortieJoueur2);
				
				choiceJ1 = StaticMethods.receiveString(entreeJoueur1);
				choiceJ2 = StaticMethods.receiveString(entreeJoueur2);
				
				//Gestion de la cast
				choiceJ1 = choiceJ1.toLowerCase();
				choiceJ2 = choiceJ2.toLowerCase();
				
				//Test si un joueur à effectuer un move illégal
				choiceJ1 = illegalMove(choiceJ1, entreeJoueur1, sortieJoueur1);
				choiceJ2 = illegalMove(choiceJ2, entreeJoueur2, sortieJoueur2);

				//Calcul du resultat du tour
				int result = makeGameTurn(choiceJ1,choiceJ2); 
				
				//Met a jour score des joueurs.
				if(result==2)
					pointsJ2++;
				else if(result==1)
					pointsJ1++;
				
				//Diffusion des resultats du tour aux deux joueurs.
				spreadTurnResults(result,pointsJ1,pointsJ2,sortieJoueur1,sortieJoueur2,choiceJ1,choiceJ2); 
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortieJoueur1);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortieJoueur2);
				
				//Verification de fin de jeu (Game Over) ou (Abandon) et demande de newgame
				if(isAbandon(choiceJ1,choiceJ2,sortieJoueur1,sortieJoueur2))
				{
					if(newGame(sortieJoueur1,sortieJoueur2,entreeJoueur1,entreeJoueur2))
					{
						pointsJ1=0;
						pointsJ2=0;
						partie++;
						StaticMethods.sendString(String.valueOf(partie),sortieJoueur1);
						StaticMethods.sendString(String.valueOf(partie),sortieJoueur2);
					}
					else
					{
						break;
					}
				}
				else if(isGameOver(pointsJ1,pointsJ2,sortieJoueur1,sortieJoueur2))
				{
					if(newGame(sortieJoueur1,sortieJoueur2,entreeJoueur1,entreeJoueur2))
					{
						pointsJ1=0;
						pointsJ2=0;
						partie++;
						StaticMethods.sendString(String.valueOf(partie),sortieJoueur1);
						StaticMethods.sendString(String.valueOf(partie),sortieJoueur2);
					}
					else
					{
						break;
					}
				}
				
			}

			entreeJoueur1.close();
			entreeJoueur2.close();
			sortieJoueur2.close();
			sortieJoueur1.close();
		}catch (SocketException e){
			StaticMethods.consolePrintln(Consts.CONNEXION_PLA_INTERRUPTED);
			
		}catch (NullPointerException e){
			StaticMethods.consolePrintln("Impossible d'envoyer au serveur");
			
		} catch (IOException e) {
			e.getMessage();
		}
	}	
	
	private String illegalMove(String choice, BufferedReader entreeJoueur,
			PrintWriter sortieJoueur) {
		//Vérifie que le move du joueur est légal, tant que move illégal, on redemande au joueur de resaisir un move
		
		while(!(moveAllowed.contains(choice.toLowerCase())))
		{
			StaticMethods.sendString(Consts.MSG_INVALID,sortieJoueur);
			StaticMethods.sendString(Consts.MSG_ERROR+"Coup illégal, veuillez rejouer :",sortieJoueur);
			choice = StaticMethods.receiveString(entreeJoueur);
		}
		StaticMethods.sendString(Consts.MSG_VALID,sortieJoueur);
		return choice;
	}

	private boolean newGame(PrintWriter sortieJoueur1, PrintWriter sortieJoueur2, BufferedReader entreeJoueur1, BufferedReader entreeJoueur2){
		//Demande au joueurs s'ils souhaitent rejouer la partie achevée
		
		String choiceJ1 = StaticMethods.receiveString(entreeJoueur1);
		String choiceJ2 = StaticMethods.receiveString(entreeJoueur2);
		
		if((choiceJ1.equals(Consts.MSG_REMATCH)) && (choiceJ2.equals(Consts.MSG_REMATCH)))
		{
			//Cas ou les deux joueurs veulent rejouer
			StaticMethods.sendString(Consts.MSG_REMATCH_ACK,sortieJoueur1);
			StaticMethods.sendString(Consts.MSG_REMATCH_ACK,sortieJoueur2);
			
			return true;		
		}
		else
		{
			if((choiceJ1.equals(Consts.MSG_NO_REMATCH)) && (choiceJ2.equals(Consts.MSG_NO_REMATCH)))
			{
				//Cas ou les deux refusent de rejouer
				
				StaticMethods.sendString(Consts.MSG_NO_REMATCH_BOTH_PLAYERS,sortieJoueur1);
				StaticMethods.sendString(Consts.MSG_NO_REMATCH_BOTH_PLAYERS,sortieJoueur2);
				return false;
			}
			else if((choiceJ1.equals(Consts.MSG_REMATCH)))
			{
				StaticMethods.sendString(Consts.MSG_NO_REMATCH_OPPONENT,sortieJoueur1);
				StaticMethods.sendString(Consts.MSG_NO_REMATCH_ACK,sortieJoueur2);
				return false;
			}
			else
			{
				StaticMethods.sendString(Consts.MSG_NO_REMATCH_ACK,sortieJoueur1);
				StaticMethods.sendString(Consts.MSG_NO_REMATCH_OPPONENT,sortieJoueur2);
				return false;
			}
		}
		
	}
	public RefereePFCLS(Socket socket) {
		//Concoit la connection avec UN joueur.
		
		if(socLastJoueur1==null){//Affectation de la socket de service de communication avec le joueur1 dans socJoueur1
			this.socLastJoueur1 = socket;
			BufferedReader entreetmp = null;
			try {
				//On recupere le nom du Joueur1
				entreetmp = new BufferedReader (new InputStreamReader (socLastJoueur1.getInputStream()));
				this.nameJoueur1=StaticMethods.receiveString(entreetmp);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{//Affectation de la socket de service de communication avec le joueur2 dans socJoueur2
			this.socLastJoueur2=socket;
			BufferedReader entreetmp2 = null;
			try {
				//On recupere le nom du Joueur2
				entreetmp2 = new BufferedReader (new InputStreamReader (socLastJoueur2.getInputStream()));
				this.nameJoueur2=StaticMethods.receiveString(entreetmp2);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private int makeGameTurn(String a, String b){
		// R?gle du jeu PFCLS
	if(a.equals("pierre"))
	{
		if(b.equals("pierre"))
		{
			return 0;
		}
		if(b.equals("feuille"))
		{
			return 2;
		}
		if(b.equals("ciseau"))
		{
			return 1;
		}
		if(b.equals("lezard"))
		{
			return 1;
		}
		if(b.equals("spoke"))
		{
			return 2;
		}
	}
	if(a.equals("feuille"))
	{
		if(b.equals("pierre"))
		{
			return 1;
		}
		if(b.equals("feuille"))
		{
			return 0;
		}
		if(b.equals("ciseau"))
		{
			return 2;
		}
		if(b.equals("lezard"))
		{
			return 2;
		}
		if(b.equals("spoke"))
		{
			return 1;
		}
	}
	if(a.equals("ciseau"))
	{
		if(b.equals("pierre"))
		{
			return 2;
		}
		if(b.equals("feuille"))
		{
			return 1;
		}
		if(b.equals("ciseau"))
		{
			return 0;
		}
		if(b.equals("lezard"))
		{
			return 1;
		}
		if(b.equals("spoke"))
		{
			return 2;
		}
	}
	if(a.equals("lezard"))
	{
		if(b.equals("pierre"))
		{
			return 2;
		}
		if(b.equals("feuille"))
		{
			return 1;
		}
		if(b.equals("ciseau"))
		{
			return 2;
		}
		if(b.equals("lezard"))
		{
			return 0;
		}
		if(b.equals("spoke"))
		{
			return 1;
		}
	}
	if(a.equals("spoke"))
	{
		if(b.equals("pierre"))
		{
			return 1;
		}
		if(b.equals("feuille"))
		{
			return 2;
		}
		if(b.equals("ciseau"))
		{
			return 1;
		}
		if(b.equals("lezard"))
		{
			return 2;
		}
		if(b.equals("spoke"))
		{
			return 0;
		}
	}
	return 3;
	}
	
	private static void possibleMove()
	{
		//Fonction qui sert à mettre a jour la variable des règles du jeu d'une manière dynamique
		
		moveAllowed = new ArrayList<String>();
		moveAllowed.add("pierre");
		moveAllowed.add("feuille");
		moveAllowed.add("ciseau");
		moveAllowed.add("lezard");
		moveAllowed.add("spoke");
		moveAllowed.add(Consts.SURRENDER);
	}
	
	private static void GameRules()
	{
		//Fonction qui sert à mettre a jour la liste de move possible dans le jeu de pierre feuille ciseau */
		//Utilisée pour tester si un utilisateur a rentré un move illégal
		
		rules = "Jeu de pierre feuille ciseau lezard spoke :\n";
		rules += "Coups possibles lorsque le jeu le demande -> (pierre,feuille,ciseau,lezard,spoke)\n";
		rules += "- pierre gagne contre (lezard,ciseau) et perd contre (feuille,spoke)\n";
		rules += "- feuille gagne contre (pierre,spoke) et perd contre (ciseau,lezard)\n";
		rules += "- ciseau gagne contre (feuille,lezard) et perd contre (pierre,spoke)\n";
		rules += "- lezard gagne contre (papier,spoke) et perd contre (pierre,ciseau)\n";
		rules += "- spoke gagne contre (pierre,ciseau) et perd contre (lezard,papier)\n";
		rules += "Seul les victoires rapportent des points (1 point par tour gagné)\n";
		rules += "Le premier joueur arrivé à "+SCORE_TO_WIN+" remporte la partie\n";
		rules += "Vous pouvez rejouer avec votre adversaire, si vous le désiré tous les deux.\n";
		rules += "Le jeu vous permet également de discuter avec votre adversaire pendant l'intégralité de la partie par le biais de la fenetre prévue à cet effet\n";
		rules += "Si vous voulez abandonner une partie en cours, entrer abandon lorsque c'est à votre tour de jouer\n";
	}
	
	public static void main(String[] args) {
		int i = 0;
		
		int myPort = Consts.DEFAULT_PORT_PFCLS;
		int maxSimultaneousGame = MAX_SIMULTANEOUS_GAME;

		if(args.length>0){
		if(args[0]!=null){ myPort=Integer.parseInt(args[0]); }
		if(args[1]!=null){ maxSimultaneousGame=Integer.parseInt(args[1]); }
		}
		

		StaticMethods.consolePrintln("Serveur multijoueurs de jeu de pierre feuille ciseau lezard spoke");
		possibleMove();
		GameRules();
		try {		
			ServerSocket s = new ServerSocket(myPort);
			while(i < maxSimultaneousGame*2) {// Par défaut : Pas plus de 10 connexions simultanees -> donc 5 parties
				Socket socket = s.accept();
				RefereePFCLS smt = new RefereePFCLS(socket);
				
				if((socLastJoueur1!=null) && (socLastJoueur2!=null))//Une fois les DEUX joueurs connectés au serveur.
				{
					//Permet d'envoyer le nom de l'aversaire au joueur sans entrer dans le run
					PrintWriter sortietmp1 = new PrintWriter (socLastJoueur1.getOutputStream(), true);
					PrintWriter sortietmp2 = new PrintWriter (socLastJoueur2.getOutputStream(), true);
					sortietmp1.println(nameJoueur2);
					sortietmp2.println(nameJoueur1);
					
					//On execute le thread qui gere le match de PFCLS
					smt.start();
					StaticMethods.consolePrintln("Un match en cours, Identifiant du Thread :"+smt.getId()+" [ "+nameJoueur1+" vs "+nameJoueur2+" ]");
				}
				++i; 
			}
			
			
			
			s.close();
		} catch (SocketException e){
			StaticMethods.consolePrintln(Consts.CONNEXION_PLA_FAILED);
			
		}catch (NullPointerException e){
			StaticMethods.consolePrintln("Impossible d'envoyer à un des joueurs");
			
		}catch (IOException e) {
			e.getMessage();
		}
	}
}