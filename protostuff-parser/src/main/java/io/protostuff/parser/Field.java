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

package io.protostuff.parser;

import java.util.Map;

import io.protostuff.WireFormat;

/**
 * Base class for fields defined in a {@link Message}.
 *
 * @author David Yu
 * @created Dec 19, 2009
 */
public class Field implements HasName, HasOptions {

	public enum Modifier {
		OPTIONAL, REQUIRED, REPEATED;

		public String getName() {
			return name().toLowerCase();
		}
	}

	final String name;
	final int number;
	final Modifier modifier;
	final WireFormat.FieldType type;
	final Map<String, Object> options;

	public Field(String name, int number, Modifier modifier, WireFormat.FieldType type, Map<String, Object> options) {
		this.name = name;
		this.number = number;
		this.modifier = modifier;
		this.type = type;
		this.options = options;
	}

	public Map<String, Object> getO() {
		return getOptions();
	}

	/**
	 * Returns this options
	 */
	@Override
	public Map<String, Object> getOptions() {
		return options;
	}

	/**
	 * Returns the option defined by the {@code key}.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <V> V getOption(String key) {
		return (V) options.get(key);
	}

	public boolean hasOption(String key) {
		return options.containsKey(key);
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return the number
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * @return the modifier
	 */
	public Modifier getModifier() {
		return modifier;
	}

	/**
	 * @return the packable
	 */
	public boolean isPackable() {
		return type.isPackable();
	}

	public boolean isRepeated() {
		return modifier == Modifier.REPEATED;
	}

	public boolean isRequired() {
		return modifier == Modifier.REQUIRED;
	}

	public boolean isOptional() {
		return modifier == Modifier.OPTIONAL;
	}

	@Override
	public String toString() {
		return new StringBuilder().append('{').append("type:").append(getClass().getSimpleName()).append(',').append("name:").append(name).append(',').append("number:")
				.append(number).append(',').append("modifier:").append(modifier).append(',').append("packable:").append(isPackable()).append('}').toString();
	}

	public WireFormat.FieldType getType() {
		return type;
	}
}
