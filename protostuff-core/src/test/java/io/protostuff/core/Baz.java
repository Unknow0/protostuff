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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import io.protostuff.api.Input;
import io.protostuff.api.Output;
import io.protostuff.api.Schema;
import io.protostuff.api.WireFormat;

/**
 * Baz - for testing
 *
 * @author David Yu
 * @created Nov 10, 2009
 */
public final class Baz implements Schema<Baz>, Externalizable {

	static final Baz DEFAULT_INSTANCE = new Baz();

	public static Schema<Baz> getSchema() {
		return DEFAULT_INSTANCE;
	}

	private static final int ID_TAG = 1 << 3 | WireFormat.WIRETYPE_VARINT;
	private static final int ID_PACK = 1 << 3 | WireFormat.WIRETYPE_LENGTH_DELIMITED;
	private static final int NAME_TAG = 2 << 3 | WireFormat.WIRETYPE_LENGTH_DELIMITED;
	private static final int TIMESTAMP_TAG = 3 << 3 | WireFormat.WIRETYPE_VARINT;
	private static final int TIMESTAMP_PACK = 3 << 3 | WireFormat.WIRETYPE_LENGTH_DELIMITED;

	private int id;
	private String name;
	private long timestamp;

	public Baz() {

	}

	public Baz(int id, String name, long timestamp) {
		this.id = id;
		this.name = name;
		this.timestamp = timestamp;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
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
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp
	 *            the timestamp to set
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public Baz newMessage() {
		return new Baz();
	}

	@Override
	public void writeTo(Output output, Baz message) throws IOException {
		if (message.id != 0) {
			output.writeUInt32(ID_TAG, "id", message.id);
		}

		if (message.name != null) {
			output.writeString(NAME_TAG, "name", message.name);
		}

		if (message.timestamp != 0l) {
			output.writeUInt64(TIMESTAMP_TAG, "timestamp", message.timestamp);
		}
	}

	@Override
	public void mergeFrom(Input input, Baz message) throws IOException {
		while (true) {
			int tag = input.readTag();
			switch (tag) {
				case 0:
					return;
				case ID_TAG:
				case ID_PACK:
					message.id = input.readInt32();
					break;
				case NAME_TAG:
					message.name = input.readString();
					break;
				case TIMESTAMP_TAG:
				case TIMESTAMP_PACK:
					message.timestamp = input.readUInt64();
					break;
				default:
					input.skipField();
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (getClass() != obj.getClass())) {
			return false;
		}
		Baz other = (Baz) obj;
		if (id != other.id) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (timestamp != other.timestamp) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Baz [id=" + id + ", name=" + name + ", timestamp=" + timestamp + "]";
	}

	@Override
	public int tag(String name) {
		switch (name) {
			case "id":
				return ID_TAG;
			case "name":
				return NAME_TAG;
			case "timestamp":
				return TIMESTAMP_TAG;
			default:
				return 0;
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		GraphIOUtil.writeDelimitedTo(out, this, getSchema());
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		GraphIOUtil.mergeDelimitedFrom(in, this, getSchema());
	}
}
