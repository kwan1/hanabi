package agents;

import hanabAI.*;

import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

public class Agent21560466 implements Agent {

    // Number of Player in the game
    public int numberOfPlayers;

    // Last state that I analised to know the state of the game

    // My position on the table
    public int myIndex = -1;

    // Used to initialize the game
    public boolean firstPlay = true;


    public int maximumLayer = 1000;


    public Agent21560466() {
    }

    public Agent21560466(int limitLayersTo) {
        this.maximumLayer = limitLayersTo;
    }

    /**
     * We need to setup what we know
     */
    public void init(State s) {
        numberOfPlayers = s.getPlayers().length;
        // maximumLayer = numberOfPlayers;
        // Figure my index
        myIndex = s.getNextPlayer();
    }

    /**
     * Given the state, return the action that the strategy chooses for this state.
     *
     * @return the action the agent chooses to perform
     */
    public Action doAction(State s) {

        long end = System.currentTimeMillis() + 100;  // One second limit on move

        // Eveytime We need to Play, we must start with :
        if (firstPlay) {
            init(s);
            firstPlay = false;
        }

        Tree21560466 tree = new Tree21560466();
        // Create the root node
        try {
            tree.rootNode = new Node21560466(s, myIndex);
        } catch (IllegalActionException e) {
            // We are at an illegal state, so ... probably game is over:
            return null;
        }

        Vector<Node21560466> currentNodes = new Vector<Node21560466>();
        currentNodes.add(tree.rootNode);

        int currentLayer = 1;
        while (System.currentTimeMillis() < end) {

            if (currentNodes.size() > 0) {
                currentNodes = expandAndSimulateLoop(currentNodes, end, currentLayer++);
            }

        }


        Node21560466 n = decideBestNode(tree);
        return n.action;
    }

    public Vector<Node21560466> expandAndSimulateLoop(Vector<Node21560466> toExpandAndSimulate, long end, int currentLayer) {
        System.out.println("Expanding and simulating");
        Vector<Node21560466> nextLayer = new Vector<Node21560466>();

        for (Node21560466 node : toExpandAndSimulate) {
            if (System.currentTimeMillis() < end) {
                node.expand();
                for (Node21560466 toSim : node.children) {
                    // SIM
                    if (System.currentTimeMillis() < end) {
                        toSim.simulate();
                        if (currentLayer < maximumLayer) {
                            nextLayer.add(toSim);  
                        }
                    }
                }
            }

        }
        return nextLayer;
    }

    public static Node21560466 decideBestNode(Tree21560466 tree) {
        System.out.println("decide Best node ");
        Node21560466 result = null; // No best node yet at this state

        result = tree.getBestLeaf();

        while (result.parent.parent != null) {
            result = result.parent;
        }


        return result;
    }

    /**
     * Reports the agents name
     */
    public String toString() {
        return "21560466";
    }

}


class CalculateFunction21560466{

    public static double WeightOfNode(Node21560466 node){

        double tempWeight = 0;
        KnowledgeState21560466 myStateNow = node.getMyStateNow();



        if ( myStateNow.getFuseTokens() < 1)
            tempWeight -=1000000000;

        if (myStateNow.getHintTokens() < 1)
            tempWeight-= 1000000000;

        for (Colour c : Colour.values()) {
            int value = myStateNow.getFireworkLastCards().get(c);
            for ( Integer playerId : myStateNow.getPlayerCards().keySet() ) {
                for ( KnowledgeCard kC : myStateNow.getPlayerCards().get(playerId)) {
                    if (kC != null && kC.complete) {
                        if (c.equals(kC.getColor())) {
                            if (kC.getValue()<= value) {
                                tempWeight -= 12; // GET RID OF THIS CARD !
                            }
                        }
                    }
                }
            }
        }

        for (Colour c : Colour.values()) {
            int value = myStateNow.getFireworkLastCards().get(c);

            for ( int i = 0 ; i <= value ; i++) {
                if (i != 0) {
                    tempWeight += ((i)*20);
                }
            }
            
        }

        

        // Points for knowing cards!
        Hashtable<Integer , KnowledgeCard[]> playerCards =  myStateNow.getPlayerCards();
        for ( Integer player : playerCards.keySet() ) {
            KnowledgeCard[] playerHand = playerCards.get(player);
            if ( playerHand != null) {
                for (KnowledgeCard card : playerHand) {
                    if (card != null) {
                        if (card.complete) {

                            if ( card.getColor()!= null && myStateNow.getFireworkLastCards().get(card.getColor()) + 1 == card.getValue()) {
                                tempWeight += 20;
                            } else {
                                tempWeight += 4; // If the player knows the card !
                            }

                        } else {
                            if (card.knowColor || card.knowValue) {
                                tempWeight += 2; // If the player knows part of the card!
                            }
                        }
                    }
                }
            }
        }

        if (node.parent != null) {
            if ( tempWeight <= node.parent.getNodeWeight())
                tempWeight -= 1000;
        }

        return tempWeight;
    }
}

