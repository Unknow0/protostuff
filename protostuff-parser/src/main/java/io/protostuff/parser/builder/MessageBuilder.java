package io.protostuff.parser.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.protostuff.parser.EnumGroup;
import io.protostuff.parser.Extension;
import io.protostuff.parser.Field;
import io.protostuff.parser.Message;
import io.protostuff.parser.Proto;

public class MessageBuilder extends HasMessageBuilder<MessageBuilder> implements HasFieldBuilder, WithOptions {
	private final List<FieldBuilder> fieldBuilders;
	private final List<GroupBuilder> groupBuilders;
	private final RangesBuilder extensionBuilder;

	public MessageBuilder(ProtoBuilder proto, AbstractBuilder parent) {
		super(proto, parent);
		this.fieldBuilders = new ArrayList<>();
		this.groupBuilders = new ArrayList<>();
		this.extensionBuilder = new RangesBuilder(proto, this);
	}

	public Message build(ProtoBuilder root, Proto proto, Message parent) {
		Map<String, Message> nestedMessages = new HashMap<>();
		Map<String, EnumGroup> nestedEnumGroups = new HashMap<>();
		List<Field> fields = new ArrayList<>();
		List<Extension> nestedExtensions = new ArrayList<>();
		Map<Integer, Field> extensions = new HashMap<>();
		Message m = new Message(name, parent, proto, nestedMessages, nestedEnumGroups, fields, nestedExtensions, extensionBuilder.ranges, extensions, options);

		for (MessageBuilder b : messageBuilders) {
			Message nm = b.build(root, proto, m);
			put(nestedMessages, "message in " + m.getFullName(), nm.getName(), nm);
		}

		for (EnumBuilder b : enumBuilders) {
			EnumGroup ne = b.build(proto, m);
			put(nestedEnumGroups, "enum in " + m.getFullName(), ne.getName(), ne);
		}

		fieldBuilders.sort((f1, f2) -> {
			int cmp = f1.number() - f2.number();
			if (cmp == 0)
				throw new IllegalStateException("Duplicate field number in " + m.getFullName());
			return cmp;
		});
		for (FieldBuilder b : fieldBuilders)
			fields.add(b.build());

		for (ExtendBuilder b : extendBuilders)
			nestedExtensions.add(b.build(proto, m));

		return m;
	}

	public RangesBuilder extensions() {
		return extensionBuilder;
	}

	@Override
	public FieldBuilder newField() {
		return add(fieldBuilders, new FieldBuilder(proto, this));
	}

	@Override
	public final GroupBuilder newGroup() {
		return add(groupBuilders, new GroupBuilder(proto, this));
	}

	@Override
	public String toString() {
		return "MessageBuilder " + name;
	}
}
