package io.protostuff.core;

import java.io.IOException;
import java.util.ArrayList;

import io.protostuff.api.Schema;
import io.protostuff.api.WireFormat;

/**
 * Input for graph format
 */
public class GraphInput extends ProtobufArrayInput {
	private final ArrayList<Object> refs;

	private boolean wasRef;

	/**
	 * new GraphInput
	 * @param bytes data
	 */
	public GraphInput(byte[] bytes) {
		this(bytes, 0, bytes.length);
	}

	/**
	 * new GraphInput
	 * @param buffer data
	 * @param offset offset of data
	 * @param length length of data
	 */
	public GraphInput(byte[] buffer, int offset, int length) {
		super(buffer, offset, length);
		this.refs = new ArrayList<>();
	}

	@Override
	public int readTag() throws IOException {
		int tag = super.readTag();
		if ((wasRef = WireFormat.getTagWireType(tag) == WireFormat.WIRETYPE_REFERENCE))
			return (tag & ~0x7) | WireFormat.WIRETYPE_START_GROUP;
		return tag;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T mergeGroup(T value, Schema<T> schema) throws IOException {
		if (wasRef)
			return (T) refs.get(readUInt32());
		if (value == null)
			value = schema.newMessage();
		refs.add(value);
		super.mergeGroup(value, schema);
		return value;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T mergeObject(T value, Schema<T> schema) throws IOException {
		if (wasRef)
			return (T) refs.get(readUInt32());
		if (value == null)
			value = schema.newMessage();
		refs.add(value);
		super.mergeObject(value, schema);
		return value;
	}
}
