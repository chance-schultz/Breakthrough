package clg.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import clg.game.GameInfo;

public class VersusDialog
{
	private GameManager gameManager;
	private JDialog versus;
	private ButtonGroup group;
	private ButtonModel previous;
	
	public VersusDialog(GameManager manager)
	{
		gameManager = manager;
		versus = new JDialog(gameManager.window);
		versus.setTitle("Opponent");
		versus.setResizable(false);
		buildGUI();
		setOnCloseListener();
	}
	
	private void buildGUI()
	{	
		JPanel pane = new JPanel();
		pane.setLayout(new GridBagLayout());
		
		versus.setContentPane(pane);
		versus.setVisible(false);
		
		JButton okButton = new JButton("Ok");
		GridBagConstraints c = new GridBagConstraints();
		
		String hVh = "HVH";
		String hVc = "HVC";
		String cVc = "CVC";
		
		group = new ButtonGroup();
		
		JRadioButton[] choices = new JRadioButton[3];
		choices[0] = new JRadioButton("Human vs. Human");
		choices[0].setActionCommand(hVh);
		
		choices[1] = new JRadioButton("Human vs. Computer");
		choices[1].setActionCommand(hVc);
		
		choices[2] = new JRadioButton("Computer vs. Computer");
		choices[2].setActionCommand(cVc);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(8, 10, 0, 10);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		c.weightx = 1;
		c.weighty = 0;
		
		for(JRadioButton b: choices)
		{
			pane.add(b,c);
			group.add(b);
			c.gridy++;
		}
		
		choices[0].setSelected(true);
		previous = group.getSelection();
		
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.CENTER;
		pane.add(Box.createHorizontalStrut(0),c);
		
		c.gridx = 2;
		pane.add(Box.createHorizontalStrut(0),c);
		
		c.gridx = 1;
		c.insets.top = 20;
		c.insets.bottom = 10;
		
		pane.add(okButton, c);
		
		okButton.addActionListener(new ActionListener() 
		{	
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String cmd = group.getSelection().getActionCommand();

				//Only make changes when the choice is changed
				if(!previous.equals(group.getSelection()))
				{
					switch (cmd)
					{
					case "HVH":
						if (gameManager.gtp1.isAgentActive())
							gameManager.gtp1.disconnect();
						if (gameManager.gtp2.isAgentActive())
							gameManager.gtp2.disconnect();
						break;

					case "HVC":
						if (gameManager.gtp1.isAgentActive())
							return;
						else
						{
							gameManager.gtp1.connect(GameInfo.cl_pathToAgent1);
							// set to be human vs. agent
							gameManager.gtp1.setAgentVersusAgent(false);
							gameManager.gtp2.setAgentVersusAgent(false);
						}        	    
						break;

					case "CVC":
						if (!gameManager.gtp1.isAgentActive())
							gameManager.gtp1.connect(GameInfo.cl_pathToAgent1);

						if (!gameManager.gtp2.isAgentActive())
							gameManager.gtp2.connect(GameInfo.cl_pathToAgent2);
						
						gameManager.gtp1.setAgentVersusAgent(true);
		                gameManager.gtp2.setAgentVersusAgent(true);

						break;
					}

					gameManager.newGame();
				}
				
				versus.dispose();
			}
		});
		
		versus.setModal(true);
		
		versus.pack();	
	}
	
	public void setOnCloseListener()
	{
		versus.addWindowListener(new WindowAdapter() 
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				if (!previous.equals(group.getSelection()))
				{
					group.setSelected(previous, true);
				}		
			}
		});
	}
	
	public void showDialog()
	{
		previous = group.getSelection();
		versus.setVisible(true);
	}
}
