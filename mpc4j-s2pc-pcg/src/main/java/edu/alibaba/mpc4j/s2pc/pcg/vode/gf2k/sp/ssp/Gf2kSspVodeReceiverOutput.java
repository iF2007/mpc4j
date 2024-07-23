package edu.alibaba.mpc4j.s2pc.pcg.vode.gf2k.sp.ssp;

import com.google.common.base.Preconditions;
import edu.alibaba.mpc4j.common.tool.MathPreconditions;
import edu.alibaba.mpc4j.common.tool.galoisfield.sgf2k.Dgf2k;
import edu.alibaba.mpc4j.s2pc.pcg.PcgPartyOutput;
import edu.alibaba.mpc4j.s2pc.pcg.vode.gf2k.Gf2kVodePartyOutput;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * GF2K-SSP-VODE receiver output.
 * <p>
 * The receiver gets (Δ, q) with t = q + Δ · x, where x and t are owned by the sender, and there are only one non-zero x.
 * Here Δ · x is done by directly treating the subfield element x as a field element.
 * </p>
 *
 * @author Weiran Liu
 * @date 2024/6/12
 */
public class Gf2kSspVodeReceiverOutput implements PcgPartyOutput, Gf2kVodePartyOutput {
    /**
     * field
     */
    private final Dgf2k field;
    /**
     * Δ
     */
    private byte[] delta;
    /**
     * q array.
     */
    private byte[][] q;

    /**
     * Creates a sender output.
     *
     * @param field field.
     * @param delta Δ.
     * @param q     q_i.
     * @return a sender output.
     */
    public static Gf2kSspVodeReceiverOutput create(Dgf2k field, byte[] delta, byte[][] q) {
        Gf2kSspVodeReceiverOutput receiverOutput = new Gf2kSspVodeReceiverOutput(field);
        Preconditions.checkArgument(field.validateElement(delta));
        receiverOutput.delta = delta;
        MathPreconditions.checkPositive("qArray.length", q.length);
        receiverOutput.q = Arrays.stream(q)
            .peek(qi -> {
                assert field.validateElement(qi);
            })
            .toArray(byte[][]::new);
        return receiverOutput;
    }

    /**
     * Creates a random receiver output.
     *
     * @param field        field.
     * @param num          num.
     * @param secureRandom random state.
     * @return a random receiver output.
     */
    public static Gf2kSspVodeReceiverOutput create(Dgf2k field, int num, byte[] delta, SecureRandom secureRandom) {
        Preconditions.checkArgument(field.validateElement(delta));
        MathPreconditions.checkPositive("num", num);
        byte[][] q = IntStream.range(0, num)
            .mapToObj(index -> field.createRandom(secureRandom))
            .toArray(byte[][]::new);
        return create(field, delta, q);
    }

    /**
     * private constructor.
     *
     * @param field field.
     */
    private Gf2kSspVodeReceiverOutput(Dgf2k field) {
        this.field = field;
    }

    @Override
    public Dgf2k getField() {
        return field;
    }

    /**
     * Gets Δ.
     *
     * @return Δ.
     */
    public byte[] getDelta() {
        return delta;
    }

    /**
     * Gets the assigned q.
     *
     * @param index index.
     * @return the assigned q.
     */
    public byte[] getQ(int index) {
        return q[index];
    }

    @Override
    public int getNum() {
        return q.length;
    }
}