class Tree21560466{

    public Node21560466 rootNode;

    public Node21560466 getBestLeaf() {
        return toTest(rootNode);
    }

    public Node21560466 toTest(Node21560466 input) {

        Node21560466 result = null;

        if ( input.children  != null && input.children.size()>0 ) {
            for ( Node21560466 child : input.children) {
                Node21560466 temp = toTest(child);
                if ( temp != null && temp.simulated ) {
                    if (result == null) {
                        result = temp;
                    } else {
                        if ( result.getNodeWeight() < temp.getNodeWeight())
                            result = temp;
                    }
                }
            }
        } else {
            if ( input.simulated ) {
                if (result == null) {
                    result = input;
                } else {
                    if ( result.getNodeWeight() < input.getNodeWeight())
                        result = input;
                }
            }
        }
        return result;
    }
}

class Node21560466{

    public Node21560466 parent; // parent nodes
    public Vector<Node21560466> children = new Vector<Node21560466>();

    private KnowledgeState21560466 stateOfTheParent;
    private KnowledgeState21560466 myStateNow;

    // Evey can aceess
    public Action action;


    public int layer = 0;
    private double nodeWeight =0 ;

    public boolean simulated = false;
    public boolean endNode = false;

    /**
     *  Used only for Root
     */
    public Node21560466(State stateInput ,
                int myIndex
    ) throws IllegalActionException {
        this.layer = 0;
        //this.myCardsThatIknow = myCardsThatIknow;
        //this.playerCards = playerCards;
        action =  null;
        simulated = true;

        myStateNow = new KnowledgeState21560466(stateInput);
    }

    /**
     * Used for nodes that are not Root
     *
     */
    public Node21560466 (
            // <----------- We use this constructor to build expanded nodes
        Node21560466 parentInput ,
        KnowledgeState21560466 stateOfTheParentInput,
        Action actionInput
    )   {

        parent = parentInput;
        layer = parentInput.layer + 1;
        action = actionInput;
        this.stateOfTheParent = stateOfTheParentInput;
    }

    public KnowledgeState21560466 getMyStateNow() {
        return myStateNow;
    }

