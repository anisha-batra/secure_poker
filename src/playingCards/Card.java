package playingCards;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;

import javax.imageio.ImageIO;

public class Card {

	private Suite suite;
	private int rank;
	
	public Card(Suite suite, int rank) {
		this.suite = suite;
		this.rank = rank;
	}
	
	public Suite getSuite() {
		return suite;
	}
	
	public int getRank() {
		return rank;
	}
	
	public String getCardName() {
		return suite.toString() + "_" + rank;
	}
	
	public static Card fromName(String cardName) {
		if (cardName.equals("NULL")) {
			return null;
		}
		
		Suite suite = Suite.valueOf(cardName.split("_")[0]);
		int rank = Integer.parseInt(cardName.split("_")[1]);
		
		return new Card(suite, rank);
	}
	
	public BufferedImage getCardImage() {
		BufferedImage originalImage = null;
		try {
		    originalImage = ImageIO.read(new File("img/all_cards.png"));
		} catch (IOException e) {
			return null;
		}
		
		int spacing = 3;
		int width = 70;
		int height = 94;
		
		int y = 0;
		if (suite == Suite.CLUB) {
			y = spacing;
		} else if (suite == Suite.SPADE) {
			y = (2 * spacing) + height;
		} else if (suite == Suite.HEART) {
			y = (3 * spacing) + (2 * height);
		} else if (suite == Suite.DIAMOND) {
			y = (4 * spacing) + (3 * height);
		}
		
		int x = 0;
		if (rank == 14) {
			x = spacing;
		} else {
			x = (rank * spacing) + ((rank - 1) * width);
		}
		
		BufferedImage croppedImage = originalImage.getSubimage(x, y, width, height);
		
		return croppedImage;
	}
	
	public static Comparator<Card> cardComparator = new Comparator<Card>() {
		@Override
		public int compare(Card card0, Card card1) {
			return Integer.compare(card0.getRank(), card1.getRank());
		}
	};
}
