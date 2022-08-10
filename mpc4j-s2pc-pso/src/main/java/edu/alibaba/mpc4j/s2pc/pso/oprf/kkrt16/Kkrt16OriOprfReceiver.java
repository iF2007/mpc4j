package edu.alibaba.mpc4j.s2pc.pso.oprf.kkrt16;

import edu.alibaba.mpc4j.common.rpc.MpcAbortException;
import edu.alibaba.mpc4j.common.rpc.Party;
import edu.alibaba.mpc4j.common.rpc.Rpc;
import edu.alibaba.mpc4j.common.rpc.utils.DataPacket;
import edu.alibaba.mpc4j.common.rpc.utils.DataPacketHeader;
import edu.alibaba.mpc4j.common.tool.CommonConstants;
import edu.alibaba.mpc4j.common.tool.bitmatrix.trans.TransBitMatrix;
import edu.alibaba.mpc4j.common.tool.bitmatrix.trans.TransBitMatrixFactory;
import edu.alibaba.mpc4j.common.tool.coder.random.RandomCoder;
import edu.alibaba.mpc4j.common.tool.coder.random.RandomCoderUtils;
import edu.alibaba.mpc4j.common.tool.crypto.crhf.Crhf;
import edu.alibaba.mpc4j.common.tool.crypto.crhf.CrhfFactory;
import edu.alibaba.mpc4j.common.tool.crypto.crhf.CrhfFactory.CrhfType;
import edu.alibaba.mpc4j.common.tool.crypto.prg.Prg;
import edu.alibaba.mpc4j.common.tool.crypto.prg.PrgFactory;
import edu.alibaba.mpc4j.common.tool.utils.BytesUtils;
import edu.alibaba.mpc4j.common.tool.utils.CommonUtils;
import edu.alibaba.mpc4j.s2pc.pcg.ot.cot.CotSenderOutput;
import edu.alibaba.mpc4j.s2pc.pcg.ot.cot.core.CoreCotFactory;
import edu.alibaba.mpc4j.s2pc.pcg.ot.cot.core.CoreCotSender;
import edu.alibaba.mpc4j.s2pc.pso.oprf.AbstractOprfReceiver;
import edu.alibaba.mpc4j.s2pc.pso.oprf.OprfReceiverOutput;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * KKRT16-ORI-OPRF协议接收方。
 *
 * @author Weiran Liu
 * @date 2022/02/05
 */
public class Kkrt16OriOprfReceiver extends AbstractOprfReceiver {
    /**
     * 核COT协议发送方
     */
    private final CoreCotSender coreCotSender;
    /**
     * 抗关联哈希函数
     */
    private final Crhf crhf;
    /**
     * 码字字节长度
     */
    private int codewordByteLength;
    /**
     * 码字比特长度
     */
    private int codewordBitLength;
    /**
     * T0的密钥
     */
    private byte[][] t0MatrixKey;
    /**
     * T1的密钥
     */
    private byte[][] t1MatrixKey;
    /**
     * 伪随机编码
     */
    private byte[] randomCoderKey;
    /**
     * 布尔矩阵
     */
    private TransBitMatrix tMatrix;

    public Kkrt16OriOprfReceiver(Rpc receiverRpc, Party senderParty, Kkrt16OriOprfConfig config) {
        super(Kkrt16OriOprfPtoDesc.getInstance(), receiverRpc, senderParty, config);
        coreCotSender = CoreCotFactory.createSender(receiverRpc, senderParty, config.getCoreCotConfig());
        coreCotSender.addLogLevel();
        crhf = CrhfFactory.createInstance(envType, CrhfType.MMO);
    }

    @Override
    public void setTaskId(long taskId) {
        super.setTaskId(taskId);
        coreCotSender.setTaskId(taskId);
    }

    @Override
    public void setParallel(boolean parallel) {
        super.setParallel(parallel);
        coreCotSender.setParallel(parallel);
    }

    @Override
    public void addLogLevel() {
        super.addLogLevel();
        coreCotSender.addLogLevel();
    }

