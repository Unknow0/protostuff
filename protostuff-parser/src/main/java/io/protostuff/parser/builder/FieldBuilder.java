package io.protostuff.parser.builder;

import io.protostuff.parser.Field;
import io.protostuff.parser.Field.Modifier;

public class FieldBuilder extends AbstractBuilder implements WithOptions, WithName<FieldBuilder>, WithModifier<FieldBuilder> {
	private Modifier modifier;
	private String name;
	private String type;
	private int number;

	public FieldBuilder(ProtoBuilder proto, AbstractBuilder parent) {
		super(proto, parent);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public FieldBuilder name(String name) {
		this.name = name;
		return this;
	}

	@Override
	public FieldBuilder modifier(Modifier modifier) {
		this.modifier = modifier;
		return this;
	}

	public FieldBuilder type(String type) {
		this.type = type;
		return this;
	}

	public int number() {
		return number;
	}

	public FieldBuilder number(int number) {
		this.number = number;
		return this;
	}

	public Field build() {
		return new Field(name, number, modifier, getWireType(type, false), options);
	}

	@Override
	public String toString() {
		return "FieldBuilder " + name;
	}
}
