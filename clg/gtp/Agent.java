package clg.gtp;

import java.io.*;
import java.util.Observable;

import javax.swing.JOptionPane;

public class Agent extends Observable implements Runnable
{
    /** Default command to call for the agent. */
    public static final String DEFAULT_COMMAND = "java -jar BreakthroughAI.jar --extrainfo --no-children";
    
    /** Process that holds the agent. */
    private Process agentProcess;
    
    public Agent()
    {
        this(DEFAULT_COMMAND);
    }

    public Agent(String command)
    {
        try
        {
            agentProcess = new ProcessBuilder(command.split(" ")).start();
        } catch (IOException e)
        {
            System.out.println("Cannot start agent.");
            JOptionPane.showMessageDialog(null, "Cannot start agent.");
        }
    }
    
    public Process getAgentProcess()
    {
        return agentProcess;
    }

    @Override
    public void run()
    {
        try
    	{
            final InputStreamReader agentStreamReader = new InputStreamReader(agentProcess.getInputStream());
            final BufferedReader outputBuffer = new BufferedReader(agentStreamReader);
            while (true)
            {
                String response = outputBuffer.readLine();
                
                if (response != null && response.length() > 2)
                {
                	setChanged();
                	notifyObservers(response);
                }
            }
        }
        catch (IOException e)
        {
            // do nothing since the thread is only reading, not writing
        }
        
    }
}