    @Override
    public void init(int maxBatchSize) throws MpcAbortException {
        setBatchInitInput(maxBatchSize);
        info("{}{} Recv. Init begin", ptoBeginLogPrefix, getPtoDesc().getPtoName());

        stopWatch.start();
        // 设置伪随机编码码字比特长度
        codewordByteLength = RandomCoderUtils.getCodewordByteLength(maxBatchSize);
        codewordBitLength = codewordByteLength * Byte.SIZE;
        byte[] cotDelta = new byte[CommonConstants.BLOCK_BYTE_LENGTH];
        secureRandom.nextBytes(cotDelta);
        // 初始化COT协议
        coreCotSender.init(cotDelta, codewordBitLength);
        stopWatch.stop();
        long initCotTime = stopWatch.getTime(TimeUnit.MILLISECONDS);
        stopWatch.reset();
        info("{}{} Recv. Init Step 1/2 ({}ms)", ptoStepLogPrefix, getPtoDesc().getPtoName(), initCotTime);

        stopWatch.start();
        // 执行COT协议
        CotSenderOutput cotSenderOutput = coreCotSender.send(codewordBitLength);
        // 将COT转换为密钥
        t0MatrixKey = new byte[codewordBitLength][];
        t1MatrixKey = new byte[codewordBitLength][];
        IntStream t0MatrixKeyIntStream = IntStream.range(0, codewordBitLength);
        t0MatrixKeyIntStream = parallel ? t0MatrixKeyIntStream.parallel() : t0MatrixKeyIntStream;
        t0MatrixKeyIntStream.forEach(index -> {
            t0MatrixKey[index] = cotSenderOutput.getR0(index);
            t0MatrixKey[index] = crhf.hash(t0MatrixKey[index]);
            t1MatrixKey[index] = cotSenderOutput.getR1(index);
            t1MatrixKey[index] = crhf.hash(t1MatrixKey[index]);
        });
        stopWatch.stop();
        long cotTime = stopWatch.getTime(TimeUnit.MILLISECONDS);
        stopWatch.reset();
        info("{}{} Recv. Init Step 2/2 ({}ms)", ptoStepLogPrefix, getPtoDesc().getPtoName(), cotTime);

        initialized = true;
        info("{}{} Recv. Init end", ptoEndLogPrefix, getPtoDesc().getPtoName());
    }

    @Override
    public OprfReceiverOutput oprf(byte[][] inputs) throws MpcAbortException {
        setBatchPtoInput(inputs);
        info("{}{} Recv. begin", ptoBeginLogPrefix, getPtoDesc().getPtoName());

        stopWatch.start();
        // 生成伪随机编码密钥
        randomCoderKey = new byte[CommonConstants.BLOCK_BYTE_LENGTH];
        secureRandom.nextBytes(randomCoderKey);
        List<byte[]> keyPayload = new LinkedList<>();
        keyPayload.add(randomCoderKey);
        // 发送伪随机编码密钥
        DataPacketHeader keyHeader = new DataPacketHeader(
            taskId, getPtoDesc().getPtoId(), Kkrt16OriOprfPtoDesc.PtoStep.RECEIVER_SEND_KEY.ordinal(), extraInfo,
            ownParty().getPartyId(), otherParty().getPartyId()
        );
        rpc.send(DataPacket.fromByteArrayList(keyHeader, keyPayload));
        stopWatch.stop();
        long initKeyTime = stopWatch.getTime(TimeUnit.MILLISECONDS);
        stopWatch.reset();
        info("{}{} Recv. Step 1/3 ({}ms)", ptoStepLogPrefix, getPtoDesc().getPtoName(), initKeyTime);

        stopWatch.start();
        // 生成矩阵
        List<byte[]> matrixPayload = generateMatrixPayload();
        DataPacketHeader matrixHeader = new DataPacketHeader(
            taskId, getPtoDesc().getPtoId(), Kkrt16OriOprfPtoDesc.PtoStep.RECEIVER_SEND_MATRIX.ordinal(), extraInfo,
            ownParty().getPartyId(), otherParty().getPartyId()
        );
        rpc.send(DataPacket.fromByteArrayList(matrixHeader, matrixPayload));
        stopWatch.stop();
        long matrixTime = stopWatch.getTime(TimeUnit.MILLISECONDS);
        stopWatch.reset();
        info("{}{} Recv. Step 2/3 ({}ms)", ptoStepLogPrefix, getPtoDesc().getPtoName(), matrixTime);

        stopWatch.start();
        OprfReceiverOutput receiverOutput = generateReceiverOutput();
        stopWatch.stop();
        long keyGenTime = stopWatch.getTime(TimeUnit.MILLISECONDS);
        stopWatch.reset();
        info("{}{} Recv. Step 3/3 ({}ms)", ptoStepLogPrefix, getPtoDesc().getPtoName(), keyGenTime);

        info("{}{} Recv. end", ptoEndLogPrefix, getPtoDesc().getPtoName());
        return receiverOutput;
    }

