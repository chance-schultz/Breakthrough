package clg.gui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractButton;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;

public class MenuListener implements ActionListener
{
	private GameManager gameManager;
	public MenuListener(GameManager manager)
	{
		gameManager = manager;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		String sel = ((AbstractButton)e.getSource()).getText();
		
		switch (sel)
		{
			case "New Game":
			    gameManager.newGame();
				break;
			case "Save Game":
				gameManager.saveGame();
				break;
			case "Load Game":
				gameManager.loadGame();
				break;
			case "Analyze":
				gameManager.analyse();
				break;
			case "Opponent":
				gameManager.versusOption();
				break;
			case "Preferences":
				gameManager.showOptions();
				break;
			case "Quit":
			        gameManager.exit();
			        break;
			case "Rules":
				gameManager.showHelp();
				break;
			case "About":
				String about = "Breakthrough \n Developed by: \n John Albichara \n Derick Cornejo \n Fernand Garin\n Chance Schultz\n Devon Smart";
				JOptionPane.showMessageDialog(null, about);
				break;
			default:
				break;
		}		
	}

}
