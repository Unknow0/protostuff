package io.protostuff.parser.builder;

public interface WithName<T extends WithName<T>> {
	T name(String name);

	String name();
}
