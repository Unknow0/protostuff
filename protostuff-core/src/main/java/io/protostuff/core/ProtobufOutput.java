package io.protostuff.core;

import java.io.DataOutput;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.protostuff.api.LinkedBuffer;
import io.protostuff.api.Output;
import io.protostuff.api.Schema;
import io.protostuff.api.SchemaEnum;
import io.protostuff.api.WireFormat;

/**
 * Output for protobuf format into buffers
 */
public class ProtobufOutput implements Output {
	private static final VarHandle INT = MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.LITTLE_ENDIAN);
	private static final int INT_REPL = 0x00BDBFEF;

	private static final int TWO_BYTE_LOWER_LIMIT = 1 << 7;
	private static final int THREE_BYTE_LOWER_LIMIT = 1 << 14;
	private static final int FOUR_BYTE_LOWER_LIMIT = 1 << 21;
	private static final int FIVE_BYTE_LOWER_LIMIT = 1 << 28;

	private static final int ONE_BYTE_EXCLUSIVE = TWO_BYTE_LOWER_LIMIT / 3 + 1;
	private static final int TWO_BYTE_EXCLUSIVE = THREE_BYTE_LOWER_LIMIT / 3 + 1;
	private static final int THREE_BYTE_EXCLUSIVE = FOUR_BYTE_LOWER_LIMIT / 3 + 1;
	private static final int FOUR_BYTE_EXCLUSIVE = FIVE_BYTE_LOWER_LIMIT / 3 + 1;

	private final int bufSize;
	private int size;
	private LinkedBuffer head;
	private LinkedBuffer tail;

	private int depth;

	/**
	 * new ProtobufOutput
	 */
	public ProtobufOutput() {
		this(LinkedBuffer.allocate(), LinkedBuffer.DEFAULT_BUFFER_SIZE);
	}

	/**
	 * new ProtobufOutput
	 * @param bufSize buffers size
	 */
	public ProtobufOutput(int bufSize) {
		this(LinkedBuffer.allocate(bufSize), bufSize);
	}

	/**
	 * new ProtobufOutput
	 * @param head buffer to use
	 */
	public ProtobufOutput(LinkedBuffer head) {
		this(head, LinkedBuffer.DEFAULT_BUFFER_SIZE);
	}

	/**
	 * new ProtobufOutput
	 * @param head buffer to use
	 * @param bufSize new buffers size
	 */
	public ProtobufOutput(LinkedBuffer head, int bufSize) {
		this.bufSize = bufSize;
		this.head = this.tail = head;
	}

	/**
	 * ensure we have at least this space in buffers
	 * @param len len needed
	 * @throws IOException in case of error
	 */
	protected void ensureSize(int len) throws IOException {
		int l = tail.buffer.length - tail.offset;
		if (l < len)
			nextBuffer(Math.max(bufSize, len));
	}

	@Override
	public void flush() throws IOException {
		tail = writeBuffers(head);
	}

	@Override
	public int close() throws IOException {
		int size = size();
		flush();
		head = tail = null;
		return size;
	}

	/**
	 * write buffer to the output if any
	 * @param head current head
	 * @return linked buffer new tail
	 * @throws IOException
	 */
	protected LinkedBuffer writeBuffers(@SuppressWarnings("unused") LinkedBuffer head) throws IOException {
		return tail;
	}

	/**
	 * get a clean buffer
	 * @param size
	 * @throws IOException in case of error
	 */
	protected void nextBuffer(int size) throws IOException {
		this.size += tail.offset - tail.start;
		if (depth == 0)
			tail = writeBuffers(head);
		if (head.offset == head.start && head.buffer.length < size)
			head = tail = LinkedBuffer.allocate(size);
		else
			tail = LinkedBuffer.allocate(size, tail);
	}

	/**
	 * written bytes
	 * @return written bytes
	 */
	public int size() {
		return size + tail.offset - tail.start;
	}

	/**
	 * output the buffer as a byte array 
	 * @return buffer content
	 */
	public byte[] toByteArray() {
		LinkedBuffer node = head;
		int offset = 0, len;
		final byte[] buf = new byte[size()];
		do {
			if ((len = node.offset - node.start) > 0) {
				System.arraycopy(node.buffer, node.start, buf, offset, len);
				offset += len;
			}
		} while ((node = node.next) != null);
		return buf;
	}

	/**
	 * write buffers to output
	 * @param out output
	 * @throws IOException in case of error
	 */
	public void writeTo(DataOutput out) throws IOException {
		LinkedBuffer.writeTo(out, head);
	}

	/**
	 * reset state, clear buffer
	 */
	public void reset() {
		depth = 0;
		size = 0;
		if (head != null)
			tail = head.clear();
		else
			tail = head = LinkedBuffer.allocate(bufSize);
	}

	/**
	 * write a varint
	 * @param value value to write
	 * @throws IOException in case of error
	 */
	protected void writeVarInt32(int value) throws IOException {
		int l = computeRawVarint32Size(value);
		ensureSize(l);
		final byte[] b = tail.buffer;
		int o = tail.offset;
		tail.offset += l;
		while (--l > 0) {
			b[o++] = (byte) ((value & 0x7F) | 0x80);
			value >>>= 7;
		}
		b[o] = (byte) value;
	}

	/**
	 * write a varint
	 * @param value value to write
	 * @throws IOException in case of error
	 */
	protected void writeVarInt64(long value) throws IOException {
		ensureSize(10);
		final byte[] buffer = tail.buffer;
		int off = tail.offset;
		while ((value & ~0x7F) != 0) {
			buffer[off++] = (byte) ((value & 0x7F) | 0x80);
			value >>>= 7;
		}
		buffer[off++] = (byte) value;
		tail.offset = off;
	}

	private void writeRawInt32(int tag, int value) throws IOException {
		int lt = computeRawVarint32Size(tag);
		int lv = computeRawVarint32Size(value);
		int total = lt + lv;
		ensureSize(total);
		final byte[] b = tail.buffer;
		int o = tail.offset;
		tail.offset += total;
		while (--lt > 0) {
			b[o++] = (byte) ((tag & 0x7F) | 0x80);
			tag >>>= 7;
		}
		b[o++] = (byte) tag;
		while (--lv > 0) {
			b[o++] = (byte) ((value & 0x7F) | 0x80);
			value >>>= 7;
		}
		b[o] = (byte) value;
	}

	@Override
	public void writeUInt32(int tag, String name, int value) throws IOException {
		if (value < 0)
			writeUInt64(tag, name, value);
		else
			writeRawInt32(tag, value);
	}

	@Override
	public void writeSInt32(int tag, String name, int value) throws IOException {
		writeRawInt32(tag, encodeZigZag32(value));
	}

	@Override
	public void writeFixed32(int tag, String name, int value) throws IOException {
		int lt = computeRawVarint32Size(tag);
		int total = lt + 4;
		ensureSize(total);
		final byte[] b = tail.buffer;
		int o = tail.offset;
		tail.offset += total;
		while (--lt > 0) {
			b[o++] = (byte) ((tag & 0x7F) | 0x80);
			tag >>>= 7;
		}
		b[o] = (byte) tag;
		b[o + 1] = (byte) (value & 0xFF);
		b[o + 2] = (byte) (value >> 8 & 0xFF);
		b[o + 3] = (byte) (value >> 16 & 0xFF);
		b[o + 4] = (byte) (value >> 24 & 0xFF);
	}

	@Override
	public void writeUInt64(int tag, String name, long value) throws IOException {
		int lt = computeRawVarint32Size(tag);
		int lv = computeRawVarint64Size(value);
		int total = lt + lv;
		ensureSize(total);
		final byte[] b = tail.buffer;
		int o = tail.offset;
		tail.offset += total;
		while (--lt > 0) {
			b[o++] = (byte) ((tag & 0x7F) | 0x80);
			tag >>>= 7;
		}
		b[o++] = (byte) tag;
		while (--lv > 0) {
			b[o++] = (byte) ((value & 0x7F) | 0x80);
			value >>>= 7;
		}
		b[o] = (byte) value;
	}

	@Override
	public void writeSInt64(int tag, String name, long value) throws IOException {
		writeUInt64(tag, name, encodeZigZag64(value));
	}

	@Override
	public void writeFixed64(int tag, String name, long value) throws IOException {
		int lt = computeRawVarint32Size(tag);
		int total = lt + 8;
		ensureSize(total);
		final byte[] b = tail.buffer;
		int o = tail.offset;
		tail.offset += total;
		while (--lt > 0) {
			b[o++] = (byte) ((tag & 0x7F) | 0x80);
			tag >>>= 7;
		}
		b[o] = (byte) tag;
		b[o + 1] = (byte) (value & 0xFF);
		b[o + 2] = (byte) (value >> 8 & 0xFF);
		b[o + 3] = (byte) (value >> 16 & 0xFF);
		b[o + 4] = (byte) (value >> 24 & 0xFF);
		b[o + 5] = (byte) (value >> 32 & 0xFF);
		b[o + 6] = (byte) (value >> 40 & 0xFF);
		b[o + 7] = (byte) (value >> 48 & 0xFF);
		b[o + 8] = (byte) (value >> 56 & 0xFF);
	}

	@Override
	public void writeFloat(int tag, String name, float value) throws IOException {
		writeFixed32(tag, name, Float.floatToRawIntBits(value));
	}

	@Override
	public void writeDouble(int tag, String name, double value) throws IOException {
		writeFixed64(tag, name, Double.doubleToRawLongBits(value));
	}

	@Override
	public void writeBool(int tag, String name, boolean value) throws IOException {
		int lt = computeRawVarint32Size(tag);
		int total = lt + 1;
		ensureSize(total);
		final byte[] b = tail.buffer;
		int o = tail.offset;
		tail.offset += total;
		while (--lt > 0) {
			b[o++] = (byte) ((tag & 0x7F) | 0x80);
			tag >>>= 7;
		}
		b[o] = (byte) tag;
		b[o + 1] = (byte) (value ? 1 : 0);
	}

	@Override
	public <E extends Enum<E>> void writeEnum(int tag, String name, E e, SchemaEnum<E> schema) throws IOException {
		writeUInt32(tag, name, schema.toNumber(e));
	}

	@Override
	public void writeBytes(int tag, String name, byte[] value) throws IOException {
		int len = value.length;
		writeUInt32(tag, name, len);
		if (tail.buffer.length - tail.offset < len) {
			this.size += tail.offset - tail.start;
			tail = LinkedBuffer.wrap(value, 0, len, tail);
			return;
		}
		System.arraycopy(value, 0, tail.buffer, tail.offset, len);
		tail.offset += len;
	}

	@Override
	public <T> void writeGroup(int tag, String name, T t, Schema<T> schema) throws IOException {
		writeVarInt32(tag);
		schema.writeTo(this, t);
		writeVarInt32((tag & ~0x7) | WireFormat.WIRETYPE_END_GROUP);
	}

	@Override
	public <T> void writeMessage(int tag, String name, T t, Schema<T> schema) throws IOException {
		writeVarInt32(tag);
		writeMessage(t, schema);
	}

	public <T> void writeMessage(T t, Schema<T> schema) throws IOException {
		depth++;
		ensureSize(2);
		final LinkedBuffer lb = tail;
		final int off = lb.offset;
		lb.offset += 2;
		int s = size();
		schema.writeTo(this, t);
		s = size() - s;
		if (s < TWO_BYTE_LOWER_LIMIT) {
			lb.buffer[off] = (byte) s;
			shift(lb, off + 1);
		} else if (s < THREE_BYTE_LOWER_LIMIT) {
			writeVarInt32(s, lb.buffer, off);
		} else {
			final LinkedBuffer view = new LinkedBuffer(lb.buffer, off + 2, lb.offset);
			lb.offset = off;
			if (lb == tail) {
				tail = view;
			} else {
				view.next = lb.next;
			}

			final byte[] delimited = new byte[computeRawVarint32Size(s)];
			writeVarInt32(s, delimited, 0);

			// add the difference
			size += (delimited.length - 2);

			// wrap the byte array (delimited) and insert between the two buffers
			LinkedBuffer.wrap(delimited, lb).next = view;
		}
		depth--;
	}

	@Override
	public void writeString(int tag, String name, CharSequence value) throws IOException {
		writeVarInt32(tag);
		writeDelimitedUtf8(value);
	}

	@Override
	public void writeUInt32List(int tag, String name, Collection<Integer> values) throws IOException {
		if (isPacked(tag)) {
			long l = 0;
			for (Integer i : values)
				l += computeRawVarint32Size(i);
			writeVarInt32(tag);
			writeVarInt64(l);
			for (Integer i : values)
				writeVarInt32(i);
			return;
		}
		for (Integer i : values)
			writeUInt32(tag, name, i);
	}

	@Override
	public void writeSInt32List(int tag, String name, Collection<Integer> values) throws IOException {
		if (isPacked(tag)) {
			long l = 0;
			List<Integer> list = new ArrayList<>(values.size());
			for (int i : values) {
				i = encodeZigZag32(i);
				list.add(i);
				l += computeRawVarint32Size(i);
			}
			writeVarInt32(tag);
			writeVarInt64(l);
			for (int i : list)
				writeVarInt32(i);
			return;
		}
		for (int i : values)
			writeSInt32(tag, name, i);
	}

	@Override
	public void writeFixed32List(int tag, String name, Collection<Integer> values) throws IOException {
		if (isPacked(tag)) {
			writeVarInt32(tag);
			writeVarInt64(values.size() * 4);
			for (int i : values) {
				ensureSize(4);
				final byte[] b = tail.buffer;
				int o = tail.offset;
				tail.offset += 4;
				b[o] = (byte) (i & 0xFF);
				b[o + 1] = (byte) (i >> 8 & 0xFF);
				b[o + 2] = (byte) (i >> 16 & 0xFF);
				b[o + 3] = (byte) (i >> 24 & 0xFF);
			}
			return;
		}
		for (int i : values)
			writeFixed32(tag, name, i);
	}

	@Override
	public void writeUInt64List(int tag, String name, Collection<Long> values) throws IOException {
		if (isPacked(tag)) {
			long l = 0;
			for (long i : values)
				l += computeRawVarint64Size(i);
			writeVarInt32(tag);
			writeVarInt64(l);
			for (long i : values)
				writeVarInt64(i);
			return;
		}
		for (long i : values)
			writeUInt64(tag, name, i);
	}

	@Override
	public void writeSInt64List(int tag, String name, Collection<Long> values) throws IOException {
		if (isPacked(tag)) {
			long l = 0;
			List<Long> list = new ArrayList<>(values.size());
			for (long i : values) {
				i = encodeZigZag64(i);
				list.add(i);
				l += computeRawVarint64Size(i);
			}
			writeVarInt32(tag);
			writeVarInt64(l);
			for (long i : list)
				writeVarInt64(i);
			return;
		}
		for (long i : values)
			writeSInt64(tag, name, i);
	}

	@Override
	public void writeFixed64List(int tag, String name, Collection<Long> values) throws IOException {
		if (isPacked(tag)) {
			writeVarInt32(tag);
			writeVarInt64(values.size() * 8);
			for (long i : values) {
				ensureSize(8);
				final byte[] b = tail.buffer;
				int o = tail.offset;
				tail.offset += 8;
				b[o] = (byte) (i & 0xFF);
				b[o + 1] = (byte) (i >> 8 & 0xFF);
				b[o + 2] = (byte) (i >> 16 & 0xFF);
				b[o + 3] = (byte) (i >> 24 & 0xFF);
				b[o + 4] = (byte) (i >> 32 & 0xFF);
				b[o + 5] = (byte) (i >> 40 & 0xFF);
				b[o + 6] = (byte) (i >> 48 & 0xFF);
				b[o + 7] = (byte) (i >> 56 & 0xFF);
			}
			return;
		}
		for (long i : values)
			writeFixed64(tag, name, i);
	}

	@Override
	public void writeFloatList(int tag, String name, Collection<Float> values) throws IOException {
		if (isPacked(tag)) {
			writeVarInt32(tag);
			writeVarInt64(values.size() * 4);
			for (float f : values) {
				ensureSize(4);
				final byte[] b = tail.buffer;
				int o = tail.offset;
				tail.offset += 4;
				int i = Float.floatToIntBits(f);
				b[o] = (byte) (i & 0xFF);
				b[o + 1] = (byte) (i >> 8 & 0xFF);
				b[o + 2] = (byte) (i >> 16 & 0xFF);
				b[o + 3] = (byte) (i >> 24 & 0xFF);
			}
			return;
		}
		for (float i : values)
			writeFloat(tag, name, i);
	}

	@Override
	public void writeDoubleList(int tag, String name, Collection<Double> values) throws IOException {
		if (isPacked(tag)) {
			writeVarInt32(tag);
			writeVarInt64(values.size() * 8);
			for (double d : values) {
				ensureSize(8);
				final byte[] b = tail.buffer;
				int o = tail.offset;
				tail.offset += 8;
				long i = Double.doubleToLongBits(d);
				b[o] = (byte) (i & 0xFF);
				b[o + 1] = (byte) (i >> 8 & 0xFF);
				b[o + 2] = (byte) (i >> 16 & 0xFF);
				b[o + 3] = (byte) (i >> 24 & 0xFF);
				b[o + 4] = (byte) (i >> 32 & 0xFF);
				b[o + 5] = (byte) (i >> 40 & 0xFF);
				b[o + 6] = (byte) (i >> 48 & 0xFF);
				b[o + 7] = (byte) (i >> 56 & 0xFF);
			}
			return;
		}
		for (double i : values)
			writeDouble(tag, name, i);
	}

	@Override
	public void writeBoolList(int tag, String name, Collection<Boolean> values) throws IOException {
		if (isPacked(tag)) {
			writeVarInt32(tag);
			writeVarInt64(values.size());
			for (boolean i : values) {
				ensureSize(1);
				tail.buffer[tail.offset++] = (byte) (i ? 1 : 0);
			}
			return;
		}
		for (boolean i : values)
			writeBool(tag, name, i);
	}

	@Override
	public <E extends Enum<E>> void writeEnumList(int tag, String name, Collection<E> values, SchemaEnum<E> schema) throws IOException {
		if (isPacked(tag)) {
			long l = 0;
			for (E e : values)
				l += computeRawVarint32Size(schema.toNumber(e));
			writeVarInt32(tag);
			writeVarInt64(l);
			for (E e : values)
				writeVarInt32(schema.toNumber(e));
			return;
		}
		for (E e : values)
			writeEnum(tag, name, e, schema);
	}

	@Override
	public void writeBytesList(int tag, String name, Collection<byte[]> values) throws IOException {
		for (byte[] b : values)
			writeBytes(tag, name, b);
	}

	@Override
	public <T> void writeGroupList(int tag, String name, Collection<T> values, Schema<T> schema) throws IOException {
		for (T o : values)
			writeGroup(tag, name, o, schema);
	}

	@Override
	public <T> void writeMessageList(int tag, String name, Collection<T> values, Schema<T> schema) throws IOException {
		for (T o : values)
			writeMessage(tag, name, o, schema);
	}

	@Override
	public void writeStringList(int tag, String name, Collection<? extends CharSequence> values) throws IOException {
		for (CharSequence s : values)
			writeString(tag, name, s);
	}

	/**
	 * write a sting as utf8 with the length prefix
	 * @param value value to write
	 * @throws IOException in case of error
	 */
	public void writeDelimitedUtf8(CharSequence value) throws IOException {
		final int len = value.length();
		if (len == 0) {
			ensureSize(1);
			tail.buffer[tail.offset++] = 0x00;
			return;
		}
		// avoid flush as we need the whole buffer
		depth++;
		// the varint will be max 1-byte. (even if all chars are non-ascii)
		if (len < ONE_BYTE_EXCLUSIVE)
			writeDelimitedUtf8(value, 0, len, 1);
		else if (len < TWO_BYTE_EXCLUSIVE)
			writeDelimitedUtf8(value, 0, len, 2);
		else if (len < THREE_BYTE_EXCLUSIVE)
			writeDelimitedUtf8(value, 0, len, 3);
		else if (len < FOUR_BYTE_EXCLUSIVE)
			writeDelimitedUtf8(value, 0, len, 4);
		else
			writeDelimitedUtf8(value, 0, len, 5);
		depth--;
	}

	private void writeDelimitedUtf8(CharSequence value, int o, int e, int expected) throws IOException {
		ensureSize(expected);
		final LinkedBuffer lb = tail;
		final int off = tail.offset;
		tail.offset += expected;
		int l = writeUTF8(value, o, e);
		int w = writeVarInt32(l, lb.buffer, off);
		if (expected > w)
			shift(lb, off + 1);
	}

	private int writeUTF8(CharSequence str, int off, final int end) throws IOException {
		final int i = tail.offset;
		off = writeUTF8Chunk(str, off, end, tail);
		int w = tail.offset - i;
		while (off < end) {
			nextBuffer(bufSize);
			off = writeUTF8Chunk(str, off, end, tail);
			w += tail.offset - tail.start;
		}
		return w;
	}

	private static boolean isPacked(int tag) {
		return WireFormat.getTagWireType(tag) == WireFormat.WIRETYPE_LENGTH_DELIMITED;
	}

	/**
	 * shift data left by one byte
	 * @param lb first buffer to shift
	 * @param off index of first byte to override
	 */
	private static void shift(LinkedBuffer lb, int off) {
		System.arraycopy(lb.buffer, off + 1, lb.buffer, off, lb.offset - off - 1);
		while (lb.next != null) {
			LinkedBuffer n = lb.next;
			lb.buffer[lb.offset - 1] = n.buffer[n.start];
			System.arraycopy(n.buffer, n.start + 1, n.buffer, n.start, n.offset - n.start - 1);
			lb = n;
		}
		lb.offset--;
	}

	/**
	 * fill lb buffer with as much possible char
	 * @param str string to write
	 * @param off start char (included)
	 * @param end end char (excluded)
	 * @param lb buffer to write to
	 * @return first char not written
	 */
	private static int writeUTF8Chunk(CharSequence str, int off, int end, LinkedBuffer lb) {
		int o = lb.offset;
		final byte[] buffer = lb.buffer;
		final int max = buffer.length - o - 4;
		while (o < max && off < end) {
			char code = str.charAt(off++);
			if (code < 0x80) {
				buffer[o++] = (byte) code;
			} else if (code < 0x800) {
				int c = 0x80C0 | (code >> 6) | ((code << 8) & 0x3F00);
				INT.set(buffer, o, c);
				o += 2;
			} else if (code < 0xD800) {
				int c = 0x008080E0 | (code >> 12) | ((code << 2) & 0x3F00) | (code << 16 & 0x3F0000);
				INT.set(buffer, o, c);
				o += 3;
			} else if (code <= 0xDC00) {
				// high surrogate
				if (off < end) {
					int low = str.charAt(off);
					if (low >= 0xDC00 && low <= 0xDFFF) {
						off++;
						int c = 0x10000 + ((code & 0x7FF) << 10) + (low & 0x3FF);
						c = 0x808080F0 | (c >> 18) | ((c >> 4) & 0x00003F00) | ((c << 10) & 0x003F0000) | (c << 24 & 0x3F000000);
						INT.set(buffer, o, c);
						o += 4;
						continue;
					}
				}
				INT.set(buffer, o, INT_REPL);
				o += 3;
			} else if (code < 0xE000) { // lone low surrogate
				INT.set(buffer, o, INT_REPL);
				o += 3;
			} else {
				int c = 0x008080E0 | (code >> 12) | ((code << 2) & 0x3F00) | (code << 16 & 0x3F0000);
				INT.set(buffer, o, c);
				o += 3;
			}
		}
		lb.offset = o;
		return off;
	}

	/**
	 * write a varint into a buffer
	 * @param value the value to write
	 * @param buffer buffer to use
	 * @param off start offset to write
	 * @return size of the varint
	 */
	public static int writeVarInt32(int value, byte[] buffer, int off) {
		int l = 1;
		while ((value & ~0x7F) != 0) {
			buffer[off++] = (byte) ((value & 0x7F) | 0x80);
			value >>>= 7;
			l++;
		}
		buffer[off] = (byte) value;
		return l;
	}

	/**
	* Compute the number of bytes that would be needed to encode a varint. {@code value} is treated as unsigned, so it
	* won't be sign-extended if negative.
	*/
	public static int computeRawVarint32Size(final int value) {
		if ((value & (0xffffffff << 7)) == 0)
			return 1;
		if ((value & (0xffffffff << 14)) == 0)
			return 2;
		if ((value & (0xffffffff << 21)) == 0)
			return 3;
		if ((value & (0xffffffff << 28)) == 0)
			return 4;
		return 5;
	}

	/**
	 * Compute the number of bytes that would be needed to encode a varint.
	 */
	public static int computeRawVarint64Size(final long value) {
		if ((value & (0xffffffffffffffffL << 7)) == 0)
			return 1;
		if ((value & (0xffffffffffffffffL << 14)) == 0)
			return 2;
		if ((value & (0xffffffffffffffffL << 21)) == 0)
			return 3;
		if ((value & (0xffffffffffffffffL << 28)) == 0)
			return 4;
		if ((value & (0xffffffffffffffffL << 35)) == 0)
			return 5;
		if ((value & (0xffffffffffffffffL << 42)) == 0)
			return 6;
		if ((value & (0xffffffffffffffffL << 49)) == 0)
			return 7;
		if ((value & (0xffffffffffffffffL << 56)) == 0)
			return 8;
		if ((value & (0xffffffffffffffffL << 63)) == 0)
			return 9;
		return 10;
	}

	/**
	 * Encode a ZigZag-encoded 32-bit value. ZigZag encodes signed integers into values that can be efficiently encoded
	 * with varint. (Otherwise, negative values must be sign-extended to 64 bits to be varint encoded, thus always
	 * taking 10 bytes on the wire.)
	 *
	 * @param n
	 *            A signed 32-bit integer.
	 * @return An unsigned 32-bit integer, stored in a signed int because Java has no explicit unsigned support.
	 */
	public static int encodeZigZag32(final int n) {
		// Note: the right-shift must be arithmetic
		return (n << 1) ^ (n >> 31);
	}

	/**
	 * Encode a ZigZag-encoded 64-bit value. ZigZag encodes signed integers into values that can be efficiently encoded
	 * with varint. (Otherwise, negative values must be sign-extended to 64 bits to be varint encoded, thus always
	 * taking 10 bytes on the wire.)
	 *
	 * @param n
	 *            A signed 64-bit integer.
	 * @return An unsigned 64-bit integer, stored in a signed int because Java has no explicit unsigned support.
	 */
	public static long encodeZigZag64(final long n) {
		// Note: the right-shift must be arithmetic
		return (n << 1) ^ (n >> 63);
	}
}
