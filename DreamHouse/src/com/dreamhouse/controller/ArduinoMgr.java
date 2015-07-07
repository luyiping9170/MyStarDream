package com.dreamhouse.controller;

/**
 * 对ardunio进行操作的统一管理器。
 * Created by jason on 2015/7/2.
 */
public class ArduinoMgr {
    private static ArduinoMgr instance;

    public synchronized static ArduinoMgr getInstance() {
        if (instance == null)
            instance = new ArduinoMgr();
        return instance;
    }

    private ArduinoMgr() {

    }
}
