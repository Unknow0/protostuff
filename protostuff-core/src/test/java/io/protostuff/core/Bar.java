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
import java.util.Arrays;

import io.protostuff.api.Input;
import io.protostuff.api.Output;
import io.protostuff.api.Schema;
import io.protostuff.api.SchemaEnum;
import io.protostuff.api.WireFormat;

/**
 * Bar - for testing
 *
 * @author David Yu
 * @created Nov 10, 2009
 */
public final class Bar implements Schema<Bar>, Externalizable {

	static final Bar DEFAULT_INSTANCE = new Bar();

	public static Bar getSchema() {
		return DEFAULT_INSTANCE;
	}

	private static final int SOMEINT_TAG = 1 << 3 | WireFormat.WIRETYPE_VARINT;
	private static final int SOMEINT_PACK = 1 << 3 | WireFormat.WIRETYPE_LENGTH_DELIMITED;
	private static final int SOMESTRING_TAG = 2 << 3 | WireFormat.WIRETYPE_LENGTH_DELIMITED;
	private static final int SOMEBAZ_TAG = 3 << 3 | WireFormat.WIRETYPE_LENGTH_DELIMITED;
	private static final int SOMEBAZ_GROUP = 3 << 3 | WireFormat.WIRETYPE_START_GROUP;
	private static final int SOMEENUM_TAG = 4 << 3 | WireFormat.WIRETYPE_VARINT;
	private static final int SOMEENUM_PACK = 4 << 3 | WireFormat.WIRETYPE_LENGTH_DELIMITED;
	private static final int SOMEBYTES_TAG = 5 << 3 | WireFormat.WIRETYPE_LENGTH_DELIMITED;
	private static final int SOMEBOOLEAN_TAG = 6 << 3 | WireFormat.WIRETYPE_VARINT;
	private static final int SOMEBOOLEAN_PACK = 6 << 3 | WireFormat.WIRETYPE_LENGTH_DELIMITED;
	private static final int SOMEFLOAT_TAG = 7 << 3 | WireFormat.WIRETYPE_FIXED32;
	private static final int SOMEFLOAT_PACK = 7 << 3 | WireFormat.WIRETYPE_LENGTH_DELIMITED;
	private static final int SOMEDOUBLE_TAG = 8 << 3 | WireFormat.WIRETYPE_FIXED64;
	private static final int SOMEDOUBLE_PACK = 8 << 3 | WireFormat.WIRETYPE_LENGTH_DELIMITED;
	private static final int SOMELONG_TAG = 9 << 3 | WireFormat.WIRETYPE_VARINT;
	private static final int SOMELONG_PACK = 9 << 3 | WireFormat.WIRETYPE_LENGTH_DELIMITED;

	public enum Status {
		PENDING, STARTED, COMPLETED;

		public static SchemaEnum<Status> getSchema() {
			return SCHEMA;
		}

		private static final SchemaEnum<Status> SCHEMA = new SchemaEnum<>() {
			@Override
			public int toNumber(Status e) {
				switch (e) {
					case PENDING:
						return 0;
					case STARTED:
						return 1;
					case COMPLETED:
						return 2;
					default:
						return 0;
				}
			}

			@Override
			public Status fromNumber(int number) {
				switch (number) {
					case 0:
						return PENDING;
					case 1:
						return STARTED;
					case 2:
						return COMPLETED;
					default:
						return null;
				}
			}

			@Override
			public String toName(Status e) {
				switch (e) {
					case PENDING:
						return "pending";
					case STARTED:
						return "started";
					case COMPLETED:
						return "completed";
					default:
						return "unknown";
				}
			}

			@Override
			public Status fromName(String name) {
				switch (name) {
					case "pending":
						return PENDING;
					case "started":
						return STARTED;
					case "completed":
						return COMPLETED;
					default:
						return null;
				}
			}
		};
	}

	private int someInt;
	private String someString;
	private Baz someBaz;
	private Status someEnum;
	private byte[] someBytes;
	private boolean someBoolean;
	private float someFloat;
	private double someDouble;
	private long someLong;

	public Bar() {

	}

