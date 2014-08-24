package clg.gui;

import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import clg.game.GameInfo;

public class Square extends JPanel
{
	public enum Team {Team1, Team2 };
	
	private static class Piece extends JLabel
	{
		private static final long serialVersionUID = 42L;
		private static ImageIcon team1 = new ImageIcon(Piece.class.getResource("images/" + GameInfo.cl_team1));
		private static ImageIcon team2 = new ImageIcon(Piece.class.getResource("images/" + GameInfo.cl_team2));
		
		public Piece(Team tm1, boolean visible)
		{
			if (tm1 == Team.Team1)
				setIcon(team1);
			else
				setIcon(team2);
			
			setVisible(visible);
		}
	}
	
	private static final long serialVersionUID = 42L;
	Point pos;
	private Image bgImg;
	private GridBagConstraints c;
	private Piece p;
	
	public Square(int j, int i)
	{
		super(new GridBagLayout());
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		pos = new Point(j, i);
	}

	public Point getPosition()
	{	
		return pos;
	}
	
	public void SetBG(String img)
	{
		bgImg = new ImageIcon(this.getClass().getResource("images/"+img)).getImage();
	}
	
	public void addPiece(Team team, boolean visible)
	{
		add(p = new Piece(team, visible), c);
	}
	
	public void updatePiece(Team team, boolean visible)
	{
		p.setVisible(visible);
		
		if (!visible)
			return;
		
		switch(team)
		{
			case Team1:
				p.setIcon(Piece.team1);
				break;
			case Team2:
				p.setIcon(Piece.team2);
				break;
		}
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		if (bgImg != null)
		    g.drawImage(bgImg, 0, 0, this);
	}
}
