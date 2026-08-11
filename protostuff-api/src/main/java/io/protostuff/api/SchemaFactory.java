package io.protostuff.api;

public interface SchemaFactory {
	/** run before precompiled schema */
	final int PRIO_FIRST = 15000;
	/** run after precompiler */
	final int PRIO_DEFAULT = 5000;
	/** run after runtime schema factory */
	final int PRIO_LAST = -15000;

	/**
	 * factory priority higher first
	 * @return the priority
	 */
	default int priority() {
		return PRIO_DEFAULT;
	}

	/**
	 * try to create a schema for a class
	 * @param clazz the clazz
	 * @return the schema or null if this factory can't create it
	 */
	<T> Schema<T> tryCreate(Class<T> clazz);
}
