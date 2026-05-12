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

/**
 * Represents an enum field defined in a {@link Message}.
 *
 * @author David Yu
 * @created Dec 19, 2009
 */
public class EnumField implements HasOptions, HasName {
	final EnumGroup enumGroup;
	final String name;
	final int number;
	final Map<String, Object> options;

	public EnumField(EnumGroup enumGroup, String name, int number, Map<String, Object> options) {
		this.enumGroup = enumGroup;
		this.name = name;
		this.number = number;
		this.options = options;
	}

	@Override
	public String getName() {
		return name;
	}

	public int getNumber() {
		return number;
	}

	public EnumGroup getEnumGroup() {
		return enumGroup;
	}

	@Override
	public Map<String, Object> getOptions() {
		return options;
	}

}
