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

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Represents an enum declared in either the {@link Proto} or nested in a {@link Message}.
 *
 * @author David Yu
 * @created Dec 21, 2009
 */
public class EnumGroup implements HasName, HasOptions, HasProto {

	/**
	 * Disabled by default (the earlier protoc 2.x versions enabled this by default, but was changed later on).
	 */
	public static final boolean ENUM_ALLOW_ALIAS = Boolean.parseBoolean("protostuff.enum_allow_alias");

	final String name;
	final Message parentMessage;
	final Proto proto;
	final List<EnumField> fields;
	final Map<String, Object> options;

	public EnumGroup(String name, Message parentMessage, Proto proto, List<EnumField> fields, Map<String, Object> options) {
		this.name = name;
		this.parentMessage = parentMessage;
		this.proto = proto;
		this.fields = fields;
		this.options = options;
	}

	@Override
	public String getName() {
		return name;
	}

	public String getFullName() {
		StringJoiner buffer = new StringJoiner(".");
		if (parentMessage != null)
			Message.resolveFullName(parentMessage, buffer);
		else
			buffer.add(proto.getPackageName());
		return buffer.add(name).toString();
	}

	public String getJavaFullName() {
		StringJoiner buffer = new StringJoiner(".");
		if (parentMessage != null)
			Message.resolveFullName(parentMessage, buffer);
		else
			buffer.add(proto.getJavaPackageName());
		return buffer.add(name).toString();
	}

	public String getRelativeName() {
		return isNested() ? parentMessage.getRelativeName() + "." + name : name;
	}

	/* ================================================== */

	public Message getParentMessage() {
		return parentMessage;
	}

	public boolean isNested() {
		return parentMessage != null;
	}

	@Override
	public Proto getProto() {
		return proto;
	}

	@Override
	public Map<String, Object> getOptions() {
		return options;
	}

	public EnumField getField(String name) {
		for (EnumField f : fields) {
			if (name.equals(f.getName()))
				return f;
		}
		return null;
	}

	public EnumField getField(int i) {
		return fields.get(i);
	}

	public int getFieldCount() {
		return fields.size();
	}

	@Override
	public String toString() {
		return new StringBuilder().append('{').append("name:").append(name).append(',').append("fields:").append(fields).append('}').toString();
	}
}
