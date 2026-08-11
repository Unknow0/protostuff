//========================================================================
//Copyright 2007-2009 David Yu dyuproject@gmail.com
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
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import io.protostuff.api.Input;
import io.protostuff.api.Output;
import io.protostuff.api.Schema;
import io.protostuff.api.WireFormat;

/**
 * Ser/deser test object that wraps an object {@link HasBar} without any schema.
 *
 * @author David Yu
 * @created Nov 13, 2009
 */
public final class HasHasBar implements Schema<HasHasBar>, Externalizable {

	private static final int NAME_TAG = 1 << 3 | WireFormat.WIRETYPE_LENGTH_DELIMITED;
	private static final int HASBAR_TAG = 2 << 3 | WireFormat.WIRETYPE_LENGTH_DELIMITED;

	private static final HasHasBar SCHEMA = new HasHasBar();

	public static Schema<HasHasBar> getSchema() {
		return SCHEMA;
	}

	private String name;
	private HasBar hasBar;

	public HasHasBar() {

	}

	public HasHasBar(String name, HasBar hasBar) {
		this.name = name;
		this.hasBar = hasBar;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the hasBar
	 */
	public HasBar getHasBar() {
		return hasBar;
	}

	/**
	 * @param hasBar
	 *            the hasBar to set
	 */
	public void setHasBar(HasBar hasBar) {
		this.hasBar = hasBar;
	}

	@Override
	public int tag(String name) {
		switch (name) {
			case "name":
				return NAME_TAG;
			case "hasBar":
				return HASBAR_TAG;
			default:
				return 0;
		}
	}

	@Override
	public HasHasBar newMessage() {
		return new HasHasBar();
	}

	@Override
	public void mergeFrom(Input input, HasHasBar message) throws IOException {
		while (true) {
			int tag = input.readTag();
			switch (tag) {
				case 0:
					return;
				case NAME_TAG:
					message.name = input.readString();
					break;
				case HASBAR_TAG:
					message.hasBar = readHasBar(input);
					break;
				default:
					input.skipField();
			}
		}
	}

	@Override
	public void writeTo(Output output, HasHasBar message) throws IOException {
		if (message.name != null) {
			output.writeString(NAME_TAG, "name", message.name);
		}
		writeHasBar(output, message.hasBar);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		GraphIOUtil.writeDelimitedTo(out, this, getSchema());
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		GraphIOUtil.mergeDelimitedFrom(in, this, getSchema());
	}

	static HasBar readHasBar(Input input) throws IOException {
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(input.readBytes()));
		try {
			return (HasBar) ois.readObject();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			ois.close();
		}
	}

	static void writeHasBar(Output output, HasBar hasBar) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		try {
			oos.writeObject(hasBar);
			output.writeBytes(HASBAR_TAG, "hasBar", baos.toByteArray());
		} finally {
			oos.close();
		}
	}
}
