package clg.main;

import javax.swing.UIManager;
import clg.game.Game;
import clg.gui.GameManager;

/**
 * @author Chance
 */
public class Main 
{

    public static void main(String[] args) 
    {
    	try
    	{
    		//Set UI look and feel to that of the OS
    		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());    
    	}
    	catch(Exception e)
    	{
    	}
    	finally
    	{
    		Settings settings = new Settings();
            GameManager manager = new GameManager(settings); 
            manager.init();
    	}
    }
}
