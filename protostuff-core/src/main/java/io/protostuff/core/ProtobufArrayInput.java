package io.protostuff.core;

import java.io.IOException;

import io.protostuff.api.Input;

/**
 * Input for protobuf format from byte array
 */
public class ProtobufArrayInput extends ProtobufAbstractInput implements Input {

	/**
	 * new ProtobufArrayInput
	 * @param bytes data
	 */
	public ProtobufArrayInput(byte[] bytes) {
		this(bytes, 0, bytes.length);
	}

	/**
	 * new ProtobufArrayInput
	 * @param buffer data
	 * @param offset data offset
	 * @param length data length
	 */
	public ProtobufArrayInput(byte[] buffer, int offset, int length) {
		super(buffer, offset, length);
	}

	@Override
	protected boolean isEof() {
		return false;
	}

	@Override
	protected int fillBuffer(byte[] buffer, int off, int len) throws IOException {
		return -1;
	}
}
