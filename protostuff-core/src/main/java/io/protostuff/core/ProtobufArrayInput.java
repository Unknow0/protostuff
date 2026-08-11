package io.protostuff.core;

import java.io.IOException;

import io.protostuff.api.Input;

public class ProtobufArrayInput extends ProtobufAbstractInput implements Input {

	public ProtobufArrayInput(byte[] bytes) {
		this(bytes, 0, bytes.length);
	}

	public ProtobufArrayInput(byte[] buffer, int offset, int limit) {
		super(buffer, offset, limit);
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
