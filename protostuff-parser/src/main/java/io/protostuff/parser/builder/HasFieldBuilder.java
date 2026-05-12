package io.protostuff.parser.builder;

public interface HasFieldBuilder {
	FieldBuilder newField();

	GroupBuilder newGroup();
}
