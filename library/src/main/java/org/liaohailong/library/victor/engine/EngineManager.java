package org.liaohailong.library.victor.engine;

import org.liaohailong.library.victor.Deliver;

/**
 * Describe as: 双引擎控制器
 * Created by LiaoHaiLong on 2018/5/1.
 */

public class EngineManager {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;

    private static final int TEXT_ENGINE_POOL_SIZE = MAXIMUM_POOL_SIZE - 1;
    private static final int FILE_ENGINE_POOL_SIZE = 1;

    private TextEngine mTextEngine;
    private FileEngine mFileEngine;

    public EngineManager(Deliver deliver) {
        mTextEngine = new TextEngine(deliver, TEXT_ENGINE_POOL_SIZE);
        mFileEngine = new FileEngine(deliver, FILE_ENGINE_POOL_SIZE);
    }

    public TextEngine getTextEngine() {
        return mTextEngine;
    }

    public FileEngine getFileEngine() {
        return mFileEngine;
    }

    public void release() {
        if (mTextEngine != null) {
            mTextEngine.release();
        }
        if (mFileEngine != null) {
            mFileEngine.release();
        }
    }
}
