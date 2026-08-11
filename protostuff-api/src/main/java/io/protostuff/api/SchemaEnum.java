package io.protostuff.api;

public interface SchemaEnum<E extends Enum<E>> {
	int toNumber(E e);

	E fromNumber(int number);

	String toName(E e);

	E fromName(String name);
}
