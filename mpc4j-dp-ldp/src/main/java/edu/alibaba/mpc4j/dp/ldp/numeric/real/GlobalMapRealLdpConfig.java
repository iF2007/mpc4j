package edu.alibaba.mpc4j.dp.ldp.numeric.real;

import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * 全局映射实数LDP机制。
 *
 * @author Weiran Liu
 * @date 2022/5/3
 */
public class GlobalMapRealLdpConfig implements RealLdpConfig {
    /**
     * 下边界
     */
    private final double lowerBound;
    /**
     * 上边界
     */
    private final double upperBound;
    /**
     * 基础差分隐私参数ε
     */
    private final double baseEpsilon;
    /**
     * 伪随机数生成器
     */
    private final RandomGenerator randomGenerator;

    private GlobalMapRealLdpConfig(Builder builder) {
        lowerBound = builder.lowerBound;
        upperBound = builder.upperBound;
        baseEpsilon = builder.baseEpsilon;
        randomGenerator = builder.randomGenerator;
    }

    @Override
    public double getLowerBound() {
        return lowerBound;
    }

    @Override
    public double getUpperBound() {
        return upperBound;
    }

    public double getBaseEpsilon() {
        return baseEpsilon;
    }

    public RandomGenerator getRandomGenerator() {
        return randomGenerator;
    }

    public static class Builder implements org.apache.commons.lang3.builder.Builder<GlobalMapRealLdpConfig> {
        /**
         * 下边界
         */
        private final double lowerBound;
        /**
         * 上边界
         */
        private final double upperBound;
        /**
         * 基础差分隐私参数ε
         */
        private final double baseEpsilon;
        /**
         * 伪随机数生成器
         */
        private RandomGenerator randomGenerator;

        public Builder(double baseEpsilon, double lowerBound, double upperBound) {
            assert baseEpsilon > 0 : "ε must be greater than 0: " + baseEpsilon;
            this.baseEpsilon = baseEpsilon;
            assert lowerBound < upperBound : "lower bound must be less than upper bound";
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
            randomGenerator = new JDKRandomGenerator();
        }

        public Builder setRandomGenerator(RandomGenerator randomGenerator) {
            this.randomGenerator = randomGenerator;
            return this;
        }

        @Override
        public GlobalMapRealLdpConfig build() {
            return new GlobalMapRealLdpConfig(this);
        }
    }
}
