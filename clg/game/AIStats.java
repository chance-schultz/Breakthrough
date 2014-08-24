package clg.game;

import java.util.Observable;
/**
 * 
 * @author Devon
 */
public class AIStats extends Observable
{
	public void updateData(String value)
	{
		setChanged();
		notifyObservers(value);
	}
}
