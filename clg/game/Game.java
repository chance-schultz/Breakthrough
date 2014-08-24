/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clg.game;

import clg.log.LogItem;
import clg.log.Logger;
import clg.main.Settings;
import java.awt.Point;
import java.text.DecimalFormat;
import java.util.Calendar;

/**
 *
 * @author Chance
 */
public class Game 
{
    private int[][] gameState;
    private Logger log;
    
    public Game()
    {
        gameState = new int[GameInfo.cl_boardSize][GameInfo.cl_boardSize];
        GameInfo.playerOne = true;
        log = new Logger();
    }
    
    /**
     * @return the log
     */
    public Logger getLog()
    {
        return log;
    }
    
    public void init()
    {
        resetGame();
        GameInfo.gameOver = false;
        GameInfo.playerOne = true;
        GameInfo.isNewGame = true;
        log.resetLog();
    }
    
    public void resetInit()
    {
        gameState = new int[GameInfo.cl_boardSize][GameInfo.cl_boardSize];
        GameInfo.cl_rowsWithoutPieces = GameInfo.cl_boardSize - (GameInfo.cl_rowsWithPieces * 2);
        init();
    }
    
    /*
     * This function resets the board to the starting state of an unplayed game.
     * The 1's represent squares on the board with black pieces.
     * The 0's represent squares on the board with white pieces.
     * The -1's represent  squares on the board with no pieces.
     */
    private void resetGame()
    {
        //The amount of rows that will contain pieces upon game start
        int rowsOfWhite = GameInfo.cl_rowsWithPieces;
        int rowsOfBlack = rowsOfWhite;
        int rowsWithoutPieces = GameInfo.cl_rowsWithoutPieces;
        GameInfo.cl_playerBlack = GameInfo.cl_boardSize*GameInfo.cl_rowsWithPieces;
        GameInfo.cl_playerWhite = GameInfo.cl_boardSize*GameInfo.cl_rowsWithPieces;
        
        //The value that will be stored in each square of the board
        int mark = 1;
        
        for (int i = 0;i < GameInfo.cl_boardSize;i++)
        {
            for (int j = 0;j < GameInfo.cl_boardSize;j++)
            {
                gameState[i][j] = mark;
            }
            
            if (rowsOfBlack > 1)        //Checks when to stop placing black pieces
            {
                rowsOfBlack--;
            }
            else if (rowsWithoutPieces > 0)     //Checks when to stop adding empty squares
            {
                rowsWithoutPieces--;
                mark = -1;
            }
            else //if((rowsOfWhite == 0)&&(rowsWithoutPieces == 0))       //Checks to make sure to place white pieces and that we don't add pieces outside of the board.
            {
                rowsOfWhite--;
                mark = 0;
                if ((rowsOfWhite == 0) && (i != GameInfo.cl_boardSize-2))
                    return;
            }
        }
        
    }
    
    public int[][] checkGameState()
    {
        return gameState;
    }
    
    public void updateGameState(Point current,Point move)
    {
        // initially assume that no capture takes place
        GameInfo.handleCapture = false;
        
        int currentX = current.x;
        int currentY = current.y;
        int newX = move.x;
        int newY = move.y;
        int numBlack = GameInfo.cl_playerBlack;
        int numWhite = GameInfo.cl_playerWhite;
        int value;
                
        if (GameInfo.playerOne)
        {
            value = 0;
            if (gameState[newY][newX] == 1)
            {
                numBlack--;
                GameInfo.handleCapture = true;
            }
        }
        else
        {
            value = 1;
            if (gameState[newY][newX] == 0)
            {
                numWhite--;
                GameInfo.handleCapture = true;
            }
        }
        
        int[][] prevState = new int[gameState.length][];
        
        for(int i = 0; i < gameState.length; i++)
        	prevState[i] = gameState[i].clone();
        	        
        gameState[newY][newX] = value;
        gameState[currentY][currentX] = -1;
        
        log.log(current, move, prevState, gameState);
        GameInfo.cl_playerBlack = numBlack;
        GameInfo.cl_playerWhite = numWhite;
        
        if (GameInfo.cl_playerWhite == 0 ||GameInfo.cl_playerBlack == 0)
            GameInfo.gameOver = true;
        else if ((GameInfo.playerOne && move.y == 0)||(!GameInfo.playerOne && move.y == gameState.length-1))
            GameInfo.gameOver = true;
        

        
        GameInfo.playerOne = !GameInfo.playerOne;
    }
    
