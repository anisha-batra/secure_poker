package poker;

import java.util.ArrayList;

import playingCards.Card;
import playingCards.Suite;

public abstract class Hand {

	ArrayList<Card> cards = new ArrayList<>();

	private Card firstCard() {
		if (cards.size() <= 0) {
			return null;
		}

		return cards.get(0);
	}

	private Card secondCard() {
		if (cards.size() <= 1) {
			return null;
		}

		return cards.get(1);
	}

	private Card thirdCard() {
		if (cards.size() <= 2) {
			return null;
		}

		return cards.get(2);
	}

	private Card fourthCard() {
		if (cards.size() <= 3) {
			return null;
		}

		return cards.get(3);
	}

	private Card fifthCard() {
		if (cards.size() <= 4) {
			return null;
		}

		return cards.get(4);
	}

	private void sortHand() {
		cards.sort(Card.cardComparator);
	}

	private Boolean isRoyalFlush() {
		// Should Be STRAIGHT_FLUSH and 5th Card Should Be ACE
		if (isStraightFlush() && fifthCard().getRank() == 14) {
			return true;
		}

		return false;
	}

	private Boolean isStraightFlush() {
		// Should Be FLUSH and STRAIGHT
		if (isFlush() && isStraight()) {
			return true;
		}

		return false;
	}

	private Boolean isFourOfAKind() {
		// Either 1st and 4th Card Have Same Rank
		if (firstCard().getRank() == fourthCard().getRank()) {
			return true;
		}

		// Or 2nd and 5th Card Have Same Rank
		if (secondCard().getRank() == fifthCard().getRank()) {
			return true;
		}

		return false;
	}

	private Boolean isFullHouse() {
		// Either 1st and 3rd Card have Same Rank
		// and 4th and 5th have Same Rank
		if ((firstCard().getRank() == thirdCard().getRank()) 
				&& (fourthCard().getRank() == fifthCard().getRank())) {
			return true;
		}

		// Or 1st and 2nd Card have Same Rank
		// and 3rd and 5th have Same Rank
		if ((firstCard().getRank() == secondCard().getRank()) 
				&& (thirdCard().getRank() == fifthCard().getRank())) {
			return true;
		}

		return false;
	}

	private Boolean isFlush() {
		if (cards.isEmpty()) {
			return false;
		}

		// All Cards Should Have Same Suite
		Suite suiteOfFirstCard = cards.get(0).getSuite();
		for (int i = 1; i < cards.size(); i++) {
			Suite suiteOfCurrCard = cards.get(i).getSuite();
			if (suiteOfCurrCard != suiteOfFirstCard) {
				return false;
			}
		}

		return true;
	}

	private Boolean isStraight() {
		if (cards.isEmpty()) {
			return false;
		}

		// Each Card Should Be 1 Greater Than Previous Card
		for (int i = 0; i < cards.size() - 1; i++) {
			Card currCard = cards.get(i);
			Card nextCard = null;

			if (i + 1 < cards.size()) {
				nextCard = cards.get(i + 1);
			}

			if (nextCard == null) {
				return false;
			}

			if (nextCard.getRank() - currCard.getRank() != 1) {
				return false;
			}
		}

		return true;
	}

	private Boolean isThreeOfAKind() {
		// Either 1st and 3rd Card Have Same Rank
		if (firstCard().getRank() == thirdCard().getRank()) {
			return true;
		}

		// Or 2nd and 4th Card Have Same Rank
		if (secondCard().getRank() == fourthCard().getRank()) {
			return true;
		}
		
		// Or 3rd and 5th Card Have Same Rank
		if (thirdCard().getRank() == fifthCard().getRank()) {
			return true;
		}

		return false;
	}

	private Boolean isTwoPair() {
		// Either 1st and 2nd Card have Same Rank
		// and 3rd and 4th Card have Same Rank
		if ((firstCard().getRank() == secondCard().getRank())
				&& (thirdCard().getRank() == fourthCard().getRank())) {
			return true;
		}
		
		// Or 1st and 2nd Card have Same Rank
		// and 4th and 5th Card have Same Rank
		if ((firstCard().getRank() == secondCard().getRank())
				&& (fourthCard().getRank() == fifthCard().getRank())) {
			return true;
		}
		
		// Or 2nd and 3nd Card have Same Rank
		// and 4th and 5th Card have Same Rank
		if ((secondCard().getRank() == thirdCard().getRank())
				&& (fourthCard().getRank() == fifthCard().getRank())) {
			return true;
		}

		return false;
	}

	private Boolean isOnePair() {
		// Either 1st and 2nd Card have Same Rank
		if (firstCard().getRank() == secondCard().getRank()) {
			return true;
		}
		
		// Or 2nd and 3rd Card have Same Rank
		if (secondCard().getRank() == thirdCard().getRank()) {
			return true;
		}
		
		// Or 3rd and 4th Card have Same Rank
		if (thirdCard().getRank() == fourthCard().getRank()) {
			return true;
		}
		
		// Or 4th and 5th Card have Same Rank
		if (fourthCard().getRank() == fifthCard().getRank()) {
			return true;
		}

		return false;
	}

	private Card getHighestCard() {
		return fifthCard();
	}

	private HandType getHandType() {
		// Sort all Cards in Hand to make Algorithm Easy
		sortHand();
		
		// Check for Hand Type in Decreasing Order of Rank
		if (isRoyalFlush()) {
			return HandType.ROYAL_FLUSH;
		} else if (isStraightFlush()) {
			return HandType.STRAIGHT_FLUSH;
		} else if (isFourOfAKind()) {
			return HandType.FOUR_OF_A_KIND;
		} else if (isFullHouse()) {
			return HandType.FULL_HOUSE;
		} else if (isFlush()) {
			return HandType.FLUSH;
		} else if (isStraight()) {
			return HandType.STRAIGHT;
		} else if (isThreeOfAKind()) {
			return HandType.THREE_OF_A_KIND;
		} else if (isTwoPair()) {
			return HandType.TWO_PAIR;
		} else if (isOnePair()) {
			return HandType.ONE_PAIR;
		}
		
		return HandType.HIGH_CARD;
	}
}
