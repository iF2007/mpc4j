package edu.alibaba.mpc4j.s2pc.aby.pcg.osn.rosn.gmr21;

import edu.alibaba.mpc4j.common.rpc.desc.SecurityModel;
import edu.alibaba.mpc4j.common.rpc.pto.AbstractMultiPartyPtoConfig;
import edu.alibaba.mpc4j.s2pc.aby.pcg.osn.rosn.NetRosnConfig;
import edu.alibaba.mpc4j.s2pc.aby.pcg.osn.rosn.RosnFactory.RosnType;
import edu.alibaba.mpc4j.s2pc.pcg.ot.cot.CotConfig;
import edu.alibaba.mpc4j.s2pc.pcg.ot.cot.CotFactory;
import edu.alibaba.mpc4j.s2pc.pcg.ot.cot.pre.PreCotConfig;
import edu.alibaba.mpc4j.s2pc.pcg.ot.cot.pre.PreCotFactory;

/**
 * GMR21 Network Random OSN config.
 *
 * @author Weiran Liu
 * @date 2024/5/8
 */
public class Gmr21NetRosnConfig extends AbstractMultiPartyPtoConfig implements NetRosnConfig {
    /**
     * COT
     */
    private final CotConfig cotConfig;
    /**
     * pre-computed COT
     */
    private final PreCotConfig preCotConfig;

    private Gmr21NetRosnConfig(Builder builder) {
        super(SecurityModel.MALICIOUS, builder.cotConfig, builder.preCotConfig);
        cotConfig = builder.cotConfig;
        preCotConfig = builder.preCotConfig;
    }

    @Override
    public CotConfig getCotConfig() {
        return cotConfig;
    }

    @Override
    public PreCotConfig getPreCotConfig() {
        return preCotConfig;
    }

    @Override
    public RosnType getPtoType() {
        return RosnType.GMR21_NET;
    }

    public static class Builder implements org.apache.commons.lang3.builder.Builder<Gmr21NetRosnConfig> {
        /**
         * COT
         */
        private CotConfig cotConfig;
        /**
         * pre-computed COT
         */
        private final PreCotConfig preCotConfig;

        public Builder(boolean silent) {
            cotConfig = CotFactory.createDefaultConfig(SecurityModel.SEMI_HONEST, silent);
            preCotConfig = PreCotFactory.createDefaultConfig(SecurityModel.SEMI_HONEST);
        }

        public Builder setCotConfig(CotConfig cotConfig) {
            this.cotConfig = cotConfig;
            return this;
        }

        @Override
        public Gmr21NetRosnConfig build() {
            return new Gmr21NetRosnConfig(this);
        }
    }
}
