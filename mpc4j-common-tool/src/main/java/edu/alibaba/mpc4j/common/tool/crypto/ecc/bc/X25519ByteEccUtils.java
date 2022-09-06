package edu.alibaba.mpc4j.common.tool.crypto.ecc.bc;

import org.bouncycastle.math.ec.rfc7748.X25519Field;

/**
 * X25519字节椭圆曲线，实现由Bouncy Castle实现改造而来，参见：
 * <p>
 * org.bouncycastle.math.ec.rfc7748.X25519.java
 * </p>
 *
 * @author Weiran Liu
 * @date 2022/9/1
 */
@SuppressWarnings("SuspiciousNameCombination")
class X25519ByteEccUtils {
    /**
     * Curve25519有限域
     */
    private static class Curve25519Field extends X25519Field {

    }

    /**
     * 点的整数长度
     */
    private static final int POINT_INTS = 8;
    /**
     * 点的字节长度
     */
    static final int POINT_BYTES = POINT_INTS * 4;
    /**
     * 幂指数的整数长度
     */
    private static final int SCALAR_INTS = 8;
    /**
     * 幂指数的字节长度
     */
    static final int SCALAR_BYTES = SCALAR_INTS * 4;
    /**
     * 无穷远点：X = 0，小端表示
     */
    static final byte[] POINT_INFINITY = new byte[]{
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
    };

    /**
     * 基点B：x = 9，小端表示
     */
    static final byte[] POINT_B = new byte[]{
        (byte) 0x09, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
    };
    /**
     * y^2 = x^3 + ax^2 + x，a的取值
     */
    private static final int C_A = 486662;
    /**
     * (a + 2) / 4
     */
    private static final int C_A24 = (C_A + 2) / 4;

    private static int decode32(byte[] bs, int off) {
        int n = bs[off] & 0xFF;
        n |= (bs[++off] & 0xFF) << 8;
        n |= (bs[++off] & 0xFF) << 16;
        n |= bs[++off] << 24;
        return n;
    }

    private static void decodeScalar(byte[] k, int[] n) {
        for (int i = 0; i < SCALAR_INTS; ++i) {
            n[i] = decode32(k, i * 4);
        }
        // 指数模l
        n[0] &= 0xFFFFFFF8;
        n[POINT_INTS - 1] &= 0x7FFFFFFF;
        n[POINT_INTS - 1] |= 0x40000000;
    }

    /**
     * 计算z = 2x。
     *
     * @param x 椭圆曲线点x。
     * @param z 椭圆曲线点z。
     */
    private static void pointDouble(int[] x, int[] z) {
        int[] a = Curve25519Field.create();
        int[] b = Curve25519Field.create();

        Curve25519Field.apm(x, z, a, b);
        Curve25519Field.sqr(a, a);
        Curve25519Field.sqr(b, b);
        Curve25519Field.mul(a, b, x);
        Curve25519Field.sub(a, b, a);
        Curve25519Field.mul(a, C_A24, z);
        Curve25519Field.add(z, b, z);
        Curve25519Field.mul(z, a, z);
    }

    static void precomputeBase() {
        Ed25519ByteEccUtils.precomputeBase();
    }

    /**
     * 计算R = k · U。
     *
     * @param k 幂指数k。
     * @param u 基点U。
     * @param r 结果点R。
     */
    static void scalarMult(byte[] k, byte[] u, byte[] r) {
        int[] n = new int[SCALAR_INTS];
        decodeScalar(k, n);

        int[] x1 = Curve25519Field.create();
        Curve25519Field.decode(u, 0, x1);
        int[] x2 = Curve25519Field.create();
        Curve25519Field.copy(x1, 0, x2, 0);
        int[] z2 = Curve25519Field.create();
        z2[0] = 1;
        int[] x3 = Curve25519Field.create();
        x3[0] = 1;
        int[] z3 = Curve25519Field.create();

        int[] t1 = Curve25519Field.create();
        int[] t2 = Curve25519Field.create();

        int bit = 254, swap = 1;
        do {
            Curve25519Field.apm(x3, z3, t1, x3);
            Curve25519Field.apm(x2, z2, z3, x2);
            Curve25519Field.mul(t1, x2, t1);
            Curve25519Field.mul(x3, z3, x3);
            Curve25519Field.sqr(z3, z3);
            Curve25519Field.sqr(x2, x2);

            Curve25519Field.sub(z3, x2, t2);
            Curve25519Field.mul(t2, C_A24, z2);
            Curve25519Field.add(z2, x2, z2);
            Curve25519Field.mul(z2, t2, z2);
            Curve25519Field.mul(x2, z3, x2);

            Curve25519Field.apm(t1, x3, x3, z3);
            Curve25519Field.sqr(x3, x3);
            Curve25519Field.sqr(z3, z3);
            Curve25519Field.mul(z3, x1, z3);

            --bit;

            int word = bit >>> 5, shift = bit & 0x1F;
            int kt = (n[word] >>> shift) & 1;
            swap ^= kt;
            Curve25519Field.cswap(swap, x2, x3);
            Curve25519Field.cswap(swap, z2, z3);
            swap = kt;
        } while (bit >= 3);

        for (int i = 0; i < 3; ++i) {
            pointDouble(x2, z2);
        }

        Curve25519Field.inv(z2, z2);
        Curve25519Field.mul(x2, z2, x2);

        Curve25519Field.normalize(x2);
        Curve25519Field.encode(x2, r, 0);
    }

    /**
     * 计算R = k · G。
     *
     * @param k 幂指数k。
     * @param r 结果R。
     */
    static void scalarMultBase(byte[] k, byte[] r) {
        int[] y = Curve25519Field.create();
        int[] z = Curve25519Field.create();
        Ed25519ByteEccUtils.scalarMultBaseYZ(k, y, z);
        Curve25519Field.apm(z, y, y, z);
        Curve25519Field.inv(z, z);
        Curve25519Field.mul(y, z, y);
        Curve25519Field.normalize(y);
        Curve25519Field.encode(y, r, 0);
    }
}
