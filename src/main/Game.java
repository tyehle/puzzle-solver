package main;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author Tobin
 */
public class Game implements Callable<Integer>
{
    /**A map containing all possible moves*/
    public static TreeMap<Integer, Pair[]> allMoves;
    
    /**The executor used to play games*/
    public static ExecutorService es;
    
    public static final TreeMap<Game, Future<Integer>> games = new TreeMap<>();;
    
    /**
     * Main method for the program.
     * @param args The command line arguments
     */
    public static void main(String args[]) throws ExecutionException, InterruptedException
    {
        allMoves = new TreeMap<>();
        // fill the list of moves
        allMoves.put(0, new Pair[] {new Pair(3, 1), new Pair(2, 5)});
        allMoves.put(1, new Pair[] {new Pair(6, 3), new Pair(8, 4)});
        allMoves.put(2, new Pair[] {new Pair(7, 4), new Pair(9, 5)});
        allMoves.put(3, new Pair[] {new Pair(0, 1), new Pair(5, 4), new Pair(10, 7), new Pair(12, 6)});
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
        
        es = Executors.newFixedThreadPool(8);
        Game firstGame = new Game();
        games.put(firstGame, es.submit(firstGame));
        
        while(true)
        {
            Future<Integer> f = null;
            synchronized(games)
            {
                for(Game g : games.keySet())
                {
                    f = games.get(g);
                    if(!f.isDone())
                    {
                        break;
                    }
                }
                
                if(f == null)
                {
                    break;
                }
            }
            
            // wait for the one that isn't done
            f.get();
        }
        
        for(Game g : games.keySet())
        {
            if(games.get(g).get() == 1)
            {
                // report game
                System.out.println("One Peg!");
            }
        }
    }
    
    /**The game board.  1 represents a pin, and 0 represents an empty space.*/
    private int board[];
    
    /**The list of moves taken this game*/
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
    
    /**
     * Makes a new game with the given board arrangement, and moves
     * taken.
     * @param board The initial board
     * @param moves The moves that have already been made this game
     */
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
    private int jump(int from, int to)
    {
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
    
    @Override
    public Integer call() throws Exception
    {
        play();
        int pegs = 0;
        for(int i = 0; i < board.length; i++)
        {
            if(board[i] == 1)
            {
                pegs++;
            }
        }
        return pegs;
    }
    
    public void play()
    {
        boolean done = false;
        while(!done)
        {
            LinkedList<Move> possibleMoves = new LinkedList<>();
            for(int from = 0; from < board.length; from++)
            {
                if(board[from] == 1)
                {
                    for(Pair p : allMoves.get(from))
                    {
                        if(board[p.to] == 0)
                        {
                            possibleMoves.add(new Move(p.to, from));
                        }
                    }
                }
            }

            if(possibleMoves.isEmpty())
            {
                // game over
                done = true;
            }
            else if(possibleMoves.size() == 1)
            {
                // only one move, take it
                Move m = possibleMoves.getFirst();
                moves.add(m);
                jump(m.from, m.to);
            }
            else
            {
                Move takeThis = possibleMoves.removeFirst();
                for(Move m : possibleMoves)
                {
                    int[] otherBoard = new int[board.length];
                    System.arraycopy(board, 0, otherBoard, 0, board.length);
                    ArrayList<Move> otherMoves = (ArrayList<Move>)moves.clone();
                    otherMoves.add(m);
                    Game otherGame = new Game(otherBoard, otherMoves);
                    otherGame.jump(m.from, m.to);
                    synchronized(games)
                    {
                        games.put(otherGame, es.submit(otherGame));
                    }
                }
                
                moves.add(takeThis);
                jump(takeThis.from, takeThis.to);
            }
        }
        
        // report games
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
