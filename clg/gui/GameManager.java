package clg.gui;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.Timer;

import clg.game.Game;
import clg.game.GameInfo;
import clg.gtp.Agent;
import clg.gtp.GTP;
import clg.log.Logger;
import clg.main.Settings;
@SuppressWarnings("unused")

/**
 * 
 * @author Devon Smart
 */
public class GameManager extends MouseAdapter
{
	private static final Pattern movePattern = Pattern.compile("^= ([a-z])([1-9]?\\d)[-x]([a-z])([1-9]?\\d)$");
	public static final Pattern verboseMovePattern = Pattern.compile("(White|Black) move : ([A-Z])([1-9]?\\d) to ([A-Z])([1-9]?\\d)");
	private final int EMPTY = -1;
	private final int BLACK = 1;
	private final int WHITE = 0;
	private final int INVALIDMOVE = 1;
	private final int NOTYOURTURN = 2;
	private int locx, locy, locx2, locy2;
	public int z = 0;
	public int row1, column1, row2, column2;
	
	/** Keeps track of whose turn it is. */
	private boolean stillPlayerTurn = true;
    protected boolean pieceSelect;
    private boolean firstAgentTurn;
	private Point moveTo;
    private Point moveFrom;
    private VersusDialog versus;
    
    public GTP gtp1;
    public GTP gtp2;
    public boolean isTimeUpdated;
    public boolean isPaused;
	public GameInfo gameInfo;
    private Settings settings;
    private Game engine;

	private BoardView gameBoard;
    private MenuListener menuHandler;
    private AnalysisModeListener analyst;
    private GameWindowListener windowListener;
    protected BGui window;
    private Logger log;
    private ArrayList<String> pausedMoveList;
    private ArrayList<Point> deselectSqrs;
	
	/**
	 * Constructor 
	 * @param engine the game engine
	 */
	public GameManager(Settings settings)
	{
        engine = new Game();
        this.settings = settings;
        engine.init();
        menuHandler = new MenuListener(this);
        analyst = new AnalysisModeListener(this);
        windowListener = new GameWindowListener(this);        
        loadSettings();
        engine.resetInit();
        // Create main ui window
        window = new BGui(this, engine.checkGameState(), menuHandler, analyst, windowListener, false);
        gameBoard = window.getGameBoard();
	}
	
	/**
	 * Initializes the game
	 */
	public void init()
	{
		// Initialize GUI state variables
        pieceSelect = true;
        gtp1 = new GTP(this, "gtp1");
        gtp2 = new GTP(this, "gtp2");
        isPaused = false;
        isTimeUpdated = false;
        pausedMoveList = new ArrayList<String>();
        log = engine.getLog();
        checkSave();
        versus = new VersusDialog(this);
        versus.showDialog();
	}
		
	private void checkSave()
	{
		File file = new File("save.txt");
		
		if (file.exists())
			window.setHasSave(true);
	}

	/**
	 * Checks if is a valid piece selection
	 * @param x	the x coordinate of the piece
	 * @param y	the y coordinate of the piece
	 * @return True if is a valid piece selection, false if not valid
	 */
	private boolean isValidSelection(int x, int y)
	{
		int[][] board = engine.checkGameState();		
		
		if (board[y][x] == -1)
		{
			window.statusUpdate("Select a Piece", INVALIDMOVE);
			return false;
		}
		else if (board[y][x] == WHITE && !GameInfo.playerOne)
		{
			window.statusUpdate("It's Black's turn", NOTYOURTURN);
			return false;			
		}
		
		else if (board[y][x] == BLACK && GameInfo.playerOne)
		{
			window.statusUpdate("It's White's turn", NOTYOURTURN);
			return false;
		}
		return true;
	}
	
	/**
	 * Checks if a move is valid
	 * @param from the position of the piece
	 * @param x	the x coordinate of the square the piece is being moved to 
	 * @param y the y coordinate of the square the piece is being moved to
	 * @return true if the move is valid, false if not valid
	 */
	private boolean isValidMove(Point from, int x, int y)
	{	
        Point to = new Point(x,y);
        
		if (to.equals(moveFrom))
        {
			//Deselecting squares if a piece is chosen and then deselected
            gameBoard.unSelect((int)from.getY(), (int)from.getX());
            deselectUnused(moveFrom, to);
            
            pieceSelect = true;
            return false;
        }
		
		boolean valid = engine.moveValidation(from, to);
		if (!valid)
		    window.statusUpdate("Invalid move", INVALIDMOVE);
		return valid;        
	}
	
