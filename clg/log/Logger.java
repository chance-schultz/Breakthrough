/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clg.log;

import clg.game.GameInfo;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Chance
 */
public class Logger 
{
    public static final Pattern movePattern = Pattern.compile("(White|Black) move : ([A-Z])([1-9]?\\d) to ([A-Z])([1-9]?\\d)");
    private ArrayList <LogItem> redoStack = new ArrayList <LogItem> ();
    private ArrayList <LogItem> moveStack = new ArrayList <LogItem> ();
    private LogItem currentState;
    
    public Logger()
    {
        currentState = null;
    }
    
    public ArrayList<String> outputUndoPlayGTP()
    {
        ArrayList<String> playCommands = new ArrayList<String>();
        String captureNotation;
        for (LogItem item : moveStack)
        {
            String gtpPlayCommand = "play";
            if (item.capture)
                captureNotation = "x";
            else
                captureNotation = "-";
            
            Matcher mat = movePattern.matcher(item.move);
            if (mat.find())
            {
                char currentPlayer = Character.toLowerCase(mat.group(1).charAt(0));
                char letterFileOrigin = Character.toLowerCase(mat.group(2).charAt(0));
                char letterFileDest = Character.toLowerCase(mat.group(4).charAt(0));
                int originRank = Integer.valueOf(mat.group(3));
                int destRank = Integer.valueOf(mat.group(5));
                
                gtpPlayCommand = gtpPlayCommand + " " + currentPlayer + " " + letterFileOrigin + originRank + captureNotation + letterFileDest + destRank;
                playCommands.add(gtpPlayCommand);
            }
        }
        
        return playCommands;
    }
    
    /**
     * Constructor
     * @param current
     * @param next
     * @param prevState
     */
    public void log(Point current, Point next, int[][] prevState, int[][]currentState)
    {
        char[] letters = {'A','B','C','D','E','F','G','H','I','J','K'
                         ,'L','M','N','O','P','Q','R','S','T','U','V'
                         ,'W','X','Y','Z'};
        
        String logInfo;
        redoStack.clear();
               
        int[][] tempState = new int[currentState.length][];
        for (int i = 0; i < tempState.length; i++)
            tempState[i] = currentState[i].clone();
            
        if (GameInfo.playerOne)
            logInfo = "White move : ";
        else
            logInfo = "Black move : ";
        
        logInfo += "" + letters[current.x] + (GameInfo.cl_boardSize - current.y) + " to " + letters[next.x] + (GameInfo.cl_boardSize - next.y) + "\n";
        LogItem tempItem = new LogItem(prevState, logInfo);
        tempItem.capture = GameInfo.handleCapture;
        tempItem.numBlackPieces = GameInfo.cl_playerBlack;
        tempItem.numWhitePieces = GameInfo.cl_playerWhite;
        
        this.currentState = new LogItem(tempState, logInfo);
        this.currentState.capture = GameInfo.handleCapture;
        this.currentState.numBlackPieces = GameInfo.cl_playerBlack;
        this.currentState.numWhitePieces = GameInfo.cl_playerWhite;
        
        moveStack.add(tempItem);
        //log.add(logInfo);
        GameInfo.cl_currentMove = logInfo;
        //movesList.add(temp);
    }
    public void saveGTP()
    {                
        try 
        {
            String fileName = "GtpLog.txt";
            File file = new File(fileName);
            if (file.exists())
                file.delete();
            
            file.createNewFile();
            FileWriter fstream = new FileWriter(fileName, true); 
            
            String temp = "boardsize " + GameInfo.cl_boardSize + "\n";
            
            for (int i = 0; i <= moveStack.size()-1; i++)
            {
                String info = moveStack.get(i).move;
                temp += "play ";
                
                if (info.contains("White"))
                    temp += "w ";
                else if (info.contains("Black"))
                    temp += "b ";

                info = info.substring(info.indexOf(':')+2, info.length()-1);
                String[] ary = info.split("\\s");
                temp += ary[0].toLowerCase();
                
                if(moveStack.get(i).capture)
                    temp += "x";
                else
                    temp += "-";
                
                temp += ary[2].toLowerCase() + "\n";
                
            	fstream.write(temp);
                temp = "";
            }
            fstream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();    
        }
    }
    
