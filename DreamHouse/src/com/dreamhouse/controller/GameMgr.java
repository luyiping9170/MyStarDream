package com.dreamhouse.controller;

/**
 * 对每一局游戏的操作管理器。
 * Created by jason on 2015/7/2.
 */
public class GameMgr {
    private static GameMgr instance;

    public synchronized static GameMgr getInstance() {
        if (instance == null)
            instance = new GameMgr();
        return instance;
    }

    private GameMgr() {

    }

    public boolean newGame() {
        return false;
    }

    public boolean abandonGame() {
        return false;
    }

    public boolean startGame() {
        return false;
    }

    public boolean resumeGame() {
        return false;
    }

    public boolean pauseGame() {
        return false;
    }

    public boolean finishGame() {
        return false;
    }

    public boolean nextStep() {
        return false;
    }

    public boolean addTime() {
        return false;
    }
}