	public void makeMove(Point from, Point to)
    {
	    // Waits until the opponent clicks to erase the highlights
	    if (!stillPlayerTurn)
	    {
	        gameBoard.unSelect((int)moveFrom.getY(), (int)moveFrom.getX());
	        gameBoard.unSelect((int)moveTo.getY(), (int)moveTo.getX());
	        stillPlayerTurn = true;
	    }
	    
	    gameBoard.selectSquare(from.y, from.x);
	    
        moveFrom = from;
        pieceSelect = !pieceSelect;
        
        if (!GameInfo.gameOver && isValidMove(from, to.x, to.y))
        {
            moveTo = to;
            gameBoard.selectSquare(moveTo.y, moveTo.x);
            window.writeToDisplay(gameBoard.displayMove());
            pieceSelect = !pieceSelect;
            
            if (GameInfo.gameOver)
                gameOver();
            else
            {
                stillPlayerTurn = false;
                if (gtp1.isAgentVersusAgent() && gtp2.isAgentVersusAgent())
                {
                    firstAgentTurn = !firstAgentTurn; // turn over control to the other player
                    
                    if (isTimeUpdated)
                    {
                        gtp1.changeThinkingTime(GameInfo.aiOneThinkTime);
                        gtp2.changeThinkingTime(GameInfo.aiTwoThinkTime);
                    }
                    
                    if (firstAgentTurn)
                        gtp1.sendMove(from, to);
                    else
                        gtp2.sendMove(from, to);
                }
            }                
        }
    }
	
	/**
	 * Gets the current game state
	 * @return an array that represents the current game state
	 */
	public int[][] state()
	{
		return engine.checkGameState();
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		if (gtp1.isAgentActive() && gtp2.isAgentActive())
	    {
	    	window.statusUpdate("C V C", NOTYOURTURN);
			return;
	    }
		
	    if (gtp1.isAgentActive() && !GameInfo.playerOne)
	    {
	        window.statusUpdate("It's Player 2's turn", NOTYOURTURN);
	        return;
	    }
	    
	    
		if (!GameInfo.gameOver)
        {
            Object eventSource = e.getSource();
            Square cell = (Square)e.getSource();
            Point pos = cell.getPosition();
        	
            // Get the x,y position of the current cell 
            int x = (int)pos.getX();
            int y = (int)pos.getY();
    		
            // If is move selection and is a valid move
            if (pieceSelect && isValidSelection(x,y))
            {
            	// Waits until the opponent clicks to erase the highlights
            	if (!stillPlayerTurn)
            	{
            		gameBoard.unSelect((int)moveFrom.getY(), (int)moveFrom.getX());
            		gameBoard.unSelect((int)moveTo.getY(), (int)moveTo.getX());
            		
            		stillPlayerTurn = true;
            		//gameBoard.unSelectSquares(stillPlayerTurn, moveFrom.y, moveFrom.x);
            	}
            	
            	availableMoves(x,y);
            	
            	moveFrom = new Point(x,y);
            	pieceSelect = !pieceSelect;
            }
            else if (!pieceSelect && isValidMove(moveFrom, x,y))
            {                
            	moveTo = new Point(x,y);
            	if (gtp1.isAgentActive() && !isPaused)
            	    gtp1.sendMove(moveFrom, moveTo);
            	
            	//Move has been made, deselecting moves that are not To and From
            	deselectUnused(moveFrom, moveTo);
            	
               	window.writeToDisplay(gameBoard.displayMove());
               	pieceSelect = !pieceSelect;
               	
               	if (stillPlayerTurn)
               	{
               		stillPlayerTurn = false;
               		//gameBoard.unSelectSquares(stillPlayerTurn, y, x);
               	}
               	// check if is game over and display message if true 
               	if (GameInfo.gameOver)
               		gameOver();
            }
        }
		GameInfo.isNewGame = false;
	}
	
	public void genMove()
	{
	    gtp1.genMove();
	}
	
