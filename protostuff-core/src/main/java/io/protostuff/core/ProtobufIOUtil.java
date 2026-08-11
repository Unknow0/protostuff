//========================================================================
//Copyright 2007-2010 David Yu dyuproject@gmail.com
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package io.protostuff.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import io.protostuff.api.LinkedBuffer;
import io.protostuff.api.Schema;

/**
 * Protobuf ser/deser util for messages/objects.
 *
 * @author David Yu
 * @created Oct 5, 2010
 */
public final class ProtobufIOUtil {

	private ProtobufIOUtil() {
	}

	/**
	 * Merges the {@code message} with the byte array using the given {@code schema}.
	 */
	public static <T> void mergeFrom(byte[] data, T message, Schema<T> schema) {
		mergeFrom(data, 0, data.length, message, schema);
	}

	/**
	 * Merges the {@code message} with the byte array using the given {@code schema}.
	 */
	public static <T> void mergeFrom(byte[] data, int offset, int length, T message, Schema<T> schema) {
		try {
			ProtobufArrayInput input = new ProtobufArrayInput(data, offset, length);
			schema.mergeFrom(input, message);
			input.checkLastTagWas(0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Merges the {@code message} from the {@link InputStream} using the given {@code schema}.
	 */
	public static <T> void mergeFrom(InputStream in, T message, Schema<T> schema) throws IOException {
		ProtobufStreamInput input = new ProtobufStreamInput(in);
		schema.mergeFrom(input, message);
		input.checkLastTagWas(0);
	}

	/**
	 * Merges the {@code message} (delimited) from the {@link InputStream} using the given {@code schema}.
	 *
	 * @return the size of the message
	 */
	public static <T> T mergeDelimitedFrom(InputStream in, T message, Schema<T> schema) throws IOException {
		ProtobufStreamInput input = new ProtobufStreamInput(in);
		return input.mergeObject(message, schema);
	}

	/**
	 * Serializes the {@code message} into a byte array using the given schema.
	 *
	 * @return the byte array containing the data.
	 */
	public static <T> byte[] toByteArray(T message, Schema<T> schema, LinkedBuffer buffer) {
		if (buffer.start != buffer.offset) {
			throw new IllegalArgumentException("Buffer previously used and had not been reset.");
		}

		final ProtobufOutput output = new ProtobufOutput(buffer);
		try {
			schema.writeTo(output, message);
		} catch (IOException e) {
			throw new RuntimeException("Serializing to a byte array threw an IOException " + "(should never happen).", e);
		}

		return output.toByteArray();
	}

	/**
	 * Writes the {@code message} into the {@link LinkedBuffer} using the given schema.
	 *
	 * @return the size of the message
	 */
	public static <T> int writeTo(LinkedBuffer buffer, T message, Schema<T> schema) {
		if (buffer.start != buffer.offset) {
			throw new IllegalArgumentException("Buffer previously used and had not been reset.");
		}

		final ProtobufOutput output = new ProtobufOutput(buffer);
		try {
			schema.writeTo(output, message);
		} catch (IOException e) {
			throw new RuntimeException("Serializing to a LinkedBuffer threw an IOException " + "(should never happen).", e);
		}

		return output.size();
	}

	/**
	 * Serializes the {@code message} into an {@link OutputStream} using the given schema.
	 *
	 * @return the size of the message
	 */
	public static <T> int writeTo(OutputStream out, T message, Schema<T> schema, LinkedBuffer buffer) throws IOException {
		if (buffer.start != buffer.offset) {
			throw new IllegalArgumentException("Buffer previously used and had not been reset.");
		}

		final ProtobufOutput output = new ProtobufOutput(buffer);
		schema.writeTo(output, message);
		return LinkedBuffer.writeTo(out, buffer);
	}

	/**
	 * Serializes the {@code message}, prefixed with its length, into an {@link OutputStream}.
	 *
	 * @return the size of the message
	 */
	public static <T> int writeDelimitedTo(OutputStream out, T message, Schema<T> schema, LinkedBuffer buffer) throws IOException {
		if (buffer.start != buffer.offset) {
			throw new IllegalArgumentException("Buffer previously used and had not been reset.");
		}

		// leave space for size
		final int o = buffer.offset += 5;
		final ProtobufOutput output = new ProtobufOutput(buffer);
		schema.writeTo(output, message);
		final int size = output.size();
		int i = o - ProtobufOutput.computeRawVarint32Size(size);
		ProtobufOutput.writeVarInt32(size, buffer.buffer, i);

		out.write(buffer.buffer, i, buffer.offset - i);
		// flush remaining
		if (buffer.next != null)
			LinkedBuffer.writeTo(out, buffer.next);

		return size;
	}

	public static void writeVarInt(OutputStream out, int value) throws IOException {
		while ((value & ~0x7F) != 0) {
			out.write((value & 0x7F) | 0x80);
			value >>>= 7;
		}
		out.write(value);
	}

	/**
	 * Serializes the {@code messages} (delimited) into an {@link OutputStream} using the given schema.
	 *
	 * @return the total size of the messages (excluding the length prefix varint)
	 */
	public static <T> int writeListTo(OutputStream out, List<T> messages, Schema<T> schema, LinkedBuffer buffer) throws IOException {
		if (buffer.start != buffer.offset) {
			throw new IllegalArgumentException("Buffer previously used and had not been reset.");
		}

		final ProtobufOutput output = new ProtobufOutput(buffer);
		int totalSize = 0;
		for (T m : messages) {
			schema.writeTo(output, m);
			final int size = output.size();
			writeVarInt(out, size);
			final int msgSize = LinkedBuffer.writeTo(out, buffer);

			assert size == msgSize;

			totalSize += size;
			output.reset();
		}
		return totalSize;
	}

	/**
	 * Parses the {@code messages} (delimited) from the {@link InputStream} using the given {@code schema}.
	 *
	 * @return the list containing the messages.
	 */
	public static <T> List<T> parseListFrom(InputStream in, Schema<T> schema) throws IOException {
		ProtobufStreamInput input = new ProtobufStreamInput(in);
		List<T> list = new ArrayList<>();
		while (!input.isAtEnd())
			list.add(input.mergeObject(null, schema));
		return list;
	}
}
