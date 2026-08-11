//========================================================================
//Copyright 2007-2009 David Yu dyuproject@gmail.com
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package io.protostuff.api;

import java.io.IOException;

/**
 * An Input lets an application read primitive data types and objects from a source of data.
 * 
 * @author David Yu
 * @created Nov 9, 2009
 */
public interface Input {
	/**
	 * Reads a tag
	 * @return the tag
	 * @throws IOException in case of error
	 */
	int readTag() throws IOException;

	/**
	 * Reads a variable int field value.
	 * @return the value
	 * @throws IOException in case of error
	 */
	int readInt32() throws IOException;

	/**
	 * Reads an unsigned int field value.
	 * @return the value
	 * @throws IOException in case of error
	 */
	int readUInt32() throws IOException;

	/**
	 * Reads a signed int field value.
	 * @return the value
	 * @throws IOException in case of error
	 */
	int readSInt32() throws IOException;

	/**
	 * Reads a fixed int(4 bytes) field value.
	 * @return the value
	 * @return the value
	 * @throws IOException in case of error
	 */
	int readFixed32() throws IOException;

	/**
	 * Reads a signed+fixed int(4 bytes) field value.
	 * @return the value
	 * @throws IOException in case of error
	 */
	int readSFixed32() throws IOException;

	/**
	 * Reads an unsigned long field value.
	 * @throws IOException in case of error
	 */
	long readUInt64() throws IOException;

	/**
	 * Reads a signed long field value.
	 * @return the value
	 * @throws IOException in case of error
	 */
	long readSInt64() throws IOException;

	/**
	 * Reads a fixed long(8 bytes) field value.
	 * @return the value
	 * @throws IOException in case of error
	 */
	long readFixed64() throws IOException;

	/**
	 * Reads a signed+fixed long(8 bytes) field value.
	 * @return the value
	 * @throws IOException in case of error
	 */
	long readSFixed64() throws IOException;

	/**
	 * Reads a float field value.
	 * @return the value
	 * @throws IOException in case of error
	 */
	float readFloat() throws IOException;

	/**
	 * Reads a double field value.
	 * @return the value
	 * @throws IOException in case of error
	 */
	double readDouble() throws IOException;

	/**
	 * Reads a boolean field value.
	 * @return the value
	 * @throws IOException in case of error
	 */
	boolean readBool() throws IOException;

	/**
	 * Reads an enum(its number) field value.
	 * @return the value
	 * @throws IOException in case of error
	 */
	<E extends Enum<E>> E readEnum(SchemaEnum<E> schema) throws IOException;

	/**
	 * Reads a {@link String} field value.
	 * @throws IOException in case of error
	 */
	String readString() throws IOException;

	/**
	 * Reads a byte array field value.
	 * @return the value
	 * @throws IOException in case of error
	 */
	byte[] readBytes() throws IOException;

	/**
	 * merge a group (with schema) field value. The provided {@link Schema schema} handles the deserialization for the object
	 * @param <T> object type
	 * @param value the object (if will a new one will be created)
	 * @param schema the schema
	 * @return the value
	 * @throws IOException in case of error
	 */
	<T> T mergeGroup(T value, Schema<T> schema) throws IOException;

	/**
	 * Merges an object(with schema) field value. The provided {@link Schema schema} handles the deserialization for the object.
	 * @param <T> object type
	 * @param value the object (if will a new one will be created)
	 * @param schema the schema
	 * @return the value
	 * @throws IOException in case of error
	 */
	<T> T mergeObject(T value, Schema<T> schema) throws IOException;

	/**
	 * skip last read field
	 * @throws IOException in case of error
	 */
	void skipField() throws IOException;

}
