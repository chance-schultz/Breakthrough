package clg.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.DefaultCaret;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import clg.game.GameInfo;

public class BGui extends JFrame
{
	private static final long serialVersionUID = 42L;
	public static final int EMPTY = -1;
    public static final int BLACK = 1;
    public static final int WHITE = 0;
    
	private final int INVALIDMOVE = 1;
	private final int NOTYOURTURN = 2;
    
    private final Color STATUSBLUE = new Color(73,86,211);
    private final Color STATUSRED = new Color(255,8,31);
    
	//private GameInformation gameInfo;
    
    private Container container;
    private JPanel statusBar;
    private JMenuBar guiMenuBar;
    private JScrollPane consoleScrollBar;
    private JTextArea console;
    private GameManager manager;
    private JLabel statusLabel;
	private JPanel debugBar;
    private JPanel boardContainer;
    private JTabbedPane tabs;
    	
    private MenuListener menuListener;
    private AnalysisModeListener analyst;
    private BoardView gameBoard;
    private OptionsView options;
    
    private boolean hasSaveFile;
    private JPanel sidebar;
    private JPanel bottomBar;
    private AIStatPane infoPanel;
    
    private UndoManager undoManager;
    
    //Labels for Debug Bar
    private JLabel dB;
    private JLabel undo;
    private JLabel redo;
    
	private Thread statusThread;

