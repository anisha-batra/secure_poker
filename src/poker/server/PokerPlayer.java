package poker.server;

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
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;

import playingCards.Card;

public class PokerPlayer {

	PrivateKey selfPrivateKey;
	Cipher cipherToEncryptSelfMsg;
	
	PublicKey clientPublicKey;
	Cipher cipherToDecryptClientMsg;
	
	private String playerName;
	private int amount = 0;
	private Card inHandCard1;
	private Card inHandCard2;

	private PokerTableThread table;
	private Socket socket;

	private BufferedReader in = null;
	private PrintWriter out = null;

	public PokerPlayer(PokerTableThread table, Socket socket, int amount) {
		this.amount = amount;
		this.table = table;
		this.socket = socket;
		
		try {
			this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			this.out = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			return;
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
			// Send Public Key to Client
			out.println(Base64.encodeBase64String(selfPublicKey.getEncoded()));
		} catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException e) {
			e.printStackTrace();
			return;
		}
		
		// Get Public Key from Client
		try {
			// Read Message from Client
			String strClientPublicKey = in.readLine();
			// Create PublicKey Object from Key String
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decodeBase64(strClientPublicKey)); // strServerPublicKey.getBytes(StandardCharsets.UTF_8));
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			clientPublicKey = keyFactory.generatePublic(keySpec);
			// Initialize Cipher to Decrypt Client Messages
			cipherToDecryptClientMsg = Cipher.getInstance("RSA");
			cipherToDecryptClientMsg.init(Cipher.DECRYPT_MODE, clientPublicKey);
			
			System.out.println("Received public key from client");
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | IOException | NoSuchPaddingException e) {
			e.printStackTrace();
		}
	}
	
	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public Card getInHandCard1() {
		return inHandCard1;
	}

	public void setInHandCard1(Card inHandCard1) {
		this.inHandCard1 = inHandCard1;

		if (inHandCard1 != null) {
			sendCommand("IN_HAND_CARD_1 " + inHandCard1.getCardName());
		} else {
			sendCommand("IN_HAND_CARD_1 NULL");
		}
	}

	public Card getInHandCard2() {
		return inHandCard2;
	}

	public void setInHandCard2(Card inHandCard2) {
		this.inHandCard2 = inHandCard2;

		if (inHandCard2 != null) {
			sendCommand("IN_HAND_CARD_2 " + inHandCard2.getCardName());
		} else {
			sendCommand("IN_HAND_CARD_2 NULL");
		}
	}

	public void sendMessage(String msg) {
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
	
	public void sendCommand(String msg) {
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
	
	public String receiveMessage() {
		try {
			String msg = in.readLine();
			
			if (cipherToDecryptClientMsg != null) {
				return new String(cipherToDecryptClientMsg.doFinal(Base64.decodeBase64(msg)), "UTF-8");
			} else {
				return msg;
			}
		} catch (IOException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
