package interview.app;

interface CalculationUtil {

    static long sumUnsigned(long unsignedLeft, long unsignedRight) throws NumberOverflowException {
        if (unsignedLeft < 0 && unsignedLeft < 0) {//overflow for sure
            throw new NumberOverflowException("Maximum value 2^64 reached.");
        }

        long result = unsignedLeft + unsignedRight;//if both negative the every part is bigger then half then this is overflow

        if (((unsignedLeft < 0 && unsignedRight > 0) || (unsignedLeft > 0 && unsignedRight < 0)) && result >= 0) {
            throw new NumberOverflowException("Maximum value 2^64 reached.");
        }

        return result;
    }

    static long sumUnsignedLongWithLittleEndianInt(long unsignedLeft, byte b0, byte b1, byte b2, byte b3) throws NumberOverflowException {
        return sumUnsigned(unsignedLeft, unsignedLittleEndianIntToLong(b0, b1, b2, b3));
    }

    static long unsignedLittleEndianIntToLong(byte b0, byte b1, byte b2, byte b3) {
        return Integer.toUnsignedLong(littleEndianToInt(b0, b1, b2, b3));
    }

    static int littleEndianToInt(byte b0, byte b1, byte b2, byte b3) {
        return (((b3) << 24) |
                ((b2 & 0xff) << 16) |
                ((b1 & 0xff) << 8) |
                ((b0 & 0xff)));
    }

}