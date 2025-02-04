
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
    boolean dealerCardHidden;
    ArrayList<Card> dealerHand;
    int dealerSum;
    int dealerAceCount; //used to track if dealer has an ace incase dealerSum goes over 21, ace can be changed from 11 to 1

    //Player variables
    ArrayList<Card> playerHand;
    int playerSum;
    int playerAceCount; //used to track if player has an ace incase playerSum goes over 21, ace can be changed from 11 to 1

    // Player second hand for split
    ArrayList<Card> playerHandSplit;
    int playerSumSplit;
    int playerAceCountSplit;
    boolean hasSplit; // Flag to track if the player has split


    //GUI Variables
    //window size
    int boardWidth = 600;
    int boardHeight = boardWidth;

    //card size ratio 1 to 1.4
    int cardWidth = 110;
    int cardHeight = 154;

    //chip pool and betting round variables
    int chipsPool = 100;
    int currentBet;

    //Create GUI and button action listeners
    JFrame frame = new JFrame("Black Jack");
    JPanel gamePanel = new JPanel() {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            try{
                //draw dealers hidden card
                Image hiddenCardImg = new ImageIcon(getClass().getResource("./cards/BACK.png")).getImage();
                if (!dealerCardHidden) {
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

                // Draw player’s split hand if split is active
                if (hasSplit) {
                    for (int i = 0; i < playerHandSplit.size(); i++) {
                        Card card = playerHandSplit.get(i);
                        Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                        g.drawImage(cardImg, 20 + (cardWidth + 5) * i, 480, cardWidth, cardHeight, null);
                    }
                }


                //display ongoing chip count
                String displayChips = "Chips: " + Integer.toString(chipsPool);
                g.setFont(new Font("Arial", Font.PLAIN, 30));
                g.setColor(Color.white);
                g.drawString(displayChips, 415, 510);

                

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    JPanel buttonPanel = new JPanel();
    JButton hitButton = new JButton("Hit");
    JButton doubleButton = new JButton("Double");
    JButton surrenderButton = new JButton("Surrender");
    JButton stayButton = new JButton("Stay");
    JButton splitButton = new JButton("Split");


    //Constructor for black jack game
    BlackJack() {
        dealerCardHidden = true;
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
        doubleButton.setFocusable(false);
        buttonPanel.add(doubleButton);
        surrenderButton.setFocusable(false);
        buttonPanel.add(surrenderButton);
        stayButton.setFocusable(false);
        buttonPanel.add(stayButton);
        buttonPanel.add(splitButton);
        splitButton.setFocusable(false);
        splitButton.setEnabled(false); // Initially disabled

        frame.add(buttonPanel,BorderLayout.SOUTH);

        //pop up window to set bet for the current round
        setBet();

        //action listener to tell what to do when the "hit" button is clicked
        hitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!hasSplit || playerHand.size() > playerHandSplit.size()) {
                    Card card = deck.remove(deck.size() - 1);
                    playerSum += card.getValue();
                    playerAceCount += card.isAce() ? 1 : 0;
                    playerHand.add(card);
        
                    if (reducePlayerAce() >= 21) {
                        hitButton.setEnabled(false);
                    }
                } else {
                    Card card = deck.remove(deck.size() - 1);
                    playerSumSplit += card.getValue();
                    playerAceCountSplit += card.isAce() ? 1 : 0;
                    playerHandSplit.add(card);
        
                    if (reduceSplitAce() >= 21) {
                        hitButton.setEnabled(false);
                    }
                }
        
                gamePanel.repaint();
            }
        });
        

        //action listener to tell what to do when the "double" button is clicked
        doubleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int doubleBet;
                doubleBet = currentBet * 2;
                currentBet = doubleBet;

                Card card = deck.remove(deck.size()-1);
                playerSum += card.getValue();
                playerAceCount += card.isAce() ? 1 : 0;
                playerHand.add(card);
                
                gamePanel.repaint();
                checkWinner();
            }
        });

        //action listener to tell what to do when the "surrender" button is clicked
        surrenderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                double halfBet;
                int returnBet;
                halfBet = currentBet / 2;
                double roundedDown = Math.floor(halfBet);
                Double d = new Double(roundedDown);
                returnBet = d.intValue();
                
                chipsPool -= returnBet;

                gamePanel.repaint();
                forfeit();
            }
        });

        //action listener to tell what to do when the "stay" button is clicked
        stayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (hasSplit && playerHand.size() > playerHandSplit.size()) {
                    // Switch to playing the second hand
                    hitButton.setEnabled(true);
                } else {
                    // Proceed with dealer's turn
                    hitButton.setEnabled(false);
                    doubleButton.setEnabled(false);
                    stayButton.setEnabled(false);
        
                    while (dealerSum < 17) {
                        Card card = deck.remove(deck.size() - 1);
                        dealerSum += card.getValue();
                        dealerAceCount += card.isAce() ? 1 : 0;
                        dealerHand.add(card);
                    }
                    gamePanel.repaint();
                    checkWinner();
                }
            }
        });
        

        splitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                splitHand();
            }
        });
        


        gamePanel.repaint();
    }



