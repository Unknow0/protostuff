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

import io.protostuff.api.WireFormat;

/**
 * Tests for {@link ProtobufStreamInput}.
 *
 * @author Max Lanin
 * @created Dec 22, 2012
 */
public class ProtobufInputTest extends AbstractTest {

	public void testSkipFieldOverTheBufferBoundary() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int tag = WireFormat.makeTag(1, WireFormat.WIRETYPE_LENGTH_DELIMITED);
		int anotherTag = WireFormat.makeTag(2, WireFormat.WIRETYPE_LENGTH_DELIMITED);
		int msgLength = 10;

		ProtobufIOUtil.writeVarInt(out, tag);
		ProtobufIOUtil.writeVarInt(out, msgLength);
		for (int i = 1; i <= msgLength; i++) {
			ProtobufIOUtil.writeVarInt(out, i);
		}
		ProtobufIOUtil.writeVarInt(out, anotherTag);

		byte[] data = out.toByteArray();

		ProtobufStreamInput ci = new ProtobufStreamInput(new ByteArrayInputStream(data));
		ci.pushLimit(msgLength + 2); // +2 for tag and length
		assertEquals(tag, ci.readTag());
		ci.skipField(tag);
		assertEquals(0, ci.readTag());
	}

}
