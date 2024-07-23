package edu.alibaba.mpc4j.s2pc.pcg.vode.gf2k.sp.ssp.gyw23;

import edu.alibaba.mpc4j.common.rpc.*;
import edu.alibaba.mpc4j.common.tool.CommonConstants;
import edu.alibaba.mpc4j.common.tool.crypto.crhf.Crhf;
import edu.alibaba.mpc4j.common.tool.crypto.crhf.CrhfFactory;
import edu.alibaba.mpc4j.common.tool.crypto.crhf.CrhfFactory.CrhfType;
import edu.alibaba.mpc4j.common.tool.utils.BytesUtils;
import edu.alibaba.mpc4j.common.tool.utils.LongUtils;
import edu.alibaba.mpc4j.s2pc.pcg.ot.cot.CotSenderOutput;
import edu.alibaba.mpc4j.s2pc.pcg.ot.cot.core.CoreCotFactory;
import edu.alibaba.mpc4j.s2pc.pcg.ot.cot.core.CoreCotSender;
import edu.alibaba.mpc4j.s2pc.pcg.vode.gf2k.Gf2kVodeReceiverOutput;
import edu.alibaba.mpc4j.s2pc.pcg.vode.gf2k.core.Gf2kCoreVodeFactory;
import edu.alibaba.mpc4j.s2pc.pcg.vode.gf2k.core.Gf2kCoreVodeReceiver;
import edu.alibaba.mpc4j.s2pc.pcg.vode.gf2k.sp.ssp.AbstractGf2kSspVodeReceiver;
import edu.alibaba.mpc4j.s2pc.pcg.vode.gf2k.sp.ssp.Gf2kSspVodeFactory;
import edu.alibaba.mpc4j.s2pc.pcg.vode.gf2k.sp.ssp.Gf2kSspVodeReceiverOutput;
import edu.alibaba.mpc4j.s2pc.pcg.vode.gf2k.sp.ssp.gyw23.Gyw23Gf2kSspVodePtoDesc.PtoStep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * GYW23 GF2K-SSP-VODE receiver.
 *
 * @author Weiran Liu
 * @date 2024/6/12
 */
public class Gyw23Gf2kSspVodeReceiver extends AbstractGf2kSspVodeReceiver {
    /**
     * core COT
     */
    private final CoreCotSender coreCotSender;
    /**
     * core GF2K-VODE receiver
     */
    private final Gf2kCoreVodeReceiver gf2kCoreVodeReceiver;
    /**
     * hash that satisfies circular correlation robustness
     */
    private final Crhf hash;
    /**
     * GF2K-VODE receiver output
     */
    private Gf2kVodeReceiverOutput gf2kVodeReceiverOutput;
    /**
     * tree depth
     */
    private int h;
    /**
     * the GGM tree. It contains (l + 1)-level keys. The i-th level contains 2^i keys.
     */
    private ArrayList<byte[][]> ggmTree;
    /**
     * K_i^0, i ∈ [1, n - 1]
     */
    private byte[][] kns;
    /**
     * K_n^0
     */
    private byte[] kn0;
    /**
     * k_n^1
     */
    private byte[] kn1;
    /**
     * COT sender output
     */
    private CotSenderOutput cotSenderOutput;

    public Gyw23Gf2kSspVodeReceiver(Rpc receiverRpc, Party senderParty, Gyw23Gf2kSspVodeConfig config) {
        super(Gyw23Gf2kSspVodePtoDesc.getInstance(), receiverRpc, senderParty, config);
        coreCotSender = CoreCotFactory.createSender(receiverRpc, senderParty, config.getCoreCotConfig());
        addSubPto(coreCotSender);
        gf2kCoreVodeReceiver = Gf2kCoreVodeFactory.createReceiver(receiverRpc, senderParty, config.getGf2kCoreVodeConfig());
        addSubPto(gf2kCoreVodeReceiver);
        hash = CrhfFactory.createInstance(envType, CrhfType.MMO);
    }