    public void undo()
    {
        LogItem temp = log.undo();
        if(temp == null)
            return;
        
        GameInfo.cl_playerBlack = temp.numBlackPieces;
        GameInfo.cl_playerWhite = temp.numWhitePieces;
        
        gameState = temp.state;
        GameInfo.playerOne = !GameInfo.playerOne;
    }
    
    public String redo()
    {
    	LogItem temp = log.redo();
        if(temp == null)
            return null;
        
        GameInfo.cl_playerBlack = temp.numBlackPieces;
        GameInfo.cl_playerWhite = temp.numWhitePieces;
        
        gameState = temp.state;
        GameInfo.playerOne = !GameInfo.playerOne;
        
        return temp.move;
    }
    
    
    /**
     * @return console display of the current game state
     */
    public String toString()
    {
        String display = "";
        for (int i = 0; i < GameInfo.cl_boardSize; i++)
        {
            for (int j = 0;j < GameInfo.cl_boardSize;j++)
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

               if ((j+1) != GameInfo.cl_boardSize)
                   display += " ";
            }
            if ((i+1) != GameInfo.cl_boardSize)
                   display += "\n";
        }
        
        return display;
    }
    
    /**
     * 
     * @param current
     * @param next
     * @param isPlayerOne
     * @return
     */
    public boolean moveValidation(Point current, Point next)
    {
        boolean isPlayerOne = GameInfo.playerOne;
        
        if (!isPlayerOne && gameState[current.y][current.x] == 0)
            return false;/*Player one can't select player two's piece*/
        else if (isPlayerOne && gameState[current.y][current.x] == 1)
            return false;/*Player two can't select player one's piece*/
        else if (gameState[current.y][current.x] == -1)
            return false;/*The player can't select an empty space*/
        
        Point[] validMoves = calcRange(current, isPlayerOne);
        
        for (int i = 0; i < validMoves.length; i++) //check if move is in range
        {
            if (validMoves[i].equals(next)) //If there is a valid move in the range
                break;
            else if (i == validMoves.length-1) //If there is no valid move in range
                return false; 
        }
        
        if (((next.x == current.x)&&(next.y + 1 == current.y||next.y - 1 == current.y))&&(gameState[next.y][next.x] != -1))
            return false;
        
        if (isPlayerOne && gameState[next.y][next.x] == 0)
            return false;/*Player one can't move their piece into a spot that is 
         * already taken by their own piece return false*/
        else if (!isPlayerOne && gameState[next.y][next.x] == 1)
            return false;/*If player two tries to move a piece into a spot that is 
         * already taken by their own piece return false*/
        
        if ((isPlayerOne && next.y == 0)||(!isPlayerOne && next.y == gameState.length-1))
            GameInfo.gameOver = true;
        
        updateGameState(current,next);
        return true;
    }
    
    /**
     * 
     * @param current
     * @param isPlayerOne
     * @return
     */
    public Point[] calcRange(Point current,boolean isPlayerOne)
    {
        int count = 0;
        int y;
        Point [] temp = new Point [3];
        
        if (isPlayerOne) //If it is player one
            y = current.y-1; //Then set the possible range of y values to the next row in front of it
        else //If it is player two
            y = current.y+1;//Then set the possible range of y values to the next row in front of it
        
        if (current.x-1>=0) //If the x value to the left is not outside the board
            temp[count++] = new Point(current.x-1,y);//Then set it as a possible value of x in the range
        
        temp[count++]= new Point(current.x,y);//Set your current x value as a possible point in the range        
        
        if (current.x+1 < gameState.length)//If the x value to the right is outside the board
            temp[count++] = new Point(current.x+1, y); //Then set it as a possible value of x in the range
                
        Point[] range = new Point[count]; //Sets the array point range equal to the possible values of temp
        for (int k = 0; k < count; k++)
        {
            if (temp != null)
                range[k] = temp[k];
        }

        return range;
    }
    
    /**
     * 
     * @return
     */
    public String printLog()
    {
        return log.toString();
    }
    
    public void saveGame()
    {
    	log.saveLog();
        log.saveGameState(gameState);
        log.saveGTP();
        log.saveGSF();
    }
    
    public String[] loadGame()
    {
        gameState = log.loadGame().state;
        String[] movesList = log.readLogFile();
        GameInfo.cl_playerBlack = 0;
        GameInfo.cl_playerWhite = 0;
        
        for (int i = 0; i < gameState.length; i++)
        {
            for (int j = 0; j < gameState.length; j++)
            {
                if (gameState[i][j] == 1)
                    GameInfo.cl_playerBlack++;
                else if (gameState[i][j] == 0)
                    GameInfo.cl_playerWhite++;
            }
        }
        	
    	return movesList;
    }
}
