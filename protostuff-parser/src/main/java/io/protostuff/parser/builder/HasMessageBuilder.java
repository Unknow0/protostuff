package io.protostuff.parser.builder;

import java.util.ArrayList;
import java.util.List;

public abstract class HasMessageBuilder<T extends HasMessageBuilder<T>> extends AbstractBuilder implements WithOptions, WithName<T> {
	protected final List<MessageBuilder> messageBuilders;
	protected final List<EnumBuilder> enumBuilders;
	protected final List<ExtendBuilder> extendBuilders;

	protected String name;

	protected HasMessageBuilder(ProtoBuilder proto, AbstractBuilder parent) {
		super(proto, parent);
		this.messageBuilders = new ArrayList<>();
		this.enumBuilders = new ArrayList<>();
		this.extendBuilders = new ArrayList<>();
	}

	@Override
	public String name() {
		return name;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T name(String name) {
		this.name = name;
		return (T) this;
	}

	public final MessageBuilder newMessage() {
		return add(messageBuilders, new MessageBuilder(proto, this));
	}

	public final EnumBuilder newEnumBuilder() {
		return add(enumBuilders, new EnumBuilder(proto, this));
	}

	public final ExtendBuilder newExtend() {
		return add(extendBuilders, new ExtendBuilder(proto, this));
	}

	public final AbstractBuilder getBuilder(String name) {
		for (MessageBuilder m : messageBuilders) {
			if (name.equals(m.name()))
				return m;
		}
		for (EnumBuilder m : enumBuilders) {
			if (name.equals(m.name()))
				return m;
		}
		return null;
	}
}