    public void saveGSF()
    {                
        try 
        {
            String fileName = "GsfLog.txt";
            File file = new File(fileName);
            if (file.exists())
                file.delete();
            
            file.createNewFile();
            FileWriter fstream = new FileWriter(fileName, true); 
            
            String temp = "(;FF[4] GM[Breakthrough]" + "\n" + "SZ[" + GameInfo.cl_boardSize + "]" + "\n";
            
            for (int i = 0; i <= moveStack.size()-1; i++)
            {
                String info = moveStack.get(i).move;
                if (info.contains("White"))
                    temp += 'W';
                else if (info.contains("Black"))
                    temp += 'B';
                
                info = info.substring(info.indexOf(':')+2, info.length()-1);
                String[] ary = info.split("\\s");

                temp += "[" + ary[0].toLowerCase() + "-" + ary[2].toLowerCase() + "];" + "\n";
                
            	fstream.write(temp);
                temp = "";
            }
            fstream.write(")");
            fstream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();    
        }
    }
    /**
     * Undoes a move
     * @return
     */   
    public LogItem undo()
    {
        if(moveStack.isEmpty())
            return null;
        
        LogItem previous = moveStack.remove(moveStack.size() - 1);
                  
        if (currentState != null)
        	redoStack.add(currentState);
        
        currentState = previous;
        
        if(!moveStack.isEmpty())
        {
        	LogItem peek = moveStack.get(moveStack.size() - 1);
        	currentState.move = peek.move;
        	currentState.capture = peek.capture;
        }
        
        return previous;
    }
    
    /**
     * Re-does last undone move
     * @return log item of the redone move
     */
    public LogItem redo()
    {
    	if (redoStack.isEmpty())
    		return null;
    	
    	LogItem current = redoStack.remove(redoStack.size() - 1);
    	
    	if(currentState != null)
    	{
    		currentState.move = current.move;
    		currentState.capture = current.capture;
    		GameInfo.handleCapture = currentState.capture;
    		moveStack.add(currentState);
    	}    		
    	
    	currentState = current;
    	
    	return current;
    }
    
    /**
     * Saves the log to a file
     */
    public void saveLog() 
    {
        try 
        {
            String fileName = "Log.txt";
            File file = new File(fileName);
            
            FileWriter fstream = new FileWriter(file, false); 
            BufferedWriter out = new BufferedWriter(fstream);
            for (int i = 0; i < moveStack.size(); i++)
            	out.write(moveStack.get(i).move);
            out.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();    
        }
    }
    
    /**
     * Saves the game state to a file
     */
    public void saveGameState(int[][] gameState) 
    {
        try 
        {
            String fileName = "save.txt";
            File file = new File(fileName);
            String convGameState = convertToSave(gameState);
            convGameState += "&";

            FileWriter fstream = new FileWriter(file); 
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(convGameState);
            out.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();    
        }
    }
    
    /**
     * Reads move log from file
     */
    public String[] readLogFile()
    {
        ArrayList <String> log = new ArrayList <String>();
        try
        {
            String fileName = "Log.txt";
            File file = new File(fileName);
        
            Scanner s1 = new Scanner(file);
            
            while (s1.hasNext())
                log.add(s1.nextLine() + "\n");
            
            s1.close();
        }
        catch (IOException e)
        {
            System.out.println(e.getStackTrace());
        }
        
        GameInfo.cl_currentMove = log.get(log.size() - 1);
        
        if (log.size() % 2 == 0)
        	GameInfo.playerOne = true;
        else
        	GameInfo.playerOne = false;
        
        return (String[])(log.toArray(new String[1]));
    }
            
    public int[][] loadGameState()
    {
        String save = "";
        
        try
        {
            String fileName = "save.txt";
            File file = new File(fileName);
            
            Scanner s1 = new Scanner(file);
            
            while (s1.hasNext())
                save += s1.nextLine();

            s1.close();
        }
        catch (Exception e)
        {
            System.out.println(e.getStackTrace());
        }
        return convertFromSave(save.substring(0,save.indexOf("&")));

    }
    
    /**
     * Resets the log
     */
    public void resetLog()
    {
        moveStack.clear();
        redoStack.clear();
    }
    