    @Override
    public void init(int subfieldL, byte[] delta) throws MpcAbortException {
        setInitInput(subfieldL, delta);
        logPhaseInfo(PtoState.INIT_BEGIN);

        stopWatch.start();
        coreCotSender.init(delta);
        gf2kCoreVodeReceiver.init(subfieldL, delta);
        stopWatch.stop();
        long initTime = stopWatch.getTime(TimeUnit.MILLISECONDS);
        stopWatch.reset();
        logStepInfo(PtoState.INIT_STEP, 1, 1, initTime);

        logPhaseInfo(PtoState.INIT_END);
    }

    @Override
    public Gf2kSspVodeReceiverOutput receive(int num) throws MpcAbortException {
        setPtoInput(num);
        return receive();
    }

    @Override
    public Gf2kSspVodeReceiverOutput receive(int num, Gf2kVodeReceiverOutput preReceiverOutput) throws MpcAbortException {
        setPtoInput(num, preReceiverOutput);
        gf2kVodeReceiverOutput = preReceiverOutput;
        return receive();
    }

    private Gf2kSspVodeReceiverOutput receive() throws MpcAbortException {
        logPhaseInfo(PtoState.PTO_BEGIN);

        Gf2kSspVodeReceiverOutput receiverOutput;
        if (num == 1) {
            // we directly use (0, β, M[β]) as output since M[β] = K[β] + β · Γ.
            stopWatch.start();
            Gf2kVodeReceiverOutput correctGf2kVodeReceiverOutput = correctVode();
            stopWatch.stop();
            long vodeTime = stopWatch.getTime(TimeUnit.MILLISECONDS);
            stopWatch.reset();
            logStepInfo(PtoState.PTO_STEP, 1, 2, vodeTime);

            stopWatch.start();
            assert correctGf2kVodeReceiverOutput.getNum() == 1;
            byte[] kBeta = correctGf2kVodeReceiverOutput.getQ(0);
            receiverOutput = Gf2kSspVodeReceiverOutput.create(field, delta, new byte[][]{kBeta});
            stopWatch.stop();
            long outputTime = stopWatch.getTime(TimeUnit.MILLISECONDS);
            stopWatch.reset();
            logStepInfo(PtoState.PTO_STEP, 2, 2, outputTime);
        } else {
            stopWatch.start();
            Gf2kVodeReceiverOutput correctGf2kVodeReceiverOutput = correctVode();
            stopWatch.stop();
            long vodeTime = stopWatch.getTime(TimeUnit.MILLISECONDS);
            stopWatch.reset();
            logStepInfo(PtoState.PTO_STEP, 1, 3, vodeTime, "Sender corrects sVODE");

            stopWatch.start();
            h = LongUtils.ceilLog2(num, 1);
            // P0 and P1 send (extend, n) to F_COT, which returns (K[r_1], . . . , K[r_n]) ∈ F^n_{2^λ} to P0 and
            // ((r_1, ..., r_n), (M[r_1], ..., M[r_n])) ∈ F_2^n × F^n_{2λ} to P1
            // such that M[r_i] = K[r_i] ⊕ r_i · ∆ for i ∈ [1, n]. Here we use α = α_1...α_n := !r_1...!r_n
            cotSenderOutput = coreCotSender.send(h);
            stopWatch.stop();
            long cotTime = stopWatch.getTime(TimeUnit.MILLISECONDS);
            stopWatch.reset();
            logStepInfo(PtoState.PTO_STEP, 2, 3, cotTime, "Sender executes COT");

            stopWatch.start();
            List<byte[]> correlationPayload;
            if (num == 2) {
                generateNum2GgmTree();
                correlationPayload = generateNum2CorrelationPayload(correctGf2kVodeReceiverOutput);
            } else {
                generateGgmTree();
                correlationPayload = generateCorrelationPayload(correctGf2kVodeReceiverOutput);
            }
            sendOtherPartyPayload(PtoStep.RECEIVER_SEND_CORRELATION.ordinal(), correlationPayload);
            // P0 outputs v, we need to reduce num
            byte[][] vs = ggmTree.get(h);
            if (num < (1 << h)) {
                byte[][] reduceWs = new byte[num][];
                System.arraycopy(vs, 0, reduceWs, 0, num);
                vs = reduceWs;
            }
            receiverOutput = Gf2kSspVodeReceiverOutput.create(field, delta, vs);
            ggmTree = null;
            stopWatch.stop();
            long ggmTime = stopWatch.getTime(TimeUnit.MILLISECONDS);
            stopWatch.reset();
            logStepInfo(PtoState.PTO_STEP, 3, 3, ggmTime, "Receiver handles GGT tree");
        }

        logPhaseInfo(PtoState.PTO_END);
        return receiverOutput;
    }