    /**
     * Constructor
     * @param manager 
     * @param info
     * @param state
     */
    public BGui(GameManager manager, int[][] state, MenuListener listener, AnalysisModeListener analyst, GameWindowListener windowListener, boolean saveFile)
    {   	
    	super("Breakthrough");
        // Game initializers
    	this.manager = manager;
    	this.analyst = analyst;
    	undoManager = new UndoManager();
    	options = new OptionsView(this, manager);
    	gameBoard = new BoardView(manager, state);
    	menuListener = listener;
    	hasSaveFile = saveFile;
    	
    	    	
        //GUI initializers
		container = getContentPane();
		
		boardContainer = new JPanel(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        addWindowListener(windowListener);
        setResizable(false);
        
        container.setLayout(new BorderLayout());
    	
        drawAnalysisBar();
        drawBoard();
		drawConsole();
		drawInfoPane();
		drawStatusBar();
		statusThread = new StatusThread();
		drawMenu();
        
		container.add(boardContainer, BorderLayout.CENTER);
        setVisible(true);
        pack();
    }
    

    /**
     * Resets the game board for a new game
     * @param state
     */
    public void reset(int[][] state)
    {
    	//gameState = state;
    	drawSideBars();
        newGame();
        gameBoard.resetBoardView(state);
        this.pack();
    }
    
    /**
     * Draws the status bar 
     */
    private void drawStatusBar()
    {
		statusBar = new JPanel(new BorderLayout());
        statusLabel = new JLabel("", JLabel.CENTER);
    	statusBar.setPreferredSize(new Dimension(this.getWidth(), 25));
    	statusBar.setBackground(new Color(232,232,232));
    	statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
    	
    	statusBar.add(statusLabel, BorderLayout.CENTER);
    	statusLabel.setForeground(Color.WHITE);
    	statusLabel.setFont(new Font("Arial Black", Font.PLAIN, 14));
    	
    	container.add(statusBar, BorderLayout.SOUTH);
    }
    
    /**
     * Creates and positions the menu bar
     */
    private void drawMenu()
    {
	    guiMenuBar = new JMenuBar();
		
      	JMenu gameMenu = new JMenu("Game");
      	JMenu optionsMenu = new JMenu("Options");
    	JMenu helpMenu = new JMenu("Help");
    	
    	JMenuItem options = new JMenuItem("Opponent");
    	JMenuItem pref = new JMenuItem("Preferences");
    	
    	JMenuItem newGame = new JMenuItem("New Game");
    	JMenuItem loadGame = new JMenuItem("Load Game");
    	JMenuItem saveGame = new JMenuItem("Save Game");
    	JMenuItem quitGame = new JMenuItem("Quit");
    	
    	if (!hasSaveFile)
    	{
    		loadGame.setEnabled(false);
    	}
    	
    	newGame.addActionListener(menuListener);
    	loadGame.addActionListener(menuListener);
    	saveGame.addActionListener(menuListener);
    	quitGame.addActionListener(menuListener);
    	
    	
    	JCheckBoxMenuItem debugMenu = new JCheckBoxMenuItem("Analyze");

    	debugMenu.addActionListener(menuListener);
    	
    	options.addActionListener(menuListener);
    	pref.addActionListener(menuListener);
    	
    	optionsMenu.add(debugMenu);
    	optionsMenu.add(options);
    	optionsMenu.add(pref);
    	
    	JMenuItem about = new JMenuItem("About");
    	JMenuItem rules = new JMenuItem("Rules");
    	
    	about.addActionListener(menuListener);
    	rules.addActionListener(menuListener);
    	
    	gameMenu.add(newGame);
    	gameMenu.add(loadGame);
    	gameMenu.add(saveGame);
    	gameMenu.add(quitGame);
    	
    	helpMenu.add(about);
    	helpMenu.add(rules);
    	
    	guiMenuBar.add(gameMenu);
    	guiMenuBar.add(optionsMenu);
    	guiMenuBar.add(helpMenu);
    	
    	this.setJMenuBar(guiMenuBar);
    }
    
    /**
     * Draws the move history panel
     */
    private void drawConsole()
    {
    	tabs = new JTabbedPane();
		console = new JTextArea();
		Font f1 = new Font("SanSerif", Font.PLAIN, 16);
	
        console.setEditable(false);
        console.setFocusable(false);
        console.setFont(f1);
        console.setOpaque(false);
        console.getDocument().addUndoableEditListener(new UndoableEditListener() 
        {
			@Override
			public void undoableEditHappened(UndoableEditEvent e)
			{
				undoManager.addEdit(e.getEdit());
				if (!undo.isEnabled())
					undo.setEnabled(true);
			}
		});
		DefaultCaret c = (DefaultCaret) console.getCaret();
        c.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);         
        consoleScrollBar = new JScrollPane(console,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        consoleScrollBar.setPreferredSize(new Dimension(190, 460));
        consoleScrollBar.setBorder(BorderFactory.createLineBorder(Color.darkGray, 2, false));
        
        tabs.addTab("Console", consoleScrollBar);

        container.add(tabs,BorderLayout.EAST);
    }
    
    /**
     * Draws the info pane
     */
    private void drawInfoPane()
    {
    	infoPanel = new AIStatPane(this, manager);
    }
	
    private void drawBoard()
    {
    	bottomBar = new JPanel(new GridBagLayout());
    	sidebar = new JPanel(new GridBagLayout());
    	drawSideBars();
    	boardContainer.add(sidebar, BorderLayout.WEST);
    	boardContainer.add(bottomBar, BorderLayout.SOUTH);
    	boardContainer.add(gameBoard, BorderLayout.CENTER);	
    }
    
    private void drawSideBars()
    {
    	bottomBar.removeAll();
    	sidebar.removeAll();
    	
    	char bottom = 'A';
    	int side = GameInfo.cl_boardSize;
    	int x = 0;
    	int y = 0;
    	
    	while (side != 0)
    	{
        	GridBagConstraints c = new GridBagConstraints();
        	c.anchor = GridBagConstraints.CENTER;
        	c.weightx = 1;
        	c.weighty  = 1;
        	c.gridx = 0;
        	c.gridy = 0;
        	
    		c.gridx = x;
    		c.gridy = 0;
    		JLabel letter = new JLabel(String.valueOf(bottom));
    		bottomBar.add(letter, c);
    		x++;
    		bottom++;
    		
    		c.gridx = 0;
    		c.gridy = y;
    		sidebar.add(new JLabel(String.valueOf(side)), c);
    		y++;
    		side--;
    	}
    }
    
    /**
     * Draws Debug Bar
     */
    private void drawAnalysisBar()
    {
    	debugBar = new JPanel(new GridBagLayout());
    	debugBar.setVisible(false);
    	debugBar.setPreferredSize(new Dimension(480, 35));
    	debugBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
    	
    	GridBagConstraints c = new GridBagConstraints();
    	
    	undo = new JLabel(new ImageIcon(this.getClass().getResource("images/UndoB.png")));
    	undo.setFocusable(false);
    	undo.setBorder(new BevelBorder(BevelBorder.RAISED));
    	undo.addMouseListener(analyst);
    	undo.setName("Undo");
    	c.gridx = 0;
    	c.gridy = 0;
    	c.insets = new Insets(0,10,0,0);
    	c.ipadx = -2;
    	c.ipady = -2;
    	debugBar.add(undo, c);
    	
    	dB = new JLabel(new ImageIcon(this.getClass().getResource("images/PauseB.png")));
    	dB.setFocusable(false);
    	dB.setBorder(new BevelBorder(BevelBorder.RAISED));
    	dB.addMouseListener(analyst);
    	dB.setName("Pause");
    	c.gridx = 1;
    	c.gridy = 0;
    	c.ipadx = -2;
    	c.ipady = -2;
    	c.insets = new Insets(0,10,0,0);
    	debugBar.add(dB, c);
    	
    	dB = new JLabel(new ImageIcon(this.getClass().getResource("images/PlayB.png")));
    	dB.setFocusable(false);
    	dB.setBorder(new BevelBorder(BevelBorder.RAISED));
    	dB.addMouseListener(analyst);
    	dB.setName("Play");
    	c.gridx = 2;
    	c.gridy = 0;
    	c.ipadx = -2;
    	c.ipady = -2;
    	c.insets = new Insets(0,10,0,0);
    	debugBar.add(dB, c);
    	
    	redo = new JLabel(new ImageIcon(this.getClass().getResource("images/RedoB.png")));
    	redo.setFocusable(false);
    	redo.setBorder(new BevelBorder(BevelBorder.RAISED));
    	redo.addMouseListener(analyst);
    	redo.setName("Redo");
    	c.gridx = 3;
    	c.gridy = 0;
    	c.ipadx = -2;
    	c.ipady = -2;
    	debugBar.add(redo, c);
    	
    	boardContainer.add(debugBar, BorderLayout.NORTH);
    	
    	undo.setEnabled(false);
    	redo.setEnabled(false);
    }
    
    
    /**
     * Displays the game analysis tool bar
     */
    public void displayAnalysisBar()
    {
    	if (debugBar.isVisible())
    	{
    		debugBar.setVisible(false);
    		tabs.remove(infoPanel);
    	}
    	else
    	{
    		debugBar.setVisible(true);
    		tabs.addTab("AI Info", infoPanel);
    	}
    	this.pack();
    }	
    
    public void showOptions()
    {
    	options.showOptions();
    }

    
    /**
     * Append last move to the move history tab
     * @param message
     */
    public void writeToDisplay(String message)
    {
    	console.append(message);
    }
    
    /**
     * 
     * @param status
     * @param errorType
     */
    public void statusUpdate(final String status, final int errorType)
    {
    	if (errorType == INVALIDMOVE)
			statusBar.setBackground(STATUSRED);
		else if (errorType == NOTYOURTURN)
			statusBar.setBackground(STATUSBLUE);
		
		statusLabel.setText(status);
		
	
		if (statusThread.isAlive())
			statusThread.interrupt();
		
		statusThread = new StatusThread();
		
		statusThread.start();
    }
    
    public BoardView getGameBoard()
    {
    	return gameBoard;
    }
   
    public void newGame()
    {
        gameBoard.reset();
        console.setText("");
        infoPanel.resetAll();
        this.pack();
    }
    
    public void loadConsole(String[] log)
    {
        console.setText("");
        
        for (String move : log)
            console.append(move);
    }
    
    public void setHasSave(boolean value)
    {
    	hasSaveFile = value;
    	guiMenuBar.getMenu(0).getItem(1).setEnabled(value);
    }


	public boolean statVisible()
	{
		return true;
	}
    
    public void undo()
    {
    	try
    	{
    		undoManager.undo();
    		
    		if(!redo.isEnabled())
    			redo.setEnabled(true);
    		
    		if(!undoManager.canUndo())
    			undo.setEnabled(false);
    	}
    	catch(CannotUndoException e){}
    }
	
	public void redo()
	{
		try
    	{
    		undoManager.redo();
    		
    		if(!undo.isEnabled())
    			undo.setEnabled(true);
    		
    		if(!undoManager.canRedo())
    			redo.setEnabled(false);
    	}
    	catch(CannotRedoException e){}	
	}
	
	public void showHelp()
	{
		String rules = "Moving Pieces\n\nPieces can be moved one space forward or diagonally forward." +
					   "\n\nCapturing Pieces\n\nPieces are captured diagonally forward. Only one piece " +
					   "can be captured per turn.\n\nGoal\n\nThe goal of the game is to move one of your " +
					   "pieces to your opponent's side.\n\nInitial Setup\n\nEach player has 16 pieces " +
					   "occupying two rows on opposite sides of the board. Your pieces are always on the " +
					   "bottom of the board.";
		
		JTextArea text = new JTextArea(10,40);
		JScrollPane scroll = new JScrollPane(text);
		text.append(rules);
		text.setLineWrap(true);
		text.setWrapStyleWord(true);
		text.setCaretPosition(0);
		text.setEditable(false);

		JOptionPane.showMessageDialog(this, scroll, "How to play", JOptionPane.INFORMATION_MESSAGE);

	}
	
	class StatusThread extends Thread
	{
		@Override
		public void run()
		{
			try
			{
				Thread.sleep(1000);
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run()
					{
						statusBar.setBackground(Color.lightGray);
						statusLabel.setText("");
					}
				});
			}
			catch (InterruptedException e)
			{
				return;
			}
		}
	}
}
	
	

