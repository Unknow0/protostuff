package io.protostuff.api;

import java.io.IOException;
import java.util.Collection;

public interface Output {
	void flush() throws IOException;

	int close() throws IOException;

	void writeUInt32(int tag, String name, int value) throws IOException;

	void writeSInt32(int tag, String name, int value) throws IOException;

	void writeFixed32(int tag, String name, int value) throws IOException;

	void writeUInt64(int tag, String name, long value) throws IOException;

	void writeSInt64(int tag, String name, long value) throws IOException;

	void writeFixed64(int tag, String name, long value) throws IOException;

	void writeFloat(int tag, String name, float value) throws IOException;

	void writeDouble(int tag, String name, double value) throws IOException;

	void writeBool(int tag, String name, boolean value) throws IOException;

	<E extends Enum<E>> void writeEnum(int tag, String name, E e, SchemaEnum<E> schema) throws IOException;

	void writeBytes(int tag, String name, byte[] value) throws IOException;

	<T> void writeGroup(int tag, String name, T t, Schema<T> schema) throws IOException;

	<T> void writeMessage(int tag, String name, T t, Schema<T> schema) throws IOException;

	void writeString(int tag, String name, CharSequence value) throws IOException;

	void writeUInt32List(int tag, String name, Collection<Integer> values) throws IOException;

	void writeSInt32List(int tag, String name, Collection<Integer> values) throws IOException;

	void writeFixed32List(int tag, String name, Collection<Integer> values) throws IOException;

	void writeUInt64List(int tag, String name, Collection<Long> values) throws IOException;

	void writeSInt64List(int tag, String name, Collection<Long> values) throws IOException;

	void writeFixed64List(int tag, String name, Collection<Long> values) throws IOException;

	void writeFloatList(int tag, String name, Collection<Float> values) throws IOException;

	void writeDoubleList(int tag, String name, Collection<Double> values) throws IOException;

	void writeBoolList(int tag, String name, Collection<Boolean> values) throws IOException;

	<E extends Enum<E>> void writeEnumList(int tag, String name, Collection<E> values, SchemaEnum<E> schema) throws IOException;

	void writeBytesList(int tag, String name, Collection<byte[]> value) throws IOException;

	<T> void writeGroupList(int tag, String name, Collection<T> values, Schema<T> schema) throws IOException;

	<T> void writeMessageList(int tag, String name, Collection<T> values, Schema<T> schema) throws IOException;

	void writeStringList(int tag, String name, Collection<? extends CharSequence> values) throws IOException;
}
