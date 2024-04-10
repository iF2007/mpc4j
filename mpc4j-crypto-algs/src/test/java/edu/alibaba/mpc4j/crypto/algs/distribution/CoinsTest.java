package edu.alibaba.mpc4j.crypto.algs.distribution;

import edu.alibaba.mpc4j.common.tool.CommonConstants;
import org.junit.Assert;
import org.junit.Test;

/**
 * Coins unit tests.
 *
 * @author Weiran Liu
 * @date 2024/1/6
 */
public class CoinsTest {
    /**
     * key length
     */
    private static final int KEY_LENGTH = CommonConstants.BLOCK_BYTE_LENGTH;
    /**
     * seed length
     */
    private static final int SEED_LENGTH = CommonConstants.BLOCK_BYTE_LENGTH;
    /**
     * round
     */
    private static final int ROUND = 1 << 20;

    @Test
    public void testIllegalArgument() {
        // zero key length
        Assert.assertThrows(IllegalArgumentException.class, () -> new Coins(new byte[0], new byte[SEED_LENGTH]));
        // short key length
        Assert.assertThrows(IllegalArgumentException.class, () -> new Coins(new byte[KEY_LENGTH - 1], new byte[SEED_LENGTH]));
        // large key length
        Assert.assertThrows(IllegalArgumentException.class, () -> new Coins(new byte[KEY_LENGTH + 1], new byte[SEED_LENGTH]));
        // zero seed length
        Assert.assertThrows(IllegalArgumentException.class, () -> new Coins(new byte[KEY_LENGTH], new byte[0]));
    }

    @Test
    public void testSeed() {
        Coins coins1, coins2;

        // same key, seed with different length generate different coins
        coins1 = new Coins(
            new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00},
            new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}
        );
        coins2 = new Coins(
            new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00},
            new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}
        );
        Assert.assertFalse(sameRandomness(coins1, coins2));

        // same key, different seed generate different coins
        coins1 = new Coins(
            new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00},
            new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}
        );
        coins2 = new Coins(
            new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00},
            new byte[] {0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11}
        );
        Assert.assertFalse(sameRandomness(coins1, coins2));

        // different key, same seed generate different coins
        coins1 = new Coins(
            new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00},
            new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}
        );
        coins2 = new Coins(
            new byte[] {0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11},
            new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}
        );
        Assert.assertFalse(sameRandomness(coins1, coins2));

        // same key, same seed generate same coins
        coins1 = new Coins(
            new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00},
            new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}
        );
        coins2 = new Coins(
            new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00},
            new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}
        );
        Assert.assertTrue(sameRandomness(coins1, coins2));
    }

    private boolean sameRandomness(Coins coins1, Coins coins2) {
        boolean same = true;
        for (int i = 0; i < ROUND; i++) {
            boolean binary1 = coins1.next();
            boolean binary2 = coins2.next();
            if (binary1 != binary2) {
                same = false;
                break;
            }
        }
        return same;
    }
}
