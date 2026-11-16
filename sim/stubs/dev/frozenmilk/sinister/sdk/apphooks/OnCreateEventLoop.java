package dev.frozenmilk.sinister.sdk.apphooks;

import android.content.Context;

import com.qualcomm.ftccommon.FtcEventLoop;

public interface OnCreateEventLoop {
    void onCreateEventLoop(Context context, FtcEventLoop ftcEventLoop);
}
