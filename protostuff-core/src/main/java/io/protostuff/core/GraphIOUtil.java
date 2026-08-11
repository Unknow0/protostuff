package io.protostuff.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import io.protostuff.api.Schema;

public class GraphIOUtil {

	public static <T> void writeDelimitedTo(DataOutput out, T message, Schema<T> schema) throws IOException {
		GraphOutput output = new GraphOutput();
		schema.writeTo(output, message);
		out.writeInt(output.size());
		output.writeTo(out);
	}

	public static <T> void mergeDelimitedFrom(DataInput in, T message, Schema<T> schema) throws IOException {
		int size = in.readInt();
		byte[] b = new byte[size];
		in.readFully(b);
		schema.mergeFrom(new GraphInput(b), message);
	}

	public static <T> byte[] toByteArray(T message, Schema<T> schema) throws IOException {
		GraphOutput output = new GraphOutput();
		schema.writeTo(output, message);
		return output.toByteArray();
	}

	public static <T> void mergeFrom(byte[] data, T message, Schema<T> schema) throws IOException {
		schema.mergeFrom(new GraphInput(data, 0, data.length), message);
	}

	public static <T> void mergeFrom(byte[] data, int offset, int length, T message, Schema<T> schema) throws IOException {
		schema.mergeFrom(new GraphInput(data, offset, length), message);
	}
}
