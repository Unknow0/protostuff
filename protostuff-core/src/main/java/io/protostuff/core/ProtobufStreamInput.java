package io.protostuff.core;

import java.io.IOException;
import java.io.InputStream;

import io.protostuff.api.Input;

/**
 * Input for Protobuf format from stream
 */
public class ProtobufStreamInput extends ProtobufAbstractInput implements Input {
	private final InputStream input;

	/**
	 * new ProtobufStreamInput
	 * @param input input stream
	 */
	public ProtobufStreamInput(final InputStream input) {
		this(input, new byte[DEFAULT_BUFFER_SIZE], 0, 0);
	}

	/**
	 * new ProtobufStreamInput
	 * @param input input stream
	 * @param buffer temp buffer
	 */
	public ProtobufStreamInput(final InputStream input, byte[] buffer) {
		this(input, buffer, 0, 0);
	}

	/**
	 * new ProtobufStreamInput
	 * @param input input stream
	 * @param buffer temp buffer / initial data
	 * @param offset offset of initial data
	 * @param limit length of initial data
	 */
	public ProtobufStreamInput(final InputStream input, byte[] buffer, int offset, int limit) {
		super(buffer, offset, limit);
		this.input = input;
	}

	@Override
	protected boolean isEof() {
		return false;
	}

	@Override
	protected int fillBuffer(byte[] buffer, int off, int len) throws IOException {
		int i = input.read(buffer, off, len);
		if (i == 0 || i < -1)
			throw new IllegalStateException("InputStream#read(byte[]) returned invalid result: " + i + "\nThe InputStream implementation is buggy.");
		return i;
	}
}
