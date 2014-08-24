package clg.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

import clg.game.GameInfo;

public class AnalysisModeListener implements MouseListener
{
	private GameManager gameManager;
	
	public AnalysisModeListener(GameManager manager)
	{
		gameManager = manager;
	}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e)
	{
		JLabel button = (JLabel)e.getSource();
    	
		if (button.isEnabled())
		{
    		button.setBorder(new BevelBorder(BevelBorder.LOWERED));
    	
	    	switch (button.getName())
	    	{
	    		case "Play":
	    		    button.setBorder(new BevelBorder(BevelBorder.LOWERED));
	    		    if (gameManager.gtp1.isAgentActive() && !gameManager.gtp1.isAgentVersusAgent() && !GameInfo.playerOne && !gameManager.gtp1.isAgentThinking())
	                    gameManager.genMove();
	    		    
	    		    if (gameManager.gtp1.isAgentActive() && gameManager.gtp1.isAgentVersusAgent() && gameManager.isPaused)
	    		        gameManager.gtp1.resumeAgent();
	    		    if (gameManager.gtp2.isAgentActive() && gameManager.gtp2.isAgentVersusAgent() && gameManager.isPaused)
	    		        gameManager.gtp2.resumeAgent();
	    		    break;
	    		case "Undo":
	    			gameManager.undo();
	    			break;
	    		case "Redo":
	    			gameManager.redo();
	    			break;
	    		case "Pause":
	    		    button.setBorder(new BevelBorder(BevelBorder.LOWERED));
	    		    if (gameManager.gtp1.isAgentActive() && gameManager.gtp1.isAgentThinking())
	    		        gameManager.gtp1.pauseAgent();
	    		    if (gameManager.gtp2.isAgentActive() && gameManager.gtp2.isAgentThinking())
	    		        gameManager.gtp2.pauseAgent();
	    		    
	    		    gameManager.isPaused = true;
	    		    break;
	    		default:
	    			break;
	    	}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e)	
	{
		JLabel button = (JLabel)e.getSource();
		button.setBorder(new BevelBorder(BevelBorder.RAISED));
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}
}
