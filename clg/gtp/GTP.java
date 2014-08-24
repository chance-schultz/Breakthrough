package clg.gtp;

import java.awt.Point;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.*;

import clg.game.GameInfo;
import clg.gui.GameManager;

public class GTP implements Observer
{

    /** Regular expression that matches moves of the form a2-a3. */
    private static final Pattern movePattern = Pattern.compile("= ([a-z])([1-9]?\\d)[-x]([a-z])([1-9]?\\d)");
    
    /** Reference to the game manager. */
    private GameManager manager;
    /** Reference to the agent's process. */
    private Process agentProcess;
    /** Reference to the agent's stdin. */
    private PrintWriter agentInput;
    /** Reference to the Agent class. */
    private Agent agent;
    /** Reference to the Thread that constantly polls the agent's process. */
    private Thread agentThread;
    /** Is agent active? */
    private boolean activeAgent;
    /** Is agent thinking? */
    private boolean agentIsThinking;
    /** Agent vs. agent? */
    private boolean agentVersusAgent;
    /** Name of agent. */
    private String id;
    
    public GTP(GameManager newManager, String newId)
    {
        manager = newManager;
        id = newId;
        activeAgent = false;
        agentIsThinking = false;
        agentVersusAgent = false;
    }
    
    /**
     * @return the agentVersusAgent
     */
    public boolean isAgentVersusAgent()
    {
        return agentVersusAgent;
    }

    /**
     * @param agentVersusAgent the agentVersusAgent to set
     */
    public void setAgentVersusAgent(boolean agentVersusAgent)
    {
        this.agentVersusAgent = agentVersusAgent;
    }

    public boolean isAgentActive()
    {
        return activeAgent;
    }
    
    public boolean isAgentThinking()
    {
        return agentIsThinking;
    }
    
    public void connect(String pathToAgent)
    {
        if (!activeAgent)
        {
            if (pathToAgent == null || pathToAgent.equals(""))
                agent = new Agent();
            else
                agent = new Agent(pathToAgent);            
        }        
        
        if (agent.getAgentProcess() != null)
        {
            agentIsThinking = false;
            // start polling thread
            agentThread = new Thread(agent);
            agentThread.start();
            activeAgent = true;
            // Subscribe the observer to the agent
            agent.addObserver(this);
            agentProcess = agent.getAgentProcess();
            agentInput = new PrintWriter(agentProcess.getOutputStream());
        }
        
    }
    
    public void disconnect()
    {
        agent.deleteObservers();
        activeAgent = false;
        agentIsThinking = false;
        agentThread.interrupt();
        agentProcess.destroy();
    }
    
    public void resetAgent()
    {
        String command = "boardsize " + GameInfo.cl_boardSize;
        agentInput.println(command);
        agentInput.flush();
    }
    
    public void playMove(String move)
    {
        agentInput.println(move);
        agentInput.flush();
    }
    
    public void sendMove(Point moveFrom, Point moveTo)
    {
        String command = "play";
        if (GameInfo.playerOne)
            command += " b ";
        else
            command += " w ";
        String moveNotation;
        if (GameInfo.handleCapture)
            moveNotation = "x";
        else
            moveNotation = "-";
        String move = pointToString(moveFrom) + moveNotation + pointToString(moveTo);
        command += move;
        agentInput.println(command);
        agentInput.flush();
        // send genmove command to agent
        genMove();
    }
    
    public void genMove()
    {
        String command = "genmove ";
        if (GameInfo.playerOne)
            command += "w";
        else
            command += "b";
        agentInput.println(command);
        agentInput.flush();
        agentIsThinking = true;
    }
    
    private String pointToString(Point pt)
    {
        int chessRank = GameInfo.cl_boardSize - pt.y;
        char chessFile = (char)('a' + pt.x);
        String pointChessNotation = "" + chessFile + chessRank;
        return pointChessNotation;
    }
    
    public void pauseAgent()
    {
        agentInput.println("# pause");
        agentInput.flush();
    }
    
    public void resumeAgent()
    {
        agentInput.println("# resume");
        agentInput.flush();
    }
    
    public void changeThinkingTime(int newTime)
    {
        agentInput.println("thinkingtime " + newTime);
        agentInput.flush();
    }
    
    @Override
    public void update(Observable obj, Object arg)
    {
        if (arg instanceof String)
        {
            String response = (String)arg;
            manager.updateStats(response, id);
            getMove(response);
        }
    }
    
    public Process getAgentProcess()
    {
        return agentProcess;
    }

    private void getMove(String response)
    {
        Matcher mat = movePattern.matcher(response);
        if (mat.find())
        {
            agentIsThinking = false;
            char letterFileOrigin = mat.group(1).charAt(0);
            char letterFileDest = mat.group(3).charAt(0);
            
            int originX = GameInfo.cl_boardSize - Integer.valueOf(mat.group(2));
            int destX = GameInfo.cl_boardSize - Integer.valueOf(mat.group(4));            
            int originY = letterFileOrigin % 'a';
            int destY = letterFileDest % 'a';
            manager.makeMove(new Point(originY, originX), new Point(destY, destX));
        }
    }

}
