package projet;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.SocketException;

import javax.swing.JScrollPane;

public class IHMChatUDP extends JFrame {

	private JPanel contentPane;
	private JTextField txtSaisissezVotreMessage;
	private JTextArea txtMsgs;
	//Permet d'intéragir avec le Classe Chat : Notamment pour envoyer un msg à l'autre joueur via UDP
	private final ChatUDP chat;
	private JScrollPane scrollPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		/*EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					IHMChatTest frame = new IHMChatTest();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});*/
	}

	/**
	 * Create the frame.
	 */
	public IHMChatUDP(final ChatUDP chat) {
		setTitle("izi");
		this.chat = chat;
		setTitle("Discussion avec :"+ chat.getNameOpponent());
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(86, 36, 309, 151);
		contentPane.add(scrollPane);
		
		txtMsgs = new JTextArea();
		scrollPane.setViewportView(txtMsgs);
		txtMsgs.setEditable(false);
		
		txtSaisissezVotreMessage = new JTextField();
		txtSaisissezVotreMessage.setBounds(86, 215, 220, 57);
		contentPane.add(txtSaisissezVotreMessage);
		txtSaisissezVotreMessage.setColumns(10);
		
		JButton btnEnvoyer = new JButton("Envoyer");
		btnEnvoyer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					chat.sendMsg(txtSaisissezVotreMessage.getText());
					txtSaisissezVotreMessage.setText("");
				} catch (SocketException e) {
					StaticMethods.consolePrintln(Consts.CONNEXION_OPP_INTERRUPTED);
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
		});
		btnEnvoyer.setBounds(318, 216, 77, 56);
		contentPane.add(btnEnvoyer);
	}
	
	public void setTextArea(String m){
		//Saisie du texte dans la JTextArea
		this.txtMsgs.setText(this.txtMsgs.getText()+"\n"+m);
	}
}
