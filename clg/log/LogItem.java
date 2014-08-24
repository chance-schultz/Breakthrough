/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clg.log;

/**
 *
 * @author Chance
 */
public class LogItem
{
    public int[][] state;
    public String move;
    public boolean capture;
    public int numBlackPieces;
    public int numWhitePieces;

    public LogItem(int[][] s, String m)
    {
        state = s;
        move = m;
    }
}
