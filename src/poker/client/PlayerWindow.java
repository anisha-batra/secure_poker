package poker.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.codec.binary.Base64;

import playingCards.Card;

public class PlayerWindow extends Thread {

	static final String hostName = "localhost";
	static final int portNumber = 1978;
	
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	
	PrivateKey selfPrivateKey;
	Cipher cipherToEncryptSelfMsg;
	
	PublicKey serverPublicKey;
	Cipher cipherToDecryptServerMsg;
	
	Card inHandCard1;
	Card inHandCard2;
	
	Card commonCard1;
	Card commonCard2;
	Card commonCard3;
	Card commonCard4;
	Card commonCard5;
	
	JFrame mainFrame = new JFrame();
	
	JTextField tfPlayerName = new JTextField();
	JButton btnConnectToServer = new JButton("Login");
	
	JLabel commonCard1Label = new JLabel();
	JLabel commonCard2Label = new JLabel();
	JLabel commonCard3Label = new JLabel();
	JLabel commonCard4Label = new JLabel();
	JLabel commonCard5Label = new JLabel();
	
	JLabel inHandCard1Label = new JLabel();
	JLabel inHandCard2Label = new JLabel();
	
	JButton btnCall = new JButton("Call");
	JButton btnRaise = new JButton("Raise");
	JButton btnFold = new JButton("Fold");
	
	JTextArea textArea = new JTextArea();

	public PlayerWindow() {
		openGameWindow();
		disablePlayerOptions();	
	}
	
	public static void main(String[] args) {
		new PlayerWindow();
	}
	
