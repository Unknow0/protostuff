package io.protostuff.core;

import java.io.IOException;

import io.protostuff.api.WireFormat;

public class ProtostuffArrayInput extends ProtobufArrayInput {
	public ProtostuffArrayInput(byte[] bytes) {
		this(bytes, 0, bytes.length);
	}

	public ProtostuffArrayInput(byte[] buffer, int offset, int limit) {
		super(buffer, offset, limit);
	}

	@Override
	public int readTag() throws IOException {
		int tag = super.readTag();
		return tag == WireFormat.TAIL_DELIMITER_TAG ? 0 : tag;
	}
}
