/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clg.main;

import clg.game.AIStats;
import clg.game.GameInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.Scanner;

import clg.game.GameInfo;

/**
 *
 * @author Chance
 */
public class Settings 
{
	
	Properties prop = new Properties();
	public int boardSize;
	public int rowsOfPieces;
	public String firstAIPath;
	public String secondAIPath;
	public String boardTheme;
	public String whiteImg;
	public String blackImg;
	public String cellBackground1;
	public String cellBackground2;
	
	public String date;
	public String result;
	public String rules;
    
    public Settings()
    {
    	loadDefault();
    	
    	try
		{
			File f = new File("breakthrough.properties");
			InputStream input = new FileInputStream(f);
			prop.load(input);
			
			boardSize = Integer.parseInt(prop.getProperty("BoardSize", String.valueOf(GameInfo.cl_boardSize)));
			boardTheme = prop.getProperty("BoardTheme", boardTheme);
			//pieceTheme = prop.getProperty("PieceTheme", gameInfo.cl_pieceTheme);
			rowsOfPieces = Integer.parseInt(prop.getProperty("RowsWithPieces", String.valueOf(rowsOfPieces)));
			//eInfo.aiOneThinkTime = Integer.parseInt(prop.getProperty("AIOneThinkTime", String.valueOf(gameInfo.aiOneThinkTime)));
			//gameInfo.aiTwoThinkTime = Integer.parseInt(prop.getProperty("AITwoThinkTime", String.valueOf(gameInfo.aiTwoThinkTime)));
			//gameInfo.cl_defaultAI = prop.getProperty("DefaultAI", gameInfo.cl_defaultAI);
			firstAIPath = prop.getProperty("AIOne", firstAIPath);
			secondAIPath = prop.getProperty("AITwo", secondAIPath);
			
			
		}
		catch (Exception e) 
		{
            
		}
    }
    
    private void loadDefault()
    {
        GameInfo.cl_boardSize = 8;
        GameInfo.cl_rowsWithPieces = 2;
        GameInfo.cl_rowsWithoutPieces = GameInfo.cl_boardSize - (GameInfo.cl_rowsWithPieces * 2);
        GameInfo.cl_playerBlack = GameInfo.cl_boardSize * GameInfo.cl_rowsWithPieces;
        GameInfo.cl_playerWhite = GameInfo.cl_playerBlack;
        
        GameInfo.cl_defaultAI = "BreakthroughAI.jar";
        GameInfo.cl_pathToAgent1 = "BreakthroughAI.jar";
        GameInfo.cl_pathToAgent2 = "BreakthroughAI.jar";
        
        GameInfo.cl_cellTheme1 = "Mahogany.png";
        GameInfo.cl_cellTheme2 = "LightWood.png";
        GameInfo.cl_boardTheme = "Default";
        GameInfo.cl_pieceTheme = "Default";
        GameInfo.aiOneStats = new AIStats();
        GameInfo.aiTwoStats = new AIStats();
        
        GameInfo.aiOneThinkTime = 3;
        GameInfo.aiTwoThinkTime = 3;
        setDate();
    }
    
    private void setDate()
    {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        DecimalFormat format = new DecimalFormat("00");
        GameInfo.date = Integer.toString(year) + "-" + format.format(month)
            + "-" + format.format(day);
    }
    
    public void updateSettings()
    {
        
    }
    
}
