//========================================================================
//Copyright 2012 David Yu
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import io.protostuff.api.LinkedBuffer;
import io.protostuff.api.Schema;
import io.protostuff.core.Foo.EnumSample;

/**
 * Test writing/reading to/from streams using writeDelimitedTo, mergeDelimitedFrom, optWriteDelimitedTo and
 * optMergeDelimitedFrom.
 *
 * @author David Yu
 * @created Aug 29, 2012
 */
public abstract class DelimiterTest extends AbstractTest {

	/**
	 * Serializes the {@code message} (delimited) into an {@link OutputStream} via {@link DeferredOutput} using the
	 * given schema.
	 */
	protected abstract <T> void writeDelimitedTo(OutputStream out, T message, Schema<T> schema) throws IOException;

	/**
	 * Deserializes from the byte array and data is merged/saved to the message.
	 */
	protected abstract <T> void mergeDelimitedFrom(InputStream in, T message, Schema<T> schema) throws IOException;

	@SuppressWarnings("unused")
	<T> void verifyOptData(byte[] optData, T message, Schema<T> schema, LinkedBuffer buffer) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		writeDelimitedTo(out, message, schema);
		byte[] data = out.toByteArray();

		// compare both outputs
		assertEquals(optData.length, data.length);

		/*
		 * StringBuilder s1 = new StringBuilder(); for(int b : optData) s1.append(b).append(' ');
		 *
		 * StringBuilder s2 = new StringBuilder(); for(int b : data) s2.append(b).append(' ');
		 *
		 * assertEquals(s1.unsignedIntToString(), s2.unsignedIntToString());
		 */

		assertTrue(Arrays.equals(optData, data));
	}

	<T> void doTest(T message, Schema<T> schema) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		writeDelimitedTo(out, message, schema);
		byte[] data = out.toByteArray();

		ByteArrayInputStream in = new ByteArrayInputStream(data);
		T parsedMessage = schema.newMessage();
		mergeDelimitedFrom(in, parsedMessage, schema);

		assertEquals(message, parsedMessage);
	}

	public void testFoo() throws Exception {
		doTest(SerializableObjects.foo, Foo.getSchema());
	}

	public void testFooEmpty() throws Exception {
		doTest(new Foo(), Foo.getSchema());
	}

	public void testFooTooLarge() throws Exception {
		Foo message = SerializableObjects.newFoo(new Integer[] { 90210, -90210, 0, 128 }, new String[] { "ab", "cd" },
				new Bar[] { SerializableObjects.bar, SerializableObjects.negativeBar }, new EnumSample[] { EnumSample.TYPE0, EnumSample.TYPE2 },
				new byte[][] { "ef".getBytes(), "gh".getBytes() }, new Boolean[] { true, false }, new Float[] { 1234.4321f, -1234.4321f, 0f },
				new Double[] { 12345678.87654321d, -12345678.87654321d, 0d }, new Long[] { 7060504030201l, -7060504030201l, 0l });

		doTest(message, Foo.getSchema());
	}

	public void testBar() throws Exception {
		doTest(SerializableObjects.bar, Bar.getSchema());
	}

	public void testBaz() throws Exception {
		doTest(SerializableObjects.baz, Baz.getSchema());
	}

	public void testBarTooLarge2() throws Exception {
		Bar message = new Bar();
		message.setSomeBytes(new byte[(1 << 14) - 1]);
		doTest(message, Bar.getSchema());
	}

	public void testBarTooLarge3() throws Exception {
		Bar message = new Bar();
		message.setSomeBytes(new byte[(1 << 21) - 1]);
		doTest(message, Bar.getSchema());
	}
}
