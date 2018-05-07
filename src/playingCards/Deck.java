package playingCards;

import java.util.ArrayList;
import java.util.Collections;

public class Deck {

	private ArrayList<Card> cards = new ArrayList<Card>();

	private Deck() {
		// Do Nothing
	}

	public static Deck getNewDeck() {
		Deck deck = new Deck();
		deck.initialize();
		return deck;
	}

	private void initialize() {
		// Add Cards to Deck
		for (Suite suite : Suite.values()) {
			for (int rank = 2; rank <= 14; rank++) {
				Card card = new Card(suite, rank);
				this.cards.add(card);
			}
		}
	}

	public void reset() {
		this.cards.clear();
		this.initialize();
	}

	public void suffle() {
		Collections.shuffle(cards);
	}

	public Boolean isEmpty() {
		return cards.isEmpty();
	}

	public Card pickCardFromTop() {
		if (this.isEmpty()) {
			return null;
		}

		// Get Card from the Top
		Card card = cards.get(cards.size() - 1);

		// Remove Card from Deck
		cards.remove(card);

		return card;
	}

}
