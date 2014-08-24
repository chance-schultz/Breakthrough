package clg.gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import clg.game.GameInfo;
import clg.gtp.Agent;


@SuppressWarnings("serial")
public class OptionsView extends JDialog
{
	private static final String TITLE = "Preferences";
	private static String[] sizeOpts = {"8x8" , "10x10", "12x12"};
	private static final Pattern getSizeFromString = Pattern.compile("([1-9]\\d?)x\\1");
	private static final String[] colorOpts = {"Default", "Metal", "Cartoon", "8-Bit"};
	private static final String[] pieceOpts = {"Default", "Software", "Cartoon", "Ghosts"};
	
	private final Font FONT = new Font(Font.SANS_SERIF, Font.BOLD, 16);
	
	private GameManager manager;
	private String pathToAgent;
	private boolean optionChanged = false;
	
	private JTextField fileBox;
	private JButton save;
	private JButton cancel;
	private JButton browse;
	private JButton defaultAgent;
	private Container pane;
	
	private JLabel size;
	private JLabel count;
	private JLabel pcImg;
	private JLabel clrSchm;
	private JLabel aiLbl;
	
	private JComboBox<String> sizeBox;
	private JComboBox<Integer> countBox;
	private JComboBox<String> pcImgBox;
	private JComboBox<String> clrSchmBox;
	
	private JLabel szTooltip;
	private JLabel cntTooltip;
	private JLabel pcTooltip;
	private JLabel clrTooltip;
	//private JLabel aiTooltip;
	
	private boolean themeChanged = false;
	
	public OptionsView(JFrame owner, GameManager newManager)
	{
		super(owner);
		manager = newManager;
		this.setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
		this.setTitle(TITLE);
		pane = this.getContentPane();
		pane.setLayout(new GridBagLayout());
		
		initComponents();
		positionElements();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setResizable(false);
		this.pack();		
	}
	
