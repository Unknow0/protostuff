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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import io.protostuff.api.Input;
import io.protostuff.api.Output;
import io.protostuff.api.Schema;
import io.protostuff.api.SchemaEnum;
import io.protostuff.api.WireFormat;

/**
 * Foo - for testing
 *
 * @author David Yu
 * @created Nov 10, 2009
 */
public final class Foo implements Schema<Foo>, Externalizable {
	static final Foo DEFAULT_INSTANCE = new Foo();

	public static Foo getSchema() {
		return DEFAULT_INSTANCE;
	}

	private static final int SOMEINT_TAG = 1 << 3 | WireFormat.WIRETYPE_VARINT;
	private static final int SOMEINT_PACK = 1 << 3 | WireFormat.WIRETYPE_LENGTH_DELIMITED;
	private static final int SOMESTRING_TAG = 2 << 3 | WireFormat.WIRETYPE_LENGTH_DELIMITED;
	private static final int SOMEBAR_TAG = 3 << 3 | WireFormat.WIRETYPE_LENGTH_DELIMITED;
	private static final int SOMEBAR_GROUP = 3 << 3 | WireFormat.WIRETYPE_START_GROUP;
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

	public enum EnumSample {
		TYPE0, TYPE1, TYPE2, TYPE3, TYPE4;

		public static SchemaEnum<EnumSample> getSchema() {
			return SCHEMA;
		}

		private static final SchemaEnum<EnumSample> SCHEMA = new SchemaEnum<>() {
			@Override
			public int toNumber(EnumSample e) {
				switch (e) {
					case TYPE0:
						return 0;
					case TYPE1:
						return 1;
					case TYPE2:
						return 2;
					case TYPE3:
						return 3;
					case TYPE4:
						return 4;
					default:
						return 0;
				}
			}

			@Override
			public EnumSample fromNumber(int number) {
				switch (number) {
					case 0:
						return TYPE0;
					case 1:
						return TYPE1;
					case 2:
						return TYPE2;
					case 3:
						return TYPE3;
					case 4:
						return TYPE4;
					default:
						return null;
				}
			}

			@Override
			public String toName(EnumSample e) {
				switch (e) {
					case TYPE0:
						return "type0";
					case TYPE1:
						return "type1";
					case TYPE2:
						return "type2";
					case TYPE3:
						return "type3";
					case TYPE4:
						return "type4";
					default:
						return "unknown";
				}
			}

			@Override
			public EnumSample fromName(String name) {
				switch (name) {
					case "type0":
						return TYPE0;
					case "type1":
						return TYPE1;
					case "type2":
						return TYPE2;
					case "type3":
						return TYPE3;
					case "type4":
						return TYPE4;
					default:
						return null;
				}
			}
		};
	}

	private List<Integer> someInt;
	private List<String> someString;
	private List<Bar> someBar;
	private List<EnumSample> someEnum;
	private List<byte[]> someBytes;
	private List<Boolean> someBoolean;
	private List<Float> someFloat;
	private List<Double> someDouble;
	private List<Long> someLong;

	public Foo() {

	}