    private List<byte[]> generateMatrixPayload() {
        // 初始化伪随机数生成器
        Prg prg = PrgFactory.createInstance(envType, CommonUtils.getByteLength(batchSize));
        // 设置随机编码矩阵
        RandomCoder randomCoder = new RandomCoder(envType, codewordByteLength);
        randomCoder.setKey(randomCoderKey);
        TransBitMatrix prcMatrix = TransBitMatrixFactory.createInstance(envType, codewordBitLength, batchSize, parallel);
        IntStream choicesIntStream = IntStream.range(0, batchSize);
        choicesIntStream = parallel ? choicesIntStream.parallel() : choicesIntStream;
        choicesIntStream.forEach(index -> {
            byte[] encode = randomCoder.encode(inputs[index]);
            prcMatrix.setColumn(index, encode);
        });
        randomCoderKey = null;
        // 转置随机编码矩阵
        TransBitMatrix prcTransposeMatrix = prcMatrix.transpose();
        // 创建矩阵T0
        tMatrix = TransBitMatrixFactory.createInstance(envType, batchSize, codewordBitLength, parallel);
        // 矩阵列加密流
        IntStream columnIndexIntStream = IntStream.range(0, codewordBitLength);
        columnIndexIntStream = parallel ? columnIndexIntStream.parallel() : columnIndexIntStream;
        return columnIndexIntStream.mapToObj(columnIndex -> {
            // Receiver forms m \times k matrices T_0, T_1 such that t_{j, 0} \oplus t_{j, 1} = (r_j || \cdots || r_j)
            byte[] columnSeed = new byte[CommonConstants.BLOCK_BYTE_LENGTH];
            secureRandom.nextBytes(columnSeed);
            byte[] column0Bytes = prg.extendToBytes(columnSeed);
            BytesUtils.reduceByteArray(column0Bytes, batchSize);
            tMatrix.setColumn(columnIndex, column0Bytes);
            byte[] column1Bytes = prcTransposeMatrix.getColumn(columnIndex);
            BytesUtils.xori(column1Bytes, column0Bytes);
            // Sender and receiver interact with OT^k_m: the receiver acts as OT sender with input t_0, t_1
            byte[] message0 = prg.extendToBytes(t0MatrixKey[columnIndex]);
            BytesUtils.reduceByteArray(message0, batchSize);
            BytesUtils.xori(message0, column0Bytes);
            byte[] message1 = prg.extendToBytes(t1MatrixKey[columnIndex]);
            BytesUtils.reduceByteArray(message1, batchSize);
            BytesUtils.xori(message1, column1Bytes);

            return new byte[][] {message0, message1};
        }).flatMap(Arrays::stream).collect(Collectors.toList());
    }

    private OprfReceiverOutput generateReceiverOutput() {
        // 生成密钥数组，将矩阵T转置，按行获取
        TransBitMatrix tMatrixTranspose = tMatrix.transpose();
        tMatrix = null;
        byte[][] ts = IntStream.range(0, batchSize)
            .mapToObj(tMatrixTranspose::getColumn)
            .toArray(byte[][]::new);
        // 接收方输出只需要读取randomCoder的长度信息，因此可以直接传入
        return new OprfReceiverOutput(codewordByteLength, inputs, ts);
    }
}