//This section contains all Java Functions-------------------------------------------------------------------------------------------------------

public void startGame() {
    // Reset game state
    hasSplit = false;
    dealerCardHidden = true;
    splitButton.setEnabled(false); // Disable Split button by default

    // Create the deck
    buildDeck();
    shuffleDeck();

    // Dealer variables initialized
    dealerHand = new ArrayList<>();
    dealerSum = 0;
    dealerAceCount = 0;

    // Deal cards to Dealer
    hiddenCard = deck.remove(deck.size() - 1); // First card (hidden)
    dealerSum += hiddenCard.getValue();
    dealerAceCount += hiddenCard.isAce() ? 1 : 0;

    Card card = deck.remove(deck.size() - 1); // Second card (visible)
    dealerSum += card.getValue();
    dealerAceCount += card.isAce() ? 1 : 0;
    dealerHand.add(card);

    // Print dealer's initial hand (for debugging)
    System.out.println("Dealer");
    System.out.println(hiddenCard);
    System.out.println(dealerHand);
    System.out.println("Dealer Sum: " + dealerSum);
    System.out.println("Dealer Ace Count: " + dealerAceCount);

    // Player variables initialized
    playerHand = new ArrayList<>();
    playerSum = 0;
    playerAceCount = 0;

    // Deal 2 cards to player
    for (int i = 0; i < 2; i++) {
        card = deck.remove(deck.size() - 1);
        playerSum += card.getValue();
        playerAceCount += card.isAce() ? 1 : 0;
        playerHand.add(card);
    }

    // Print player's initial hand (for debugging)
    System.out.println("Player");
    System.out.println(playerHand);
    System.out.println("Player Sum: " + playerSum);
    System.out.println("Player Ace Count: " + playerAceCount);

    // Check if the player has a pair and enable Split button if true
    if (playerHand.size() == 2 && playerHand.get(0).getValue() == playerHand.get(1).getValue()) {
        System.out.println("Pair detected! Enabling Split button.");
        SwingUtilities.invokeLater(() -> splitButton.setEnabled(true)); // Ensures UI updates properly
    } else {
        System.out.println("No pair detected. Split button remains disabled.");
    }

    // Refresh UI
    gamePanel.repaint();
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
    //Function provides the logic to change the Ace from 11 to 1 if hand is over 21 total for player split hand
    public int reduceSplitAce() {
        while (playerSumSplit > 21 && playerAceCountSplit > 0) {
            playerSumSplit -= 10;
            playerAceCountSplit -= 1;
        }
        return playerSumSplit;
    }
    

    //Function creates betting window and sets bet amount from user input
    public void setBet() {

        hitButton.setEnabled(false);
        doubleButton.setEnabled(false);
        surrenderButton.setEnabled(false);
        stayButton.setEnabled(false);

         JFrame betFrame = new JFrame("Place Bet");
         JButton commitButton = new JButton("Submit");
         JTextField betField = new JTextField(12);
         JPanel betPanel = new JPanel();
         
         betPanel.add(betField);
         betPanel.add(commitButton);
  
         betFrame.add(betPanel);
         betFrame.setSize(300, 100);
         betFrame.setLocationRelativeTo(null);
         betFrame.setResizable(false);
         betFrame.toFront();
         betFrame.setVisible(true);

         commitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String textBet = (betField.getText());
                System.out.println(textBet);
                currentBet = Integer.parseInt(textBet);
                System.out.println(currentBet);
                hitButton.setEnabled(true);
                doubleButton.setEnabled(true);
                surrenderButton.setEnabled(true);
                stayButton.setEnabled(true);
                betFrame.dispose();
            }
        });


    }

    //function for completing dealer behavior, comparing final hands, resolving bets, and starting a new hand
    public void checkWinner() {
        dealerCardHidden = false;
        hitButton.setEnabled(false);
        doubleButton.setEnabled(false);
        surrenderButton.setEnabled(false);
        stayButton.setEnabled(false);
    
        dealerSum = reduceDealerAce();
        playerSum = reducePlayerAce();
    
        int betOutcome = 0; // Tracks chip adjustments
        String message = "Hand Results:\n\n"; // Plain text message
    
        if (hasSplit) {
            playerSumSplit = reduceSplitAce();
    
            // Check first hand
            String firstHandResult = getOutcomeMessage(playerSum, dealerSum);
            message += "First Hand: " + firstHandResult + "\n";
            betOutcome += adjustChipsForResult(firstHandResult, currentBet);
    
            // Check second hand
            String secondHandResult = getOutcomeMessage(playerSumSplit, dealerSum);
            message += "Second Hand: " + secondHandResult + "\n";
            betOutcome += adjustChipsForResult(secondHandResult, currentBet);
        } else {
            String result = getOutcomeMessage(playerSum, dealerSum);
            message += result + "\n";
            betOutcome += adjustChipsForResult(result, currentBet);
        }
    
        chipsPool += betOutcome; // Adjust chips based on outcome
    
        // Append updated chip count
        message += "\nUpdated Chips: " + chipsPool;
    
        displayResult(message);
    }
    
    
    public void forfeit(){
        dealerCardHidden = false;
        hitButton.setEnabled(false);
        doubleButton.setEnabled(false);
        surrenderButton.setEnabled(false);
        stayButton.setEnabled(false);

        JFrame resultFrame = new JFrame("Hand Results");
        JButton playAgainButton = new JButton("Play Again");
        JPanel resultPanel = new JPanel();
        JLabel resultLabel = new JLabel("Forfeit");
        
        resultPanel.add(resultLabel);
        resultPanel.add(playAgainButton);

        resultFrame.add(resultPanel);
        resultFrame.setSize(300, 100);
        resultFrame.setLocationRelativeTo(null);
        resultFrame.setResizable(false);
        resultFrame.toFront();
        resultFrame.setVisible(true);

        playAgainButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dealerCardHidden = true;
                startGame();
                hitButton.setEnabled(true);
                doubleButton.setEnabled(true);
                surrenderButton.setEnabled(true);
                stayButton.setEnabled(true);
                gamePanel.repaint();
                resultFrame.dispose();
                setBet();

            }
        });
    }

    public void splitHand() {
        hasSplit = true;
    
        // Move second card to a new hand
        playerHandSplit = new ArrayList<Card>();
        playerHandSplit.add(playerHand.remove(1));
    
        // Update hand values
        playerSumSplit = playerHandSplit.get(0).getValue();
        playerAceCountSplit = playerHandSplit.get(0).isAce() ? 1 : 0;
    
        playerSum -= playerHandSplit.get(0).getValue();
        playerAceCount -= playerHandSplit.get(0).isAce() ? 1 : 0;
    
        // Draw one card for each hand
        playerHand.add(deck.remove(deck.size() - 1));
        playerHandSplit.add(deck.remove(deck.size() - 1));
    
        // Update sums
        playerSum += playerHand.get(1).getValue();
        playerAceCount += playerHand.get(1).isAce() ? 1 : 0;
    
        playerSumSplit += playerHandSplit.get(1).getValue();
        playerAceCountSplit += playerHandSplit.get(1).isAce() ? 1 : 0;
    
        // Disable Split button after use
        splitButton.setEnabled(false);
    
        // Repaint the game board to show the split
        gamePanel.repaint();
    }
    
    public String getOutcomeMessage(int playerSum, int dealerSum) {
        if (playerSum > 21) return "Bust! You Lose!";
        else if (dealerSum > 21) return "Dealer Busts! You Win!";
        else if (playerSum == dealerSum) return "It's a Draw!";
        else if (playerSum > dealerSum) return "You Win!";
        else return "You Lose!";
    }
    

    public void displayResult(String message) {
        JFrame resultFrame = new JFrame("Hand Results");
        resultFrame.setSize(350, 180);
        resultFrame.setLocationRelativeTo(null);
        resultFrame.setResizable(false);
    
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BorderLayout());
    
        // Display the message in normal plain text
        JTextArea resultTextArea = new JTextArea(message);
        resultTextArea.setEditable(false);
        resultTextArea.setWrapStyleWord(true);
        resultTextArea.setLineWrap(true);
        resultPanel.add(new JScrollPane(resultTextArea), BorderLayout.CENTER);
    
        JButton playAgainButton = new JButton("Play Again");
        resultPanel.add(playAgainButton, BorderLayout.SOUTH);
    
        resultFrame.add(resultPanel);
        resultFrame.setVisible(true);
    
        playAgainButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dealerCardHidden = true;
                hasSplit = false; // Reset split state
                startGame();
                hitButton.setEnabled(true);
                doubleButton.setEnabled(true);
                surrenderButton.setEnabled(true);
                stayButton.setEnabled(true);
                splitButton.setEnabled(false); // Reset split button
                gamePanel.repaint();
                resultFrame.dispose();
                setBet();
            }
        });
    }
    
    public int adjustChipsForResult(String result, int bet) {
        if (result.contains("Win")) {
            return bet;  // Win: Gain the bet amount
        } else if (result.contains("Lose")) {
            return -bet; // Lose: Lose the bet amount
        } 
        return 0; // Draw: No chip change
    }
    
    


//Main Function to run black jack game-------------------------------------------------------------------------------------------------------------------
    public static void main(String[] args) throws Exception {
        BlackJack blackJack = new BlackJack();
    }

}
