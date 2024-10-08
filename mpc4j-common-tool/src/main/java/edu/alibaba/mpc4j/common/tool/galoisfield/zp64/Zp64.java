package edu.alibaba.mpc4j.common.tool.galoisfield.zp64;

import edu.alibaba.mpc4j.common.tool.galoisfield.LongField;
import edu.alibaba.mpc4j.common.tool.galoisfield.zp64.Zp64Factory.Zp64Type;

/**
 * Zp64有限域运算接口。
 *
 * @author Weiran Liu
 * @date 2022/7/7
 */
public interface Zp64 extends LongField {
    /**
     * Gets the Zp64 type.
     *
     * @return the Zp64 type.
     */
    Zp64Type getZp64Type();

    /**
     * Gets the prime.
     *
     * @return the prime.
     */
    long getPrime();

    /**
     * Computes a mod p.
     *
     * @param a the input a.
     * @return a mod p.
     */
    long module(final long a);
}
