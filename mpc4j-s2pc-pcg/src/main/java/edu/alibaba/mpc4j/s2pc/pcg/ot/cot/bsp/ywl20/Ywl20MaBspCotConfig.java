package edu.alibaba.mpc4j.s2pc.pcg.ot.cot.bsp.ywl20;

import edu.alibaba.mpc4j.common.rpc.desc.SecurityModel;
import edu.alibaba.mpc4j.common.rpc.pto.AbstractMultiPartyPtoConfig;
import edu.alibaba.mpc4j.s2pc.pcg.dpprf.bp.BpDpprfConfig;
import edu.alibaba.mpc4j.s2pc.pcg.dpprf.bp.BpDpprfFactory;
import edu.alibaba.mpc4j.s2pc.pcg.ot.cot.bsp.BspCotConfig;
import edu.alibaba.mpc4j.s2pc.pcg.ot.cot.bsp.BspCotFactory;
import edu.alibaba.mpc4j.s2pc.pcg.ot.cot.core.CoreCotConfig;
import edu.alibaba.mpc4j.s2pc.pcg.ot.cot.core.CoreCotFactory;

/**
 * malicious YWL20-BSP-COT config.
 *
 * @author Weiran Liu
 * @date 2022/6/7
 */
public class Ywl20MaBspCotConfig extends AbstractMultiPartyPtoConfig implements BspCotConfig {
    /**
     * core COT
     */
    private final CoreCotConfig coreCotConfig;
    /**
     * BP-DPPRF
     */
    private final BpDpprfConfig bpDpprfConfig;

    private Ywl20MaBspCotConfig(Builder builder) {
        super(SecurityModel.MALICIOUS, builder.coreCotConfig, builder.bpDpprfConfig);
        coreCotConfig = builder.coreCotConfig;
        bpDpprfConfig = builder.bpDpprfConfig;
    }

    public CoreCotConfig getCoreCotConfig() {
        return coreCotConfig;
    }

    public BpDpprfConfig getBpDpprfConfig() {
        return bpDpprfConfig;
    }

    @Override
    public BspCotFactory.BspCotType getPtoType() {
        return BspCotFactory.BspCotType.YWL20_MALICIOUS;
    }

    public static class Builder implements org.apache.commons.lang3.builder.Builder<Ywl20MaBspCotConfig> {
        /**
         * core COT
         */
        private CoreCotConfig coreCotConfig;
        /**
         * BP-DPPRF
         */
        private BpDpprfConfig bpDpprfConfig;

        public Builder() {
            coreCotConfig = CoreCotFactory.createDefaultConfig(SecurityModel.MALICIOUS);
            bpDpprfConfig = BpDpprfFactory.createDefaultConfig(SecurityModel.MALICIOUS);
        }

        public Builder setCoreCotConfig(CoreCotConfig coreCotConfig) {
            this.coreCotConfig = coreCotConfig;
            return this;
        }

        public Builder setBpDpprfConfig(BpDpprfConfig bpDpprfConfig) {
            this.bpDpprfConfig = bpDpprfConfig;
            return this;
        }

        @Override
        public Ywl20MaBspCotConfig build() {
            return new Ywl20MaBspCotConfig(this);
        }
    }
}