    /**
     *
     */
    public Vector<Node21560466> expand(){
        //System.out.println("Expand Layer : " + layer);

        // For Averages
        if ( parent != null) {
            int totalSiblings = 0;
            int totalWeights = 0;
            for (Node21560466 nodeSibling : parent.children) {
                totalWeights += nodeSibling.nodeWeight;
                totalSiblings++;
            }
            double averageSiblings = totalWeights / totalSiblings;

            if (!myStateNow.isMoreToFollow() || nodeWeight <= averageSiblings)
                return children; // empty !
        }


        int player = myStateNow.getNextPlayer() ;

        // This gets the Knwodleged card of the player that is making the move
        int cardPosition = 0;
        for ( KnowledgeCard kCard : myStateNow.getPlayerCards().get(player) ) {

            // Doing it Blindly , The player doesn't look at the cards
            // Action a = new Action( player , state.getPlayers()[player] , ActionType.PLAY , cardPosition );

            // Or do it with more intelligence, by checking the knowdleged of the card
            if ( kCard != null && kCard.complete && kCard.getColor() != null) {

                if (myStateNow == null || myStateNow.getFireworkLastCards() == null ||  myStateNow .getFireworkLastCards().get(kCard.getColor())== null)
                    System.out.println("PROBLEM"); // Used just to insert break point on debugger

                int fireWorkLastCard = myStateNow .getFireworkLastCards().get(kCard.getColor());

                if ( fireWorkLastCard  <  kCard.getValue()  ) {
                    if ( fireWorkLastCard +1 ==  kCard.getValue() ) {
                        // Very Good to Play !
                        try {
                            Action a = new Action(player, myStateNow.getPlayers()[player], ActionType.PLAY, cardPosition);
                            children.add(
                                    new Node21560466(
                                            this,
                                            myStateNow,
                                            a
                                    )
                            );
                        } catch (IllegalActionException e) {
                            System.out.println("Illegal Action for Node, Skipping");
                        }
                    } else {
                        // We Keep it and don't Discard it !
                    }
                } else {
                    // DISCARD
                    try {
                        if ( myStateNow.getHintTokens()<8 ) {
                            Action a = new Action(player, myStateNow.getPlayers()[player], ActionType.DISCARD, cardPosition);
                            children.add(
                                    new Node21560466(
                                            this,
                                            myStateNow,
                                            a
                                    )
                            );
                        }
                    } catch (IllegalActionException e) {
                        System.out.println("Illegal Action for Node, Skipping");
                    }
                }

                // Then play
                // OR
                // Check stack to see if you want really yo play, or it is a card to DISCARD
                // Or do we want to discard straight away ? Is there any advantage ?
            } else {
                
                // DISCARD
                if ( kCard != null) {
                    try {
                        if (myStateNow.getHintTokens() < 8) {
                            Action a = new Action(player, myStateNow.getPlayers()[player], ActionType.DISCARD, cardPosition);
                            children.add(
                                    new Node21560466(
                                            this,
                                            myStateNow,
                                            a
                                    )
                            );
                        }
                    } catch (IllegalActionException e) {
                        System.out.println("Illegal Action for Node, Skipping");
                    }
                }
            }
            cardPosition++;
        }

        // Giving HINTS !!!!
        if (myStateNow.getHintTokens()>=1) {
            for (Integer key : myStateNow.getPlayerCards().keySet()) {
                if (key != player) { // only give hints to other players !

                   

                    // giving hints about Colors to other player
                    Vector<Colour> coloursThePlayerDoesntKnow = new Vector<Colour>();

                    for (Colour c : Colour.values()) {
                        for (KnowledgeCard kCard : myStateNow.getPlayerCards().get(key)) {
                            if (kCard!= null && !kCard.knowColor) {
                                if (!coloursThePlayerDoesntKnow.contains(kCard.getColor()))
                                    coloursThePlayerDoesntKnow.add(kCard.getColor());
                            }
                        }
                    }

                    // giving hints about Values to other player
                    Vector<Integer> valuesThePlayerDoesntKnow = new Vector<Integer>();
                    for (int value = 1; value <= 5; value++) {
                        for (KnowledgeCard kCard : myStateNow.getPlayerCards().get(key)) {
                            if (kCard!= null &&  !kCard.knowValue) {
                                if (!valuesThePlayerDoesntKnow.contains(kCard.getValue()))
                                    valuesThePlayerDoesntKnow.add(kCard.getValue());
                            }
                        }
                    }

                    // Giving Colour Hints
                    for (Colour c : coloursThePlayerDoesntKnow) {
                        KnowledgeCard[] thePlayerCards = myStateNow.getPlayerCards().get(key);
                        boolean[] cardPositionToHint = new boolean[thePlayerCards.length];

                        int count = 0;

                        int posCardTemp = 0;
                        for (KnowledgeCard specificCard : thePlayerCards) {

                            if (specificCard != null && specificCard.getColor() == c) {
                                cardPositionToHint[posCardTemp] = true;
                                count++;
                            } else
                                cardPositionToHint[posCardTemp] = false;

                            posCardTemp++;
                        }

                        if (count > 0) {
                            try {
                                Action a = new Action(player, myStateNow.getPlayers()[player],
                                        ActionType.HINT_COLOUR, key, cardPositionToHint, c);
                                children.add(
                                        new Node21560466(
                                                this,
                                                myStateNow,
                                                a
                                        )
                                );
                            } catch (IllegalActionException e) {
                                System.out.println("Illegal Action for Node, Skipping");
                            }
                        }

                    }

                    // Giving Value Hints
                    for (Integer v : valuesThePlayerDoesntKnow) {
                        KnowledgeCard[] thePlayerCards = myStateNow.getPlayerCards().get(key);
                        boolean[] cardPositionToHint = new boolean[thePlayerCards.length];

                        int count = 0;
                        int posCardTemp = 0;
                        for (KnowledgeCard c : thePlayerCards) {
                            if (c != null && c.getValue() == v) {
                                cardPositionToHint[posCardTemp] = true;
                                count++;
                            } else
                                cardPositionToHint[posCardTemp] = false;

                            posCardTemp++;
                        }

                        if (count > 0) {
                            try {
                                Action a = new Action(player, myStateNow.getPlayers()[player],
                                        ActionType.HINT_VALUE, key, cardPositionToHint, v);
                                children.add(
                                        new Node21560466(
                                                this,
                                                myStateNow,
                                                a
                                        )
                                );
                            } catch (IllegalActionException e) {
                                System.out.println("Illegal Action for Node, Skipping");
                            }
                        }
                    }
                }
            }
        }

        if (children.size() == 0 ) {
            // Hard Core play and Discard
            int cP = 0;
            for ( KnowledgeCard kCard : myStateNow.getPlayerCards().get(player) ) {
                if ( kCard != null) {
                    try {
                        if (myStateNow.getHintTokens() < 8) {
                            Action a = new Action(player, myStateNow.getPlayers()[player], ActionType.DISCARD, cP);
                            children.add(
                                    new Node21560466(
                                            this,
                                            myStateNow,
                                            a
                                    )
                            );
                        }
                    } catch (IllegalActionException e) {
                        System.out.println("Illegal Action for Node, Skipping");
                    }

                    try {
                        Action a = new Action(player, myStateNow.getPlayers()[player], ActionType.PLAY, cP);
                        children.add(
                                new Node21560466(
                                        this,
                                        myStateNow,
                                        a
                                )
                        );
                    } catch (IllegalActionException e) {
                        System.out.println("Illegal Action for Node, Skipping");
                    }
                }
                cP++;
            }
        }

        return children;
    }

