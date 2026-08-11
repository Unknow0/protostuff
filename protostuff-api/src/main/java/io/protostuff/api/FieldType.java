package io.protostuff.api;

/**
	 * This is here to support runtime schemas.
	 */
//	public enum JavaType {
//		INT(0), LONG(0L), FLOAT(0F), DOUBLE(0D), BOOLEAN(false), STRING(""), BYTE_STRING(ByteString.EMPTY), ENUM(null), MESSAGE(null);
//
//		JavaType(final Object defaultDefault) {
//			this.defaultDefault = defaultDefault;
//		}
//
//		/**
//		 * The default default value for fields of this type, if it's a primitive type.
//		 */
//		Object getDefaultDefault() {
//			return defaultDefault;
//		}
//
//		private final Object defaultDefault;
//	}
//
	/**
	 * This is here to support runtime schemas.
	 */
	public final class FieldType {
		public static final FieldType DOUBLE = new FieldType("double", WireFormat.WIRETYPE_FIXED64, true);
		public static final FieldType FLOAT = new FieldType("float", WireFormat.WIRETYPE_FIXED32, true);
		public static final FieldType INT64 = new FieldType("long", WireFormat.WIRETYPE_VARINT, true);
		public static final FieldType UINT64 = new FieldType("long", WireFormat.WIRETYPE_VARINT, true);
		public static final FieldType INT32 = new FieldType("int", WireFormat.WIRETYPE_VARINT, true);
		public static final FieldType FIXED64 = new FieldType("long", WireFormat.WIRETYPE_FIXED64, true);
		public static final FieldType FIXED32 = new FieldType("int", WireFormat.WIRETYPE_FIXED32, true);
		public static final FieldType BOOL = new FieldType("boolean", WireFormat.WIRETYPE_VARINT, true);
		public static final FieldType STRING = new FieldType("java.lang.String", WireFormat.WIRETYPE_LENGTH_DELIMITED, false);
		public static final FieldType BYTES = new FieldType("byte[]", WireFormat.WIRETYPE_LENGTH_DELIMITED, false);
		public static final FieldType UINT32 = new FieldType("int", WireFormat.WIRETYPE_VARINT, true);
		public static final FieldType SFIXED32 = new FieldType("int", WireFormat.WIRETYPE_FIXED32, true);
		public static final FieldType SFIXED64 = new FieldType("long", WireFormat.WIRETYPE_FIXED64, true);
		public static final FieldType SINT32 = new FieldType("int", WireFormat.WIRETYPE_VARINT, true);
		public static final FieldType SINT64 = new FieldType("long", WireFormat.WIRETYPE_VARINT, true);

		public static FieldType forEnum(String javaType) {
			return new FieldType(javaType, WireFormat.WIRETYPE_VARINT, true);
		}

		public static FieldType forMessage(String javaType) {
			return new FieldType(javaType, WireFormat.WIRETYPE_LENGTH_DELIMITED, false);
		}

		public static FieldType forGroup(String javaType) {
			return new FieldType(javaType, WireFormat.WIRETYPE_START_GROUP, false);
		}

		private final String javaType;
		private final int wireType;
		private final boolean packable;

		public FieldType(final String javaType, final int wireType, final boolean packable) {
			this.javaType = javaType;
			this.wireType = wireType;
			this.packable = packable;
		}

		public String getJavaType() {
			return javaType;
		}

		public int getWireType() {
			return wireType;
		}

		public boolean isPackable() {
			return packable;
		}
	}