	private void initComponents()
	{
		size = new JLabel("Board Size");
		count = new JLabel("Row Count");
		pcImg = new JLabel("Theme");
		clrSchm = new JLabel("Color Scheme");
		aiLbl = new JLabel("Choose Agent");
				
		sizeBox = new JComboBox<String>(sizeOpts);
		countBox = new JComboBox<Integer>();
		pcImgBox = new JComboBox<String>(pieceOpts);
		clrSchmBox = new JComboBox<String>(colorOpts);
		fileBox = new JTextField("", 10);
		
		save = new JButton("Save");
		cancel = new JButton("Cancel");
		defaultAgent = new JButton ("Default AI");
		browse = new JButton(new ImageIcon(this.getClass().getResource("images/folderIcon.png")));
		browse.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));
		
		sizeBox.setBackground(Color.white);
		countBox.setBackground(Color.white);
		pcImgBox.setBackground(Color.white);
		clrSchmBox.setBackground(Color.white);
				
		setFont();		
		setToolTips();
		load();
		loadSettings();
		addListeners();
	}
	
	private void setToolTips()
	{
		ImageIcon i = new ImageIcon(this.getClass().getResource("images/toolTip.png"));
		szTooltip = new JLabel(i);
		cntTooltip = new JLabel(i);
		pcTooltip = new JLabel(i);
		clrTooltip = new JLabel(i);
		
		szTooltip.setToolTipText("Board size stuff");
		cntTooltip.setToolTipText("Num of pieces stuff");
		pcTooltip.setToolTipText("Piece theme stuff");
		clrTooltip.setToolTipText("Board color stuff");
	}
	
	private void setFont()
	{
		//Label fonts
		size.setFont(FONT);
		count.setFont(FONT);
		pcImg.setFont(FONT);
		clrSchm.setFont(FONT);
		aiLbl.setFont(FONT);
		
		//Box fonts
		sizeBox.setFont(FONT);
		fileBox.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
	}
	
	private void loadSettings()
	{
		sizeBox.setSelectedItem(GameInfo.cl_boardSize);
		countBox.setSelectedItem(GameInfo.cl_rowsWithPieces);
		pcImgBox.setSelectedItem(GameInfo.cl_pieceTheme);
		clrSchmBox.setSelectedItem(GameInfo.cl_boardTheme);
		fileBox.setText(GameInfo.cl_pathToAgent1);
		colorScheme();
		pieceSelection();
	}
	
	private void load()
	{
	    switch (GameInfo.cl_boardSize)
	    {
	        case 8:
	            sizeBox.setSelectedIndex(0);
	            break;
	        case 10:
	            sizeBox.setSelectedIndex(1);
	            break;
	        case 12:
	            sizeBox.setSelectedIndex(2);
	            break;
	    }
	    
	    int size = GameInfo.cl_boardSize / 2;
	    fillRowCountBox(size);
	    
	    if (GameInfo.cl_rowsWithPieces < size)
	        countBox.setSelectedItem(GameInfo.cl_rowsWithPieces);
	    else
	        countBox.setSelectedItem(2);
	}
	
	private void addListeners()
	{
	    countBox.addActionListener(new ActionListener()
        {
            
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                optionChanged = true;
            }
        });
		
		sizeBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				int maxFilledRows = 0;
				
				@SuppressWarnings("unchecked")
				JComboBox<String> cb = (JComboBox<String>) e.getSource();
				Matcher mat = getSizeFromString.matcher((String)cb.getSelectedItem());
				
				if (mat.find())
				    maxFilledRows = Integer.parseInt(mat.group(1)) / 2;
				
				fillRowCountBox(maxFilledRows);
				
				if (GameInfo.cl_rowsWithPieces < maxFilledRows)
		            countBox.setSelectedItem(GameInfo.cl_rowsWithPieces);
		        else
		            countBox.setSelectedItem(2);
				
				optionChanged = true;
			}
		});
		
		fileBox.getDocument().addDocumentListener(new DocumentListener()
        {
			
            @Override
            public void removeUpdate(DocumentEvent e)
            {
                pathToAgent = fileBox.getText();
                optionChanged = true;
            }
            
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                pathToAgent = fileBox.getText();
                optionChanged = true;
            }
            
            @Override
            public void changedUpdate(DocumentEvent e)
            {
                
            }
        });
		
		cancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				dispose();
			}
		});
		
		browse.addActionListener(new ActionListener()
        {       
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                JFileChooser chooser = new JFileChooser();
                int status = chooser.showOpenDialog(null);
                if (status == JFileChooser.APPROVE_OPTION)
                {
                    try
                    {
                        pathToAgent = chooser.getSelectedFile().getCanonicalPath();
                        if (pathToAgent.matches(".*\\.jar$"))
                            pathToAgent = "java -jar " + pathToAgent;
                                                
                        fileBox.setText(pathToAgent);
                        optionChanged = true;
                    }
                    catch (IOException e)
                    {
                        System.out.println("Cannot access selected agent.");
                    }
                }
            }
        });
		
		save.addActionListener(new ActionListener()
        {  
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int boardSize = GameInfo.cl_boardSize;
                int rowsWithPieces = GameInfo.cl_rowsWithPieces;
               
                if (optionChanged)
                {
                    if (pathToAgent != null && !pathToAgent.equals(GameInfo.cl_pathToAgent1))
                    {
                        GameInfo.cl_pathToAgent1 = pathToAgent;
                        if (manager.gtp1.isAgentActive())
                        {
                            manager.gtp1.disconnect();
                            manager.gtp1.connect(GameInfo.cl_pathToAgent1);                            
                            manager.newGame();
                        }
                        manager.gtp1.setAgentVersusAgent(false);
                    }
                    
                    Matcher mat = getSizeFromString.matcher((String)sizeBox.getSelectedItem());
                    if (mat.find())
                        boardSize = Integer.parseInt(mat.group(1));
                    
                    rowsWithPieces = (Integer)countBox.getSelectedItem();
                    
                    if (GameInfo.cl_boardSize != boardSize)
                    {
                        GameInfo.cl_boardSize = boardSize;
                    }
                    
                    if (GameInfo.cl_rowsWithPieces != rowsWithPieces)
                    {
                        GameInfo.cl_rowsWithPieces = rowsWithPieces;
                    }
                    
                    if (themeChanged)
                    {
                    	colorScheme();
                    	pieceSelection();
                    }
                    manager.resetGameEngine();
                }
                dispose();
            }
        });
		
		defaultAgent.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				GameInfo.cl_pathToAgent1 = Agent.DEFAULT_COMMAND;
				pathToAgent = Agent.DEFAULT_COMMAND;
				fileBox.setText(pathToAgent);
			}
		});
		
		clrSchmBox.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				if (checkScheme())
				{
					optionChanged = true;
					themeChanged = true;
				}
			}
		});
		
		pcImgBox.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				if (checkPiece())
				{
					optionChanged = true;
					themeChanged = true;
				}
			}
		});
	}
	
	public void showOptions()
	{
		this.setVisible(true);
	}
	
	private void fillRowCountBox(int max)
	{
		countBox.removeAllItems();

		for (int i = 1; i <= max; i++)
			countBox.addItem(i);		
	}
	
	private void positionElements()
	{		
		Insets labels = new Insets(10,25,10,0);
		Insets boxes = new Insets (10,25,10,10);
		Insets toolTip = new Insets (10,0,10,15);
		Insets buttons = new Insets(10,0,10,0);
		
		GridBagConstraints c = new GridBagConstraints();
		c.insets = labels;		
		c.gridx = 0;
		c.gridy = 0;
	
		c.anchor = GridBagConstraints.WEST;

		//Add labels
		pane.add(size, c);
		
		c.gridy = 1;
		pane.add(count, c);
		
		c.gridy = 2;
		pane.add(pcImg, c);
		
		c.gridy = 3;
		pane.add(clrSchm, c);
		
		c.gridy++;
		
		pane.add(aiLbl, c);
		
		c.weightx = 1;
		c.weighty = 1;	
		//Add combo boxes		
		c.insets = boxes;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
				
		pane.add(sizeBox,c);
		
		c.gridy = 1;
		pane.add(countBox,c);
		
		c.gridy = 2;
		pane.add(pcImgBox, c);
		
		c.gridy = 3;
		pane.add(clrSchmBox, c);
		
		boxes.right = 0;		
		c.gridy++;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		pane.add(fileBox, c);
			
		c.weightx = 0;
		c.weighty = 0;
		//Add tooltips
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.NONE;
		c.insets = toolTip;
		c.gridx = 3;
		c.gridy = 0;
		
		pane.add(szTooltip, c);
		
		c.gridy++;
		pane.add(cntTooltip,c);
		
		c.gridy++;
		pane.add(pcTooltip,c);
		
		c.gridy++;
		pane.add(clrTooltip,c);
		
		
		//Add buttons
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = buttons;
		c.gridx = 3;
		c.gridy = 4;
		pane.add(browse,c);
		
		
		//Add default button
		buttons.left = 10;
		buttons.right = 10;		
		c.gridx = 4;
		pane.add(defaultAgent,c);
		
		//Add separator
		c.gridy = 5;
		c.gridx = 0;
		c.gridwidth = 5;
		c.fill = GridBagConstraints.HORIZONTAL;
		buttons.bottom = 0;
		pane.add(new JSeparator(SwingConstants.HORIZONTAL),c);
		
		
		//Add save button
		buttons.right = 0;
		buttons.bottom = 10;
		c.gridy = 6;
		c.gridx = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 4;
		
		pane.add(save, c);
		
		//Add cancel button
		buttons.left = 10;
		buttons.right = 10;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;
		c.gridx = 4;
		pane.add(cancel,c);
		
	}
	
	private void colorScheme()
	{
		String s = clrSchmBox.getSelectedItem().toString();
		GameInfo.cl_boardTheme = s;
		
    	switch(s)
        {
        	case "Metal":
        		GameInfo.cl_cellTheme1 = "metallight.png";
        		GameInfo.cl_cellTheme2 = "metaldark.png";
                break;
        	case "Cartoon":
        		GameInfo.cl_cellTheme1 = "CGrassTile.png";
        		GameInfo.cl_cellTheme2 = "CRockTile.png";
                break;
        	case "8-Bit":
        		GameInfo.cl_cellTheme1 = "8BitBrickTile.png";
        		GameInfo.cl_cellTheme2 = "8BitBrickTile.png";
        		break;
        	case "Default":
                GameInfo.cl_cellTheme1 = "LightWood.png";
                GameInfo.cl_cellTheme2 = "Mahogany.png";
        		break;
        }
	}
	
	private void pieceSelection()
	{
		String s = pcImgBox.getSelectedItem().toString();
		GameInfo.cl_pieceTheme = s;
		
    	switch(s)
        {
        	case "Software":
        		GameInfo.cl_team1 = "FireFoxPiece.png";
        		GameInfo.cl_team2 = "LinuxPiece.png";
                break;
        	case "Cartoon":
        		GameInfo.cl_team1 = "BK201Piece.png";
    			GameInfo.cl_team2 = "KrillinPiece.png";
    			break;
        	case "Ghosts":
        		GameInfo.cl_team1 = "ghostRobotPiece.png";
    			GameInfo.cl_team2 = "ghostAlienPiece.png";
    			break;
        	case "Default":
        		GameInfo.cl_team1 = "team1.png";
        		GameInfo.cl_team2 = "team2.png";
                break;
        }
	}
	/**
	 * Determines if the board theme has been changed
	 * @return true if the theme has changed false if not 
	 */
	private boolean checkScheme()
	{
		String s = clrSchmBox.getSelectedItem().toString();
		return !s.equalsIgnoreCase(GameInfo.cl_boardTheme);
	}
	private boolean checkPiece()
	{
		String s = pcImgBox.getSelectedItem().toString();
		return !s.equalsIgnoreCase(GameInfo.cl_pieceTheme);
	}
}
