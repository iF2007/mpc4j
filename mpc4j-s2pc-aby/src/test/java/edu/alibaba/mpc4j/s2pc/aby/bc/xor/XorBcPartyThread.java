package edu.alibaba.mpc4j.s2pc.aby.bc.xor;

import edu.alibaba.mpc4j.common.rpc.MpcAbortException;
import edu.alibaba.mpc4j.s2pc.aby.bc.BcBitVector;
import edu.alibaba.mpc4j.s2pc.aby.bc.BcParty;

/**
 * XOR-BC协议参与方线程。
 *
 * @author Weiran Liu
 * @date 2022/02/14
 */
class XorBcPartyThread extends Thread {
    /**
     * BC协议参与方
     */
    private final BcParty bcParty;
    /**
     * xi
     */
    private final BcBitVector xi;
    /**
     * yi
     */
    private final BcBitVector yi;
    /**
     * 运算数量
     */
    private final int num;
    /**
     * zi
     */
    private BcBitVector zi;

    XorBcPartyThread(BcParty bcParty, BcBitVector xi, BcBitVector yi) {
        this.bcParty = bcParty;
        assert xi.bitLength() == yi.bitLength();
        this.xi = xi;
        this.yi = yi;
        num = xi.bitLength();
    }

    BcBitVector getPartyOutput() {
        return zi;
    }

    @Override
    public void run() {
        try {
            bcParty.getRpc().connect();
            bcParty.init(num, num);
            zi = bcParty.xor(xi, yi);
            bcParty.getRpc().disconnect();
        } catch (MpcAbortException e) {
            e.printStackTrace();
        }
    }
}
