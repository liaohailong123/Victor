package org.liaohailong.library.victor.engine;

import org.liaohailong.library.victor.Deliver;

import java.math.BigDecimal;

/**
 * Describe as: 双引擎控制器
 * Created by LiaoHaiLong on 2018/5/1.
 */

public class EngineManager {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    // We want at least 2 threads and at most 4 threads in the core pool,
    // preferring to have 1 less than the CPU count to avoid saturating
    // the CPU with background work
    public static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;

    private static final int TEXT_ENGINE_POOL_SIZE = new BigDecimal(MAXIMUM_POOL_SIZE * 2f / 3f).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
    private static final int FILE_ENGINE_POOL_SIZE = new BigDecimal(MAXIMUM_POOL_SIZE * 1f / 3f).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();


    private TextEngine mTextEngine;

    public EngineManager(Deliver deliver) {
        mTextEngine = new TextEngine(deliver, TEXT_ENGINE_POOL_SIZE);
    }

    public TextEngine getTextEngine() {
        return mTextEngine;
    }

    public void fire() {
        if (mTextEngine != null) {
            mTextEngine.start();
        }
    }
}