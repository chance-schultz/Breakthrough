package clg.gui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class GameWindowListener implements WindowListener
{
    private GameManager manager;
    
    public GameWindowListener(GameManager manager)
    {
        this.manager = manager;
    }

    @Override
    public void windowActivated(WindowEvent arg0) {}

    @Override
    public void windowClosed(WindowEvent arg0) {}

    @Override
    public void windowClosing(WindowEvent arg0)
    {
    	manager.quitGame();
    }

    @Override
    public void windowDeactivated(WindowEvent arg0) {}

    @Override
    public void windowDeiconified(WindowEvent arg0) {}

    @Override
    public void windowIconified(WindowEvent arg0) {}

    @Override
    public void windowOpened(WindowEvent arg0) {}

}
