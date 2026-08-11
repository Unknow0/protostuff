package io.protostuff.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import io.protostuff.api.Schema;

public class GraphIOUtil {

	/**
	 * write delimited
	 * @param <T> message type
	 * @param out output to write to
	 * @param message message to write
	 * @param schema the schema
	 * @throws IOException in case of error
	 */
	public static <T> void writeDelimitedTo(DataOutput out, T message, Schema<T> schema) throws IOException {
		GraphOutput output = new GraphOutput();
		schema.writeTo(output, message);
		out.writeInt(output.size());
		output.writeTo(out);
	}

	/**
	 * read delimited
	 * @param <T> message type
	 * @param in input to read from
	 * @param message the message to fill
	 * @param schema the schema
	 * @throws IOException in case of error
	 */
	public static <T> void mergeDelimitedFrom(DataInput in, T message, Schema<T> schema) throws IOException {
		int size = in.readInt();
		byte[] b = new byte[size];
		in.readFully(b);
		schema.mergeFrom(new GraphInput(b), message);
	}

	/**
	 * to bytes
	 * @param <T> message type
	 * @param message the message
	 * @param schema the schema
	 * @return the message as bytes
	 * @throws IOException in case of error
	 */
	public static <T> byte[] toByteArray(T message, Schema<T> schema) throws IOException {
		GraphOutput output = new GraphOutput();
		schema.writeTo(output, message);
		return output.toByteArray();
	}

	/**
	 * read a message
	 * @param <T> message type
	 * @param data data to read
	 * @param message the message
	 * @param schema the schema
	 * @throws IOException in case of error
	 */
	public static <T> void mergeFrom(byte[] data, T message, Schema<T> schema) throws IOException {
		schema.mergeFrom(new GraphInput(data, 0, data.length), message);
	}

	/**
	 * read a message
	 * @param <T> message type
	 * @param data data to read
	 * @param offset data offset
	 * @param length data length
	 * @param message the message
	 * @param schema the schema
	 * @throws IOException in case of error
	 */
	public static <T> void mergeFrom(byte[] data, int offset, int length, T message, Schema<T> schema) throws IOException {
		schema.mergeFrom(new GraphInput(data, offset, length), message);
	}
}
