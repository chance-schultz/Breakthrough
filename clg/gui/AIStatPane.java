package clg.gui;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


import clg.game.GameInfo;

/**
 * 
 * @author Devon Smart
 */
@SuppressWarnings("serial")
public class AIStatPane extends JPanel 
{
	private JLabel label1;
	private JLabel label2;
	private JTextField aiOneTextBox;
	private JTextField aiTwoTextBox;
	private JButton browseButton1;
	private JButton browseButton2;
	private JButton aiOneInfo;
	private JButton aiTwoInfo;
	private JButton aiOneTTT;
	private JButton aiTwoTTT;
	private JTextField aiOneThinkTime;
	private JTextField aiTwoThinkTime;
	private JButton apply;
	

	private GameManager manager;
	private StatWindow aiOneStatsWindow;
	private StatWindow aiTwoStatsWindow;
	
	private JPanel top;
	private JPanel bottom;
	
	private String pathToAgent1;
	private String pathToAgent2;
	private int agentOneThinkTime;
	private int agentTwoThinkTime;
	private boolean optionChanged;
	
	public AIStatPane(JFrame parent, GameManager gameManager)
	{
		super();
		manager = gameManager;
		
		
		aiOneStatsWindow = new StatWindow(parent, GameInfo.aiOneStats, "AI One", 0);
		aiTwoStatsWindow = new StatWindow(parent, GameInfo.aiTwoStats, "AI Two", 1);
		
		top = new JPanel(new GridBagLayout());
		bottom = new JPanel(new GridBagLayout());
		
		pathToAgent1 = "";
		pathToAgent2 = "";
		
		agentOneThinkTime = GameInfo.aiOneThinkTime;
		agentTwoThinkTime = GameInfo.aiTwoThinkTime;
		
		optionChanged = false;
		
		initComponents();
		layoutComponents(); 
		addListeners();
	}
	
	/**
	 * Initialize all primary components
	 */
	private void initComponents()
	{
		//Initialize components
    	label1 = new JLabel("AI 1");
    	label2 = new JLabel("AI 2");

    	aiOneTextBox = new JTextField();
    	aiTwoTextBox = new JTextField();
    	
    	aiOneTextBox.setColumns(21);
    	aiTwoTextBox.setColumns(21);
    	
    	browseButton1 = new JButton(new ImageIcon(this.getClass().getResource("images/folderIcon.png")));
		browseButton1.setBorder(BorderFactory.createEmptyBorder());
		browseButton1.setActionCommand("AI One");
		
		browseButton2 = new JButton(new ImageIcon(this.getClass().getResource("images/folderIcon.png")));
		browseButton2.setBorder(BorderFactory.createEmptyBorder());
		browseButton2.setActionCommand("AI Two");
		
		aiOneInfo = new JButton("AI 1 Stats");
		aiTwoInfo = new JButton("AI 2 Stats");
		
		aiOneTTT = new JButton("Change1");
		aiTwoTTT = new JButton("Change2");
		aiOneThinkTime = new JTextField(String.valueOf(agentOneThinkTime));
		aiTwoThinkTime = new JTextField(String.valueOf(agentTwoThinkTime));
		aiOneThinkTime.setEditable(false);
		aiTwoThinkTime.setEditable(false);
		
		apply = new JButton("Apply");
		apply.setEnabled(false);
	}
	
