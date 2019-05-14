package com.silencedut.fpsviewer;

import android.app.Application;
import androidx.annotation.Nullable;
import com.silencedut.fpsviewer.api.IDisplayFps;
import com.silencedut.fpsviewer.api.IEventRelay;
import com.silencedut.fpsviewer.api.IUtilities;
import com.silencedut.fpsviewer.background.Background;
import com.silencedut.fpsviewer.jank.MainThreadJankSniper;
import com.silencedut.fpsviewer.transfer.TransferCenter;
import com.silencedut.hub_annotation.HubInject;
import org.jetbrains.annotations.NotNull;

/**
 * @author SilenceDut
 * @date 2019/3/19
 */
@HubInject(api = IViewer.class)
public class FpsViewer implements IViewer{

    private FpsConfig mFpsConfig;

    @Override
    public void onCreate() {
    }

    public static IViewer getViewer() {
        return TransferCenter.getImpl(IViewer.class);
    }

    @Override
    public void initViewer(@NotNull Application application, @Nullable FpsConfig fpsConfig) {
        TransferCenter.getImpl(IUtilities.class).setApplication(application);
        if(fpsConfig == null) {
            this.mFpsConfig = FpsConfig.defaultConfig();
        }else {
            this.mFpsConfig = fpsConfig;
        }
        Background.INSTANCE.init(application);
        if(this.mFpsConfig.isFpsViewEnable()) {
            TransferCenter.getImpl(IUtilities.class).mainHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    TransferCenter.getImpl(IDisplayFps.class).show();
                    MainThreadJankSniper.prepare();
                    TransferCenter.getImpl(IEventRelay.class).recordFps(true);
                }
            },3000);
        }
    }

    @NotNull
    @Override
    public FpsConfig fpsConfig() {
        return mFpsConfig;
    }
}
