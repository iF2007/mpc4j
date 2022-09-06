package edu.alibaba.mpc4j.common.tool.crypto.ecc.openssl;

import edu.alibaba.mpc4j.common.tool.crypto.ecc.NativeEcc;

import java.nio.ByteBuffer;

/**
 * OpenSSL实现SecP256r1椭圆曲线运算的本地函数。
 *
 * @author Weiran Liu
 * @date 2022/9/2
 */
public class SecP256r1OpensslNativeEcc implements NativeEcc {
    /**
     * 单例模式
     */
    private static final SecP256r1OpensslNativeEcc INSTANCE = new SecP256r1OpensslNativeEcc();

    /**
     * 单例模式
     */
    private SecP256r1OpensslNativeEcc() {
        // empty
    }

    public static SecP256r1OpensslNativeEcc getInstance() {
        return INSTANCE;
    }

    @Override
    public synchronized native void init();

    @Override
    public synchronized native ByteBuffer precompute(String pointString);

    @Override
    public synchronized native void destroyPrecompute(ByteBuffer windowHandler);

    @Override
    public native String singleFixedPointMultiply(ByteBuffer windowHandler, String rString);

    @Override
    public native String[] fixedPointMultiply(ByteBuffer windowHandler, String[] rStrings);

    @Override
    public native String singleMultiply(String pointString, String rString);

    @Override
    public native String[] multiply(String pointString, String[] rStrings);

    @Override
    public synchronized native void reset();
}
