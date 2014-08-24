package clg.gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import clg.game.AIStats;

public class StatWindow extends JDialog implements Runnable
{
	private static final long serialVersionUID = 42L;
	
	private JTextArea display;
	private JScrollPane scroll;
	private AIStats info;
	private int index;

	public StatWindow(JFrame owner, AIStats stat, String title, int pos)
	{
		super();
		setTitle(title);
		info = stat;
		index = pos;
		this.setModalityType(JDialog.ModalityType.MODELESS);
	}
	
	private void setUpDisplay()
	{
		display = new JTextArea();
		display.setEditable(false);
		display.setEnabled(true);
		display.setRows(15);
		display.setColumns(50);
		DefaultCaret c = (DefaultCaret) display.getCaret();
        c.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		scroll = new JScrollPane(display,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(scroll);
	}
	
	private void positionDialog()
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(screenSize.width - getWidth() , index * (getHeight()));		
	}
	
	public void reset()
	{
		if (display != null)
			display.setText("");
	}
	
	Observer observer = new Observer() 
	{	
		@Override
		public void update(Observable o, Object arg)
		{
			if (arg instanceof String)
			{
				String disp = (String)arg;
				display.append(disp);
			}
		}
	};

	@Override
	public void run()
	{
		info.addObserver(observer);
		setUpDisplay();
		this.pack();
		this.setVisible(true);
		positionDialog();
	}
}
