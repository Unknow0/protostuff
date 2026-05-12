package io.protostuff.parser.builder;

import io.protostuff.parser.Field;
import io.protostuff.parser.Field.Modifier;

public class GroupBuilder extends MessageBuilder implements WithModifier<GroupBuilder> {

	private Field.Modifier modifier;
	private int number;

	public GroupBuilder(ProtoBuilder proto, AbstractBuilder parent) {
		super(proto, parent);
	}

	@Override
	public GroupBuilder modifier(Modifier mod) {
		modifier = mod;
		return this;
	}

	public GroupBuilder number(int number) {
		this.number = number;
		return this;
	}

	@Override
	public String toString() {
		return "GroupBuilder " + name;
	}
}
