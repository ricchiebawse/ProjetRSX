package projet;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class RefereePuissance4 extends Thread {
	
	public static final int SCORE_TO_WIN=2;
	public static final int MAX_SIMULTANEOUS_GAME=5;

	//Classe servant de serveur de jeu de puissance 4, message en TCP
	
	private ServerSocket s;
	//Sockets de service avec les deux DERNIERS joueurs connectes.
	private static Socket socLastJoueur1=null;
	private static Socket socLastJoueur2=null;
	
	private static String nameJoueur1;  // Nom joueur 1 de la derniere partie commenc?e
	private static String nameJoueur2; // Nom joueur 2 de la derniere partie commenc?e

	private static int nbline;
	private static int nbcolumn;
	private static String rules;
	

	private void sendOpponentData(String str, PrintWriter sortie, Socket soc)
	{
		//Envoie de l'IP et du port d'un joueur à l'autre joueur.
		if(str.equals(Consts.MSG_DATA))
		{
			sortie.println(soc.getInetAddress().toString());
			sortie.println(soc.getPort());
		}
	}

	private boolean isAbandon(String str1, PrintWriter sortie1, PrintWriter sortie2, int joueur)
	{
		//Envoie des resultats du match si Abandon.
				if(str1.equals(Consts.SURRENDER)){
					if(joueur==1)
					{
						StaticMethods.sendString(Consts.WIN_BY_SURRENDER,sortie2);
						StaticMethods.sendString(Consts.DEFEAT_BY_SURRENDER,sortie1);
					}
					else
					{
						StaticMethods.sendString(Consts.WIN_BY_SURRENDER,sortie1);
						StaticMethods.sendString(Consts.DEFEAT_BY_SURRENDER,sortie2);
					}
					
					return true;
				}
				return false;
	}
	

	
	public void run() {//Thread de gestion d'UNE partie de Puissance 4 entre DEUX joueurs.
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
			
			int[][] grid  = new int[nbline][nbcolumn];
			
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
			
			//Initier grid a 0
			for(int i = 0; i < grid.length; i++){
				   for(int j = 0; j < grid[i].length; j++){
				     grid[i][j] = 0;  
				   }
				}
			
			//Envoie les regles du jeu aux joueurs qui le veulent
			
			//Envoie à J1 s'il le désire
			if(choiceJ1.equals(Consts.MSG_ACK))
			{
				StaticMethods.sendString(rules,sortieJoueur1);
				//Fin de message
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortieJoueur1);
			}
			
			//Envoie à J2 s'il le désire
			if(choiceJ2.equals(Consts.MSG_ACK))
			{
				StaticMethods.sendString(rules,sortieJoueur2);
				//Fin de message
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortieJoueur2);
			}
			
			StaticMethods.sendString(String.valueOf(partie),sortieJoueur1);
			StaticMethods.sendString(String.valueOf(partie),sortieJoueur2);
			StaticMethods.sendString(String.valueOf(numPlayer1),sortieJoueur1);
			StaticMethods.sendString(String.valueOf(numPlayer2),sortieJoueur2);
			
			while(true) {//UN tour de cette boucle corresponds a UN tour de jeu pour les deux joueurs.
				
				StaticMethods.sendString(gridToString(grid),sortieJoueur1);
				//Fin de message
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortieJoueur1);
				
				choiceJ1 = StaticMethods.receiveString(entreeJoueur1);
				
				//Gestion de la cast (sert à rien à priori)
				choiceJ1 = choiceJ1.toLowerCase();
				
				//Test si J1 à effectuer un move illégal
				choiceJ1 = illegalMove(choiceJ1, entreeJoueur1, sortieJoueur1, grid);
		
				if(isAbandon(choiceJ1,sortieJoueur1,sortieJoueur2,1))
				{
					if(newGame(sortieJoueur1,sortieJoueur2,entreeJoueur1,entreeJoueur2))
					{
						pointsJ2++;
						partie++;
						
						//On échange les joueur 1 et joueur 2 pour un souci d'équité 
						
						int numTemp = numPlayer1;
						numPlayer1 = numPlayer2;
						numPlayer2 = numTemp;
						PrintWriter sortieTemp = sortieJoueur1;
						sortieJoueur1 = sortieJoueur2;
						sortieJoueur2 = sortieTemp;
						BufferedReader entreeTemp = entreeJoueur1;
						entreeJoueur1 = entreeJoueur2;
						entreeJoueur2 = entreeTemp;
						//Remettre le grid a 0
						for(int i = 0; i < grid.length; i++){
							   for(int d = 0; d < grid[i].length; d++){
							     grid[i][d] = 0;  
							   }
							}
						StaticMethods.sendString(String.valueOf(partie),sortieJoueur1);
						StaticMethods.sendString(String.valueOf(partie),sortieJoueur2);
					}
					else
					{
						break;
					}
				}
				
				//Mise a jour du plateau de jeu avec tour du joueur 1
				
				int columnChosen1 = Integer.parseInt(choiceJ1);
				//Diff en affichage et grid
				columnChosen1 = columnChosen1-1;
				int ligneChosen1 = 0;
				int intAInserer = 1;
				
				int j=nbline-1;
				int testEntrerBoucle =0;
				do
				{
					if(grid[j-1][columnChosen1]!=0)
					{
						grid[j][columnChosen1]=intAInserer;
						ligneChosen1=j;
						testEntrerBoucle++;
					}
					j--;
				}while((grid[j][columnChosen1]==0)&&(j!=0));
				
				if(testEntrerBoucle==0)
				{
					grid[0][columnChosen1]=intAInserer;
					ligneChosen1=0;
				}
				
				StaticMethods.sendString(gridToString(grid),sortieJoueur1);
				//Fin de message
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortieJoueur1);
				if(isGameOver(ligneChosen1,columnChosen1,grid,1,sortieJoueur1,sortieJoueur2))
				{
					if(newGame(sortieJoueur1,sortieJoueur2,entreeJoueur1,entreeJoueur2))
					{
						pointsJ1++;
						partie++;
						
						//On échange les joueur 1 et joueur 2 pour un souci d'équité 
						
						int numTemp = numPlayer1;
						numPlayer1 = numPlayer2;
						numPlayer2 = numTemp;
						PrintWriter sortieTemp = sortieJoueur1;
						sortieJoueur1 = sortieJoueur2;
						sortieJoueur2 = sortieTemp;
						BufferedReader entreeTemp = entreeJoueur1;
						entreeJoueur1 = entreeJoueur2;
						entreeJoueur2 = entreeTemp;
						//Remettre le grid a 0
						for(int i = 0; i < grid.length; i++){
							   for(int d = 0; d < grid[i].length; d++){
							     grid[i][d] = 0;  
							   }
							}
						StaticMethods.sendString(String.valueOf(partie),sortieJoueur1);
						StaticMethods.sendString(String.valueOf(partie),sortieJoueur2);
					}
					else
					{
						break;
					}
				}
				
				//TOUR DE J2 
				
				StaticMethods.sendString(gridToString(grid),sortieJoueur2);
				//Fin de message
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortieJoueur2);
				
				choiceJ2 = StaticMethods.receiveString(entreeJoueur2);
				
				//Gestion de la cast (dans ce cas la cela sert à rien à priori)
				choiceJ2 = choiceJ2.toLowerCase();
				
				//Test si J2 à effectuer un move illégal
				choiceJ2 = illegalMove(choiceJ2, entreeJoueur2, sortieJoueur2, grid);
				
				
				if(isAbandon(choiceJ2,sortieJoueur1,sortieJoueur2,2))
				{
					if(newGame(sortieJoueur1,sortieJoueur2,entreeJoueur1,entreeJoueur2))
					{
						pointsJ1++;
						partie++;
						
						//On échange les joueur 1 et joueur 2 pour un souci d'équité 
						
						int numTemp = numPlayer1;
						numPlayer1 = numPlayer2;
						numPlayer2 = numTemp;
						PrintWriter sortieTemp = sortieJoueur1;
						sortieJoueur1 = sortieJoueur2;
						sortieJoueur2 = sortieTemp;
						BufferedReader entreeTemp = entreeJoueur1;
						entreeJoueur1 = entreeJoueur2;
						entreeJoueur2 = entreeTemp;
						//Remettre le grid a 0
						for(int i = 0; i < grid.length; i++){
							   for(int d = 0; d < grid[i].length; d++){
							     grid[i][d] = 0;  
							   }
							}
						StaticMethods.sendString(String.valueOf(partie),sortieJoueur1);
						StaticMethods.sendString(String.valueOf(partie),sortieJoueur2);
						
					}
					else
					{
						break;
					}
				}
				
				
				
				//MISE A JOUR DE LA GRID AVEC LE CHOIX DE J2
				int columnChosen2 = Integer.parseInt(choiceJ2);
				
				//Diff entre affichage et grid
				columnChosen2 = columnChosen2-1;
				int ligneChosen2=0;
				intAInserer = 2;
				
				j=nbline-1;
				testEntrerBoucle=0;
				do
				{
					if(grid[j-1][columnChosen2]!=0)
					{
						grid[j][columnChosen2]=intAInserer;
						ligneChosen2=j;

						testEntrerBoucle++;
					}
					j--;
				}while((grid[j][columnChosen2]==0)&&(j!=0));
				
				if(testEntrerBoucle==0)
				{
					grid[0][columnChosen2]=intAInserer;
					ligneChosen2=0;
				}
				
				
				
				StaticMethods.sendString(gridToString(grid),sortieJoueur2);
				//Fin de message
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortieJoueur2);
				if(isGameOver(ligneChosen2,columnChosen2,grid,2,sortieJoueur1,sortieJoueur2))
				{
					if(newGame(sortieJoueur1,sortieJoueur2,entreeJoueur1,entreeJoueur2))
					{
						//Les points sont les victoires, pas utilisé dans le code pour l'instant
						pointsJ2++;
						partie++;
						
						//On échange les joueurs 1 et joueurs 2 pour un souci d'équité 
						
						int numTemp = numPlayer1;
						numPlayer1 = numPlayer2;
						numPlayer2 = numTemp;
						PrintWriter sortieTemp = sortieJoueur1;
						sortieJoueur1 = sortieJoueur2;
						sortieJoueur2 = sortieTemp;
						BufferedReader entreeTemp = entreeJoueur1;
						entreeJoueur1 = entreeJoueur2;
						entreeJoueur2 = entreeTemp;
						
						//Remettre le grid a 0
						for(int i = 0; i < grid.length; i++){
							   for(int d = 0; d < grid[i].length; d++){
							     grid[i][d] = 0;  
							   }
							}
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
		} catch (SocketException e){
			StaticMethods.consolePrintln(Consts.CONNEXION_PLA_INTERRUPTED);
			
		}catch (NullPointerException e){
			StaticMethods.consolePrintln("Impossible d'envoyer au serveur");
			
		}catch (IOException e) {
			e.getMessage();
		}
	}	
	
	private String errorMove(String value,int[][] grid)
	{
	//Methode servant a effectuer tous les actions afin de déterminer si le choix du joueur est bon
		
		//Test abandon
		if(value==Consts.SURRENDER)
		{
			return Consts.MSG_VALID;
		}
		//Test format
		int val;
		try {
			val = Integer.parseInt(value);
			} catch (NumberFormatException nFE) {
			return Consts.MSG_ERROR+"Mauvais format, veuillez entrer un entier";
			}
		
		//Test entier légal
		if((val<1) || (val>nbcolumn) )
		{
			return Consts.MSG_ERROR+"L'entier renseigné n'est pas compris dans l'intervalle [1,"+nbcolumn+"], veuillez entrer un entier dans cet intervalle";
		}
		
		//Test move possible
		if(grid[nbline-1][val-1]!= 0)
		{
			return Consts.MSG_ERROR+"Impossible de jouer à cette endroit, la ligne est remplie, veuillez choisir une autre colonne";
		}
		
		//Move ok
		return Consts.MSG_VALID;
		
	}
	private String illegalMove(String choice, BufferedReader entreeJoueur,
			PrintWriter sortieJoueur, int[][] grid) {
		
		int test=0;
		
		do{
			String result = errorMove(choice,grid);
			if((result.equals(Consts.MSG_VALID)))
			{
				test=1;
			}
			else
			{
				StaticMethods.sendString(Consts.MSG_INVALID,sortieJoueur);
				StaticMethods.sendString(result,sortieJoueur);
				choice = StaticMethods.receiveString(entreeJoueur);
			}
		}while(test==0);
		
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
	public RefereePuissance4(Socket socket) {
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
	
	private String gridToString(int [][] grid)
	{
		//Prend une matrice d'int et la transforme en string (avec les lignes inversé pour l'affichage)
		
		String stringGrid="";
		for(int i = grid.length-1 ; i >-1; i-- ){
		      for(int j = 0; j< grid[i].length; j++){
		    	if(grid[i][j]!=0)
		    	{
		    		stringGrid+= " "+grid[i][j]+" ";
		    	}
		    	else
		    		stringGrid+= " x ";
		        }
		      stringGrid+="\n";
		      }
		
		return stringGrid;
	}
	
	private boolean isGameOver(int ligne,int colonne,int [][] grid,int joueur,PrintWriter sortie1,PrintWriter sortie2){
		
		// Methode qui vérifier que quite au dernier mouvement, un vainqueur n'est pas déclaré (ou si la grille esr remplie)
		
		//TestDiagonale
		
		int testligne=ligne;
		int testcolonne=colonne;
		int compteur=0;
		// TEST DIAGONAL 1
		do{
			compteur++;
			testligne++;
			testcolonne--;
			if(compteur==SCORE_TO_WIN)
			{
				if(joueur==1)
				{
				StaticMethods.sendString(Consts.WIN,sortie1);
				StaticMethods.sendString(Consts.DEFEAT,sortie2);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie1);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie2);
				}
				else
				{
				StaticMethods.sendString(Consts.WIN,sortie2);
				StaticMethods.sendString(Consts.DEFEAT,sortie1);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie1);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie2);
				}
				return true;
			}
			
		}while((testligne<nbline-1)&&(testligne>-1)&&(testcolonne<nbcolumn-1)&&(testcolonne>-1)&&(grid[testligne][testcolonne]==joueur));
		
		testligne=ligne;
		testcolonne=colonne;
		compteur=0;
		// TEST DIAGONAL 2
		do{
			compteur++;
			testligne++;
			testcolonne++;
			if(compteur==SCORE_TO_WIN)
			{
				if(joueur==1)
				{
				StaticMethods.sendString(Consts.WIN,sortie1);
				StaticMethods.sendString(Consts.DEFEAT,sortie2);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie1);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie2);
				}
				else
				{
				StaticMethods.sendString(Consts.WIN,sortie2);
				StaticMethods.sendString(Consts.DEFEAT,sortie1);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie1);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie2);
				}
				return true;
			}
			
		}while((testligne<nbline-1)&&(testligne>-1)&&(testcolonne<nbcolumn-1)&&(testcolonne>-1)&&(grid[testligne][testcolonne]==joueur));
		

		testligne=ligne;
		testcolonne=colonne;
		compteur=0;
		// TEST DIAGONAL 3
		do{
			compteur++;
			testligne--;
			testcolonne++;
			if(compteur==SCORE_TO_WIN)
			{
				if(joueur==1)
				{
				StaticMethods.sendString(Consts.WIN,sortie1);
				StaticMethods.sendString(Consts.DEFEAT,sortie2);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie1);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie2);
				}
				else
				{
				StaticMethods.sendString(Consts.WIN,sortie2);
				StaticMethods.sendString(Consts.DEFEAT,sortie1);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie1);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie2);
				}
				return true;
			}
			
		}while((testligne<nbline-1)&&(testligne>-1)&&(testcolonne<nbcolumn-1)&&(testcolonne>-1)&&(grid[testligne][testcolonne]==joueur));
		

		testligne=ligne;
		testcolonne=colonne;
		compteur=0;
		
		//TEST DIAGONAL 4
		do{
			compteur++;
			testligne--;
			testcolonne--;
			if(compteur==SCORE_TO_WIN)
			{
				if(joueur==1)
				{
				StaticMethods.sendString(Consts.WIN,sortie1);
				StaticMethods.sendString(Consts.DEFEAT,sortie2);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie1);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie2);
				}
				else
				{
				StaticMethods.sendString(Consts.WIN,sortie2);
				StaticMethods.sendString(Consts.DEFEAT,sortie1);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie1);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie2);
				}
				return true;
			}
			
		}while((testligne<nbline-1)&&(testligne>-1)&&(testcolonne<nbcolumn-1)&&(testcolonne>-1)&&(grid[testligne][testcolonne]==joueur));
		
		//TestHorizontal
		

		testligne=ligne;
		testcolonne=colonne;
		compteur=0;
		// TEST HORIZONTAL 1
		do{
			compteur++;
			testcolonne++;
			if(compteur==SCORE_TO_WIN)
			{
				if(joueur==1)
				{
				StaticMethods.sendString(Consts.WIN,sortie1);
				StaticMethods.sendString(Consts.DEFEAT,sortie2);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie1);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie2);
				}
				else
				{
				StaticMethods.sendString(Consts.WIN,sortie2);
				StaticMethods.sendString(Consts.DEFEAT,sortie1);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie1);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie2);
				}
				return true;
			}
			
		}while((testligne<nbline-1)&&(testligne>-1)&&(testcolonne<nbcolumn-1)&&(testcolonne>-1)&&(grid[testligne][testcolonne]==joueur));
		

		testligne=ligne;
		testcolonne=colonne;
		compteur=0;
		
		// TEST HORIZONTAL 2
		do{
			compteur++;
			testcolonne--;
			if(compteur==SCORE_TO_WIN)
			{
				if(joueur==1)
				{
				StaticMethods.sendString(Consts.WIN,sortie1);
				StaticMethods.sendString(Consts.DEFEAT,sortie2);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie1);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie2);
				}
				else
				{
				StaticMethods.sendString(Consts.WIN,sortie2);
				StaticMethods.sendString(Consts.DEFEAT,sortie1);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie1);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie2);
				}
				return true;
			}
			
		}while((testligne<nbline-1)&&(testligne>-1)&&(testcolonne<nbcolumn-1)&&(testcolonne>-1)&&(grid[testligne][testcolonne]==joueur));
		
		
		//TestVertical
		

		testligne=ligne;
		testcolonne=colonne;
		compteur=0;
		// TEST VERTICAL 1
		do{
			compteur++;
			testligne++;
			if(compteur==SCORE_TO_WIN)
			{
				if(joueur==1)
				{
				StaticMethods.sendString(Consts.WIN,sortie1);
				StaticMethods.sendString(Consts.DEFEAT,sortie2);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie1);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie2);
				}
				else
				{
				StaticMethods.sendString(Consts.WIN,sortie2);
				StaticMethods.sendString(Consts.DEFEAT,sortie1);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie1);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie2);
				}
				return true;
			}
			
		}while((testligne<nbline-1)&&(testligne>-1)&&(testcolonne<nbcolumn-1)&&(testcolonne>-1)&&(grid[testligne][testcolonne]==joueur));
	
		testligne=ligne;
		testcolonne=colonne;
		compteur=0;
		
		//TEST VERTICAL 2
		do{
			compteur++;
			testligne--;
			if(compteur==SCORE_TO_WIN)
			{
				if(joueur==1)
				{
				StaticMethods.sendString(Consts.WIN,sortie1);
				StaticMethods.sendString(Consts.DEFEAT,sortie2);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie1);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie2);
				}
				else
				{
				StaticMethods.sendString(Consts.WIN,sortie2);
				StaticMethods.sendString(Consts.DEFEAT,sortie1);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie1);
				StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie2);
				}
				return true;
			}
			
		}while((testligne<nbline-1)&&(testligne>-1)&&(testcolonne<nbcolumn-1)&&(testcolonne>-1)&&(grid[testligne][testcolonne]==joueur));
	
		//TEST GRID FULL 
		int testGrid=0;
		for(int i = grid.length-1 ; i >-1; i-- ){
			      for(int j = 0; j< grid[i].length; j++){
			    	if(grid[i][j]==0)
			    	{
			    		testGrid++;
			    	}
			      }
			}
		
		if(testGrid==0)
		{
			StaticMethods.sendString(Consts.MSG_GRID_FULL,sortie1);
			StaticMethods.sendString(Consts.MSG_GRID_FULL,sortie2);
			StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie1);
			StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie2);
			return true;
		}
		
		
		if(joueur==1)
		{
			StaticMethods.sendString(Consts.CONTINUE,sortie1);
			StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie1);
		}
		else
		{
			StaticMethods.sendString(Consts.CONTINUE,sortie2);
			StaticMethods.sendString(Consts.MSG_END_OF_MESSAGE,sortie2);
		}
		return false;
	
	}
	
	
	private static void GameRules()
	{
		//Fonction qui sert à mettre a jour la variable des règles du jeu d'une manière dynamique
		
		rules = "Jeu de puissance "+SCORE_TO_WIN+" :\n";
		rules += "La grille est une grille de "+nbline+" lignes et "+nbcolumn+" colonnes\n";
		rules += "Le but du jeu est d'aligner "+SCORE_TO_WIN+" pions horizontalement, verticalement ou en diagonale";
		rules += "Tour à tour dans la colonne de leur choix, le pions coulisse alors jusqu'à la position la plus basse de ladite colonne à la suite de quoi c'est à l'adversaire de jouer.\n";
		rules += "Le premier joueur à aligner "+SCORE_TO_WIN+" est déclaré vanqueur\n";
		rules += "Si la grille est pleine avant qu'un des joueurs réussisse à remplir la grille, un match nul est prononcée\n";
		rules += "Vous pouvez rejouer avec votre adversaire, si vous le désiré tous les deux, si vous étiez J1 lors de partie, vous deviendrez J2 et vice versa par souci d'équité\n";
		rules += "Le jeu vous permet également de discuter avec votre adversaire pendant l'intégralité de la partie par le biais de la fenetre prévue à cet effet\n";
		rules += "Si vous voulez abandonner une partie en cours, entrer abandon lorsque c'est à votre tour de jouer\n";
	}
	
	public static void main(String[] args) {
		int i = 0;
		nbline=6;
		nbcolumn = 7;
		int myPort = Consts.DEFAULT_PORT_P4;
		int maxSimultaneousGame = MAX_SIMULTANEOUS_GAME;

		if(args.length>0){
		if(args[0]!=null){ myPort=Integer.parseInt(args[0]); }
		if(args.length>1 && args[1]!=null){ maxSimultaneousGame=Integer.parseInt(args[1]); }
		}
		
		
		StaticMethods.consolePrintln("Serveur multijoueurs de jeu de puissance 4");
		GameRules();
		try {		
				ServerSocket s = new ServerSocket(myPort);
				while(i < maxSimultaneousGame*2) {// Par défaut : Pas plus de 10 connexions simultanees -> donc 5 parties
				//Socket socket = s.accept();
				Socket socket = s.accept();
				RefereePuissance4 smt = new RefereePuissance4(socket);
				
					if((socLastJoueur1!=null) && (socLastJoueur2!=null))//Une fois les DEUX joueurs connectés au serveur.
					{
						//Permet d'envoyer le nom de l'aversaire au joueur sans entrer dans le run
						PrintWriter sortietmp1 = new PrintWriter (socLastJoueur1.getOutputStream(), true);
						PrintWriter sortietmp2 = new PrintWriter (socLastJoueur2.getOutputStream(), true);
						
						sortietmp1.println(nameJoueur2);
						sortietmp2.println(nameJoueur1);
						
						//On execute le thread qui gere le match de Puissance 4
						smt.start();
						StaticMethods.consolePrintln("Un match en cours, Identifiant du Thread :"+smt.getId()+" [ "+nameJoueur1+" vs "+nameJoueur2+" ]");
					}
				++i; 
				}
			s.close();
		}catch (SocketException e){
			StaticMethods.consolePrintln(Consts.CONNEXION_PLA_FAILED);
			
		}catch (NullPointerException e){
			StaticMethods.consolePrintln("Impossible d'envoyer au serveur");
			
		} catch (IOException e) {
			e.getMessage();
		}
	}
}