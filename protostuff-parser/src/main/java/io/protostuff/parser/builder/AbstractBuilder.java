package io.protostuff.parser.builder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import io.protostuff.api.FieldType;

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

	public FieldType getWireType(String fullId, boolean allowVoid) {
		switch (fullId) {
			case "bool":
				return FieldType.BOOL;
			case "string":
				return FieldType.STRING;
			case "bytes":
				return FieldType.BYTES;
			case "int32":
				return FieldType.INT32;
			case "uint32":
				return FieldType.UINT32;
			case "sint32":
				return FieldType.SINT32;
			case "fixed32":
				return FieldType.FIXED32;
			case "sfixed32":
				return FieldType.SFIXED32;
			case "int64":
				return FieldType.INT64;
			case "uint64":
				return FieldType.UINT64;
			case "sint64":
				return FieldType.SINT64;
			case "fixed64":
				return FieldType.FIXED64;
			case "sfixed64":
				return FieldType.SFIXED64;
			case "double":
				return FieldType.DOUBLE;
			case "float":
				return FieldType.FLOAT;
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

	public static FieldType getWireType(AbstractBuilder b) {
		if (b instanceof EnumBuilder)
			return FieldType.forEnum(b.getFullName());
		if (b instanceof GroupBuilder)
			return FieldType.forGroup(b.getFullName());
		if (b instanceof MessageBuilder)
			return FieldType.forMessage(b.getFullName());
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
