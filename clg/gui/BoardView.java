package clg.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Point;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import clg.game.GameInfo;
import clg.gui.Square.Team;

/**
 * @author Derick Cornejo
 * @author Devon Smart
 */

public class BoardView extends JPanel
{
	private static final long serialVersionUID = 42L;
	public static final int EMPTY = -1;
	public static final int BLACK = 1;
	public static final int WHITE = 0;

	private Square[][] board;
	private int[][] gameState;
	int row1, column1, row2, column2;
	int locX, locY, locX2, locY2;
	private boolean isNewGame;
	private GameManager gManager;
	private Dimension boardSize;
	private ArrayList<Point> possibleMoves;

	public BoardView(GameManager manager, int[][] state) 
	{
		gameState = state;
		gManager = manager;

		board = new Square[GameInfo.cl_boardSize][GameInfo.cl_boardSize];
		boardSize = new Dimension(60 * GameInfo.cl_boardSize, 60 * GameInfo.cl_boardSize);
		possibleMoves = new ArrayList<Point>();
		this.setBorder(new BevelBorder(BevelBorder.RAISED));
		this.setPreferredSize(boardSize);
		this.setBackground(Color.darkGray);
		this.setLayout(new GridLayout(GameInfo.cl_boardSize, GameInfo.cl_boardSize));

		newGame();
		setVisible(true);		
	}

	public void resetBoardView(int[][] state)
	{
		gameState= state;
		this.removeAll();
		this.setLayout(new GridLayout(GameInfo.cl_boardSize, GameInfo.cl_boardSize));

		board = new Square[GameInfo.cl_boardSize][GameInfo.cl_boardSize];
		boardSize.height = 60 * GameInfo.cl_boardSize;
		boardSize.width = 60 * GameInfo.cl_boardSize;

		this.setPreferredSize(boardSize);

		newGame();
		this.repaint();
	}

	private void newGame()
	{
		drawBoard();
		placePieces();     
	}

	public void setState(int[][] state)
	{
		gameState = state;
	}

	/**
	 * Sets the game state
	 * @param state
	 */
	 public void setGameState(int[][] state)
	{
		gameState = state;
	}

	public void reset()
	{
		isNewGame = true;
		updatePieces();
		this.repaint();
	}

	/**  
	 * Displays a move on the board
	 * @param gameState
	 */
	public String displayMove()
	{
		updatePieces();
		this.repaint();
		return GameInfo.cl_currentMove;
	}

	/**
	 * Loads the game state
	 * @param state
	 */
	public void loadBoard(int[][] state)
	{
		isNewGame = true;
		gameState = state;
		updatePieces();
		this.repaint();
	}

	/**
	 * Draws the game board
	 */
	private void drawBoard() 
	{
		GameInfo.cl_boardWidth = boardSize.width;
		GameInfo.cl_boardHeight = boardSize.height;
		for (int i = 0; i < board.length; i++) 
		{
			for (int j = 0; j < board.length; j++) 
			{
				board[i][j] = new Square(j, i);
				board[i][j].addMouseListener(gManager);
				this.add(board[i][j]);

				if (i % 2 == 0) 
				{
					if (j % 2 != 0)
					{
						board[i][j].SetBG(GameInfo.cl_cellTheme1);
					}	
					else                        
						board[i][j].SetBG(GameInfo.cl_cellTheme2);
				} 
				else 
				{
					if (j % 2 == 0) 
						board[i][j].SetBG(GameInfo.cl_cellTheme1);
					else                         
						board[i][j].SetBG(GameInfo.cl_cellTheme2);
				}
			}
		}
	}

	/**
	 * Places the pieces on the board
	 * @param gameState
	 */
	private void placePieces() 
	{
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		
		Team team = Team.Team1; 
		boolean visible = true;
		
		for (int row = 0; row < gameState.length; row++) 
		{
			for (int col = 0; col < gameState.length; col++) 
			{
				if (gameState[row][col] == 1) 
				{
					team = Team.Team2;
					visible = true;
				} 
				else if (gameState[row][col] == 0) 
				{
					team = Team.Team1;
					visible = true;
				} 
				else
					visible = false;
				
				board[row][col].addPiece(team, visible);
			}
		}   
	}

	/**
	 * Update the location of the pieces
	 */
	private void updatePieces() 
	{
		for (int row = 0; row < gameState.length; row++) 
		{
			for (int col = 0; col < gameState.length; col++) 
			{
				Team team = Team.Team1;
				boolean visible = true;
				
				if (isNewGame)
					unSelect(row,col);

				if (gameState[row][col] == 1) 
				{
					team = Team.Team2;
					visible = true;
				} 
				else if (gameState[row][col] == 0) 
				{
					team = Team.Team1;
					visible = true;
				} 
				else if (gameState[row][col] == -1) 
				{
					visible = false;
				}
				
				board[row][col].updatePiece(team, visible);
			}
		}

		if (isNewGame)
			isNewGame = false;
		repaint();
	}

	/**
	 * Highlights a square
	 * @param y
	 * @param x
	 */
	public void selectSquare(int y, int x)
	{
		board[y][x].setBorder(BorderFactory.createLineBorder(Color.CYAN));
	}
	
	public void selectPossibleMoves(Point[] moves, Point current)
	{
		selectSquare(current.y, current.x);
		for(Point next : moves)
		{
			if (((next.x == current.x))&&(gameState[next.y][next.x] != -1));
			else if (GameInfo.playerOne && gameState[next.y][next.x] == 0);
			else if (!GameInfo.playerOne && gameState[next.y][next.x] == 1);
			else
			{
				possibleMoves.add(next);
				selectSquare(next.y,next.x);
			}
		}	
	}
	
	public void unSelectSquares(boolean stillTurn, int y, int x)
	{
		if(stillTurn)
			unSelect(y,x);
		for (Point square: possibleMoves)
		{
			if(!(square.x == x && square.y == y))
				unSelect(square.y, square.x);
		}
		possibleMoves.clear();
	}

	/**
	 * Un-highlights a square
	 * @param y
	 * @param x
	 */
	public void unSelect(int y, int x)
	{
		board[y][x].setBorder(BorderFactory.createEmptyBorder());
	}
	
	public String toString()
	{
		String display = "";
		for (int i = 0; i < gameState.length; i++)
		{
			for (int j = 0;j < gameState.length;j++)
			{
				switch (gameState[i][j])
				{
				case 1:
					display += "B";
					break;
				case 0:
					display += "W";
					break;
				default:
					display += "-";
				}
				if ((j+1) != gameState.length)
					display += " ";
			}
			if ((i+1) != gameState.length)
				display += "\n";
		}

		return display;
	}
	 
}