package io.protostuff.core;

import java.io.IOException;
import java.util.Collection;

import io.protostuff.api.LinkedBuffer;
import io.protostuff.api.Schema;
import io.protostuff.api.WireFormat;

/**
 * Output for protostuff format into LinkBuffer
 */
public class ProtostuffOutput extends ProtobufOutput {
	/**
	 * new ProtosStuffOutput
	 */
	public ProtostuffOutput() {
		this(LinkedBuffer.allocate(), LinkedBuffer.DEFAULT_BUFFER_SIZE);
	}

	/**
	 * new ProtosStuffOutput
	 * @param bufSize size of buffers
	 */
	public ProtostuffOutput(int bufSize) {
		this(LinkedBuffer.allocate(bufSize), bufSize);
	}

	/**
	 * new ProtosStuffOutput
	 * @param head buffer to use
	 */
	public ProtostuffOutput(LinkedBuffer head) {
		this(head, LinkedBuffer.DEFAULT_BUFFER_SIZE);
	}

	/**
	 * new ProtosStuffOutput
	 * @param head buffer to use
	 * @param bufSize size of next buffers
	 */
	public ProtostuffOutput(LinkedBuffer head, int bufSize) {
		super(head, bufSize);
	}

	@Override
	public <T> void writeMessage(int tag, String name, T t, Schema<T> schema) throws IOException {
		int n = (tag & ~0x7);
		writeVarInt32(n | WireFormat.WIRETYPE_START_GROUP);
		schema.writeTo(this, t);
		writeVarInt32(n | WireFormat.WIRETYPE_END_GROUP);
	}

	@Override
	public <T> void writeMessageList(int tag, String name, Collection<T> values, Schema<T> schema) throws IOException {
		int n = (tag & ~0x7);
		int start = n | WireFormat.WIRETYPE_START_GROUP;
		int end = n | WireFormat.WIRETYPE_END_GROUP;
		for (T t : values) {
			writeVarInt32(start);
			schema.writeTo(this, t);
			writeVarInt32(end);
		}
	}
}
