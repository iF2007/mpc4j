package edu.alibaba.mpc4j.s2pc.pir.stdpir.kw;

import com.google.common.base.Preconditions;
import edu.alibaba.mpc4j.common.rpc.Party;
import edu.alibaba.mpc4j.common.rpc.Rpc;
import edu.alibaba.mpc4j.common.rpc.desc.PtoDesc;
import edu.alibaba.mpc4j.common.rpc.pto.AbstractTwoPartyPto;
import edu.alibaba.mpc4j.common.tool.MathPreconditions;
import edu.alibaba.mpc4j.common.tool.utils.BytesUtils;
import edu.alibaba.mpc4j.common.tool.utils.CommonUtils;
import edu.alibaba.mpc4j.common.tool.utils.ObjectUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * abstract standard keyword PIR client.
 *
 * @author Liqiang Peng
 * @date 2024/7/19
 */
public abstract class AbstractStdKwPirClient<T> extends AbstractTwoPartyPto implements StdKwPirClient<T> {
    /**
     * database size
     */
    protected int n;
    /**
     * value bit length
     */
    protected int l;
    /**
     * value byte length
     */
    protected int byteL;
    /**
     * ByteBuffer for ⊥
     */
    protected ByteBuffer botByteBuffer;
    /**
     * max batch num
     */
    private int maxBatchNum;
    /**
     * batch num
     */
    protected int batchNum;

    protected AbstractStdKwPirClient(PtoDesc ptoDesc, Rpc clientRpc, Party serverParty, StdKwPirConfig config) {
        super(ptoDesc, clientRpc, serverParty, config);
    }

    protected void setInitInput(int n, int l, int maxBatchNum) {
        MathPreconditions.checkPositive("n", n);
        this.n = n;
        MathPreconditions.checkPositive("l", l);
        this.l = l;
        byteL = CommonUtils.getByteLength(l);
        byte[] bot = new byte[byteL];
        Arrays.fill(bot, (byte) 0xFF);
        BytesUtils.reduceByteArray(bot, l);
        botByteBuffer = ByteBuffer.wrap(bot);
        MathPreconditions.checkPositive("max_batch_num", maxBatchNum);
        this.maxBatchNum = maxBatchNum;
        initState();
    }

    protected void setPtoInput(ArrayList<T> keys) {
        checkInitialized();
        MathPreconditions.checkPositiveInRangeClosed("batch_num", keys.size(), maxBatchNum);
        this.batchNum = keys.size();
        for (T x : keys) {
            ByteBuffer keywordByteBuffer = ByteBuffer.wrap(ObjectUtils.objectToByteArray(x));
            Preconditions.checkArgument(!keywordByteBuffer.equals(botByteBuffer), "x must not equal ⊥");
        }
    }
}