package poker.server;

import java.net.Socket;
import java.util.ArrayList;

import playingCards.Card;
import playingCards.Deck;

public class PokerTableThread extends Thread {

	static final int MAX_PLAYER_LIMIT = 5;
	static final int MIN_PLAYER_TO_START_GAME = 2;
	
	private ArrayList<PokerPlayer> players = new ArrayList<PokerPlayer>();
	private ArrayList<PokerPlayer> queuedPlayers = new ArrayList<PokerPlayer>();
	
	private Deck deck;
	private Card commonCard1;
	private Card commonCard2;
	private Card commonCard3;
	private Card commonCard4;
	private Card commonCard5;
	
	private Boolean isGameRunning = false;
	
	public Boolean hasCapacity() {
		return players.size() + queuedPlayers.size() < MAX_PLAYER_LIMIT;
	}
	
	public PokerPlayer addPlayer(Socket socket, int amount) {
		if (!hasCapacity()) {
			return null;
		}
		
		PokerPlayer player = new PokerPlayer(this, socket, amount);
		String msgFromPlayer = player.receiveMessage();
		if (msgFromPlayer.startsWith("CMD:PLAYER_NAME ")) {
			String playerName = msgFromPlayer.substring("CMD:PLAYER_NAME ".length());
			player.setPlayerName(playerName);
		}
		
		if (!isGameRunning) {
			players.add(player);
			if (players.size() < MIN_PLAYER_TO_START_GAME) {
				player.sendMessage("Welcome, please wait for other players to join");
			} else { 
				player.sendMessage("Welcome");
			}
		} else {
			queuedPlayers.add(player);
			player.sendMessage("Welcome, please wait for current game to finish");
		}
		
		if (!this.isAlive()) {
			if (players.size() >= MIN_PLAYER_TO_START_GAME) {
				this.start();
			}
		}
		return player;
	}
	
	public void run() {
		deck = Deck.getNewDeck();
		
		while (true) {
			
			isGameRunning = true;
			
			// Reset & Shuffle Deck
			deck.reset();
			deck.suffle();
			
			// Give each Player 2 Cards
			for (PokerPlayer player : players) {
				player.setInHandCard1(deck.pickCardFromTop());
				player.setInHandCard2(deck.pickCardFromTop());
			}
			
			// Place 5 Cards on Table
			commonCard1 = deck.pickCardFromTop();
			commonCard2 = deck.pickCardFromTop();
			commonCard3 = deck.pickCardFromTop();
			commonCard4 = deck.pickCardFromTop();
			commonCard5 = deck.pickCardFromTop();
			
			// Play 3 Betting Rounds
			for (int round = 1; round <= 3; round++) {
				Boolean isCallEnabledForThisRound = true;
				
				for (PokerPlayer player : players) {
					if (isCallEnabledForThisRound) {
						player.sendCommand("SELECT_OPTION CALL RAISE FOLD");
					} else {
						player.sendCommand("SELECT_OPTION RAISE FOLD");
					}
					
					String msgReceivedFromPlayer = player.receiveMessage();
					System.out.println("Player (" + player.getPlayerName() + "): " + msgReceivedFromPlayer);
					
					if (msgReceivedFromPlayer.startsWith("CMD:")) {
						String cmd = msgReceivedFromPlayer.substring("CMD:".length());
						
						if (cmd.startsWith("CALL")) {
							// Do Nothing
						} else if (cmd.startsWith("RAISE")) {
							// TODO: Raise
							isCallEnabledForThisRound = false;
						} else if (cmd.startsWith("FOLD")) {
							// TODO: Fold
						}
					} else {
						// Send Message To All Players
					}
				}
				
				if (round == 1) {
					for (PokerPlayer player : players) {
						player.sendCommand("COMMON_CARD_1 " + commonCard1.getCardName());
						player.sendCommand("COMMON_CARD_2 " + commonCard2.getCardName());
						player.sendCommand("COMMON_CARD_3 " + commonCard3.getCardName());
					}
				} else if (round == 2) {
					for (PokerPlayer player : players) {
						player.sendCommand("COMMON_CARD_4 " + commonCard4.getCardName());
					}
				} else if (round == 3) {
					for (PokerPlayer player : players) {
						player.sendCommand("COMMON_CARD_5 " + commonCard5.getCardName());
					}
				} 
			}
			
			isGameRunning = false;
			
			players.addAll(queuedPlayers);
			queuedPlayers.clear();
			
			break;
		}
	}
}