	public Bar(int someInt, String someString, Baz baz, Status someEnum, byte[] someBytes, boolean someBoolean, float someFloat, double someDouble, long someLong) {
		this.someInt = someInt;
		this.someString = someString;
		this.someBaz = baz;
		this.someEnum = someEnum;
		this.someBytes = someBytes;
		this.someBoolean = someBoolean;
		this.someFloat = someFloat;
		this.someDouble = someDouble;
		this.someLong = someLong;
	}

	/**
	 * @return the someInt
	 */
	public int getSomeInt() {
		return someInt;
	}

	/**
	 * @param someInt
	 *            the someInt to set
	 */
	public void setSomeInt(int someInt) {
		this.someInt = someInt;
	}

	/**
	 * @return the someString
	 */
	public String getSomeString() {
		return someString;
	}

	/**
	 * @param someString
	 *            the someString to set
	 */
	public void setSomeString(String someString) {
		this.someString = someString;
	}

	/**
	 * @return the someBaz
	 */
	public Baz getSomeBaz() {
		return someBaz;
	}

	/**
	 * @param baz
	 *            the someBaz to set
	 */
	public void setSomeBaz(Baz baz) {
		this.someBaz = baz;
	}

	/**
	 * @return the someEnum
	 */
	public Status getSomeEnum() {
		return someEnum;
	}

	/**
	 * @param someEnum
	 *            the someEnum to set
	 */
	public void setSomeEnum(Status someEnum) {
		this.someEnum = someEnum;
	}

	/**
	 * @return the someBytes
	 */
	public byte[] getSomeBytes() {
		return someBytes;
	}

	/**
	 * @param someBytes
	 *            the someBytes to set
	 */
	public void setSomeBytes(byte[] someBytes) {
		this.someBytes = someBytes;
	}

	/**
	 * @return the someBoolean
	 */
	public boolean getSomeBoolean() {
		return someBoolean;
	}

	/**
	 * @param someBoolean
	 *            the someBoolean to set
	 */
	public void setSomeBoolean(boolean someBoolean) {
		this.someBoolean = someBoolean;
	}

	/**
	 * @return the someFloat
	 */
	public float getSomeFloat() {
		return someFloat;
	}

	/**
	 * @param someFloat
	 *            the someFloat to set
	 */
	public void setSomeFloat(float someFloat) {
		this.someFloat = someFloat;
	}

	/**
	 * @return the someDouble
	 */
	public double getSomeDouble() {
		return someDouble;
	}

	/**
	 * @param someDouble
	 *            the someDouble to set
	 */
	public void setSomeDouble(double someDouble) {
		this.someDouble = someDouble;
	}

	/**
	 * @return the someLong
	 */
	public long getSomeLong() {
		return someLong;
	}

	/**
	 * @param someLong
	 *            the someLong to set
	 */
	public void setSomeLong(long someLong) {
		this.someLong = someLong;
	}

	@Override
	public Bar newMessage() {
		return new Bar();
	}

	@Override
	public int tag(String name) {
		switch (name) {
			case "someInt":
				return SOMEINT_TAG;
			case "someString":
				return SOMESTRING_TAG;
			case "someBaz":
				return SOMEBAZ_TAG;
			case "someEnum":
				return SOMEENUM_TAG;
			case "someBytes":
				return SOMEBYTES_TAG;
			case "someBoolean":
				return SOMEBOOLEAN_TAG;
			case "someFloat":
				return SOMEFLOAT_TAG;
			case "someDouble":
				return SOMEDOUBLE_TAG;
			case "someLong":
				return SOMELONG_TAG;
			default:
				return 0;
		}
	}

//	@Override
//	public void readExternal(ObjectInput in) throws IOException {
//		GraphIOUtil.mergeDelimitedFrom(in, this, this);
//	}
//
//	@Override
//	public void writeExternal(ObjectOutput out) throws IOException {
//		GraphIOUtil.writeDelimitedTo(out, this, this);
//	}

