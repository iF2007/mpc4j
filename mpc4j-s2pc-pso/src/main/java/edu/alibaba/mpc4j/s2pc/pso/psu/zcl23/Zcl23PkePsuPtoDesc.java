package edu.alibaba.mpc4j.s2pc.pso.psu.zcl23;

import edu.alibaba.mpc4j.common.rpc.desc.PtoDesc;
import edu.alibaba.mpc4j.common.rpc.desc.PtoDescManager;

/**
 * ZCL23-PKE-PSU. The protocol comes from the following paper:
 * <p>
 * Zhang, Cong, Yu Chen, Weiran Liu, Min Zhang, and Dongdai Lin. Linear Private Set Union from Multi-Query Reverse
 * Private Membership Test. USENIX Security 2023, pp. 337-354. 2023.
 * </p>
 *
 * @author Weiran Liu
 * @date 2022/02/16
 */
class Zcl23PkePsuPtoDesc implements PtoDesc {
    /**
     * protocol ID
     */
    private static final int PTO_ID = Math.abs((int) 3242597016769861554L);
    /**
     * protocol name
     */
    private static final String PTO_NAME = "ZCL23_PKE_PSU";

    /**
     * protocol step
     */
    enum PtoStep {
        /**
         * server sends encrypted elements
         */
        SERVER_SEND_ENC_ELEMENTS,
    }

    /**
     * singleton mode
     */
    private static final Zcl23PkePsuPtoDesc INSTANCE = new Zcl23PkePsuPtoDesc();

    /**
     * private constructor.
     */
    private Zcl23PkePsuPtoDesc() {
        // empty
    }

    public static PtoDesc getInstance() {
        return INSTANCE;
    }

    static {
        PtoDescManager.registerPtoDesc(getInstance());
    }

    @Override
    public int getPtoId() {
        return PTO_ID;
    }

    @Override
    public String getPtoName() {
        return PTO_NAME;
    }
}