package ru.nsu.fit.towerdefense.server.lobby;

import java.util.ArrayList;
import java.util.List;

public class Lobby {
    private final Long id;
    private String levelName;
    private int playersNumber;
    private List<Long> userIds;

    public Lobby(long id){
        this.id = id;
        userIds = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public int getPlayersNumber() {
        return playersNumber;
    }

    public List<Long> getUserIds() {
        return userIds;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public void setPlayersNumber(int playersNumber) {
        this.playersNumber = playersNumber;
    }

    public void addUserId(long userId) {
        userIds.add(userId);
    }

    public void userLeaves(long userId) {
        userIds.remove(userId);
    }

}