	private void layoutComponents()
	{
		this.setLayout(new GridBagLayout());
		GridBagConstraints main = new GridBagConstraints();
		
		main.gridx = 0;
		main.gridy = 0;
		main.fill = GridBagConstraints.BOTH;
		main.weightx = 1;
		main.weighty = .2;
		this.add(top, main);
		main.weighty = .7;
		main.gridy = 1;
		this.add(bottom, main);
		
		main.weighty = .1;
		main.gridy = 2;
		main.fill = GridBagConstraints.NONE;
		main.anchor = GridBagConstraints.NORTH;
		this.add(apply,main);
		//Time to think labels
		JLabel aiOneTThink = new JLabel("AI 1");
		JLabel aiTwoTThink = new JLabel("AI 2");
		aiOneTThink.setHorizontalAlignment(SwingConstants.CENTER);
		aiTwoTThink.setHorizontalAlignment(SwingConstants.CENTER);
		
		//Create GridBagConstraint
		GridBagConstraints c = new GridBagConstraints();
    	Insets inset = new Insets(10,10,10,0);
    	
    	//Add labels
    	c.insets = inset;
    	c.weightx = 0;
    	c.weighty = 0;
    	c.anchor = GridBagConstraints.EAST;
    	
    	c.gridx = 0;
    	c.gridy = 0;
		
		c.fill = GridBagConstraints.HORIZONTAL;
		
		top.add(label1, c);
		
		c.gridy = 1;
		
		top.add(label2, c);
		
		//Add text fields
		c.weightx = 1;
     	c.gridy = 0;
		c.gridx = 1;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 3;
		
		top.add(aiOneTextBox, c);
		
		c.gridx = 1;
		c.gridy = 1;

		top.add(aiTwoTextBox, c);
		
		//Add browse button
		c.weightx = 0;
		c.gridx = 4;
		c.gridy = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		inset.right = 10;
		
		top.add(browseButton1, c);
		
		c.gridy = 1;
		top.add(browseButton2, c);
					
		//Add stat buttons
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		
		c.gridwidth = 2;
		
		top.add(aiOneInfo, c);
	
		c.gridx = 3;
		inset.right = 10;
		
		top.add(aiTwoInfo, c);

		//Add struts for precise positioning
		inset.set(0,0,0,0);
		c.weightx = 1;
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 2;
		
		top.add(Box.createHorizontalStrut(120), c);
		
		c.gridx = 3;
		top.add(Box.createHorizontalStrut(120), c);
		
		c.gridx = 3;
		c.gridheight = 1;
		c.gridwidth = 1;
		//Push components to the top of the pane by setting all extra vertical space to the bottom
		top.add(Box.createVerticalStrut(0), c);
		
		JPanel group = new JPanel(new GridBagLayout());
		inset = new Insets(5,5,10,5);
		c.anchor = GridBagConstraints.CENTER;
		c.insets = inset;
		c.weightx = 1;
		c.weighty = 0;

		
		c.gridx = 3;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		
		group.add(new JLabel("Think Time"),c);
		
		c.gridx = 1;
		c.gridy = 3;
		
		group.add(aiOneTThink, c);
				
		c.gridy = 5;
		group.add(aiTwoTThink, c);
		
		c.gridx = 3;
		c.gridy = 3;
		
		group.add(aiOneThinkTime, c);
		
		c.gridy = 5;
		group.add(aiTwoThinkTime, c);
		
		c.gridx = 5;
		c.gridy = 3;
		group.add(aiOneTTT, c);
		
		c.gridy = 5;
		group.add(aiTwoTTT, c);
		
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		
		inset = new Insets(0,10,0,10);
		c.insets = inset;
		group.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		bottom.add(group, c);
		c.gridy = 1;
		c.weighty = 1;
		
		bottom.add(Box.createVerticalStrut(0), c);		
	}
	
	/**
	 * Add action listeners to all components
	 */
	private void addListeners()
	{
		aiOneInfo.addActionListener(new ActionListener() 
		{	
			@Override
			public void actionPerformed(ActionEvent e)
			{
				(new Thread(aiOneStatsWindow)).start();
			}
		});
		
		aiTwoInfo.addActionListener(new ActionListener() 
		{	
			@Override
			public void actionPerformed(ActionEvent e)
			{
				(new Thread(aiTwoStatsWindow)).start();
			}
		});
		
		aiOneTTT.addActionListener(new ActionListener()
		{	
			@Override
			public void actionPerformed(ActionEvent e)
			{
				agentOneThinkTime = setThinkTime("AI 1");
				aiOneThinkTime.setText(String.valueOf(agentOneThinkTime));
			}
		});
		
		aiTwoTTT.addActionListener(new ActionListener()
		{	
			@Override
			public void actionPerformed(ActionEvent e)
			{
				agentTwoThinkTime = setThinkTime("AI 2");
				aiTwoThinkTime.setText(String.valueOf(agentTwoThinkTime));
			}
		});
		
		aiOneTextBox.getDocument().addDocumentListener(new DocumentListener()
        {
            
            @Override
            public void removeUpdate(DocumentEvent e)
            {
                pathToAgent1 = aiOneTextBox.getText();
                optionChanged = true;
            }
            
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                pathToAgent1 = aiOneTextBox.getText();
                optionChanged = true;
            }
            
            @Override
            public void changedUpdate(DocumentEvent e)
            {
                
            }
        });
		
