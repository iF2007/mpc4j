package edu.alibaba.mpc4j.s2pc.pcg.vole.gf2k.nc;

import edu.alibaba.mpc4j.common.rpc.Party;
import edu.alibaba.mpc4j.common.rpc.Rpc;
import edu.alibaba.mpc4j.common.rpc.desc.SecurityModel;
import edu.alibaba.mpc4j.common.rpc.pto.PtoFactory;
import edu.alibaba.mpc4j.s2pc.pcg.vole.gf2k.nc.wykw21.Wykw21Gf2kNcVoleConfig;
import edu.alibaba.mpc4j.s2pc.pcg.vole.gf2k.nc.wykw21.Wykw21Gf2kNcVoleReceiver;
import edu.alibaba.mpc4j.s2pc.pcg.vole.gf2k.nc.wykw21.Wykw21Gf2kNcVoleSender;

/**
 * no-choice GF2K-VOLE factory.
 *
 * @author Weiran Liu
 * @date 2023/7/23
 */
public class Gf2kNcVoleFactory implements PtoFactory {
    /**
     * private constructor.
     */
    private Gf2kNcVoleFactory() {
        // empty
    }

    /**
     * protocol type.
     */
    public enum Gf2kNcVoleType {
        /**
         * WYKW21
         */
        WYKW21,
    }

    /**
     * Creates a sender.
     *
     * @param senderRpc     the sender RPC.
     * @param receiverParty the receiver party.
     * @param config        the config.
     * @return a sender.
     */
    public static Gf2kNcVoleSender createSender(Rpc senderRpc, Party receiverParty, Gf2kNcVoleConfig config) {
        Gf2kNcVoleType type = config.getPtoType();
        //noinspection SwitchStatementWithTooFewBranches
        switch (type) {
            case WYKW21:
                return new Wykw21Gf2kNcVoleSender(senderRpc, receiverParty, (Wykw21Gf2kNcVoleConfig) config);
            default:
                throw new IllegalArgumentException("Invalid " + Gf2kNcVoleType.class.getSimpleName() + ": " + type.name());
        }
    }

    /**
     * Creates a receiver.
     *
     * @param receiverRpc the receiver RPC.
     * @param senderParty the sender party.
     * @param config      the config.
     * @return a receiver.
     */
    public static Gf2kNcVoleReceiver createReceiver(Rpc receiverRpc, Party senderParty, Gf2kNcVoleConfig config) {
        Gf2kNcVoleType type = config.getPtoType();
        //noinspection SwitchStatementWithTooFewBranches
        switch (type) {
            case WYKW21:
                return new Wykw21Gf2kNcVoleReceiver(receiverRpc, senderParty, (Wykw21Gf2kNcVoleConfig) config);
            default:
                throw new IllegalArgumentException("Invalid " + Gf2kNcVoleType.class.getSimpleName() + ": " + type.name());
        }
    }

    /**
     * Creates a default config.
     *
     * @param securityModel the security model.
     * @return a default config.
     */
    public static Gf2kNcVoleConfig createDefaultConfig(SecurityModel securityModel) {
        return new Wykw21Gf2kNcVoleConfig.Builder(securityModel).build();
    }
}
