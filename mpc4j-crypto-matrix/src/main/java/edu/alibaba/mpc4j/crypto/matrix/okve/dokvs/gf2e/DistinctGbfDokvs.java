package edu.alibaba.mpc4j.crypto.matrix.okve.dokvs.gf2e;

import com.google.common.base.Preconditions;
import edu.alibaba.mpc4j.common.tool.CommonConstants;
import edu.alibaba.mpc4j.common.tool.EnvType;
import edu.alibaba.mpc4j.common.tool.MathPreconditions;
import edu.alibaba.mpc4j.common.tool.crypto.prf.Prf;
import edu.alibaba.mpc4j.common.tool.crypto.prf.PrfFactory;
import edu.alibaba.mpc4j.common.tool.utils.BytesUtils;
import edu.alibaba.mpc4j.common.tool.utils.CommonUtils;
import edu.alibaba.mpc4j.common.tool.utils.ObjectUtils;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Distinct Garbled Bloom Filter (GBF) DOKVS. The original scheme is described in the following paper:
 * <p>
 * Dong C, Chen L, Wen Z. When private set intersection meets big data: an efficient and scalable protocol. CCS 2013.
 * ACM, 2013 pp. 789-800.
 * </p>
 * In this implementation, we require that any inputs have constant distinct positions in the Garbled Bloom Filter.
 * This requirement is used in the following paper:
 * <p>
 * Lepoint, Tancrede, Sarvar Patel, Mariana Raykova, Karn Seth, and Ni Trieu. Private join and compute from PIR with
 * default. ASIACRYPT 2021, Part II, pp. 605-634. Cham: Springer International Publishing, 2021.
 * </p>
 *
 * @author Weiran Liu
 * @date 2023/7/3
 */
class DistinctGbfDokvs<T> extends AbstractGf2eDokvs<T> implements SparseConstantGf2eDokvs<T> {
    /**
     * Garbled Bloom Filter needs λ hashes
     */
    static int TOTAL_HASH_NUM = CommonConstants.STATS_BIT_LENGTH;

    /**
     * Gets m for the given n.
     *
     * @param n number of key-value pairs.
     * @return m.
     */
    static int getM(int n) {
        MathPreconditions.checkPositive("n", n);
        // m = n / ln(2) * σ, flooring so that m % Byte.SIZE = 0.
        return CommonUtils.getByteLength(
            (int) Math.ceil(n * CommonConstants.STATS_BIT_LENGTH / Math.log(2))
        ) * Byte.SIZE;
    }

    /**
     * hashes
     */
    private final Prf[] prfs;

    public DistinctGbfDokvs(EnvType envType, int n, int l, byte[][] keys) {
        super(n, getM(n), l);
        MathPreconditions.checkEqual("keys.length", "hash_num", keys.length, TOTAL_HASH_NUM);
        prfs = IntStream.range(0, TOTAL_HASH_NUM)
            .mapToObj(hashIndex -> {
                Prf hash = PrfFactory.createInstance(envType, Integer.BYTES);
                hash.setKey(keys[hashIndex]);
                return hash;
            })
            .toArray(Prf[]::new);
    }

    @Override
    public int sparsePositionRange() {
        return m;
    }

    @Override
    public int[] sparsePositions(T key) {
        byte[] keyBytes = ObjectUtils.objectToByteArray(key);
        int[] sparsePositions = new int[TOTAL_HASH_NUM];
        TIntSet positionSet = new TIntHashSet(TOTAL_HASH_NUM);
        // h1
        sparsePositions[0] = prfs[0].getInteger(0, keyBytes, m);
        positionSet.add(sparsePositions[0]);
        // generate other λ - 1 distinct positions
        for (int i = 1; i < TOTAL_HASH_NUM; i++) {
            int hiIndex = 0;
            do {
                sparsePositions[i] = prfs[i].getInteger(hiIndex, keyBytes, m);
                hiIndex++;
            } while (positionSet.contains(sparsePositions[i]));
            positionSet.add(sparsePositions[i]);
        }
        return sparsePositions;
    }

    @Override
    public int sparsePositionNum() {
        return TOTAL_HASH_NUM;
    }

    @Override
    public boolean[] binaryDensePositions(T key) {
        // garbled bloom filter does not contain dense part
        return new boolean[0];
    }

    @Override
    public int maxDensePositionNum() {
        return 0;
    }

    @Override
    public byte[][] encode(Map<T, byte[]> keyValueMap, boolean doublyEncode) throws ArithmeticException {
        MathPreconditions.checkLessOrEqual("key-value size", keyValueMap.size(), n);
        keyValueMap.values().forEach(x -> Preconditions.checkArgument(BytesUtils.isFixedReduceByteArray(x, byteL, l)));
        Set<T> keySet = keyValueMap.keySet();
        // compute positions for all keys, create shares.
        byte[][] storage = new byte[m][];
        for (T key : keySet) {
            byte[] finalShare = BytesUtils.clone(keyValueMap.get(key));
            assert finalShare.length == byteL;
            int[] sparsePositions = sparsePositions(key);
            int emptySlot = -1;
            for (int position : sparsePositions) {
                if (storage[position] == null && emptySlot == -1) {
                    // if we find an empty position, reserve the location for finalShare）
                    emptySlot = position;
                } else if (storage[position] == null) {
                    // if the current position is null, generate a new share
                    storage[position] = BytesUtils.randomByteArray(byteL, l, secureRandom);
                    BytesUtils.xori(finalShare, storage[position]);
                } else {
                    // if the current position is not null, reuse the share
                    BytesUtils.xori(finalShare, storage[position]);
                }
            }
            if (emptySlot == -1) {
                // we cannot find an empty position, which happens with probability 1 - 2^{-λ}
                throw new ArithmeticException("Failed to encode Key-Value Map, cannot find empty slot");
            }
            storage[emptySlot] = finalShare;
        }
        // pad random elements in all empty positions.
        for (int i = 0; i < m; i++) {
            if (storage[i] == null) {
                storage[i] = BytesUtils.randomByteArray(byteL, l, secureRandom);
            }
        }

        return storage;
    }

    @Override
    public byte[] decode(byte[][] storage, T key) {
        // here we do not verify bit length for each storage, otherwise decode would require O(n) computation.
        assert storage.length == getM();
        int[] sparsePositions = sparsePositions(key);
        byte[] value = new byte[byteL];
        for (int position : sparsePositions) {
            BytesUtils.xori(value, storage[position]);
        }
        assert BytesUtils.isFixedReduceByteArray(value, byteL, l);
        return value;
    }

    @Override
    public Gf2eDokvsFactory.Gf2eDokvsType getType() {
        return Gf2eDokvsFactory.Gf2eDokvsType.DISTINCT_GBF;
    }
}