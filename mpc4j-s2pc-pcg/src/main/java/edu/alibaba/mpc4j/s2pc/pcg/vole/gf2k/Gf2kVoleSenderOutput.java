package edu.alibaba.mpc4j.s2pc.pcg.vole.gf2k;

import com.google.common.base.Preconditions;
import edu.alibaba.mpc4j.common.tool.MathPreconditions;
import edu.alibaba.mpc4j.common.tool.galoisfield.gf2e.Gf2e;
import edu.alibaba.mpc4j.common.tool.galoisfield.sgf2k.Sgf2k;
import edu.alibaba.mpc4j.common.tool.utils.BytesUtils;
import edu.alibaba.mpc4j.s2pc.pcg.MergedPcgPartyOutput;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * GF2K-VOLE sender output. The sender gets (x, t) with t = q + Δ · x, where Δ and q is owned by the receiver.
 *
 * @author Weiran Liu
 * @date 2023/3/16
 */
public class Gf2kVoleSenderOutput implements MergedPcgPartyOutput, Gf2kVolePartyOutput {
    /**
     * field
     */
    private final Sgf2k field;
    /**
     * x array
     */
    private byte[][] x;
    /**
     * t array
     */
    private byte[][] t;

    /**
     * Creates a sender output.
     *
     * @param field field.
     * @param x     x_i.
     * @param t     t_i.
     * @return a sender output.
     */
    public static Gf2kVoleSenderOutput create(Sgf2k field, byte[][] x, byte[][] t) {
        Gf2kVoleSenderOutput senderOutput = new Gf2kVoleSenderOutput(field);
        MathPreconditions.checkEqual("x.length", "t.length", x.length, t.length);
        Gf2e subfield = field.getSubfield();
        senderOutput.x = Arrays.stream(x)
            .peek(xi -> Preconditions.checkArgument(subfield.validateElement(xi)))
            .toArray(byte[][]::new);
        senderOutput.t = Arrays.stream(t)
            .peek(ti -> Preconditions.checkArgument(field.validateElement(ti)))
            .toArray(byte[][]::new);

        return senderOutput;
    }

    /**
     * Creates an empty sender output.
     *
     * @param field field.
     * @return an empty sender output.
     */
    public static Gf2kVoleSenderOutput createEmpty(Sgf2k field) {
        Gf2kVoleSenderOutput senderOutput = new Gf2kVoleSenderOutput(field);
        senderOutput.x = new byte[0][];
        senderOutput.t = new byte[0][];

        return senderOutput;
    }

    /**
     * Creates a random sender output.
     *
     * @param receiverOutput receiver output.
     * @param secureRandom   random state.
     * @return a random sender output.
     */
    public static Gf2kVoleSenderOutput createRandom(Gf2kVoleReceiverOutput receiverOutput, SecureRandom secureRandom) {
        int num = receiverOutput.getNum();
        Sgf2k field = receiverOutput.getField();
        Gf2e subfield = receiverOutput.getSubfield();
        Gf2kVoleSenderOutput senderOutput = new Gf2kVoleSenderOutput(field);
        senderOutput.x = IntStream.range(0, num)
            .mapToObj(i -> subfield.createNonZeroRandom(secureRandom))
            .toArray(byte[][]::new);
        byte[] delta = receiverOutput.getDelta();
        senderOutput.t = IntStream.range(0, num)
            .mapToObj(i -> {
                byte[] ti = field.mixMul(senderOutput.x[i], delta);
                field.addi(ti, receiverOutput.getQ(i));
                return ti;
            })
            .toArray(byte[][]::new);
        return senderOutput;
    }

    /**
     * private constructor.
     *
     * @param field field.
     */
    private Gf2kVoleSenderOutput(Sgf2k field) {
        this.field = field;
    }

    @Override
    public int getNum() {
        return x.length;
    }

    @Override
    public Gf2kVoleSenderOutput copy() {
        Gf2kVoleSenderOutput copy = new Gf2kVoleSenderOutput(field);
        copy.x = BytesUtils.clone(x);
        copy.t = BytesUtils.clone(t);
        return copy;
    }

    @Override
    public Gf2kVoleSenderOutput split(int splitNum) {
        int num = getNum();
        MathPreconditions.checkPositiveInRangeClosed("split_num", splitNum, num);
        // split x
        byte[][] subX = new byte[splitNum][];
        byte[][] remainX = new byte[num - splitNum][];
        System.arraycopy(x, num - splitNum, subX, 0, splitNum);
        System.arraycopy(x, 0, remainX, 0, num - splitNum);
        x = remainX;
        // split t
        byte[][] subT = new byte[splitNum][];
        byte[][] remainT = new byte[num - splitNum][];
        System.arraycopy(t, num - splitNum, subT, 0, splitNum);
        System.arraycopy(t, 0, remainT, 0, num - splitNum);
        t = remainT;

        return create(field, subX, subT);
    }

    @Override
    public void reduce(int reduceNum) {
        int num = getNum();
        MathPreconditions.checkPositiveInRangeClosed("reduce_num", reduceNum, num);
        if (reduceNum < num) {
            // if the reduced num is less than num, do split. If not, keep the current state.
            byte[][] remainX = new byte[reduceNum][];
            System.arraycopy(x, 0, remainX, 0, reduceNum);
            x = remainX;
            byte[][] remainT = new byte[reduceNum][];
            System.arraycopy(t, 0, remainT, 0, reduceNum);
            t = remainT;
        }
    }

    @Override
    public void merge(MergedPcgPartyOutput other) {
        Gf2kVoleSenderOutput that = (Gf2kVoleSenderOutput) other;
        Preconditions.checkArgument(this.field.equals(that.field));
        // merge x
        byte[][] mergeX = new byte[this.x.length + that.x.length][];
        System.arraycopy(this.x, 0, mergeX, 0, this.x.length);
        System.arraycopy(that.x, 0, mergeX, this.x.length, that.x.length);
        x = mergeX;
        // merge t
        byte[][] mergeT = new byte[this.t.length + that.t.length][];
        System.arraycopy(this.t, 0, mergeT, 0, this.t.length);
        System.arraycopy(that.t, 0, mergeT, this.t.length, that.t.length);
        t = mergeT;
    }

    @Override
    public Sgf2k getField() {
        return field;
    }

    /**
     * Gets x_i.
     *
     * @param index the index.
     * @return x_i.
     */
    public byte[] getX(int index) {
        return x[index];
    }

    /**
     * Gets x.
     *
     * @return x.
     */
    public byte[][] getX() {
        return x;
    }

    /**
     * Gets t_i.
     *
     * @param index the index.
     * @return t_i.
     */
    public byte[] getT(int index) {
        return t[index];
    }

    /**
     * Gets t.
     *
     * @return t.
     */
    public byte[][] getT() {
        return t;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(field)
            .append(x)
            .append(t)
            .hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Gf2kVoleSenderOutput that) {
            return new EqualsBuilder()
                .append(this.field, that.field)
                .append(this.x, that.x)
                .append(this.t, that.t)
                .isEquals();
        }
        return false;
    }
}
