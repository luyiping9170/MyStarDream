package com.dreamhouse.controller;

/**
 * 对用户登陆及相关权限控制的管理器。
 * Created by jason on 2015/7/2.
 */
public class UserMgr {
    private static UserMgr instance;

    public synchronized static UserMgr getInstance() {
        if (instance == null)
            instance = new UserMgr();
        return instance;
    }

    private UserMgr() {

    }

    public int loginAdmin(String username, String password) {
        return 0;
    }

    public boolean loginPlayer(String playerId) {
        return false;
    }

    public boolean addAdmin(String username, String password) {
        return false;
    }

    public boolean addPlayer() {
        return false;
    }
}
