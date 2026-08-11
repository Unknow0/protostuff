package io.protostuff.api;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

public class Utf8Decoder {
	private static final VarHandle INT = MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.LITTLE_ENDIAN);
	private static final VarHandle SHORT = MethodHandles.byteArrayViewVarHandle(short[].class, ByteOrder.LITTLE_ENDIAN);
	private static final char REPL = '�';

	private final StringBuilder sb;

	/** code point in building */
	private int cp;
	/** min code point allowed */
	private int minCp;
	/** remaining octet to read */
	private int r;

	public Utf8Decoder() {
		sb = new StringBuilder();
	}

	public String done() {
		if (r > 0) {
			sb.append(REPL);
			r = 0;
		}
		String str = sb.toString();
		sb.setLength(0);
		return str;
	}

	public void ensureCapacity(int minSize) {
		sb.ensureCapacity(minSize);
	}

	public void append(byte[] bytes) {
		append(bytes, 0, bytes.length);
	}

	public void append(byte[] bytes, int off, int lim) {
		if (off < 0 || lim > bytes.length)
			throw new ArrayIndexOutOfBoundsException("Invalid bound array lenth: " + bytes.length + " off=" + off + ", lim= " + lim);

		if (r > 0) {
			off = remainingArray(bytes, off, lim, cp, r);
			if (r > 0)
				return;
		}
		int code;
		while (off < lim) {
			int b = bytes[off++];
			if (b >= 0)
				sb.append((char) b);
			else if (b < -64)
				sb.append(REPL);
			else if (b < -32) {
				code = b & 0x1F;
				if (off == lim) {
					this.cp = code;
					r = 1;
					minCp = 0x80;
					return;
				}
				b = bytes[off++];
				if ((b & 0xc0) == 0x80) {
					code = (code << 6) | (b & 0x3F);
					sb.append(code < 0x80 ? REPL : (char) code);
				} else
					sb.append(REPL);
			} else if (b < -16) {
				code = b & 0x0F;
				if (off + 1 >= lim) {
					minCp = 0x800;
					remainingArray(bytes, off, lim, code, 2);
					return;
				}
				short s = (short) SHORT.get(bytes, off);
				if ((s & 0xc0c0) == 0x8080) {
					code = (code << 12) | ((s & 0x3F) << 6) | ((s >> 8) & 0x3F);
					sb.append(code < 0x800 ? REPL : (char) code);
				} else
					sb.append(REPL);
				off += 2;
			} else if (b < -8) {
				code = b & 0x07;
				if (off + 3 >= lim) {
					minCp = 0x10000;
					remainingArray(bytes, off, lim, code, 3);
					return;
				}
				int i = (int) INT.get(bytes, off);
				if ((i & 0x00C0C0C0) == 0x00808080) {
					code = (code << 18) | ((i & 0x3F) << 12) | (((i >> 8) & 0x3F) << 6) | ((i >> 16) & 0x3F);
					if (code >= 0x10000) {
						if (code > 0xFFFF) { // Surrogate pair
							code -= 0x10000;
							sb.append((char) ((code >> 10) | 0xD800));
							sb.append((char) ((code & 0x3FF) | 0xDC00));
						} else
							sb.append((char) code);
					} else
						sb.append(REPL);
				} else
					sb.append(REPL);
				off += 3;
			} else
				sb.append(REPL);
		}
	}

	private int remainingArray(byte[] bytes, int off, int lim, int cp, int r) {
		while (off < lim) {
			int b = bytes[off++];
			if ((b & 0xc0) != 0x80) {
				sb.append(REPL);
				r = 0;
				return off - 1;
			}
			cp = (cp << 6) | (b & 0x3F);
			if (--r == 0) {
				if (cp >= minCp) {
					if (cp > 0xFFFF) { // Surrogate pair
						cp -= 0x10000;
						sb.append((char) ((cp >> 10) | 0xD800));
						sb.append((char) ((cp & 0x3FF) | 0xDC00));
					} else
						sb.append((char) cp);
				} else
					sb.append(REPL);
				this.r = r;
				return off;
			}
		}
		this.cp = cp;
		this.r = r;
		return off;
	}
}
