//========================================================================
//Copyright 2007-2010 David Yu dyuproject@gmail.com
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

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Represents an extend block declared in either the {@link Proto} or nested in a {@link Message}.
 *
 * @author Philippe Laflamme
 */
public class Extension implements HasFields, HasProto {
	final Proto proto;
	final Message parentMessage;
	final String type;

	final List<Field> fields;
	final Map<String, Object> options;

	public Extension(Proto proto, Message parentMessage, String type, List<Field> fields, Map<String, Object> options) {
		this.proto = proto;
		this.parentMessage = parentMessage;
		this.type = type;
		this.fields = fields;
		this.options = options;
	}

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
	public Collection<Field> getFields() {
		return fields;
	}

	@Override
	public Map<String, Object> getOptions() {
		return options;
	}

	public String getExtendedMessageFullName() {
		return type;
	}

	public String getEnclosingNamespace() {
		return isNested() ? getParentMessage().getFullName() : getProto().getPackageName();
	}

	@Override
	public String toString() {
		return new StringBuilder().append('{').append("extend:").append(getExtendedMessageFullName()).append(',').append("fields:").append(fields).append('}').toString();
	}

}