    public String convertToSave(int[][] gs)
    {
        int[][] gameState = gs;
        
        String temp = "";
        String converted = "";
        String emptyIndexes = "";
        String len = "";
        int zerosToAdd = new String("" + (gameState.length*gameState.length)).length();

        
        for (int i = 0; i < gameState.length; i++)
        {
            for (int j = 0; j < gameState.length; j++)
            {
                if (gameState[i][j] == -1)
                {
                    temp += 1;
                    emptyIndexes += i + "" + j; 
                }
                else
                    temp += gameState[i][j];
                
                if (j%4 == 3)
                {
                    //System.out.print(temp);
                    temp = "" + Integer.valueOf(temp,2);
                    if (temp.length() != 2)
                        temp = "0" + temp;
                    
                    //converted += Integer.toBinaryString(new Integer(temp));
                    converted += temp;
                    temp = "";
                }
            }
            System.out.println();
        }
        
        len += emptyIndexes.length();
        
        for (int i = 0; i < zerosToAdd-1; i++)
        {
            if (len.length() < zerosToAdd)
                len = "0" + len;
        }
        
        //System.out.println(emptyIndexes);
        emptyIndexes = len + emptyIndexes;
        
        return (emptyIndexes + converted);
    }
    
    public int[][] convertFromSave(String save)
    {
        if (save.equals(""))
            return null;
        //System.out.println(save + "," + save.length());
        int[][] gameState = new int[GameInfo.cl_boardSize][GameInfo.cl_boardSize];
        int zerosToAdd = new String("" + (gameState.length*gameState.length)).length();
        
        String len = save.substring(0, zerosToAdd);
        //System.out.println(len);
        
        if (len.charAt(0) == '0')
        {
            len = len.substring(len.lastIndexOf("0"));
            //System.out.println(len);
        }

        String emptyIndexes = save.substring(zerosToAdd, new Integer(len) + zerosToAdd);
        //System.out.println(emptyIndexes);
        
        String decToBin = save.substring(emptyIndexes.length() + zerosToAdd,save.length());
        //System.out.println(decToBin);
        
        int yIndex = 0;
        int xIndex = 0;
        int y = -1;
        int x = -1;
        if (emptyIndexes.length() >= 2)
        {
            y = new Integer(emptyIndexes.substring(0,1));
            x = new Integer(emptyIndexes.substring(1,2));
            //System.out.println (emptyIndexes.length());
        }
        
        while (decToBin.length() > 0)
        {
            String temp = "" + Integer.toBinaryString(new Integer(decToBin.substring(0,2)));
            while (temp.length() < 4)
            {
                temp = "0" + temp;
            }
            
            //System.out.print(temp);

            for (int i = 0; i < temp.length(); i++)
            {
                if (yIndex == y && xIndex == x)
                {
                    //System.out.println(y + ";" + x);
                    gameState[yIndex][xIndex] = -1;
                    if (emptyIndexes.length()-2 >= 2)
                    {
                        emptyIndexes = emptyIndexes.substring(2);
                        y = new Integer(emptyIndexes.substring(0, 1));
                        x = new Integer(emptyIndexes.substring(1, 2));
                    }
                    else
                    {
                        y = -1;
                        x = -1;
                    }
                }
                else
                {
                    //System.out.println(yIndex + "," + xIndex);
                    gameState[yIndex][xIndex] = new Integer(temp.substring(i, i+1));
                }
                
//                if(temp.length() > 1)
//                	temp = temp.substring(1);
                
                xIndex++;
            }
            
            if(xIndex%gameState.length == 0 && yIndex < gameState.length)
            {
                //System.out.println();
                yIndex++;
                xIndex = 0;
            }
            
            decToBin = decToBin.substring(2);
        }
        
        return gameState;
    }
    
    public LogItem loadGame()
    {
        if(!moveStack.isEmpty())
        {
            moveStack.clear();
            redoStack.clear();
        }
        
        String[] tempList = readLogFile();
        for(int i = 0; i < tempList.length-1; i++)
            moveStack.add(new LogItem(null,tempList[i]));
        
        moveStack.add(new LogItem(loadGameState(),GameInfo.cl_currentMove));
        
        return moveStack.get(moveStack.size()-1);
    }
    
}
    