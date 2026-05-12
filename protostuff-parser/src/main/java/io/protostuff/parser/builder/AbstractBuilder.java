package io.protostuff.parser.builder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import io.protostuff.WireFormat;
import io.protostuff.WireFormat.FieldType;

public abstract class AbstractBuilder {
	protected final ProtoBuilder proto;
	protected final AbstractBuilder parent;
	protected final Map<String, Object> options;

	protected AbstractBuilder(ProtoBuilder proto, AbstractBuilder parent) {
		this.proto = proto == null ? (ProtoBuilder) this : proto;
		this.parent = parent;
		this.options = new HashMap<>();
	}

	private final HasMessageBuilder<?> getMessage() {
		AbstractBuilder b = this;
		while (!(b instanceof HasMessageBuilder))
			b = b.parent;
		return (HasMessageBuilder<?>) b;
	}

	public AbstractBuilder findBuilder(String fullId) {
		String[] split = fullId.split("\\.");
		HasMessageBuilder<?> m = getMessage();
		int i = 0;
		while (i < split.length - 1) {
			AbstractBuilder b = m.getBuilder(split[i++]);
			if (!(b instanceof HasMessageBuilder))
				return null;
			m = (HasMessageBuilder<?>) b;
		}
		return m.getBuilder(split[i]);
	}

	public WireFormat.FieldType getWireType(String fullId, boolean allowVoid) {
		switch (fullId) {
			case "bool":
				return WireFormat.FieldType.BOOL;
			case "string":
				return WireFormat.FieldType.STRING;
			case "bytes":
				return WireFormat.FieldType.BYTES;
			case "int32":
				return WireFormat.FieldType.INT32;
			case "uint32":
				return WireFormat.FieldType.UINT32;
			case "sint32":
				return WireFormat.FieldType.SINT32;
			case "fixed32":
				return WireFormat.FieldType.FIXED32;
			case "sfixed32":
				return WireFormat.FieldType.SFIXED32;
			case "int64":
				return WireFormat.FieldType.INT64;
			case "uint64":
				return WireFormat.FieldType.UINT64;
			case "sint64":
				return WireFormat.FieldType.SINT64;
			case "fixed64":
				return WireFormat.FieldType.FIXED64;
			case "sfixed64":
				return WireFormat.FieldType.SFIXED64;
			case "double":
				return WireFormat.FieldType.DOUBLE;
			case "float":
				return WireFormat.FieldType.FLOAT;
			case "void":
				if (!allowVoid)
					throw new IllegalStateException("Void not allowed");
				return null;
			default:
		}
		FieldType type = getWireType(findBuilder(fullId));
		return type != null ? type : proto.getWireType(fullId);
	}

	public String getFullName() {
		StringJoiner sj = new StringJoiner(".");
		resolveFullName(sj);
		return sj.toString();
	}

	protected void resolveFullName(StringJoiner sj) {
		parent.resolveFullName(sj);
		if (this instanceof WithName)
			sj.add(((WithName<?>) this).name());
	}

	public static WireFormat.FieldType getWireType(AbstractBuilder b) {
		if (b instanceof EnumBuilder)
			return WireFormat.FieldType.forEnum(b.getFullName());
		if (b instanceof GroupBuilder)
			return WireFormat.FieldType.forGroup(b.getFullName());
		if (b instanceof MessageBuilder)
			return WireFormat.FieldType.forMessage(b.getFullName());
		return null;
	}

	public void putOption(String key, Object value) {
		put(options, "option", key, value);
	}

	protected static <T> void put(Map<String, T> map, String name, String key, T value) {
		if (map.put(key, value) != null)
			throw new IllegalStateException("Duplicate " + name + " " + key);
	}

	protected static <T> T add(Collection<T> c, T t) {
		c.add(t);
		return t;
	}
}