	public void run() {
		String msgFromPokerServer = null;
		try {
			while ((msgFromPokerServer = receiveMessage()) != null) {
				System.out.println("Server: " + msgFromPokerServer);
				
				if (msgFromPokerServer.startsWith("CMD:")) {
					String cmd = msgFromPokerServer.substring("CMD:".length());
					addLogMessage("Command From Server: " + cmd);
					
					if (cmd.startsWith("SELECT_OPTION")) {
						if (cmd.contains("RAISE")) {
							btnRaise.setEnabled(true);
						}
						if (cmd.contains("CALL")) {
							btnCall.setEnabled(true);
						}
						if (cmd.contains("FOLD")) {
							btnFold.setEnabled(true);
						}
					} else if (cmd.startsWith("IN_HAND_CARD_1")) {
						String cardName = cmd.split(" ")[1];
						inHandCard1 = Card.fromName(cardName);
						inHandCard1Label.setIcon(scaleImageIcon(new ImageIcon(inHandCard1.getCardImage()), 74, 100));
					} else if (cmd.startsWith("IN_HAND_CARD_2")) {
						String cardName = cmd.split(" ")[1];
						inHandCard2 = Card.fromName(cardName);
						inHandCard2Label.setIcon(scaleImageIcon(new ImageIcon(inHandCard2.getCardImage()), 74, 100));
					} else if (cmd.startsWith("COMMON_CARD_1")) {
						String cardName = cmd.split(" ")[1];
						commonCard1 = Card.fromName(cardName);
						commonCard1Label.setIcon(scaleImageIcon(new ImageIcon(commonCard1.getCardImage()), 74, 100));
					} else if (cmd.startsWith("COMMON_CARD_2")) {
						String cardName = cmd.split(" ")[1];
						commonCard2 = Card.fromName(cardName);
						commonCard2Label.setIcon(scaleImageIcon(new ImageIcon(commonCard2.getCardImage()), 74, 100));
					}  else if (cmd.startsWith("COMMON_CARD_3")) {
						String cardName = cmd.split(" ")[1];
						commonCard3 = Card.fromName(cardName);
						commonCard3Label.setIcon(scaleImageIcon(new ImageIcon(commonCard3.getCardImage()), 74, 100));
					} else if (cmd.startsWith("COMMON_CARD_4")) {
						String cardName = cmd.split(" ")[1];
						commonCard4 = Card.fromName(cardName);
						commonCard4Label.setIcon(scaleImageIcon(new ImageIcon(commonCard4.getCardImage()), 74, 100));
					} else if (cmd.startsWith("COMMON_CARD_5")) {
						String cardName = cmd.split(" ")[1];
						commonCard5 = Card.fromName(cardName);
						commonCard5Label.setIcon(scaleImageIcon(new ImageIcon(commonCard5.getCardImage()), 74, 100));
					}
				} else if (msgFromPokerServer.startsWith("MSG:")) {
					String msg = msgFromPokerServer.substring("MSG:".length());
					addLogMessage("Message From Server: " + msg);
				}
				
//				if (msgFromPokerServer.equals("Bye"))
//					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sendMessage(String msg) {
		msg = "MSG:" + msg;
		
		if (cipherToEncryptSelfMsg != null) {
			try {
				out.println(Base64.encodeBase64String(cipherToEncryptSelfMsg.doFinal(msg.getBytes("UTF-8"))));
			} catch (IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} else {
			out.println(msg);
		}
	}
	
	private void sendCommand(String msg) {
		msg = "CMD:" + msg;
		
		if (cipherToEncryptSelfMsg != null) {
			try {
				out.println(Base64.encodeBase64String(cipherToEncryptSelfMsg.doFinal(msg.getBytes("UTF-8"))));
			} catch (IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} else {
			out.println(msg);
		}
	}
	
	private String receiveMessage() {
		try {
			String msg = in.readLine();
			
			if (cipherToDecryptServerMsg != null) {
				return new String(cipherToDecryptServerMsg.doFinal(Base64.decodeBase64(msg)), "UTF-8");
			} else {
				return msg;
			}
		} catch (IOException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	private void openGameWindow() {
		mainFrame.setPreferredSize(new Dimension(500, 420));
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		mainFrame.add(mainPanel);

		// Panel - User Login
		JPanel jpnlUserLogin = new JPanel();
		jpnlUserLogin.setLayout(new BoxLayout(jpnlUserLogin, BoxLayout.LINE_AXIS));
		jpnlUserLogin.add(new JLabel("Enter Player Name:"));
		jpnlUserLogin.add(tfPlayerName);
		jpnlUserLogin.add(Box.createRigidArea(new Dimension(5, 0)));
		jpnlUserLogin.add(btnConnectToServer);
		mainPanel.add(jpnlUserLogin, BorderLayout.NORTH);
		
		// Panel - Common Cards
		JPanel jpnlPokerCommonCards = new JPanel();
		jpnlPokerCommonCards.setLayout(new BoxLayout(jpnlPokerCommonCards, BoxLayout.LINE_AXIS));
		commonCard1Label.setPreferredSize(new Dimension(66, 100));
		commonCard2Label.setPreferredSize(new Dimension(66, 100));
		commonCard3Label.setPreferredSize(new Dimension(66, 100));
		commonCard4Label.setPreferredSize(new Dimension(66, 100));
		commonCard5Label.setPreferredSize(new Dimension(66, 100));
		commonCard1Label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		commonCard2Label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		commonCard3Label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		commonCard4Label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		commonCard5Label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		jpnlPokerCommonCards.add(commonCard1Label);
		jpnlPokerCommonCards.add(Box.createRigidArea(new Dimension(5, 0)));
		jpnlPokerCommonCards.add(commonCard2Label);
		jpnlPokerCommonCards.add(Box.createRigidArea(new Dimension(5, 0)));
		jpnlPokerCommonCards.add(commonCard3Label);
		jpnlPokerCommonCards.add(Box.createRigidArea(new Dimension(5, 0)));
		jpnlPokerCommonCards.add(commonCard4Label);
		jpnlPokerCommonCards.add(Box.createRigidArea(new Dimension(5, 0)));
		jpnlPokerCommonCards.add(commonCard5Label);
		
		// Panel - In Hand Cards
		JPanel jpnlInHandCards = new JPanel();
		jpnlInHandCards.setLayout(new BoxLayout(jpnlInHandCards, BoxLayout.LINE_AXIS));
		inHandCard1Label.setPreferredSize(new Dimension(74, 100));
		inHandCard2Label.setPreferredSize(new Dimension(74, 100));
		inHandCard1Label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		inHandCard2Label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		jpnlInHandCards.add(inHandCard1Label);
		jpnlInHandCards.add(Box.createRigidArea(new Dimension(5, 0)));
		jpnlInHandCards.add(inHandCard2Label);
		
		// Panel - Center
		JPanel jpnlCenter = new JPanel();
		jpnlCenter.setLayout(new BoxLayout(jpnlCenter, BoxLayout.PAGE_AXIS));
		jpnlCenter.add(Box.createRigidArea(new Dimension(5, 5)));
		jpnlCenter.add(jpnlPokerCommonCards);
		jpnlCenter.add(Box.createRigidArea(new Dimension(5, 20)));
		jpnlCenter.add(jpnlInHandCards);
		jpnlCenter.add(Box.createRigidArea(new Dimension(5, 5)));
		mainPanel.add(jpnlCenter, BorderLayout.CENTER);
		
		// Panel - Poker Player Options
		JPanel jpnlPokerOptions = new JPanel();
		jpnlPokerOptions.setLayout(new BoxLayout(jpnlPokerOptions, BoxLayout.LINE_AXIS));
		jpnlPokerOptions.add(btnCall);
		jpnlPokerOptions.add(Box.createRigidArea(new Dimension(5, 0)));
		jpnlPokerOptions.add(btnRaise);
		jpnlPokerOptions.add(Box.createRigidArea(new Dimension(5, 0)));
		jpnlPokerOptions.add(btnFold);

		// TextArea - for Logs
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(100, 100));
		textArea.setEditable(false);
		
		// Panel - Bottom
		JPanel jpnlBottom = new JPanel();
		jpnlBottom.setLayout(new BoxLayout(jpnlBottom, BoxLayout.PAGE_AXIS));
		jpnlBottom.add(jpnlPokerOptions);
		jpnlBottom.add(scrollPane);
		mainPanel.add(jpnlBottom, BorderLayout.SOUTH);

		mainFrame.pack();
		mainFrame.setVisible(true);
		
		// Event Handlers
		btnConnectToServer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (tfPlayerName.getText().isEmpty()) {
					JOptionPane.showMessageDialog(mainFrame
							, "Kindly enter a player name before connecting to server"
							, "Enter Player Name"
							, JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				addLogMessage("Connecting To Poker Server...");
				try {
					socket = new Socket(hostName, portNumber);
					out = new PrintWriter(socket.getOutputStream(), true);
					in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					addLogMessage("Connection Successful");
				} catch (Exception e) {
					addLogMessage("Connection Failed: " + e.getMessage());
					e.printStackTrace();
					return;
				}
					
				tfPlayerName.setEnabled(false);
				btnConnectToServer.setEnabled(false);
				
				// Show Common Cards - Reversed
				commonCard1Label.setIcon(scaleImageIcon(new ImageIcon("img/card_reverse.png"), 74, 100));
				commonCard2Label.setIcon(scaleImageIcon(new ImageIcon("img/card_reverse.png"), 74, 100));
				commonCard3Label.setIcon(scaleImageIcon(new ImageIcon("img/card_reverse.png"), 74, 100));
				commonCard4Label.setIcon(scaleImageIcon(new ImageIcon("img/card_reverse.png"), 74, 100));
				commonCard5Label.setIcon(scaleImageIcon(new ImageIcon("img/card_reverse.png"), 74, 100));
				
				// Show In Hand Cards - Reversed
				inHandCard1Label.setIcon(scaleImageIcon(new ImageIcon("img/card_reverse.png"), 74, 100));
				inHandCard2Label.setIcon(scaleImageIcon(new ImageIcon("img/card_reverse.png"), 74, 100));
				
				// Get Public Key from Server
				try {
					// Read Message from Server
					String strServerPublicKey = in.readLine();
					// Create PublicKey Object from Key String
					X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decodeBase64(strServerPublicKey)); //  strServerPublicKey.getBytes(StandardCharsets.UTF_8));
					KeyFactory keyFactory = KeyFactory.getInstance("RSA");
					serverPublicKey = keyFactory.generatePublic(keySpec);
					// Initialize Cipher to Decrypt Server Messages
					cipherToDecryptServerMsg = Cipher.getInstance("RSA");
					cipherToDecryptServerMsg.init(Cipher.DECRYPT_MODE, serverPublicKey);
					
					addLogMessage("Received public key from server");
				} catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | IOException | NoSuchPaddingException e) {
					e.printStackTrace();
				}
				
				// Generate Public and Private Keys
				try {
					// Get RSA Key Generator
					KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
					// Set Key Length to 1024 Bytes
					keyGen.initialize(1024);
					// Generate KeyPair (Public + Private)
					KeyPair key = keyGen.generateKeyPair();
					// Get Private Key Object
					selfPrivateKey = key.getPrivate();
					// Initialize Cipher to Encrypt our Messages using Private Key
					cipherToEncryptSelfMsg = Cipher.getInstance("RSA");
					cipherToEncryptSelfMsg.init(Cipher.ENCRYPT_MODE, selfPrivateKey);
					// Get Public Key Object
					PublicKey selfPublicKey = key.getPublic();
					// Send Public Key to Server
					out.println(Base64.encodeBase64String(selfPublicKey.getEncoded()));
				} catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException e) {
					e.printStackTrace();
					return;
				}
				
				start();
				
				sendCommand("PLAYER_NAME " + tfPlayerName.getText());
			}
		});
		
		btnCall.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				disablePlayerOptions();
				sendCommand("CALL");
			}
		});
		
		btnRaise.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				disablePlayerOptions();
				sendCommand("RAISE");
			}
		});
		
		btnFold.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				disablePlayerOptions();
				sendCommand("FOLD");
			}
		});
	}
	
	private void disablePlayerOptions() {
		btnCall.setEnabled(false);
		btnRaise.setEnabled(false);
		btnFold.setEnabled(false);
	}
	
	private void addLogMessage(String msg) {
		textArea.append(msg + "\n");
	}
	
	private ImageIcon scaleImageIcon(ImageIcon srcImgIcon, int w, int h){
		Image srcImg = srcImgIcon.getImage();
		
	    BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g2 = resizedImg.createGraphics();

	    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g2.drawImage(srcImg, 0, 0, w, h, null);
	    g2.dispose();

	    srcImgIcon.setImage(resizedImg);
	    
	    return srcImgIcon;
	}
}
