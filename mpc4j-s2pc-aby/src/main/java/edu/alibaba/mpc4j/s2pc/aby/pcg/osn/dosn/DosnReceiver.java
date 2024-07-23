package edu.alibaba.mpc4j.s2pc.aby.pcg.osn.dosn;

import edu.alibaba.mpc4j.common.rpc.MpcAbortException;
import edu.alibaba.mpc4j.common.rpc.pto.TwoPartyPto;

/**
 * Decisin OSN receiver.
 *
 * @author Weiran Liu
 * @date 2022/02/09
 */
public interface DosnReceiver extends TwoPartyPto {
    /**
     * Inits the protocol.
     *
     * @throws MpcAbortException the protocol failure aborts.
     */
    void init() throws MpcAbortException;

    /**
     * Executes the protocol.
     *
     * @param pi         permutation π.
     * @param byteLength element byte length.
     * @return receiver output.
     * @throws MpcAbortException the protocol failure aborts.
     */
    DosnPartyOutput dosn(int[] pi, int byteLength) throws MpcAbortException;
}