    public double getNodeWeight() {
        return nodeWeight;
    }

    public void simulate()  {
        simulated = true;

        myStateNow = stateOfTheParent.nextState(action);


        if ( ! myStateNow.isMoreToFollow() ) {
            endNode = true;
            return;
        }

        int myIndex = myStateNow.getNextPlayer();
        this.nodeWeight = CalculateFunction21560466.WeightOfNode(this);
    }


}

class KnowledgeCard {

    private Colour color = null;
    private int value = -1;

    // True, when the person ( my card = person = me ) ( other player = person = them )
    // True when the person knows everything!
    public boolean complete = false;

    public boolean knowColor = false;
    public boolean knowValue = false;

    public Vector<Colour> possibilitiesC = new Vector<Colour>();
    public Vector<Integer> possibilitiesV = new Vector<Integer>();

    /* For our cards we use this, because we know nothing */
    public KnowledgeCard(){

        //posibilities of colour in our hand
        possibilitiesC.add(Colour.BLUE);
        possibilitiesC.add(Colour.RED);
        possibilitiesC.add(Colour.GREEN);
        possibilitiesC.add(Colour.WHITE);
        possibilitiesC.add(Colour.YELLOW);

        //posibilities of value in our hand
        possibilitiesV.add(1);
        possibilitiesV.add(2);
        possibilitiesV.add(3);
        possibilitiesV.add(4);
        possibilitiesV.add(5);
    }

    /* For The other player cards we use this */
    public KnowledgeCard(Card card){

        color = card.getColour();
        value = card.getValue();

        possibilitiesC.add(Colour.BLUE);
        possibilitiesC.add(Colour.RED);
        possibilitiesC.add(Colour.GREEN);
        possibilitiesC.add(Colour.WHITE);
        possibilitiesC.add(Colour.YELLOW);

        possibilitiesV.add(1);
        possibilitiesV.add(2);
        possibilitiesV.add(3);
        possibilitiesV.add(4);
        possibilitiesV.add(5);
    }

