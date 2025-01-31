import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;


public class BlackJack {

    //Class to create card objects
    private class Card {
        String value;
        String type;

        //Card constructor
        Card(String value, String type) {
            this.value = value;
            this.type = type;
        }
        //override toString to print card format of value anbd type
        public String toString() {
            return value + "-" + type;
        }

        //Create Function to get the value of each card
        public int getValue() {
            if ("AJQK".contains(value)) { //if statement to cover Ace, Jack, Queen and King
                if (value == "A") {
                    return 11;
                }
                return 10;
            }
            return Integer.parseInt(value); //Else return integer value of 2-10
        }

        //Function to test if card is an ace
        public boolean isAce() {
            return value == "A";
        }

        public String getImagePath () {
            return "./cards/" + toString() + ".png";
        }
    }

    //Create an Arraylist object named deck
    ArrayList<Card> deck;

    //Create an instance of Random to be used when shuffling the deck
    Random random = new Random();

    //Dealer variables
    Card hiddenCard;
    ArrayList<Card> dealerHand;
    int dealerSum;
    int dealerAceCount; //used to track if dealer has an ace incase dealerSum goes over 21, ace can be changed from 11 to 1

    //Player variables
    ArrayList<Card> playerHand;
    int playerSum;
    int playerAceCount; //used to track if player has an ace incase playerSum goes over 21, ace can be changed from 11 to 1

    //GUI Variables
    //window size
    int boardWidth = 600;
    int boardHeight = boardWidth;

    //card size ratio 1 to 1.4
    int cardWidth = 110;
    int cardHeight = 154;

    //chip pool and betting round variables
    int chipsPool = 100;
    int currentBet = 20;

    //Create GUI and button action listeners
    JFrame frame = new JFrame("Black Jack");
    JPanel gamePanel = new JPanel() {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            try{
                //draw dealers hidden card
                Image hiddenCardImg = new ImageIcon(getClass().getResource("./cards/BACK.png")).getImage();
                if (!stayButton.isEnabled()) {
                    hiddenCardImg = new ImageIcon(getClass().getResource(hiddenCard.getImagePath())).getImage();
                }
                //sets dealers first card 20 pixels from left and 20 pixels down from top
                g.drawImage(hiddenCardImg, 20, 20, cardWidth, cardHeight, null);

                //draw dealers subsequest visible cards
                for (int i = 0; i < dealerHand.size(); i++) {
                    Card card = dealerHand.get(i);
                    Image cardImage = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    //sets all subsequest cards to the left of the hidden card with a space of 5 between them
                    g.drawImage(cardImage, cardWidth + 25 + (cardWidth + 5)*i, 20, cardWidth, cardHeight, null);
                }

                //draw players cards
                for (int i = 0; i < playerHand.size(); i++) {
                  Card card = playerHand.get(i);
                  Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                  //sets first card to 20 pixels from left, then each subsequent card with a space of 5
                  g.drawImage(cardImg, 20 + (cardWidth + 5)*i, 320, cardWidth, cardHeight, null);
                }

                //display ongoing chip count
                String displayChips = "Chips: " + Integer.toString(chipsPool);
                g.setFont(new Font("Arial", Font.PLAIN, 30));
                g.setColor(Color.white);
                g.drawString(displayChips, 415, 510);

                //Logic to decide who won once the stay button has been clicked
                if(!stayButton.isEnabled()) {
                    dealerSum = reduceDealerAce();
                    playerSum = reducePlayerAce();
                    System.out.println("Stay: ");
                    System.out.println(dealerSum);
                    System.out.println(playerSum);

                    String message = "";
                    if (playerSum > 21) {
                        chipsPool -= currentBet;
                        System.out.println(chipsPool);
                        message = "You Lose!";
                    }
                    else if (dealerSum > 21) {
                        chipsPool += currentBet;
                        System.out.println(chipsPool);
                        message = "You Win!";
                    }
                    else if (playerSum == dealerSum) {
                        message = "Draw!";
                    }
                    else if (playerSum > dealerSum) {
                        chipsPool += currentBet;
                        System.out.println(chipsPool);
                        message = "You Win!";
                    }
                    else if (playerSum < dealerSum) {
                        chipsPool -= currentBet;
                        System.out.println(chipsPool);
                        message = "You Lose!";
                    }

                   g.setFont(new Font("Arial", Font.PLAIN, 30));
                   g.setColor(Color.white);
                   g.drawString(message, 220, 250);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    JPanel buttonPanel = new JPanel();
    JButton hitButton = new JButton("Hit");
    JButton stayButton = new JButton("Stay");
    JButton playAgainButton = new JButton("Play Again");

    //Constructor for black jack game
    BlackJack() {
        startGame();
        
        //Create frame and show on screen
        frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        gamePanel.setLayout(new BorderLayout());
        gamePanel.setBackground(new Color(53, 101, 77));
        frame.add(gamePanel);

        hitButton.setFocusable(false);
        buttonPanel.add(hitButton);
        stayButton.setFocusable(false);
        buttonPanel.add(stayButton);
        playAgainButton.setFocusable(false);
        buttonPanel.add(playAgainButton);
        frame.add(buttonPanel,BorderLayout.SOUTH);

        //pop up window to set bet for the current round
        setBet();

        //action listener to tell what to do when the "hit" button is clicked
        hitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Card card = deck.remove(deck.size()-1);
                playerSum += card.getValue();
                playerAceCount += card.isAce() ? 1 : 0;
                playerHand.add(card);
                if (reducePlayerAce() >= 21) {
                    hitButton.setEnabled(false);
                }
                gamePanel.repaint();
            }
        });

        stayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hitButton.setEnabled(false);
                stayButton.setEnabled(false);

                while (dealerSum < 17) {
                    Card card = deck.remove(deck.size()-1);
                    dealerSum += card.getValue();
                    dealerAceCount += card.isAce() ? 1 : 0;
                    dealerHand.add(card);
                }
                gamePanel.repaint();
            }
        });

        playAgainButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startGame();
                hitButton.setEnabled(true);
                stayButton.setEnabled(true);
                gamePanel.repaint();
                setBet();

            }
        });

        gamePanel.repaint();
    }



