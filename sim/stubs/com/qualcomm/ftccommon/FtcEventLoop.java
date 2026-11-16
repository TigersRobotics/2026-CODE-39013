package com.qualcomm.ftccommon;

import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl;

public class FtcEventLoop {
    private final OpModeManagerImpl opModeManager;

    public FtcEventLoop(OpModeManagerImpl opModeManager) {
        this.opModeManager = opModeManager;
    }

    public OpModeManagerImpl getOpModeManager() { return opModeManager; }
}