    public void setColor(Colour c) {
        this.color = c;
        this.knowColor = true;
        if ( knowColor && knowValue )
            complete = true;
    }

    public void setValue(int v) {
        this.value = v;
        this.knowValue = true;
        if ( knowColor && knowValue )
            complete = true;
    }

    /* Exclude the possibility of this color  */
    public void excludeColor(Colour c){
        possibilitiesC.remove(c);
        //left 1 possibility colour on hand then get the colour of the card
        if (possibilitiesC.size() == 1) {
            color = possibilitiesC.get(0);
            knowColor = true;
            if ( knowColor && knowValue )
                complete = true;
        }
    }

    /* Exclude the possibility of this value  */
    public void excludeValue(int v){
        possibilitiesV.remove(new Integer(v));
        //left 1 possibility value on hand then get the value of the card
        if (possibilitiesV.size() == 1) {
            value = possibilitiesV.get(0);
            knowValue = true;
            if ( knowColor && knowValue )
                complete = true;
        }
    }

    public Colour getColor() {
        return color;
    }

    public int getValue() {
        return value;
    }

    public KnowledgeCard createCopy() {
        KnowledgeCard kc = new KnowledgeCard();
        kc.color = color;
        kc.value = value;
        kc.complete = complete ;

        kc.knowColor = knowColor;
        kc.knowValue = knowValue;

        kc.possibilitiesC = new Vector<Colour>();
        kc.possibilitiesC.addAll(possibilitiesC);

        kc.possibilitiesV = new Vector<Integer>();
        kc.possibilitiesV.addAll(possibilitiesV);

        return kc;
    }
}


class KnowledgeState21560466 {

    private int numberOfPlayers;
    private int nextPlayer;
    private String[] players;

    private int hintTokens;
    private int fuseTokens;

    private Stack<Card> discarded;
    private Hashtable<Colour, Integer> fireworkLastCards = new Hashtable<Colour, Integer>();
    private Hashtable<Integer , KnowledgeCard[]> playerCards = new Hashtable<Integer, KnowledgeCard[]>();

    private boolean moreToFollow = true;