		aiTwoTextBox.getDocument().addDocumentListener(new DocumentListener()
        {
            
            @Override
            public void removeUpdate(DocumentEvent e)
            {
                pathToAgent2 = aiTwoTextBox.getText();
                optionChanged = true;
            }
            
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                pathToAgent2 = aiTwoTextBox.getText();
                optionChanged = true;
            }
            
            @Override
            public void changedUpdate(DocumentEvent e)
            {
                
            }
        });
		
		browseButton1.addActionListener(pickAI);
		browseButton2.addActionListener(pickAI);
		
		apply.addActionListener(new ActionListener()
		{	
			@Override
			public void actionPerformed(ActionEvent e)
			{
				applyChanges();
			}
		});
	}
	
	
	ActionListener pickAI = new ActionListener() 
	{	
		@Override
		public void actionPerformed(ActionEvent e)
		{
			String button = e.getActionCommand();
			String pathToAgent = "";
			
			JFileChooser chooser = new JFileChooser();
			int status = chooser.showOpenDialog(null);
			if (status == JFileChooser.APPROVE_OPTION)
			{
				try
				{
					pathToAgent = chooser.getSelectedFile().getCanonicalPath();
					if (pathToAgent.matches(".*\\.jar$"))
						pathToAgent = "java -jar " + pathToAgent;
				}
				catch (IOException ex)
				{
					System.out.println("Cannot access selected agent.");
				}
			}
			
			switch (button)
			{
				case "AI One":
					pathToAgent1 = pathToAgent;
					aiOneTextBox.setText(pathToAgent1);
					optionChanged = true;
					apply.setEnabled(true);
					break;
				case "AI Two":
					pathToAgent2 = pathToAgent;
					aiTwoTextBox.setText(pathToAgent2);
					optionChanged = true;
					apply.setEnabled(true);
					break;
			}
		}
	};
	
	private void applyChanges()
	{
	    if (optionChanged)
	    {
	        if ((pathToAgent1 != null && !pathToAgent1.equals(GameInfo.cl_pathToAgent1)) || (pathToAgent2 != null && !pathToAgent2.equals(GameInfo.cl_pathToAgent2)))
	        {
	            if (!pathToAgent1.equals(GameInfo.cl_pathToAgent1))
	            {
	                GameInfo.cl_pathToAgent1 = pathToAgent1;

	                if (manager.gtp1.isAgentActive())
	                    manager.gtp1.disconnect();
	                
	                manager.gtp1.connect(GameInfo.cl_pathToAgent1);
	            }

	            if (!pathToAgent2.equals(GameInfo.cl_pathToAgent2))
	            {
	                GameInfo.cl_pathToAgent2 = pathToAgent2;

	                if (manager.gtp2.isAgentActive())
	                    manager.gtp2.disconnect();
	                
	                manager.gtp2.connect(GameInfo.cl_pathToAgent2);
	                manager.gtp1.setAgentVersusAgent(true);
	                manager.gtp2.setAgentVersusAgent(true);
	            }
	                        
	            manager.newGame();
	        }
	        
	        boolean thinkingTimeChanged = false;
	        
	        if (agentOneThinkTime != GameInfo.aiOneThinkTime)
	        {
	            GameInfo.aiOneThinkTime = agentOneThinkTime;
	            thinkingTimeChanged = true;
	        }
	        
	        if (agentTwoThinkTime != GameInfo.aiTwoThinkTime)
	        {
	            GameInfo.aiTwoThinkTime = agentTwoThinkTime;
	            thinkingTimeChanged = true;
	        }
	        
	        if (thinkingTimeChanged)
	            manager.updateThinkTime();
	    }
		
	}
	
	private int setThinkTime(String agent)
	{
		String val = JOptionPane.showInputDialog("Enter "+ agent +" Thinking Time");
		int time = 0;
		try
		{
			if(val == null)
				time = 0;
			else
				time = Integer.parseInt(val);
		}
		catch (NumberFormatException e)
		{
			time = setThinkTime(agent);
		}
		return time;
	}

	public void resetAll()
	{
		if (aiOneStatsWindow != null)
			aiOneStatsWindow.reset();
		
		if (aiTwoStatsWindow != null)
			aiTwoStatsWindow.reset();
	}
}