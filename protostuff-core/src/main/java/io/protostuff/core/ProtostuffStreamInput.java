package io.protostuff.core;

import java.io.IOException;
import java.io.InputStream;

import io.protostuff.api.WireFormat;

public class ProtostuffStreamInput extends ProtobufStreamInput {

	/**
	 * new ProtobufStreamInput
	 * @param input input stream
	 */
	public ProtostuffStreamInput(final InputStream input) {
		this(input, new byte[DEFAULT_BUFFER_SIZE], 0, 0);
	}

	/**
	 * new ProtobufStreamInput
	 * @param input input stream
	 * @param buffer temp buffer
	 */
	public ProtostuffStreamInput(final InputStream input, byte[] buffer) {
		this(input, buffer, 0, 0);
	}

	/**
	 * new ProtobufStreamInput
	 * @param input input stream
	 * @param buffer temp buffer / initial data
	 * @param offset offset of initial data
	 * @param limit length of initial data
	 */
	public ProtostuffStreamInput(final InputStream input, byte[] buffer, int offset, int limit) {
		super(input, buffer, offset, limit);
	}

	@Override
	public int readTag() throws IOException {
		int tag = super.readTag();
		return tag == WireFormat.TAIL_DELIMITER_TAG ? 0 : tag;
	}
}
