package io.protostuff.core;

import java.io.IOException;
import java.util.ArrayList;

import io.protostuff.api.Schema;
import io.protostuff.api.WireFormat;

public class GraphInput extends ProtobufArrayInput {
	private final ArrayList<Object> refs;

	private boolean wasRef;

	public GraphInput(byte[] bytes) {
		this(bytes, 0, bytes.length);
	}

	public GraphInput(byte[] buffer, int offset, int limit) {
		super(buffer, offset, limit);
		this.refs = new ArrayList<>();
	}

	@Override
	public int readTag() throws IOException {
		int tag = super.readTag();
		System.out.println("read tag "+WireFormat.getTagFieldNumber(tag));
		if ((wasRef = WireFormat.getTagWireType(tag) == WireFormat.WIRETYPE_REFERENCE))
			return (tag & ~0x7) | WireFormat.WIRETYPE_START_GROUP;
		return tag;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T mergeGroup(T value, Schema<T> schema) throws IOException {
		if (wasRef) {
			int i = readInt32();
			System.out.println("read ref " + i + " " + refs);
			return (T) refs.get(i);
		}
		if (value == null)
			value = schema.newMessage();
		System.out.println("read " + refs.size() + " " + value.getClass());
		refs.add(value);
		super.mergeGroup(value, schema);
		return value;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T mergeObject(T value, Schema<T> schema) throws IOException {
		if (wasRef)
			return (T) refs.get(readInt32());
		if (value == null)
			value = schema.newMessage();
		refs.add(value);
		super.mergeObject(value, schema);
		return value;
	}
}
