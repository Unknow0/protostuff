package io.protostuff.parser.builder;

import java.util.ArrayList;
import java.util.List;

import io.protostuff.parser.EnumField;
import io.protostuff.parser.EnumGroup;
import io.protostuff.parser.Message;
import io.protostuff.parser.Proto;

public class EnumBuilder extends AbstractBuilder implements WithName<EnumBuilder> {
	private String name;
	private final List<EnumFieldBuilder> fields;

	public EnumBuilder(ProtoBuilder proto, AbstractBuilder parent) {
		super(proto, parent);
		fields = new ArrayList<>();
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public EnumBuilder name(String name) {
		this.name = name;
		return this;
	}

	public EnumFieldBuilder newField() {
		return add(fields, new EnumFieldBuilder(proto, this));
	}

	public EnumGroup build(Proto proto, Message parent) {
		List<EnumField> f = new ArrayList<>(fields.size());
		fields.sort(null);
		EnumGroup e = new EnumGroup(name, parent, proto, f, options);
		for (EnumFieldBuilder b : fields)
			f.add(b.build(e));
		return e;
	}

	@Override
	public String toString() {
		return "EnumBuilder " + name;
	}

	public static class EnumFieldBuilder extends AbstractBuilder implements WithName<EnumFieldBuilder>, Comparable<EnumFieldBuilder> {
		private String name;
		private int number;

		public EnumFieldBuilder(ProtoBuilder proto, AbstractBuilder parent) {
			super(proto, parent);
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public EnumFieldBuilder name(String name) {
			this.name = name;
			return this;
		}

		public EnumFieldBuilder number(int number) {
			this.number = number;
			return this;
		}

		public EnumField build(EnumGroup group) {
			return new EnumField(group, name, number, options);
		}

		@Override
		public int compareTo(EnumFieldBuilder o) {
			return number - o.number;
		}
	}
}
