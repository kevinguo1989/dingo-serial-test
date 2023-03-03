package io.dingodb.serial.v2.t1.schema;

import io.dingodb.serial.v1.schema.Type;
import io.dingodb.serial.v2.t1.Buf;

public class LongSchema implements DingoSchema<Long> {

    private int index;
    private boolean isKey;
    private boolean allowNull = true;

    @Override
    public Type getType() {
        return Type.LONG;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIsKey(boolean isKey) {
        this.isKey = isKey;
    }

    @Override
    public boolean isKey() {
        return isKey;
    }

    @Override
    public int getLength() {
        if (allowNull) {
            return getWithNullTagLength();
        }
        return getDataLength();
    }

    private int getWithNullTagLength() {
        return 9;
    }

    private int getDataLength() {
        return 8;
    }

    @Override
    public void setAllowNull(boolean allowNull) {
        this.allowNull = allowNull;
    }

    @Override
    public boolean isAllowNull() {
        return allowNull;
    }

    @Override
    public void encodeKey(Buf buf, Long data) {
        if (allowNull) {
            buf.ensureRemainder(getWithNullTagLength());
            if (data == null) {
                buf.write(NULL);
                internalEncodeNull(buf);
            } else {
                buf.write(NOTNULL);
                internalEncodeKey(buf, data);
            }
        } else {
            buf.ensureRemainder(getDataLength());
            internalEncodeKey(buf, data);
        }
    }

    @Override
    public void encodeKeyForUpdate(Buf buf, Long data) {
        if (allowNull) {
            if (data == null) {
                buf.write(NULL);
                internalEncodeNull(buf);
            } else {
                buf.write(NOTNULL);
                internalEncodeKey(buf, data);
            }
        } else {
            internalEncodeKey(buf, data);
        }
    }

    private void internalEncodeNull(Buf buf) {
        buf.write((byte) 0);
        buf.write((byte) 0);
        buf.write((byte) 0);
        buf.write((byte) 0);
        buf.write((byte) 0);
        buf.write((byte) 0);
        buf.write((byte) 0);
        buf.write((byte) 0);
    }

    private void internalEncodeKey(Buf buf, Long data) {
        buf.write((byte) (data >>> 56 ^ 0x80));
        buf.write((byte) (data >>> 48));
        buf.write((byte) (data >>> 40));
        buf.write((byte) (data >>> 32));
        buf.write((byte) (data >>> 24));
        buf.write((byte) (data >>> 16));
        buf.write((byte) (data >>> 8));
        buf.write((byte) data.longValue());
    }

    @Override
    public Long decodeKey(Buf buf) {
        if (allowNull) {
            if (buf.read() == NULL) {
                buf.skip(getDataLength());
                return null;
            }
        }
        long l = 0;
        l |= buf.read() & 0xFF ^ 0x80;
        for (int i = 0; i < 7; i++) {
            l <<= 8;
            l |= buf.read() & 0xFF;
        }
        return l;
    }

    @Override
    public void skipKey(Buf buf) {
        buf.skip(getLength());
    }

    @Override
    public void encodeKeyPrefix(Buf buf, Long data) {
        encodeKey(buf, data);
    }

    @Override
    public void encodeValue(Buf buf, Long data) {
        if (allowNull) {
            buf.ensureRemainder(getWithNullTagLength());
            if (data == null) {
                buf.write(NULL);
                internalEncodeNull(buf);
            } else {
                buf.write(NOTNULL);
                internalEncodeValue(buf, data);
            }
        } else {
            buf.ensureRemainder(getDataLength());
            internalEncodeValue(buf, data);
        }
    }

    private void internalEncodeValue(Buf buf, Long data) {
        buf.write((byte) (data >>> 56));
        buf.write((byte) (data >>> 48));
        buf.write((byte) (data >>> 40));
        buf.write((byte) (data >>> 32));
        buf.write((byte) (data >>> 24));
        buf.write((byte) (data >>> 16));
        buf.write((byte) (data >>> 8));
        buf.write((byte) data.longValue());
    }

    @Override
    public Long decodeValue(Buf buf) {
        if (allowNull) {
            if (buf.read() == NULL) {
                buf.skip(getDataLength());
                return null;
            }
        }
        long l = 0;
        for (int i = 0; i < 8; i++) {
            l <<= 8;
            l |= buf.read() & 0xFF;
        }
        return l;
    }

    @Override
    public void skipValue(Buf buf) {
        buf.skip(getLength());
    }
}
