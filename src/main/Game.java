package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.TreeMap;

/**
 *
 * @author Tobin
 */
public class Game
{
    /**A map containing all possible moves*/
    public static TreeMap<Integer, Pair[]> allMoves;
    
    /**
     * Main method for the program.
     * @param args The command line arguments
     */
    public static void main(String args[])
    {
        allMoves = new TreeMap<>();
        // fill the list of moves
        allMoves.put(0, new Pair[] {new Pair(3, 1), new Pair(5, 2)});
        allMoves.put(1, new Pair[] {new Pair(6, 3), new Pair(8, 4)});
        allMoves.put(2, new Pair[] {new Pair(7, 4), new Pair(9, 5)});
        allMoves.put(3, new Pair[] {new Pair(0, 1), new Pair(5, 4), new Pair(10, 7), new Pair(12, 7)});
        allMoves.put(4, new Pair[] {new Pair(11, 7), new Pair(13, 8)});
        allMoves.put(5, new Pair[] {new Pair(0, 2), new Pair(3, 4), new Pair(12, 8), new Pair(14, 9)});
        allMoves.put(6, new Pair[] {new Pair(1, 3), new Pair(8, 7)});
        allMoves.put(7, new Pair[] {new Pair(2, 4), new Pair(9, 8)});
        allMoves.put(8, new Pair[] {new Pair(1, 4), new Pair(6, 7)});
        allMoves.put(9, new Pair[] {new Pair(2, 5), new Pair(7, 8)});
        allMoves.put(10, new Pair[] {new Pair(3, 6), new Pair(12, 11)});
        allMoves.put(11, new Pair[] {new Pair(4, 7), new Pair(13, 12)});
        allMoves.put(12, new Pair[] {new Pair(3, 7), new Pair(5, 8), new Pair(10, 11), new Pair(14, 13)});
        allMoves.put(13, new Pair[] {new Pair(4, 8), new Pair(11, 12)});
        allMoves.put(14, new Pair[] {new Pair(5, 9), new Pair(12, 13)});
        
        Game g = new Game();
        LinkedList<GameResult> results = g.play();
        
        System.out.println(results.size() + " games played");
        
        int count = 0;
        for(GameResult r : results)
        {
            if(r.pegsLeft == 1)
            {
                count++;
                
                g = new Game();
                for(Move m : r.moves)
                {
                    System.out.println("\n" + m.from + " to " + m.to);
                    g.jump(m);
                    System.out.println(g.getBoardState());
                }
                
                break;
            }
        }
    }
    
    /**The game board.  1 represents a pin, and 0 represents an empty space.*/
    private int board[];
    
    /**The list of moves to take this game*/
    private ArrayList<Move> moves;
    
    /**
     * Makes a new game with the default board
     */
    public Game()
    {
        // initialize the board with all pins in except one.
        board = new int[15];
        for(int i = 0; i < board.length; i++)
        {
            board[i] = 1;
        }
        board[4] = 0;
        
        moves = new ArrayList<>();
    }
    
    public Game(int board[], ArrayList<Move> moves)
    {
        this.board = board;
        this.moves = moves;
    }
    
    /**
     * Moves a pin from the from location to the to location.  If the move
     * is not valid returns -1, otherwise returns the index of the pin it jumped
     * over.
     * @param from The index the pin came from
     * @param to The index the pin is going to
     * @return The index of the pin that was jumped
     */
    private int jump(Move m)
    {
        int from = m.from, to = m.to;
        // if the to index is filled, or the from index is empty it is not a
        // valid jump
        if(board[to] == 1 || board[from] == 0)
        {
            return -1;
        }
        
        // make sure you are jumping over a filled space
        int jumped = -1;
        for(int i = 0; i < allMoves.get(from).length; i++)
        {
            if(allMoves.get(from)[i].to == to)
            {
                jumped = allMoves.get(from)[i].over;
            }
        }
        
        // update the board and return
        if(jumped != -1 && board[jumped] == 1)
        {
            // update the board
            board[to] = 1;
            board[from] = 0;
            board[jumped] = 0;
            return jumped;
        }
        else
        {
            return -1;
        }
    }
    
    private int getPegsRemaining()
    {
        int sum = 0;
        
        for(int i : board)
        {
            if(i == 1)
            {
                sum++;
            }
        }
        
        return sum;
    }
    
    private String getBoardState()
    {
        String out = board[0] + "\n";
        
        for(int i = 1; i < board.length; i++)
        {
            out += board[i] + " ";
            if(i == 2 || i == 5 || i == 9)
            {
                out += "\n";
            }
        }
        
        return out;
    }
    
    public LinkedList<GameResult> play()
    {
        LinkedList<GameResult> games = new LinkedList<>();
        
        while(true)
        {
            // get a list of all available moves
            LinkedList<Move> possibleMoves = new LinkedList<>();
            for(int from = 0; from < board.length; from++)
            {
                if(board[from] == 1)
                {
                    for(Pair p : allMoves.get(from))
                    {
                        if(board[p.over] == 1 && board[p.to] == 0)
                        {
                            possibleMoves.add(new Move(p.to, from));
                        }
                    }
                }
            }
            
            // if there are no more moves this game return
            if(possibleMoves.isEmpty())
            {
                games.add(new GameResult(moves, getPegsRemaining()));
                return games;
            }
            
            // if there is only one move, take it and continue
            Move takeThis = possibleMoves.removeFirst();
            
//            System.out.println(possibleMoves.size() + " other possible moves");
            
            // take all other possible moves
            while(!possibleMoves.isEmpty())
            {
                int[] otherBoard = new int[board.length];
                System.arraycopy(board, 0, otherBoard, 0, board.length);
                ArrayList<Move> otherMoves = (ArrayList<Move>)moves.clone();
                Game g = new Game(otherBoard, otherMoves);
                Move m = possibleMoves.removeFirst();
                g.jump(m);
                g.moves.add(m);
                games.addAll(g.play());
            }
            
            jump(takeThis);
            moves.add(takeThis);
        }
    }
    
    public class GameResult
    {
        public final ArrayList<Move> moves;
        public final int pegsLeft;
        
        /**
         * Makes a new game result with the given sequence of moves and results
         * in the given number of pegs left.
         * @param moves The sequence of moves to get this game result
         * @param pegs The number of pegs left at the end of the game
         */
        public GameResult(ArrayList<Move> moves, int pegs)
        {
            this.moves = moves;
            this.pegsLeft = pegs;
        }
    }
    
    /**
     * A class that defines a pair of integers.
     * @author Tobin
     */
    public static class Pair
    {
        public final int to, over;
        public Pair(int to, int over)
        {
            this.to = to;
            this.over = over;
        }
    }
    
    /**
     * Represents a move in a game.
     * @author Tobin
     */
    public class Move
    {
        public final int to, from;
        public Move(int to, int from)
        {
            this.to = to;
            this.from = from;
        }
    }
}
