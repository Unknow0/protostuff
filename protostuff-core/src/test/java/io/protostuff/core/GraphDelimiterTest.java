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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.protostuff.api.Schema;

/**
 * Delimiter tests for the graph format
 *
 * @author David Yu
 * @created Aug 29, 2012
 */
public class GraphDelimiterTest extends DelimiterTest {

	@Override
	protected <T> void writeDelimitedTo(OutputStream out, T message, Schema<T> schema) throws IOException {
		try (DataOutputStream dos = new DataOutputStream(out)) {
			GraphIOUtil.writeDelimitedTo(dos, message, schema);
		}
	}

	@Override
	protected <T> void mergeDelimitedFrom(InputStream in, T message, Schema<T> schema) throws IOException {
		try (DataInputStream dis = new DataInputStream(in)) {
			GraphIOUtil.mergeDelimitedFrom(dis, message, schema);
		}
	}

}