//This section contains all Java Functions-------------------------------------------------------------------------------------------------------

    public void startGame() {


        //create the deck 
        buildDeck();
        shuffleDeck();

        //dealer variables initialized
        dealerHand = new ArrayList<Card>();
        dealerSum = 0;
        dealerAceCount = 0;

        //Deal cards to Dealer
        //First deal hidden card
        hiddenCard = deck.remove(deck.size()-1); //deals to dealer from the back of the deck
        dealerSum += hiddenCard.getValue();
        dealerAceCount += hiddenCard.isAce() ? 1 : 0; //if the hidden card is an ace add 1 to dealer ace count if not add 0
        //second deal visible card
        Card card = deck.remove(deck.size()-1);
        dealerSum += card.getValue();
        dealerAceCount += card.isAce() ? 1 : 0;//if the card is an ace add 1 to dealer ace count if not add 0
        dealerHand.add(card);


        //Print commands to show dealers initial hand
        System.out.println("Dealer");
        System.out.println(hiddenCard);
        System.out.println(dealerHand);
        System.out.println(dealerSum);
        System.out.println(dealerAceCount);

        //player variables initialized
        playerHand = new ArrayList<Card>();
        playerSum = 0;
        playerAceCount = 0;

        //deal 2 cards to player
        for (int i = 0; i < 2; i++) {
            card = deck.remove(deck.size()-1);
            playerSum += card.getValue();
            playerAceCount += card.isAce() ? 1 : 0; //if the card is an ace add 1 to player ace count if not add 0
            playerHand.add(card);
        }

        //Print commands to show players initial hand
        System.out.println("Player");
        System.out.println(playerHand);
        System.out.println(playerSum);
        System.out.println(playerAceCount);
    }

    //Function to build the deck looping through the types, and values
    //Builds one deck of cards
    public void buildDeck() {
        deck = new ArrayList<Card>();
        String[] values = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        String[] types = {"C", "D", "H", "S"};

        for (int i = 0; i < types.length; i++) {
            for (int j = 0; j < values.length; j++) {
                Card card = new Card(values[j], types[i]);
                deck.add(card);
            }
        }

        //Print commands to show deck being built
        System.out.println("Build Deck");
        System.out.println(deck);

    }

    //Function to shuffle the deck of cards
    public void shuffleDeck() {
        for (int i = 0; i < deck.size(); i++) {
            int j = random.nextInt(deck.size());
            Card currCard = deck.get(i);
            Card randomCard = deck.get(j);
            deck.set(i, randomCard);
            deck.set(j, currCard);
        }

        //Princ commands to \show deck after shuffle
        System.out.println("AFTER SHUFFLE");
        System.out.println(deck);
    }
   
   
    //Function provides the logic to change the Ace from 11 to 1 if hand is over 21 total for player
    public int reducePlayerAce() {
        while (playerSum > 21 && playerAceCount > 0) {
            playerSum -= 10;
            playerAceCount -= 1;
        }
        return playerSum;
    }

    //Function provides the logic to change the Ace from 11 to 1 if hand is over 21 total for dealer
    public int reduceDealerAce() {
        while (dealerSum > 21 && dealerAceCount > 0) {
            dealerSum -= 10;
            dealerAceCount -= 1;
        }
        return dealerSum;
    }

    //Function creates betting window and sets bet amount from user input
    public void setBet() {

         JFrame f = new JFrame("Place Bet");
  
         JButton b = new JButton("Submit");
  
         JTextField t = new JTextField(12);
  
         JPanel p = new JPanel();
  
         p.add(t);
         p.add(b);
  
         f.add(p);
         f.setSize(300, 100);
         f.setLocationRelativeTo(null);
         f.setResizable(false);
         f.toFront();
         f.setVisible(true);

         b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String textBet = (t.getText());
                currentBet = Integer.parseInt(textBet);
                f.dispose();
            }
        });


    }


//Main Function to run black jack game-------------------------------------------------------------------------------------------------------------------
    public static void main(String[] args) throws Exception {
        BlackJack blackJack = new BlackJack();
    }

}