package vip.testops.gungnir.internal.data;

public class GungnirCRC64 {
    private static final long POLY64REV = 0xd800000000000000L;

    private static final long[] LOOKUPTABLE;

    static {
        LOOKUPTABLE = new long[0x100];
        for (int i = 0; i < 0x100; i++) {
            long v = i;
            for (int j = 0; j < 8; j++) {
                if ((v & 1) == 1) {
                    v = (v >>> 1) ^ POLY64REV;
                } else {
                    v = (v >>> 1);
                }
            }
            LOOKUPTABLE[i] = v;
        }
    }

    private static long update(final long sum, final byte b) {
        final int lookupidx = ((int) sum ^ b) & 0xff;
        return (sum >>> 8) ^ LOOKUPTABLE[lookupidx];
    }

    private static long update(long sum, final byte[] bytes,
                               final int fromIndexInclusive, final int toIndexExclusive) {
        for (int i = fromIndexInclusive; i < toIndexExclusive; i++) {
            sum = update(sum, bytes[i]);
        }
        return sum;
    }

    public static long classId(final byte[] bytes) {
        return update(0, bytes, 0, bytes.length);
    }

}