    // For Root
    public KnowledgeState21560466(State state) throws IllegalActionException {

        // We save the discarded Cards, to keep track
        discarded = state.getDiscards();

        hintTokens = state.getHintTokens();
        fuseTokens = state.getFuseTokens();

        numberOfPlayers = state.getPlayers().length;
        nextPlayer = state.getNextPlayer();
        players = state.getPlayers();

        // We record the Fireworks
        for( Colour c : Colour.values()) {
            Stack<Card> s = state.getFirework(c);
            if (s.isEmpty()) {
                fireworkLastCards.put(c, 0);
            } else {
                fireworkLastCards.put(c, s.peek().getValue());
            }
        }

        // We record Card Knowledge that we have !
        int numberOfCards = state.getPlayers().length < 4 ? 5 : 4 ;
        for ( int playerId = 0 ; playerId < state.getPlayers().length ; playerId++) {
            playerCards.put(playerId, new KnowledgeCard[ numberOfCards] );
            int cardN = 0 ;
            for ( Card c : state.getHand(playerId) ) {
                if ( c != null) {
                    playerCards.get(playerId)[cardN] = new KnowledgeCard(c);
                } else {
                    playerCards.get(playerId)[cardN] = new KnowledgeCard();
                }
                cardN++;
            }
        }

        int myId = state.getNextPlayer(); // This is my ID

        Vector<Action> listOfLastActions = new Vector<Action>();
        State tempStateToTraverse = state ;

        while ( 1 < tempStateToTraverse.getOrder()) {
            //adding previous actions to action list
            listOfLastActions .add( tempStateToTraverse.getPreviousAction() ); //<-----
            tempStateToTraverse = tempStateToTraverse.getPreviousState();
        }
        //At the end of the action list, no need to do anything else

        // This loop makes me iterate from Right to Left the list of actions
        for ( int actionId = listOfLastActions.size() ; actionId > 0 ; actionId-- ) {
            Action a = listOfLastActions.get(actionId-1);

            if ( a.getType() == ActionType.PLAY  ) {
                // Who played the card?
                int indexOfPlayer = a.getPlayer();
                // What card did he Play ?
                int cardPlayed = a.getCard();

                // ->>>>>>> when a card is player, the player gets a new card !
                if (indexOfPlayer == myId) {
                    playerCards.get(indexOfPlayer) [cardPlayed] = new KnowledgeCard();// as I know nothing about it(new card from deck)
                } else {
                    if ( state.getHand(indexOfPlayer)[cardPlayed] != null) {
                        playerCards.get(indexOfPlayer)[cardPlayed] = new KnowledgeCard(
                                state.getHand(indexOfPlayer)[cardPlayed]
                        );
                    } else {
                        playerCards.get(indexOfPlayer)[cardPlayed] = null;
                    }
                }


            } else if ( a.getType() == ActionType.DISCARD  ) {
                // Who discard the card?
                int indexOfPlayer = a.getPlayer();
                // What card did he discard ?
                int cardPlayed = a.getCard();

                if (indexOfPlayer == myId) {
                    playerCards.get(indexOfPlayer) [cardPlayed] = new KnowledgeCard();// as I know nothing about it(new card from deck)
                } else {
                    if( state.getHand(indexOfPlayer)[cardPlayed] != null) {
                        playerCards.get(indexOfPlayer)[cardPlayed] = new KnowledgeCard(
                                state.getHand(indexOfPlayer)[cardPlayed]
                        );
                    } else
                        playerCards.get(indexOfPlayer)[cardPlayed] = null;
                }

            } else if ( a.getType() == ActionType.HINT_COLOUR  ) {
                // Who received the Hint?
                int indexOfPlayer = a.getHintReceiver();
                // What Color ?
                Colour colorHint = a.getColour();
                // Which Cards ?
                boolean[]  subjectCards = a.getHintedCards() ;

                for (int cardId = 0 ; cardId < subjectCards.length ; cardId++) {
                    if(colorHint != null) {
                        if (playerCards.get(indexOfPlayer) != null && playerCards.get(indexOfPlayer)[cardId] != null) {
                            if (subjectCards[cardId] == true) {
                                // This card "i" is of this color!
                                playerCards.get(indexOfPlayer)[cardId].setColor(colorHint);
                            } else {
                                // This card "i" is NOT of this color!
                                playerCards.get(indexOfPlayer)[cardId].excludeColor(colorHint);
                            }
                        }
                    }
                }

            } else if ( a.getType() == ActionType.HINT_VALUE  ) {
                // Who received the Hint?
                int indexOfPlayer = a.getHintReceiver();
                // What Value ?
                int valueHint = a.getValue();
                // Which Cards ?
                boolean[]  subjectCards = a.getHintedCards() ;

                for (int cardId = 0 ; cardId < subjectCards.length ; cardId++) {
                    if (valueHint != -1) {
                        if (playerCards.get(indexOfPlayer) != null && playerCards.get(indexOfPlayer)[cardId] != null) {
                            if (subjectCards[cardId] == true) {
                                // This card "i" is of this value!
                                playerCards.get(indexOfPlayer)[cardId].setValue(valueHint);
                            } else {
                                // This card "i" is NOT of this vallue!
                                playerCards.get(indexOfPlayer)[cardId].excludeValue(valueHint);
                            }
                        }
                    }
                }
            }
        }
    }

    // Used for Children nodes !
    private KnowledgeState21560466() {

    }

    public boolean isMoreToFollow() {
        return moreToFollow;
    }

    public String[] getPlayers() {
        return players;
    }

    public int getNextPlayer() {
        return nextPlayer;
    }

    public int getHintTokens() {
        return hintTokens;
    }

    public int getFuseTokens() {
        return fuseTokens;
    }

    public Hashtable<Colour, Integer> getFireworkLastCards() {
        return fireworkLastCards;
    }

    public Hashtable<Integer, KnowledgeCard[]> getPlayerCards() {
        return playerCards;
    }

    public Stack<Card> getDiscardedCards() {
        return discarded;
    }

