package io.protostuff.api;

import java.io.IOException;

public interface Schema<T> {
	/**
	 * create a new message
	 * @return a new message
	 */
	T newMessage();

	/**
	 * 
	 * @param input
	 * @param message
	 * @throws IOException
	 */
	void mergeFrom(Input input, T message) throws IOException;

	void writeTo(Output output, T message) throws IOException;

	/**
	 * get a tag from a field name
	 * @param name field name
	 * @return tag
	 */
	int tag(String name);
}
