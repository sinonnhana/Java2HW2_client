package org.example.demo;

import java.util.List;

public class Player {
    private final String username;
    private final String status;
    private final List<String> gameRecords;

    public Player(String username, String status, List<String> gameRecords) {
        this.username = username;
        this.status = status;
        this.gameRecords = gameRecords;
    }

    public String getUsername() {
        return username;
    }

    public String getStatus() {
        return status;
    }

    public List<String> getGameRecords() {
        return gameRecords;
    }
}