	public Foo(List<Integer> someInt, List<String> someString, List<Bar> someBar, List<EnumSample> someEnum, List<byte[]> someBytes, List<Boolean> someBoolean,
			List<Float> someFloat, List<Double> someDouble, List<Long> someLong) {
		this.someInt = someInt;
		this.someString = someString;
		this.someBar = someBar;
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
	public List<Integer> getSomeInt() {
		return someInt;
	}

	/**
	 * @param someInt
	 *            the someInt to set
	 */
	public void setSomeInt(List<Integer> someInt) {
		this.someInt = someInt;
	}

	/**
	 * @return the someString
	 */
	public List<String> getSomeString() {
		return someString;
	}

	/**
	 * @param someString
	 *            the someString to set
	 */
	public void setSomeString(List<String> someString) {
		this.someString = someString;
	}

	/**
	 * @return the someBar
	 */
	public List<Bar> getSomeBar() {
		return someBar;
	}

	/**
	 * @param someBar
	 *            the someBar to set
	 */
	public void setSomeBar(List<Bar> someBar) {
		this.someBar = someBar;
	}

	/**
	 * @return the someEnum
	 */
	public List<EnumSample> getSomeEnum() {
		return someEnum;
	}

	/**
	 * @param someEnum
	 *            the someEnum to set
	 */
	public void setSomeEnum(List<EnumSample> someEnum) {
		this.someEnum = someEnum;
	}

	/**
	 * @return the someBytes
	 */
	public List<byte[]> getSomeBytes() {
		return someBytes;
	}

	/**
	 * @param someBytes
	 *            the someBytes to set
	 */
	public void setSomeBytes(List<byte[]> someBytes) {
		this.someBytes = someBytes;
	}

	/**
	 * @return the someBoolean
	 */
	public List<Boolean> getSomeBoolean() {
		return someBoolean;
	}

	/**
	 * @param someBoolean
	 *            the someBoolean to set
	 */
	public void setSomeBoolean(List<Boolean> someBoolean) {
		this.someBoolean = someBoolean;
	}

	/**
	 * @return the someFloat
	 */
	public List<Float> getSomeFloat() {
		return someFloat;
	}

	/**
	 * @param someFloat
	 *            the someFloat to set
	 */
	public void setSomeFloat(List<Float> someFloat) {
		this.someFloat = someFloat;
	}

	/**
	 * @return the someDouble
	 */
	public List<Double> getSomeDouble() {
		return someDouble;
	}

	/**
	 * @param someDouble
	 *            the someDouble to set
	 */
	public void setSomeDouble(List<Double> someDouble) {
		this.someDouble = someDouble;
	}

	/**
	 * @return the someLong
	 */
	public List<Long> getSomeLong() {
		return someLong;
	}

	/**
	 * @param someLong
	 *            the someLong to set
	 */
	public void setSomeLong(List<Long> someLong) {
		this.someLong = someLong;
	}

	@Override
	public int tag(String name) {
		switch (name) {
			case "someInt":
				return SOMEINT_TAG;
			case "someString":
				return SOMESTRING_TAG;
			case "someBar":
				return SOMEBAR_TAG;
			case "someEnum":
				return SOMEENUM_TAG;
			case "someBytes":
				return SOMEBAR_TAG;
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

	@Override
	public void writeTo(Output output, Foo message) throws IOException {
		if (message.someInt != null) {
			output.writeUInt32List(SOMEINT_TAG, "someInt", message.someInt);
		}
		if (message.someString != null) {
			output.writeStringList(SOMESTRING_TAG, "someString", message.someString);
		}
		if (message.someBar != null) {
			output.writeMessageList(SOMEBAR_TAG, "someBar", message.someBar, Bar.getSchema());
		}
		if (message.someEnum != null) {
			output.writeEnumList(SOMEENUM_TAG, "someEnum", message.someEnum, EnumSample.getSchema());
		}
		if (message.someBytes != null) {
			output.writeBytesList(SOMEBYTES_TAG, "someByte", message.someBytes);
		}
		if (message.someBoolean != null) {
			output.writeBoolList(SOMEBOOLEAN_TAG, "someBoolean", message.someBoolean);
		}
		if (message.someFloat != null) {
			output.writeFloatList(SOMEFLOAT_TAG, "someFloat", message.someFloat);
		}
		if (message.someDouble != null) {
			output.writeDoubleList(SOMEDOUBLE_TAG, "someDouble", message.someDouble);
		}
		if (message.someLong != null) {
			output.writeUInt64List(SOMELONG_TAG, "someLong", message.someLong);
		}
	}

	@Override
	public void mergeFrom(Input input, Foo message) throws IOException {
		while (true) {
			int tag = input.readTag();
			switch (tag) {
				case 0:
					return;
				case SOMEINT_TAG:
				case SOMEINT_PACK:
					if (message.someInt == null) {
						message.someInt = new ArrayList<>();
					}
					message.someInt.add(input.readUInt32());
					break;
				case SOMESTRING_TAG:
					if (message.someString == null) {
						message.someString = new ArrayList<>();
					}
					message.someString.add(input.readString());
					break;
				case SOMEBAR_TAG:
					if (message.someBar == null) {
						message.someBar = new ArrayList<>();
					}
					message.someBar.add(input.mergeObject(null, Bar.getSchema()));
					break;
				case SOMEBAR_GROUP:
					if (message.someBar == null) {
						message.someBar = new ArrayList<>();
					}
					message.someBar.add(input.mergeGroup(null, Bar.getSchema()));
					break;
				case SOMEENUM_TAG:
				case SOMEENUM_PACK:
					if (message.someEnum == null) {
						message.someEnum = new ArrayList<>();
					}
					message.someEnum.add(input.readEnum(EnumSample.getSchema()));
					break;
				case SOMEBYTES_TAG:
					if (message.someBytes == null) {
						message.someBytes = new ArrayList<>();
					}
					message.someBytes.add(input.readBytes());
					break;
				case SOMEBOOLEAN_TAG:
				case SOMEBOOLEAN_PACK:
					if (message.someBoolean == null) {
						message.someBoolean = new ArrayList<>();
					}
					message.someBoolean.add(input.readBool());
					break;
				case SOMEFLOAT_TAG:
				case SOMEFLOAT_PACK:
					if (message.someFloat == null) {
						message.someFloat = new ArrayList<>();
					}
					message.someFloat.add(input.readFloat());
					break;
				case SOMEDOUBLE_TAG:
				case SOMEDOUBLE_PACK:
					if (message.someDouble == null) {
						message.someDouble = new ArrayList<>();
					}
					message.someDouble.add(input.readDouble());
					break;
				case SOMELONG_TAG:
				case SOMELONG_PACK:
					if (message.someLong == null) {
						message.someLong = new ArrayList<>();
					}
					message.someLong.add(input.readUInt64());
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
		result = prime * result + ((someBar == null) ? 0 : someBar.hashCode());
		result = prime * result + ((someBoolean == null) ? 0 : someBoolean.hashCode());
		result = prime * result + ((someBytes == null) ? 0 : someBytes.hashCode());
		result = prime * result + ((someDouble == null) ? 0 : someDouble.hashCode());
		result = prime * result + ((someEnum == null) ? 0 : someEnum.hashCode());
		result = prime * result + ((someFloat == null) ? 0 : someFloat.hashCode());
		result = prime * result + ((someInt == null) ? 0 : someInt.hashCode());
		result = prime * result + ((someLong == null) ? 0 : someLong.hashCode());
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
		Foo other = (Foo) obj;
		if (someBar == null) {
			if (other.someBar != null) {
				return false;
			}
		} else if (!someBar.equals(other.someBar)) {
			return false;
		}
		if (someBoolean == null) {
			if (other.someBoolean != null) {
				return false;
			}
		} else if (!someBoolean.equals(other.someBoolean)) {
			return false;
		}
		if (someBytes == null) {
			if (other.someBytes != null) {
				return false;
			}
		} else if (other.someBytes == null || someBytes.size() != other.someBytes.size()) {
			return false;
		} else {
			Iterator<byte[]> i1 = someBytes.iterator();
			Iterator<byte[]> i2 = other.someBytes.iterator();
			while (i1.hasNext()) {
				if (!Arrays.equals(i1.next(), i2.next()))
					return false;
			}
		}
		if (someDouble == null) {
			if (other.someDouble != null) {
				return false;
			}
		} else if (!someDouble.equals(other.someDouble)) {
			return false;
		}
		if (someEnum == null) {
			if (other.someEnum != null) {
				return false;
			}
		} else if (!someEnum.equals(other.someEnum)) {
			return false;
		}
		if (someFloat == null) {
			if (other.someFloat != null) {
				return false;
			}
		} else if (!someFloat.equals(other.someFloat)) {
			return false;
		}
		if (someInt == null) {
			if (other.someInt != null) {
				return false;
			}
		} else if (!someInt.equals(other.someInt)) {
			return false;
		}
		if (someLong == null) {
			if (other.someLong != null) {
				return false;
			}
		} else if (!someLong.equals(other.someLong)) {
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
		return "Foo [someBar=" + someBar + ", someBoolean=" + someBoolean + ", someBytes=" + someBytes + ", someDouble=" + someDouble + ", someEnum=" + someEnum
				+ ", someFloat=" + someFloat + ", someInt=" + someInt + ", someLong=" + someLong + ", someString=" + someString + "]";
	}

	@Override
	public Foo newMessage() {
		return new Foo();
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