	/**
	 * Checks for game over state and displays pop up dialog
	 */
	private void gameOver()
	{
        String winner;
   		int choice;
   		if (!GameInfo.playerOne)
   		    winner = "White";
   		else
   			winner = "Black";
   		String msg = "Game Over! " + winner + " won";
   		Object[] options = {"New Game", "Quit"};
   		choice = JOptionPane.showOptionDialog(window, msg, "Game Over", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
   		window.writeToDisplay(msg);
   		
   		if (choice == JOptionPane.YES_OPTION)
   		    newGame();
   		
	}
	
	/**
	 * Resets the game to a new game state 
	 */
	public void newGame()
	{
		engine.init();
		window.newGame();
		pieceSelect = true;
		if (gtp1.isAgentActive())
		    gtp1.resetAgent();
		if (gtp2.isAgentActive())
		    gtp2.resetAgent();
		
		if (gtp1.isAgentVersusAgent() && gtp2.isAgentVersusAgent())
		{
		    firstAgentTurn = true;
		    gtp1.genMove(); // have the first agent make the first move
		}    
	}
	
	public void resetGameEngine()
    {
        engine.resetInit();
        window.reset(engine.checkGameState());
        pieceSelect = true;
        stillPlayerTurn = true;
        if (gtp1.isAgentActive())
		    gtp1.resetAgent();
        if (gtp2.isAgentActive())
        	gtp2.resetAgent();
    }
    
	/**
	 * Saves the current game state
	 */
	public void saveGame()
	{
		engine.saveGame();
		window.setHasSave(true);
	}
	
	/**
	 * Loads a saved game
	 */
	public void loadGame()
	{
		String[] log = engine.loadGame();
		window.loadConsole(log);
		gameBoard.loadBoard(engine.checkGameState());
	}
	
	/**
	 * Quits the application
	 */
	public void quitGame()
	{
		if (!GameInfo.isNewGame)
		{
			int choice;
			String msg = "Would you like to save before you quit?";
			Object[] options = {"Save Game", "Quit"};
			choice = JOptionPane.showOptionDialog(window, msg, "Quit Game", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

			if (choice == JOptionPane.YES_OPTION)
				saveGame();
		}
		
        if (gtp1.isAgentActive())
            gtp1.disconnect();
        if (gtp2.isAgentActive())
        	gtp2.disconnect();
        saveSettings();
	}
	
	public void exit()
	{
		window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
	}
	
	/**
	 * Displays analysis mode
	 */
	public void analyse()
	{
		window.displayAnalysisBar();
	}
    
    public void updateSettings()
    {
        settings.updateSettings();
    }
	
	public void showOptions()
	{
		window.showOptions();
	}
	
	/**
	 * Undo a move
	 */
	public void undo()
	{
		engine.undo();
		// undoes move from within the move console
		window.undo();
		if (gtp1.isAgentActive())
		{
		    gtp1.resetAgent();
		    List<String> gtpCommands = log.outputUndoPlayGTP();
		    for (String move : gtpCommands)
		        gtp1.playMove(move);
		}
		gameBoard.setState(engine.checkGameState());
		gameBoard.displayMove();
	}
	
	/**
	 * Redo an undone move
	 */
	public void redo()
	{
		String move = engine.redo();
		window.redo();
		if (gtp1.isAgentActive() && move != null)
		{
		    String captureNotation;
		    String gtpPlayCommand = "play";
            if (GameInfo.handleCapture)
                captureNotation = "x";
            else
                captureNotation = "-";
            
            Matcher mat = verboseMovePattern.matcher(move);
            if (mat.find())
            {
                char currentPlayer = Character.toLowerCase(mat.group(1).charAt(0));
                char letterFileOrigin = Character.toLowerCase(mat.group(2).charAt(0));
                char letterFileDest = Character.toLowerCase(mat.group(4).charAt(0));
                int originRank = Integer.valueOf(mat.group(3));
                int destRank = Integer.valueOf(mat.group(5));
                
                gtpPlayCommand = gtpPlayCommand + " " + currentPlayer + " " + letterFileOrigin + originRank + captureNotation + letterFileDest + destRank;
                gtp1.playMove(gtpPlayCommand);
            }
		}
		gameBoard.setState(engine.checkGameState());
		gameBoard.displayMove();
	}
	
    public void availableMoves(int x, int y)
    {       
            int[][] gameState = engine.checkGameState();
            Point from = new Point(x,y);
            Point[] range = engine.calcRange(from, GameInfo.playerOne);
            ArrayList<Point> vp = new ArrayList<Point>();
            vp.add(from);
            
            
            for(int i = 0; i < range.length; i++)
            {
                    boolean isVal = true;
                    
                    if( (range[i].x == from.x) && (range[i].y == from.y) )
                            isVal = false;
                    
                    if(GameInfo.playerOne &&  gameState[range[i].y][range[i].x] == 0)
                            isVal = false;
                    if(!GameInfo.playerOne &&  gameState[range[i].y][range[i].x] == 1)
                            isVal = false;
                    
                    if(GameInfo.playerOne && i == 1 && gameState[range[i].y][range[i].x] == 1)
                            isVal = false;
                    if(!GameInfo.playerOne && i == 1 && gameState[range[i].y][range[i].x] == 0)
                            isVal = false;
                    
                    if(isVal)
                            vp.add(range[i]);
            }
            
            for(int i = 0; i < vp.size(); i++)
            {
                    gameBoard.selectSquare((int) vp.get(i).y, (int) vp.get(i).x);
            }
            
            deselectSqrs = vp;
    }
    
    public void deselectUnused(Point from, Point to)
    {
            
            for(int i = 0; i < deselectSqrs.size(); i++)
            {
                    boolean valDesel = true;
                    
                    if( (deselectSqrs.get(i).x == from.x && deselectSqrs.get(i).y == from.y) )
                    {
                            valDesel = false;
                    }
                    if((deselectSqrs.get(i).x == to.x && deselectSqrs.get(i).y == to.y) )
                    {
                            valDesel = false;
                    }
                    
                    if(valDesel)
                    {
                            gameBoard.unSelect((int) deselectSqrs.get(i).y, (int) deselectSqrs.get(i).x);
                    }
            }
            
            deselectSqrs = null;
    }

	public void updateStats(String response, String id)
	{
		switch (id)
		{
			case "gtp1":
				GameInfo.aiOneStats.updateData(response + "\n");
				break;
			case "gtp2":
				GameInfo.aiTwoStats.updateData(response + "\n");
				break;
			default:
				break;
		}		    
	}

    public void updateThinkTime()
    {
        isTimeUpdated = true;
    }

	public void versusOption()
	{
		versus.showDialog();		
	}

	public void showHelp()
	{
		window.showHelp();		
	}
	
	public void loadSettings()
	{
		Properties prop = new Properties();
		
		try
		{
			File f = new File("breakthrough.properties");
			InputStream input = new FileInputStream(f);
			prop.load(input);
			
			GameInfo.cl_boardSize = Integer.parseInt(prop.getProperty("BoardSize", String.valueOf(8)));
			GameInfo.cl_boardTheme = prop.getProperty("BoardTheme", GameInfo.cl_boardTheme);
			GameInfo.cl_pieceTheme = prop.getProperty("PieceTheme", GameInfo.cl_pieceTheme);
			GameInfo.cl_rowsWithPieces = Integer.parseInt(prop.getProperty("RowsWithPieces", String.valueOf(GameInfo.cl_rowsWithPieces)));
			GameInfo.aiOneThinkTime = Integer.parseInt(prop.getProperty("AIOneThinkTime", String.valueOf(GameInfo.aiOneThinkTime)));
			GameInfo.aiTwoThinkTime = Integer.parseInt(prop.getProperty("AITwoThinkTime", String.valueOf(GameInfo.aiTwoThinkTime)));
			GameInfo.cl_defaultAI = prop.getProperty("DefaultAI", GameInfo.cl_defaultAI);
			GameInfo.cl_pathToAgent1 = prop.getProperty("AIOne", GameInfo.cl_pathToAgent1);
			GameInfo.cl_pathToAgent2 = prop.getProperty("AITwo", GameInfo.cl_pathToAgent2);
			
		}
		catch (Exception e) 
		{
		
		}
	}
	
	public void saveSettings()
	{
		Properties prop = new Properties();
		prop.setProperty("BoardSize", String.valueOf(GameInfo.cl_boardSize));
		prop.setProperty("BoardTheme", GameInfo.cl_boardTheme);
		prop.setProperty("PieceTheme", GameInfo.cl_pieceTheme);
		prop.setProperty("RowsWithPieces", String.valueOf(GameInfo.cl_rowsWithPieces));
		prop.setProperty("AIOneThinkTime", String.valueOf(GameInfo.aiOneThinkTime));
		prop.setProperty("AITwoThinkTime", String.valueOf(GameInfo.aiTwoThinkTime));
		prop.setProperty("DefaultAI", Agent.DEFAULT_COMMAND);
		prop.setProperty("AIOne", GameInfo.cl_pathToAgent1);
		prop.setProperty("AITwo", GameInfo.cl_pathToAgent2);
		
		File f = new File("breakthrough.properties");
		try (OutputStream out = new FileOutputStream(f))
		{
			prop.store(out,"This file should not be manually edited as this could result in unpredictable application behavior");
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