    private Gf2kVodeReceiverOutput correctVode() throws MpcAbortException {
        // P0 and P1 send (extend, 1) to F_sVODE,
        // which returns K[s] ∈ K to P0 and (s, M[s]) ∈ F × K to P1 such that M[s] = K[s] + s · Γ.
        int preVodeNum = Gf2kSspVodeFactory.getPrecomputeNum(config, subfieldL, num);
        assert preVodeNum == 1;
        if (gf2kVodeReceiverOutput == null) {
            gf2kVodeReceiverOutput = gf2kCoreVodeReceiver.receive(preVodeNum);
        } else {
            gf2kVodeReceiverOutput.reduce(preVodeNum);
        }
        // P1 samples β ← F^∗, sets M[β] := M[s], and sends d := s − β ∈ F to P0
        List<byte[]> dPayload = receiveOtherPartyPayload(PtoStep.SENDER_SEND_D.ordinal());
        MpcAbortPreconditions.checkArgument(dPayload.size() == 1);
        byte[] d = dPayload.get(0);
        // P0 sets K[β] := K[s] + d · Γ such that M[β] = K[β] + β · Γ.
        byte[] kBeta = gf2kVodeReceiverOutput.getQ(0);
        field.addi(kBeta, field.mixMul(d, delta));
        gf2kVodeReceiverOutput = null;
        return Gf2kVodeReceiverOutput.create(field, delta, new byte[][]{kBeta});
    }

    private void generateNum2GgmTree() {
        ggmTree = new ArrayList<>(h + 1);
        // treat Δ as the root node
        ggmTree.add(new byte[][]{delta});
        kn0 = field.createRandom(secureRandom);
        kn1 = field.createRandom(secureRandom);
        ggmTree.add(new byte[][] {kn0, kn1});
    }

    private void generateGgmTree() {
        kns = new byte[h - 1][];
        ggmTree = new ArrayList<>(h + 1);
        // treat Δ as the root node
        ggmTree.add(new byte[][]{delta});
        // X_1^0 = k, later we let c_1 := K[r1] ⊕ k
        byte[][] level1 = new byte[2][CommonConstants.BLOCK_BYTE_LENGTH];
        secureRandom.nextBytes(level1[0]);
        // X_1^1 = Δ - k
        level1[1] = BytesUtils.xor(delta, level1[0]);
        // the first level should use randomness
        ggmTree.add(level1);
        // For i ∈ {1,...,h - 1}, j ∈ [2^{i − 1}], do X_i^{2j} = H(X_{i - 1}^j), X_i^{2j + 1} = X_{i - 1}^j - X_i^{2j}
        for (int i = 2; i <= h - 1; i++) {
            byte[][] previousLowLevel = ggmTree.get(i - 1);
            byte[][] currentLevel = new byte[1 << i][];
            for (int j = 0; j < (1 << (i - 1)); j++) {
                // X_i^{2j} = H(X_{i - 1}^j)
                currentLevel[2 * j] = hash.hash(previousLowLevel[j]);
                currentLevel[2 * j + 1] = BytesUtils.xor(previousLowLevel[j], currentLevel[2 * j]);
            }
            ggmTree.add(currentLevel);
        }
        // for j ∈ [0, 2^{n − 1}), b ∈ {0, 1} do X_n^{2j+b} := H(X_{n-1}^j ⊕ b)
        byte[][] previousLastLevel = ggmTree.get(h - 1);
        byte[][] lastLevel = new byte[1 << h][];
        byte[] one = new byte[CommonConstants.BLOCK_BYTE_LENGTH];
        Arrays.fill(one, (byte) 0b11111111);
        for (int j = 0; j < (1 << (h - 1)); j++) {
            // X_i^{2j} = H(X_{i - 1}^j)
            lastLevel[2 * j] = hash.hash(previousLastLevel[j]);
            lastLevel[2 * j + 1] = hash.hash(BytesUtils.xor(previousLastLevel[j], one));
        }
        ggmTree.add(lastLevel);
        // For each i ∈ {1,...,h - 1}, do K_i^0 = ⊕_{j ∈ [2^{i - 1}]} X_i^{2j}
        for (int i = 1; i <= h - 1; i++) {
            int hIndex = i - 1;
            byte[][] currentLevel = ggmTree.get(i);
            // K_i^0 = ⊕_{j ∈ [2^{i - 1}]} X_i^{2j}
            kns[hIndex] = new byte[CommonConstants.BLOCK_BYTE_LENGTH];
            for (int j = 0; j < (1 << (i - 1)); j++) {
                BytesUtils.xori(kns[hIndex], currentLevel[2 * j]);
            }
        }
        // K_n^0, K_n^1
        kn0 = new byte[CommonConstants.BLOCK_BYTE_LENGTH];
        kn1 = new byte[CommonConstants.BLOCK_BYTE_LENGTH];
        for (int j = 0; j < (1 << (h - 1)); j++) {
            field.addi(kn0, lastLevel[2 * j]);
            field.addi(kn1, lastLevel[2 * j + 1]);
        }
    }

