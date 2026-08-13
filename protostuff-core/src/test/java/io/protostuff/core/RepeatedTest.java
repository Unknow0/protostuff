package io.protostuff.core;

import java.io.IOException;

import io.protostuff.api.LinkedBuffer;

/**
 */
public class RepeatedTest extends AbstractTest {
	public void testPackedRepeatedByteArray() throws IOException {
		final LinkedBuffer buffer = getProtobufBuffer();

		PojoWithRepeated test = new PojoWithRepeated();
		test.mergeFrom(new ProtobufArrayInput(buffer.buffer, 0, buffer.offset), test);

		verify(test);
	}

	private LinkedBuffer getProtobufBuffer() throws IOException {
		// Generate protobuf with packed repeated fields
		final LinkedBuffer buffer = new LinkedBuffer(LinkedBuffer.DEFAULT_BUFFER_SIZE);
		final ProtobufOutput output = new ProtobufOutput(buffer);
		// 03 // first element (varint 3)
		// 8E 02 // second element (varint 270)
		output.writeBytes(PojoWithRepeated.SOMEINT32_PACK, "someInt32", new byte[] { (byte) 0x03, (byte) 0x8E, (byte) 0x02 });
		// Interleave
		output.writeFixed64(PojoWithRepeated.SOMEFIXED64_TAG, "someFixed64", 8);
		// Non packed
		output.writeUInt32(PojoWithRepeated.SOMEINT32_TAG, "someInt32", 1234);
		// Interleave
		output.writeBytes(PojoWithRepeated.SOMEFIXED64_PACK, "someFixed64", new byte[] { 9, 0, 0, 0, 0, 0, 0, 0 });
		// 9E A7 05 // third element (varint 86942)
		output.writeBytes(PojoWithRepeated.SOMEINT32_PACK, "someInt32", new byte[] { (byte) 0x9E, (byte) 0xA7, (byte) 0x05 });

		return buffer;
	}

	private void verify(final PojoWithRepeated test) {
		assertEquals(4, test.getSomeInt32Count());
		assertEquals((Integer) 3, test.getSomeInt32(0));
		assertEquals((Integer) 270, test.getSomeInt32(1));
		assertEquals((Integer) 1234, test.getSomeInt32(2));
		assertEquals((Integer) 86942, test.getSomeInt32(3));

		assertEquals(2, test.getSomeFixed64Count());
		assertEquals((Long) 8L, test.getSomeFixed64(0));
		assertEquals((Long) 9L, test.getSomeFixed64(1));
	}
}