	@Override
	public void writeTo(Output output, Bar message) throws IOException {
		if (message.someInt != 0) {
			output.writeUInt32(SOMEINT_TAG, "someInt", message.someInt);
		}

		if (message.someString != null) {
			output.writeString(SOMESTRING_TAG, "someString", message.someString);
		}

		if (message.someBaz != null) {
			output.writeMessage(SOMEBAZ_TAG, "someBaz", message.someBaz, Baz.getSchema());
		}

		if (message.someEnum != null) {
			output.writeEnum(SOMEENUM_TAG, "someEnum", message.someEnum, Status.getSchema());
		}

		if (message.someBytes != null) {
			output.writeBytes(SOMEBYTES_TAG, "someBytes", message.someBytes);
		}

		if (message.someBoolean) {
			output.writeBool(SOMEBOOLEAN_TAG, "someBoolean", message.someBoolean);
		}

		if (message.someFloat != 0f) {
			output.writeFloat(SOMEFLOAT_TAG, "someFloat", message.someFloat);
		}

		if (message.someDouble != 0d) {
			output.writeDouble(SOMEDOUBLE_TAG, "someDouble", message.someDouble);
		}

		if (message.someLong != 0l) {
			output.writeUInt64(SOMELONG_TAG, "someLong", message.someLong);
		}
	}

	@Override
	public void mergeFrom(Input input, Bar message) throws IOException {
		while (true) {
			int tag = input.readTag();
			switch (tag) {
				case 0:
					return;
				case SOMEINT_TAG:
				case SOMEINT_PACK:
					message.someInt = input.readUInt32();
					break;
				case SOMESTRING_TAG:
					message.someString = input.readString();
					break;
				case SOMEBAZ_TAG:
					message.someBaz = input.mergeObject(message.someBaz, Baz.getSchema());
					break;
				case SOMEBAZ_GROUP:
					message.someBaz = input.mergeGroup(message.someBaz, Baz.getSchema());
					break;
				case SOMEENUM_TAG:
				case SOMEENUM_PACK:
					message.someEnum = input.readEnum(Status.getSchema());
					break;
				case SOMEBYTES_TAG:
					message.someBytes = input.readBytes();
					break;
				case SOMEBOOLEAN_TAG:
				case SOMEBOOLEAN_PACK:
					message.someBoolean = input.readBool();
					break;
				case SOMEFLOAT_TAG:
				case SOMEFLOAT_PACK:
					message.someFloat = input.readFloat();
					break;
				case SOMEDOUBLE_TAG:
				case SOMEDOUBLE_PACK:
					message.someDouble = input.readDouble();
					break;
				case SOMELONG_TAG:
				case SOMELONG_PACK:
					message.someLong = input.readUInt64();
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
		result = prime * result + ((someBaz == null) ? 0 : someBaz.hashCode());
		result = prime * result + (someBoolean ? 1231 : 1237);
		result = prime * result + ((someBytes == null) ? 0 : someBytes.hashCode());
		long temp;
		temp = Double.doubleToLongBits(someDouble);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((someEnum == null) ? 0 : someEnum.hashCode());
		result = prime * result + Float.floatToIntBits(someFloat);
		result = prime * result + someInt;
		result = prime * result + (int) (someLong ^ (someLong >>> 32));
		result = prime * result + ((someString == null) ? 0 : someString.hashCode());
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
		Bar other = (Bar) obj;
		if (someBaz == null) {
			if (other.someBaz != null) {
				return false;
			}
		} else if (!someBaz.equals(other.someBaz)) {
			return false;
		}
		if (someBoolean != other.someBoolean) {
			return false;
		}
		if (someBytes == null) {
			if (other.someBytes != null) {
				return false;
			}
		} else if (!Arrays.equals(someBytes, other.someBytes)) {
			return false;
		}
		if (Double.doubleToLongBits(someDouble) != Double.doubleToLongBits(other.someDouble)) {
			return false;
		}
		if (someEnum == null) {
			if (other.someEnum != null) {
				return false;
			}
		} else if (!someEnum.equals(other.someEnum)) {
			return false;
		}
		if (Float.floatToIntBits(someFloat) != Float.floatToIntBits(other.someFloat)) {
			return false;
		}
		if (someInt != other.someInt) {
			return false;
		}
		if (someLong != other.someLong) {
			return false;
		}
		if (someString == null) {
			if (other.someString != null) {
				return false;
			}
		} else if (!someString.equals(other.someString)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Bar [someBaz=" + someBaz + ", someBoolean=" + someBoolean + ", someBytes=" + someBytes + ", someDouble=" + someDouble + ", someEnum=" + someEnum
				+ ", someFloat=" + someFloat + ", someInt=" + someInt + ", someLong=" + someLong + ", someString=" + someString + "]";
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