    private List<byte[]> generateNum2CorrelationPayload(Gf2kVodeReceiverOutput correctGf2kVodeReceiverOutput) {
        byte[][] correlation = new byte[4][];
        byte[] mu = BytesUtils.randomByteArray(CommonConstants.BLOCK_BYTE_LENGTH, secureRandom);
        correlation[0] = mu;
        // c_n^b := H(µ ⊕ K[r_n] ⊕ b · ∆)) + K_n^b for b ∈ {0, 1}
        byte[] cn0 = BytesUtils.xor(mu, cotSenderOutput.getR0(0));
        cn0 = hash.hash(cn0);
        field.addi(cn0, kn0);
        correlation[1] = cn0;
        byte[] cn1 = BytesUtils.xor(mu, cotSenderOutput.getR1(0));
        cn1 = hash.hash(cn1);
        field.addi(cn1, kn1);
        correlation[2] = cn1;
        // ψ := K_n^0 + K_n^1 - K[β]
        byte[] kBeta = correctGf2kVodeReceiverOutput.getQ(0);
        byte[] phi = field.add(kn0, kn1);
        field.subi(phi, kBeta);
        correlation[3] = phi;
        kn0 = null;
        kn1 = null;
        cotSenderOutput = null;
        return Arrays.stream(correlation).collect(Collectors.toList());
    }

    private List<byte[]> generateCorrelationPayload(Gf2kVodeReceiverOutput correctGf2kVodeReceiverOutput) {
        byte[][] correlation = new byte[h + 3][];
        // c_1 := K[r1] ⊕ k, c_i := K[r_i] ⊕ K_i^0 for i ∈ [2, n − 1]
        for (int i = 0; i < h - 1; i++) {
            byte[] ci = BytesUtils.xor(kns[i], cotSenderOutput.getR0(i));
            correlation[i] = ci;
        }
        byte[] mu = BytesUtils.randomByteArray(CommonConstants.BLOCK_BYTE_LENGTH, secureRandom);
        correlation[h - 1] = mu;
        // c_n^b := H(µ ⊕ K[r_n] ⊕ b · ∆)) + K_n^b for b ∈ {0, 1}
        byte[] cn0 = BytesUtils.xor(cotSenderOutput.getR0(h - 1), mu);
        cn0 = hash.hash(cn0);
        field.addi(cn0, kn0);
        correlation[h] = cn0;
        byte[] cn1 = BytesUtils.xor(cotSenderOutput.getR0(h - 1), mu);
        BytesUtils.xori(cn1, delta);
        cn1 = hash.hash(cn1);
        field.addi(cn1, kn1);
        correlation[h + 1] = cn1;
        // ψ := K_n^0 + K_n^1 - K[β]
        byte[] kBeta = correctGf2kVodeReceiverOutput.getQ(0);
        byte[] phi = field.add(kn0, kn1);
        field.subi(phi, kBeta);
        correlation[h + 2] = phi;
        kns = null;
        kn0 = null;
        kn1 = null;
        cotSenderOutput = null;
        return Arrays.stream(correlation).collect(Collectors.toList());
    }
}