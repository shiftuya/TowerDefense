package ru.nsu.fit.towerdefense.server.lobby;

import ru.nsu.fit.towerdefense.metadata.GameMetaData;
import ru.nsu.fit.towerdefense.metadata.map.GameMap;
import ru.nsu.fit.towerdefense.server.sockets.UserConnection;
import ru.nsu.fit.towerdefense.server.sockets.receivers.MessageReceiver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class LobbyControl
{

    public enum State {
        PREGAME,
        INGAME,
        POSTGAME
    }

    private static final int AWAITINGLIMIT = 10;
    private static final int TIMEOUT = 30000;
    private final Lobby lobby;
    private final HashSet<String> joinedTokens;
    private final HashSet<String> awaitingTokens;

    private HashMap<MessageReceiver, String> userToToken;
    private HashMap<String, MessageReceiver> tokenToUser;

    private Thread gameThread;
    private HashSet<String> readyUsers;

    public LobbyControl(long id)
    {
        lobby = new Lobby(id);
        joinedTokens = new HashSet<>();
        awaitingTokens = new HashSet<>();
        userToToken = new HashMap<>();
        tokenToUser = new HashMap<>();
        this.setLevel("Level 1_4");
        gameThread = new Thread(() -> {
            try
            {
                Thread.sleep(TIMEOUT);
            }
            catch (Exception e) {}
            checkIfEmpty();
        });
        gameThread.start();
    }

    public int getPlayersNumber()
    {
        return lobby.getPlayersNumber();
    }

    public long getId()
    {
        return lobby.getId();
    }

    public String getLevelName()
    {
        return lobby.getLevelName();
    }

    private void setLevel(String levelName)
    {
        GameMap level = GameMetaData.getInstance().getMapDescription(levelName);
        lobby.setLevelName(levelName);
        lobby.setPlayersNumber(level.getPlayersNumber());

        var iter = joinedTokens.iterator();
        while (joinedTokens.size() > lobby.getPlayersNumber())
        {
            iter.remove();
        }
    }


    public synchronized void join(String token){
        if (awaitingTokens.contains(token)){
            awaitingTokens.remove(token);
            joinedTokens.add(token);
        }
    }

    public synchronized boolean userLeaves(String token)
    {
        awaitingTokens.remove(token);
        joinedTokens.remove(token);
        return joinedTokens.size() == 0;
    }

    public synchronized boolean addAwaitingToken(String token){
        if (awaitingTokens.size() < AWAITINGLIMIT)
        {
            awaitingTokens.add(token);
            return true;
        }
        return false;
    }

    public boolean canJoin() {
        return joinedTokens.size() < lobby.getPlayersNumber();
    }

    public int getJoined(){
        return joinedTokens.size();
    }

    public void connectedUserLeaves(MessageReceiver receiver)
    {
        String token = userToToken.get(receiver);

        userToToken.remove(receiver, token);
        tokenToUser.remove(token, receiver);
    }


    private void checkIfEmpty()
    {
        if (joinedTokens.size() == 0)
        {
            LobbyManager.getInstance().removeLobby(this);
        }
    }

    public boolean isTokenValid(String token)
    {
        synchronized (this)
        {
            return awaitingTokens.contains(token) || joinedTokens.contains(token);
        }
    }

    public boolean addConnection(String token)
    {
        synchronized (this)
        {

        }
        return false;
    }

    public boolean addMessageReceiver(String token, MessageReceiver receiver)
    {
        synchronized (this)
        {
            if (joinedTokens.contains(token))
            {
                if (tokenToUser.containsKey(token))
                {
                    System.err.println("Connection already exists");
                    return false;
                }
                System.err.println("Connected!");
                tokenToUser.put(token, receiver);
                userToToken.put(receiver, token);
                return true;
            }
            else if (awaitingTokens.contains(token))
            {
                if (joinedTokens.size() < getPlayersNumber())
                {
                    awaitingTokens.remove(token);
                    joinedTokens.add(token);
                    tokenToUser.put(token, receiver);
                    userToToken.put(receiver, token);
                    System.err.println("Connected!");
                    return true;
                }
                System.err.println("Lobby is full");
            }
            return false;
        }
    }




}
