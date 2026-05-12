package io.protostuff.parser.builder;

import io.protostuff.parser.Field;

public interface WithModifier<T extends WithModifier<T>> {
	T modifier(Field.Modifier mod);
}
