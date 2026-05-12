package io.protostuff.parser.builder;

import java.util.ArrayList;
import java.util.List;

import io.protostuff.parser.Extension;
import io.protostuff.parser.Field;
import io.protostuff.parser.Message;
import io.protostuff.parser.Proto;

public class ExtendBuilder extends AbstractBuilder implements HasFieldBuilder, WithName<ExtendBuilder> {
	private final List<FieldBuilder> fieldBuilders;
	private final List<GroupBuilder> groupBuilders;

	private String name;

	public ExtendBuilder(ProtoBuilder proto, AbstractBuilder parent) {
		super(proto, parent);
		this.fieldBuilders = new ArrayList<>();
		this.groupBuilders = new ArrayList<>();
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public ExtendBuilder name(String name) {
		this.name = name;
		return this;
	}

	@Override
	public FieldBuilder newField() {
		return add(fieldBuilders, new FieldBuilder(proto, this));
	}

	@Override
	public GroupBuilder newGroup() {
		return add(groupBuilders, new GroupBuilder(proto, this));
	}

	public Extension build(Proto proto, Message parent) {
		List<Field> fields = new ArrayList<>();
		Extension e = new Extension(proto, parent, name, fields, options);

		for (FieldBuilder f : fieldBuilders)
			fields.add(f.build());

		return e;
	}
}
