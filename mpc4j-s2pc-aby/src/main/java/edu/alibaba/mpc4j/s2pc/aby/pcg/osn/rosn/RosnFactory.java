package edu.alibaba.mpc4j.s2pc.aby.pcg.osn.rosn;

import edu.alibaba.mpc4j.common.rpc.Party;
import edu.alibaba.mpc4j.common.rpc.Rpc;
import edu.alibaba.mpc4j.common.rpc.desc.SecurityModel;
import edu.alibaba.mpc4j.common.rpc.pto.PtoFactory;
import edu.alibaba.mpc4j.s2pc.aby.pcg.osn.rosn.cgp20.Cgp20CstRosnConfig;
import edu.alibaba.mpc4j.s2pc.aby.pcg.osn.rosn.cgp20.Cgp20CstRosnReceiver;
import edu.alibaba.mpc4j.s2pc.aby.pcg.osn.rosn.cgp20.Cgp20CstRosnSender;
import edu.alibaba.mpc4j.s2pc.aby.pcg.osn.rosn.gmr21.Gmr21NetRosnConfig;
import edu.alibaba.mpc4j.s2pc.aby.pcg.osn.rosn.gmr21.Gmr21NetRosnReceiver;
import edu.alibaba.mpc4j.s2pc.aby.pcg.osn.rosn.gmr21.Gmr21NetRosnSender;
import edu.alibaba.mpc4j.s2pc.aby.pcg.osn.rosn.lll24.*;
import edu.alibaba.mpc4j.s2pc.aby.pcg.osn.rosn.ms13.Ms13NetRosnConfig;
import edu.alibaba.mpc4j.s2pc.aby.pcg.osn.rosn.ms13.Ms13NetRosnReceiver;
import edu.alibaba.mpc4j.s2pc.aby.pcg.osn.rosn.ms13.Ms13NetRosnSender;
import edu.alibaba.mpc4j.s2pc.aby.pcg.osn.rosn.prrs24.Prrs24OprfRosnConfig;
import edu.alibaba.mpc4j.s2pc.aby.pcg.osn.rosn.prrs24.Prrs24OprfRosnReceiver;
import edu.alibaba.mpc4j.s2pc.aby.pcg.osn.rosn.prrs24.Prrs24OprfRosnSender;
import edu.alibaba.mpc4j.s2pc.pcg.ot.cot.CotFactory;

/**
 * Random OSN factory.
 *
 * @author Weiran Liu
 * @date 2024/5/8
 */
public class RosnFactory implements PtoFactory {
    /**
     * private constructor.
     */
    private RosnFactory() {
        // empty
    }

    /**
     * protocol type
     */
    public enum RosnType {
        /**
         * LLL24 Network
         */
        LLL24_NET,
        /**
         * LLL24 Compose Share Translation
         */
        LLL24_CST,
        /**
         * PRRS24
         */
        PRRS24_OPRF,
        /**
         * CGP20 Compose Share Translation
         */
        CGP20_CST,
        /**
         * GMR21
         */
        GMR21_NET,
        /**
         * MS13
         */
        MS13_NET,
    }

    /**
     * Creates a sender.
     *
     * @param senderRpc     sender RPC.
     * @param receiverParty receiver party.
     * @param config        config.
     * @return a sender.
     */
    public static RosnSender createSender(Rpc senderRpc, Party receiverParty, RosnConfig config) {
        RosnType type = config.getPtoType();
        switch (type) {
            case MS13_NET:
                return new Ms13NetRosnSender(senderRpc, receiverParty, (Ms13NetRosnConfig) config);
            case GMR21_NET:
                return new Gmr21NetRosnSender(senderRpc, receiverParty, (Gmr21NetRosnConfig) config);
            case CGP20_CST:
                return new Cgp20CstRosnSender(senderRpc, receiverParty, (Cgp20CstRosnConfig) config);
            case LLL24_CST:
                return new Lll24CstRosnSender(senderRpc, receiverParty, (Lll24CstRosnConfig) config);
            case LLL24_NET:
                return new Lll24NetRosnSender(senderRpc, receiverParty, (Lll24NetRosnConfig) config);
            case PRRS24_OPRF:
                return new Prrs24OprfRosnSender(senderRpc, receiverParty, (Prrs24OprfRosnConfig) config);
            default:
                throw new IllegalArgumentException("Invalid " + RosnType.class.getSimpleName() + ": " + type.name());
        }
    }

    /**
     * Creates a receiver.
     *
     * @param receiverRpc receiver RPC.
     * @param senderParty sender party.
     * @param config      config.
     * @return a receiver.
     */
    public static RosnReceiver createReceiver(Rpc receiverRpc, Party senderParty, RosnConfig config) {
        RosnType type = config.getPtoType();
        switch (type) {
            case MS13_NET:
                return new Ms13NetRosnReceiver(receiverRpc, senderParty, (Ms13NetRosnConfig) config);
            case GMR21_NET:
                return new Gmr21NetRosnReceiver(receiverRpc, senderParty, (Gmr21NetRosnConfig) config);
            case CGP20_CST:
                return new Cgp20CstRosnReceiver(receiverRpc, senderParty, (Cgp20CstRosnConfig) config);
            case LLL24_CST:
                return new Lll24CstRosnReceiver(receiverRpc, senderParty, (Lll24CstRosnConfig) config);
            case LLL24_NET:
                return new Lll24NetRosnReceiver(receiverRpc, senderParty, (Lll24NetRosnConfig) config);
            case PRRS24_OPRF:
                return new Prrs24OprfRosnReceiver(receiverRpc, senderParty, (Prrs24OprfRosnConfig) config);
            default:
                throw new IllegalArgumentException("Invalid " + RosnType.class.getSimpleName() + ": " + type.name());
        }
    }

    /**
     * Creates a default config.
     *
     * @param silent        using silent OT.
     * @param securityModel the security model.
     * @return a default config.
     */
    public static RosnConfig createDefaultConfig(SecurityModel securityModel, boolean silent) {
        switch (securityModel) {
            case IDEAL:
            case TRUSTED_DEALER:
            case SEMI_HONEST:
                return new Lll24NetRosnConfig.Builder(silent).build();
            case MALICIOUS:
                return new Lll24NetRosnConfig.Builder(silent).setCotConfig(CotFactory.createDefaultConfig(SecurityModel.MALICIOUS, silent)).build();
            default:
                throw new IllegalArgumentException("Invalid " + SecurityModel.class.getSimpleName() + ": " + securityModel.name());
        }
    }
}