    public KnowledgeState21560466 nextState(Action action) {
        KnowledgeState21560466 nextState = new KnowledgeState21560466();
        nextState.numberOfPlayers = numberOfPlayers;
        nextState.nextPlayer = nextPlayer+1;
        if (nextState.nextPlayer >= numberOfPlayers) {
            nextState.nextPlayer = 0 ;
        }

        nextState.players = players;
        nextState.hintTokens = hintTokens;
        nextState.fuseTokens = fuseTokens;
        nextState.discarded = discarded;

        for ( Colour c : Colour.values()) {
            nextState.fireworkLastCards.put(c, fireworkLastCards.get(c));
        }

        for ( Integer k : playerCards.keySet()) {
            nextState.playerCards.put(k , new KnowledgeCard[ playerCards.get(k).length ]);
            int pos = 0 ;
            for (KnowledgeCard kC : playerCards.get(k)) {
                if (kC != null)
                    nextState.playerCards.get(k)[pos] = kC.createCopy();
                else
                    nextState.playerCards.get(k)[pos] = null;
                pos++;
            }
        }

        if ( action.getType() == ActionType.PLAY) {
            int positionCard = 0;
            try {
                positionCard = action.getCard();
            } catch (IllegalActionException e) {
                e.printStackTrace();
            }
            int whoPerformed = action.getPlayer();

            KnowledgeCard cardPlayed = nextState.playerCards.get( whoPerformed )[positionCard] ;
            Colour c = cardPlayed.getColor();
            int value  = cardPlayed.getValue();

            if ( nextState.fireworkLastCards.get(c) +1 == value ) {
                // GOOD PLAY
                nextState.fireworkLastCards.remove(c);
                nextState.fireworkLastCards.put(c, fireworkLastCards.get(c)+1);
            } else {
                nextState.fuseTokens = nextState.fuseTokens - 1;
            }
            nextState.playerCards.get( whoPerformed )[positionCard] = new KnowledgeCard();
        }
        if (action.getType() == ActionType.DISCARD) {
            int positionCard = 0;
            try {
                positionCard = action.getCard();
            } catch (IllegalActionException e) {
                e.printStackTrace();
            }
            int whoPerformed = action.getPlayer();

            KnowledgeCard cardDiscarded= nextState.playerCards.get( whoPerformed )[positionCard] ;

            if ( cardDiscarded.getValue() != -1 && cardDiscarded.getColor()!=null) {
                nextState.discarded.add(new Card(cardDiscarded.getColor(), cardDiscarded.getValue()));
            }
            // We set it to unkown, because we dont know which card he could get
            nextState.playerCards.get( whoPerformed )[positionCard] = new KnowledgeCard();

        }
        if (action.getType() == ActionType.HINT_COLOUR ) {
            boolean[] positionCard = null;
            int whoReceived = -1;
            Colour c = null;
            try {
                positionCard = action.getHintedCards();
                whoReceived = action.getHintReceiver();
                c = action.getColour();
            } catch (IllegalActionException e) {
                e.printStackTrace();
            }

            int pos = 0 ;
            for ( boolean isToApply : positionCard) {
                if ( isToApply ) {
                    nextState.playerCards.get( whoReceived )[pos].setColor(c);
                }
                pos++;
            }
        }
        if (action.getType() == ActionType.HINT_VALUE ) {
            boolean[] positionCard = null;
            int whoReceived = -1;
            int value = -1;
            try {
                positionCard = action.getHintedCards();
                whoReceived = action.getHintReceiver();
                value = action.getValue();
            } catch (IllegalActionException e) {
                e.printStackTrace();
            }

            int pos = 0 ;
            for ( boolean isToApply : positionCard) {
                if ( isToApply ) {
                    nextState.playerCards.get( whoReceived )[pos].setValue(value);
                }
                pos++;
            }
        }

        if (fuseTokens > 3)
            nextState.moreToFollow = false;
        else {
            int count = 0;
            for ( Colour c : nextState.fireworkLastCards.keySet() ) {
                count += nextState.fireworkLastCards.get(c);
            }
            if (count == 25)
                nextState.moreToFollow = false;
        }
        return nextState;
    }
}
