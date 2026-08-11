package io.protostuff.core;

import static io.protostuff.api.WireFormat.TAG_TYPE_BITS;
import static io.protostuff.api.WireFormat.WIRETYPE_END_GROUP;
import static io.protostuff.api.WireFormat.WIRETYPE_FIXED32;
import static io.protostuff.api.WireFormat.WIRETYPE_FIXED64;
import static io.protostuff.api.WireFormat.WIRETYPE_LENGTH_DELIMITED;
import static io.protostuff.api.WireFormat.WIRETYPE_START_GROUP;
import static io.protostuff.api.WireFormat.WIRETYPE_VARINT;
import static io.protostuff.api.WireFormat.getTagWireType;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import io.protostuff.api.Input;
import io.protostuff.api.Schema;
import io.protostuff.api.SchemaEnum;
import io.protostuff.api.Utf8Decoder;

/**
 * Base input for protobuf
 */
public abstract class ProtobufAbstractInput implements Input {
	private static final VarHandle INT = MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.LITTLE_ENDIAN);
	private static final VarHandle LONG = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.LITTLE_ENDIAN);

	private static final String EMPTY_STR = "";
	private static final int DEFAULT_SIZE_LIMIT = 64 << 20; // 64MB
	/** default buffer size */
	protected static final int DEFAULT_BUFFER_SIZE = 4096;

	private final Utf8Decoder dec = new Utf8Decoder();
	private final byte[] buffer;
	private int bufferSize;
	private int bufferSizeAfterLimit;
	private int bufferPos;
	private int lastTag;
	private boolean wasLen;
	private int packedLimit = 0;

	/**
	 * The total number of bytes read before the current buffer. The total bytes read up to the current position can be
	 * computed as {@code totalBytesRetired + bufferPos}. This value may be negative if reading started in the middle of
	 * the current buffer (e.g. if the constructor that takes a byte array and an offset was used).
	 */
	private int totalBytesRetired;
	/**
	 * The absolute position of the end of the current message.
	 */
	private int currentLimit = Integer.MAX_VALUE;
	/**
	 * See setSizeLimit()
	 */
	private int sizeLimit = DEFAULT_SIZE_LIMIT;

	/**
	 * new ProtobufAbstractInput
	 */
	protected ProtobufAbstractInput() {
		this(new byte[DEFAULT_BUFFER_SIZE], 0, 0);
	}

	/**
	 * new ProtobufAbstractInput
	 * @param buffer data to use
	 */
	protected ProtobufAbstractInput(byte[] buffer) {
		this(buffer, 0, buffer.length);
	}

	/**
	 * new ProtobufAbstractInput
	 * @param buffer buffer to use
	 * @param off start offset of data
	 * @param len length of data
	 */
	protected ProtobufAbstractInput(byte[] buffer, final int off, final int len) {
		if (off < 0 || off + len > buffer.length)
			throw new IllegalArgumentException();
		this.buffer = buffer;
		bufferSize = len;
		bufferPos = off;
	}

	/**
	 * Resets the current size counter to zero (see {@link #setSizeLimit(int)}). The field {@code totalBytesRetired}
	 * will be negative if the initial position was not zero.
	 */
	public void resetSizeCounter() {
		totalBytesRetired = -bufferPos;
	}

	/**
	 * Set the maximum message size. In order to prevent malicious messages from exhausting memory or causing integer
	 * overflows, {@code ProtobufAbstractInput} limits how large a message may be. The default limit is 64MB. You should set this
	 * limit as small as you can without harming your app's functionality. Note that size limits only apply when reading
	 * from an {@code InputStream}, not when constructed around a raw byte array.
	 * <p>
	 * If you want to read several messages from a single CodedInput, you could call {@link #resetSizeCounter()} after
	 * each one to avoid hitting the size limit.
	 * @param limit the new limit
	 * @return the old limit.
	 */
	public int setSizeLimit(final int limit) {
		if (limit < 0)
			throw new IllegalArgumentException("Size limit cannot be negative: " + limit);

		final int oldLimit = sizeLimit;
		sizeLimit = limit;
		return oldLimit;
	}

	/**
	 * Note that {@code pushLimit()} does NOT affect how many bytes the {@code CodedInputStream} reads from an
	 * underlying source when refreshing its buffer. If you need to prevent reading past a certain point in
	 * the underlying {@code InputStream} (e.g. because you expect it to contain more data after the end of the message
	 * which you need to handle differently) then you must place a wrapper around your {@code InputStream} which limits
	 * the amount of data that can be read from it.
	 *
	 * @return the old limit.
	 */
	public int pushLimit(int byteLimit) throws ProtobufException {
		if (byteLimit < 0) {
			throw ProtobufException.negativeSize();
		}
		byteLimit += totalBytesRetired + bufferPos;
		final int oldLimit = currentLimit;
		if (byteLimit > oldLimit) {
			throw ProtobufException.truncatedMessage();
		}
		currentLimit = byteLimit;

		recomputeBufferSizeAfterLimit();

		return oldLimit;
	}

	private void recomputeBufferSizeAfterLimit() {
		bufferSize += bufferSizeAfterLimit;
		final int bufferEnd = totalBytesRetired + bufferSize;
		if (bufferEnd > currentLimit) {
			// Limit is in current buffer.
			bufferSizeAfterLimit = bufferEnd - currentLimit;
			bufferSize -= bufferSizeAfterLimit;
		} else {
			bufferSizeAfterLimit = 0;
		}
	}

	/**
	 * Discards the current limit, returning to the previous limit.
	 *
	 * @param oldLimit
	 *            The old limit, as returned by {@code pushLimit}.
	 */
	public void popLimit(final int oldLimit) {
		currentLimit = oldLimit;
		recomputeBufferSizeAfterLimit();
	}

	/**
	* Returns true if the stream has reached the end of the input. This is the case if either the end of the underlying
	* input source has been reached or if the stream has reached a limit created using {@link #pushLimit(int)}.
	*/
	public boolean isAtEnd() throws IOException {
		if (bufferPos < bufferSize)
			return false;
		return !refillBuffer(false);
	}

	/**
	 * The total bytes read up to the current position. Calling {@link #resetSizeCounter()} resets this value to zero.
	 */
	public int getTotalBytesRead() {
		return totalBytesRetired + bufferPos;
	}

	/**
	* Called with {@code this.buffer} is empty to read more bytes from the input. If {@code mustSucceed} is true,
	* refillBuffer() guarantees that either there will be at least one byte in the buffer when it returns or it will
	* throw an exception. If {@code mustSucceed} is false, refillBuffer() returns false if no more bytes were
	* available.
	*/
	private boolean refillBuffer(final boolean mustSucceed) throws IOException {
		return refillBuffer(bufferPos, mustSucceed) != bufferPos;
	}

	/**
	 * @return true if fillBuffer will return -1, if we can't know return false
	 * @throws IOException in case of error
	 */
	protected abstract boolean isEof() throws IOException;

	/**
	 * fill the buffer with as much data as possible
	 * @param buffer buffer to fill
	 * @param off start index
	 * @param len number of byte to add
	 * @return number of byte written
	 * @throws IOException in case of error
	 */
	protected abstract int fillBuffer(byte[] buffer, int off, int len) throws IOException;

	private int refillBuffer(int off, final boolean mustSucceed) throws IOException {
		if (totalBytesRetired + bufferSize == currentLimit || isEof()) {
			// Oops, we hit a limit.
			if (mustSucceed) {
				throw ProtobufException.truncatedMessage();
			}
			return bufferSize;
		}

		int avail = bufferSize - off;
		if (avail > 0)
			System.arraycopy(buffer, off, buffer, 0, avail);

		totalBytesRetired += off;

		bufferPos = 0;
		bufferSize = fillBuffer(buffer, avail, buffer.length - avail);
		if (bufferSize == 0 || bufferSize < -1) {
			throw new IllegalStateException("InputStream#read(byte[]) returned invalid result: " + bufferSize + "\nThe InputStream implementation is buggy.");
		}
		if (bufferSize == -1) {
			bufferSize = 0;
			if (mustSucceed) {
				throw ProtobufException.truncatedMessage();
			}
			return bufferSize;
		}
		bufferSize += avail;
		recomputeBufferSizeAfterLimit();
		final int totalBytesRead = totalBytesRetired + bufferSize + bufferSizeAfterLimit;
		if (totalBytesRead > sizeLimit || totalBytesRead < 0) {
			throw ProtobufException.sizeLimitExceeded();
		}
		return bufferSize;
	}

	/**
	 * @return last read tag
	 */
	public int lastTag() {
		return lastTag;
	}

	/**
	 * Verifies that the last call to readTag() returned the given tag value. This is used to verify that a nested group
	 * ended with the correct end tag.
	 *
	 * @throws ProtobufException
	 *             {@code value} does not match the last tag.
	 */
	public void checkLastTagWas(final int value) throws ProtobufException {
		if (lastTag != value) {
			throw ProtobufException.invalidEndTag();
		}
	}

	/**
	* Read one byte from the input.
	*
	* @throws ProtobufException
	*             The end of the stream or the current limit was reached.
	*/
	public byte readRawByte() throws IOException {
		if (bufferPos == bufferSize) {
			refillBuffer(true);
		}
		return buffer[bufferPos++];
	}

	/**
	 * Read a fixed size of bytes from the input.
	 *
	 * @throws ProtobufException
	 *             The end of the stream or the current limit was reached.
	 */
	public byte[] readRawBytes(final int size) throws IOException {
		if (size < 0) {
			throw ProtobufException.negativeSize();
		}

		if (totalBytesRetired + bufferPos + size > currentLimit) {
			// Read to the end of the stream anyway.
			skipRawBytes(currentLimit - totalBytesRetired - bufferPos);
			// Then fail.
			throw ProtobufException.truncatedMessage();
		}

		final byte[] bytes = new byte[size];
		int avail = bufferSize - bufferPos;
		if (size <= avail) {
			// We have all the bytes we need already.
			System.arraycopy(buffer, bufferPos, bytes, 0, size);
			bufferPos += size;
			return bytes;
		}

		System.arraycopy(buffer, bufferPos, bytes, 0, avail);
		bufferPos = bufferSize;
		if (isEof())
			throw ProtobufException.truncatedMessage();
		int off = avail;
		while (off < size) {
			int l = fillBuffer(bytes, off, size - off);
			if (l == -1)
				throw ProtobufException.truncatedMessage();
			off += l;
		}
		return bytes;
	}

	/**
	 * Reads and discards {@code size} bytes.
	 *
	 * @throws ProtobufException
	 *             The end of the stream or the current limit was reached.
	 */
	public void skipRawBytes(final int size) throws IOException {
		if (size < 0) {
			throw ProtobufException.negativeSize();
		}

		if (totalBytesRetired + bufferPos + size > currentLimit) {
			// Read to the end of the stream anyway.
			skipRawBytes(currentLimit - totalBytesRetired - bufferPos);
			// Then fail.
			throw ProtobufException.truncatedMessage();
		}

		if (size <= bufferSize - bufferPos) {
			// We have all the bytes we need already.
			bufferPos += size;
		} else {
			// Skipping more bytes than are in the buffer. First skip what we have.
			int pos = bufferSize - bufferPos;
			bufferPos = bufferSize;

			// Keep refilling the buffer until we get to the point we wanted to skip
			// to. This has the side effect of ensuring the limits are updated
			// correctly.
			refillBuffer(true);
			while (size - pos > bufferSize) {
				pos += bufferSize;
				bufferPos = bufferSize;
				refillBuffer(true);
			}

			bufferPos = size - pos;
		}
	}

	/**
	* Check if this field have been packed into a length-delimited field. If so, update internal state to reflect that
	* packed fields are being read.
	*
	* @throws IOException
	*/
	private void checkIfPackedField() throws IOException {
		// Do we have the start of a packed field?
		if (packedLimit == 0 && wasLen) {
			final int length = readRawVarint32();
			if (length < 0) {
				throw ProtobufException.negativeSize();
			}

			this.packedLimit = getTotalBytesRead() + length;
		}
	}

	/**
	 * Read a raw Varint from the stream. If larger than 32 bits, discard the upper bits.
	 */
	public int readRawVarint32() throws IOException {
		byte[] buf = buffer;
		int off = bufferPos;
		if (off + 10 > bufferSize)
			return (int) readRawVarint64Slow();

		try {
			int b = buf[off++];
			if (b >= 0)
				return b;
			int r = b & 0x7F;
			if ((b = buf[off++]) >= 0)
				return r | (b << 7);
			r |= (b & 0x7F) << 7;
			if ((b = buf[off++]) >= 0)
				return r | (b << 14);
			r |= (b & 0x7F) << 14;
			if ((b = buf[off++]) >= 0)
				return r | (b << 21);
			r |= (b & 0x7F) << 21;
			if ((b = buf[off++]) >= 0)
				return r | (b << 28);
			r |= (b & 0x7F) << 28;
			for (int i = 0; i < 5; i++) {
				if ((b = buf[off++]) >= 0)
					return r;
			}
			throw ProtobufException.malformedVarint();
		} finally {
			bufferPos = off;
		}
	}

	/**
	 * Read a raw Varint from the stream.
	 */
	public long readRawVarint64() throws IOException {
		byte[] buf = buffer;
		int off = bufferPos;
		if (off + 10 > bufferSize)
			return readRawVarint64Slow();

		try {
			long b = buf[off++];
			if (b >= 0)
				return b;
			long r = b & 0x7F;
			if ((b = buf[off++]) >= 0)
				return r | b << 7;
			r |= (b & 0x7F) << 7;
			if ((b = buf[off++]) >= 0)
				return r | b << 14;
			r |= (b & 0x7F) << 14;
			if ((b = buf[off++]) >= 0)
				return r | b << 21;
			r |= (b & 0x7F) << 21;
			if ((b = buf[off++]) >= 0)
				return r | b << 28;
			r |= (b & 0x7F) << 28;
			if ((b = buf[off++]) >= 0)
				return r | b << 35;
			r |= (b & 0x7F) << 35;
			if ((b = buf[off++]) >= 0)
				return r | b << 42;
			r |= (b & 0x7F) << 42;
			if ((b = buf[off++]) >= 0)
				return r | b << 49;
			r |= (b & 0x7F) << 49;
			if ((b = buf[off++]) >= 0)
				return r | b << 56;
			r |= (b & 0x7F) << 56;
			if ((b = buf[off++]) < 0)
				throw ProtobufException.malformedVarint();
			return r | b << 63;
		} finally {
			bufferPos = off;
		}
	}

	private long readRawVarint64Slow() throws IOException {
		long b = readRawByte();
		if (b >= 0)
			return b;
		long r = b & 0x7F;
		if ((b = readRawByte()) >= 0)
			return r | b << 7;
		r |= (b & 0x7F) << 7;
		if ((b = readRawByte()) >= 0)
			return r | b << 14;
		r |= (b & 0x7F) << 14;
		if ((b = readRawByte()) >= 0)
			return r | b << 21;
		r |= (b & 0x7F) << 21;
		if ((b = readRawByte()) >= 0)
			return r | b << 28;
		r |= (b & 0x7F) << 28;
		if ((b = readRawByte()) >= 0)
			return r | b << 35;
		r |= (b & 0x7F) << 35;
		if ((b = readRawByte()) >= 0)
			return r | b << 42;
		r |= (b & 0x7F) << 42;
		if ((b = readRawByte()) >= 0)
			return r | b << 49;
		r |= (b & 0x7F) << 49;
		if ((b = readRawByte()) >= 0)
			return r | b << 56;
		r |= (b & 0x7F) << 56;
		if ((b = readRawByte()) < 0)
			throw ProtobufException.malformedVarint();
		return r | b << 63;
	}

	/**
	* Read a 32-bit little-endian integer from the stream.
	 * @throws IOException in case of error
	*/
	public int readRawLittleEndian32() throws IOException {
		if (bufferSize - bufferPos >= 4) {
			int i = (int) INT.get(buffer, bufferPos);
			bufferPos += 4;
			return i;
		}
		final byte b1 = readRawByte();
		final byte b2 = readRawByte();
		final byte b3 = readRawByte();
		final byte b4 = readRawByte();
		return ((b1 & 0xff)) | ((b2 & 0xff) << 8) | ((b3 & 0xff) << 16) | ((b4 & 0xff) << 24);
	}

	/**
	 * Read a 64-bit little-endian integer from the stream.
	 * @throws IOException in case of error
	 */
	public long readRawLittleEndian64() throws IOException {
		if (bufferSize - bufferPos >= 8) {
			long l = (long) LONG.get(buffer, bufferPos);
			bufferPos += 8;
			return l;
		}
		final byte b1 = readRawByte();
		final byte b2 = readRawByte();
		final byte b3 = readRawByte();
		final byte b4 = readRawByte();
		final byte b5 = readRawByte();
		final byte b6 = readRawByte();
		final byte b7 = readRawByte();
		final byte b8 = readRawByte();
		return (((long) b1 & 0xff)) | (((long) b2 & 0xff) << 8) | (((long) b3 & 0xff) << 16) | (((long) b4 & 0xff) << 24) | (((long) b5 & 0xff) << 32)
				| (((long) b6 & 0xff) << 40) | (((long) b7 & 0xff) << 48) | (((long) b8 & 0xff) << 56);
	}

	/**
	 * Attempt to read a field tag, returning zero if we have reached EOF. Protocol message parsers use this to read
	 * tags, since a protocol message may legally end wherever a tag occurs, and zero is not a valid tag number.
	 * @return a tag
	 */
	@Override
	public int readTag() throws IOException {
		if (isAtEnd()) {
			lastTag = 0;
			return 0;
		}

		if (packedLimit > 0) {
			int totalBytesRead = getTotalBytesRead();
			if (packedLimit > totalBytesRead)
				return lastTag;
			if (packedLimit < totalBytesRead)
				throw ProtobufException.misreportedSize();
			packedLimit = 0;
		}

		final int tag = readRawVarint32();
		if (tag >>> TAG_TYPE_BITS == 0) {
			// If we actually read zero, that's not a valid tag.
			throw ProtobufException.invalidTag();
		}
		wasLen = getTagWireType(tag) == WIRETYPE_LENGTH_DELIMITED;
		lastTag = tag;
		return getTagWireType(tag) == WIRETYPE_END_GROUP ? 0 : tag;
	}

	/**
	 * Reads and discards previously read tag
	 */
	@Override
	public void skipField() throws IOException {
		skipField(lastTag);
	}

	/**
	 * Reads and discards a single field, given its tag value.
	 */
	void skipField(int tag) throws IOException {
		switch (getTagWireType(tag)) {
			case WIRETYPE_VARINT:
				skipRawBytes(4);
				return;
			case WIRETYPE_FIXED32:
				skipRawBytes(4);
				return;
			case WIRETYPE_FIXED64:
				skipRawBytes(8);
				return;
			case WIRETYPE_LENGTH_DELIMITED:
				skipRawBytes(readRawVarint32());
				return;
			case WIRETYPE_START_GROUP:
				skipGroup();
				checkLastTagWas((tag & ~0x7) | WIRETYPE_END_GROUP);
				return;
			case WIRETYPE_END_GROUP:
				return;
			default:
				throw ProtobufException.invalidWireType();
		}
	}

	/**
	 * Reads and discards an entire message. This will read either until EOF or until an endgroup tag, whichever comes
	 * first.
	 * @throws IOException in case of error
	 */
	public void skipGroup() throws IOException {
		while (true) {
			final int tag = readTag();
			if (tag == 0)
				return;
			skipField(tag);
		}
	}

	/**
	 * Read a {@code double} field value from the stream.
	 */
	@Override
	public double readDouble() throws IOException {
		checkIfPackedField();
		return Double.longBitsToDouble(readRawLittleEndian64());
	}

	/**
	 * Read a {@code float} field value from the stream.
	 */
	@Override
	public float readFloat() throws IOException {
		checkIfPackedField();
		return Float.intBitsToFloat(readRawLittleEndian32());
	}

	/**
	 * Read a {@code uint64} field value from the stream.
	 */
	@Override
	public long readUInt64() throws IOException {
		checkIfPackedField();
		return readRawVarint64();
	}

	/**
	 * Read an {@code int32} field value from the stream.
	 */
	@Override
	public int readUInt32() throws IOException {
		checkIfPackedField();
		return readRawVarint32();
	}

	/**
	 * Read a {@code fixed64} field value from the stream.
	 */
	@Override
	public long readFixed64() throws IOException {
		checkIfPackedField();
		return readRawLittleEndian64();
	}

	/**
	 * Read a {@code fixed32} field value from the stream.
	 */
	@Override
	public int readFixed32() throws IOException {
		checkIfPackedField();
		return readRawLittleEndian32();
	}

	/**
	 * Read a {@code bool} field value from the stream.
	 */
	@Override
	public boolean readBool() throws IOException {
		checkIfPackedField();
		return readRawVarint32() != 0;
	}

	/**
	 * Read an enum field value from the stream. Caller is responsible for converting the numeric value to an actual
	 * enum.
	 */
	@Override
	public <E extends Enum<E>> E readEnum(SchemaEnum<E> schema) throws IOException {
		checkIfPackedField();
		return schema.fromNumber(readRawVarint32());
	}

	/**
	 * Read an {@code sint32} field value from the stream.
	 */
	@Override
	public int readSInt32() throws IOException {
		checkIfPackedField();
		return decodeZigZag32(readRawVarint32());
	}

	/**
	 * Read an {@code sint64} field value from the stream.
	 */
	@Override
	public long readSInt64() throws IOException {
		checkIfPackedField();
		return decodeZigZag64(readRawVarint64());
	}

	/**
	 * Read a {@code string} field value from the stream.
	 */
	@Override
	public String readString() throws IOException {
		int size = readRawVarint32();
		if (size < 0)
			throw ProtobufException.negativeSize();

		if (size == 0)
			return EMPTY_STR;
		int avail = bufferSize - bufferPos;
		if (size <= avail) {
			String result = new String(buffer, bufferPos, size, StandardCharsets.UTF_8);
			bufferPos += size;
			return result;
		}

		dec.ensureCapacity(size);
		dec.append(buffer, bufferPos, avail);
		size -= avail;
		bufferPos = bufferSize;
		refillBuffer(true);

		while (size > bufferSize) {
			dec.append(buffer, 0, bufferSize);
			size -= bufferSize;
			bufferPos = bufferSize;
			refillBuffer(true);
		}
		dec.append(buffer, bufferPos, size);
		bufferPos += size;
		return dec.done();
	}

	@Override
	public byte[] readBytes() throws IOException {
		final int size = readRawVarint32();
		return readRawBytes(size);
	}

	@Override
	public <T> T mergeGroup(T value, final Schema<T> schema) throws IOException {
		final int endTag = (lastTag & ~0x7) | WIRETYPE_END_GROUP;
		if (value == null)
			value = schema.newMessage();
		schema.mergeFrom(this, value);
		checkLastTagWas(endTag);
		return value;
	}

	@Override
	public <T> T mergeObject(T value, final Schema<T> schema) throws IOException {
		final int length = readRawVarint32();
		final int oldLimit = pushLimit(length);
		if (value == null)
			value = schema.newMessage();
		schema.mergeFrom(this, value);
		checkLastTagWas(0);
		popLimit(oldLimit);
		return value;
	}

	/**
	* Decode a ZigZag-encoded 32-bit value. ZigZag encodes signed integers into values that can be efficiently encoded
	* with varint. (Otherwise, negative values must be sign-extended to 64 bits to be varint encoded, thus always
	* taking 10 bytes on the wire.)
	*
	* @param n
	*            An unsigned 32-bit integer, stored in a signed int because Java has no explicit unsigned support.
	* @return A signed 32-bit integer.
	*/
	public static int decodeZigZag32(final int n) {
		return (n >>> 1) ^ -(n & 1);
	}

	/**
	 * Decode a ZigZag-encoded 64-bit value. ZigZag encodes signed integers into values that can be efficiently encoded
	 * with varint. (Otherwise, negative values must be sign-extended to 64 bits to be varint encoded, thus always
	 * taking 10 bytes on the wire.)
	 *
	 * @param n
	 *            An unsigned 64-bit integer, stored in a signed int because Java has no explicit unsigned support.
	 * @return A signed 64-bit integer.
	 */
	public static long decodeZigZag64(final long n) {
		return (n >>> 1) ^ -(n & 1);
	}

}
