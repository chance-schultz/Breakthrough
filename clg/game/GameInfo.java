/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clg.game;

/**
 *
 * @author Chance
 */
public class GameInfo 
{
    public static int cl_boardSize;
    public static int cl_playerBlack;
    public static int cl_playerWhite;
    public static int cl_rowsWithPieces;
    public static int cl_rowsWithoutPieces;
    public static int cl_boardWidth;
    public static int cl_boardHeight;
    
    public static String cl_BGimg1;
    public static String cl_BGimg2;
    public static String cl_Piece1;
    public static String cl_Piece2;
    public static String cl_date;
    public static String cl_result;
    public static String cl_rules;
    public static String cl_currentMove;
    public static String cl_pathToAgent1;
    public static String cl_pathToAgent2;
    
    public static  String cl_team1 = "team1.png";
    public static  String cl_team2 = "team2.png";
    
    public static String cl_theme;
    public static String cl_boardTheme;
    public static String cl_pieceTheme;
    public static String cl_cellTheme1;
    public static String cl_cellTheme2;
    public static String cl_defaultAI;
    public static int aiOneThinkTime;
    public static int aiTwoThinkTime;
    
    public static AIStats aiOneStats;
    public static AIStats aiTwoStats;
  
    public static boolean gameOver;
    public static boolean playerOne;
    public static boolean isNewGame;
    public static boolean handleCapture;
    
    public static String date;
  
}
