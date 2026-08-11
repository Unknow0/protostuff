package io.protostuff.core;

import java.io.IOException;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

import io.protostuff.api.Schema;
import io.protostuff.api.WireFormat;

public class GraphOutput extends ProtobufOutput {
	private final Map<Object, Integer> identity;

	public GraphOutput() {
		this.identity = new IdentityHashMap<>();
	}

	@Override
	public <T> void writeMessage(int tag, String name, T t, Schema<T> schema) throws IOException {
		writeGroup((tag & ~0x7) | WireFormat.WIRETYPE_START_GROUP, name, t, schema);
	}

	@Override
	public <T> void writeMessageList(int tag, String name, Collection<T> values, Schema<T> schema) throws IOException {
		tag = (tag & ~0x7) | WireFormat.WIRETYPE_START_GROUP;
		for (T o : values)
			writeGroup(tag, name, o, schema);
	}

	@Override
	public <T> void writeGroup(int tag, String name, T t, Schema<T> schema) throws IOException {
		Integer i = identity.get(t);
		if (i == null) {
			System.out.println("write " + identity.size() + " " + t.getClass());
			identity.put(t, identity.size());
			super.writeGroup(tag, name, t, schema);
		} else {
			System.out.println("write ref " + i);
			writeVarInt32((tag & ~0x7) | WireFormat.WIRETYPE_REFERENCE);
			writeVarInt32(i);
		}
	}

	@Override
	public <T> void writeGroupList(int tag, String name, Collection<T> values, Schema<T> schema) throws IOException {
		for (T o : values)
			writeGroup(tag, name, o, schema);
	}
}
