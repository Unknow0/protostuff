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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Represents the message defined in the {@link Proto}.
 *
 * @author David Yu
 * @created Dec 19, 2009
 */
public class Message implements HasName, HasFields, HasOptions, HasProto {

	final String name;
	final Message parentMessage;
	final Proto proto;

	final Map<String, Message> nestedMessages;
	final Map<String, EnumGroup> nestedEnumGroups;

	final List<Field> fields;
	final List<Extension> nestedExtensions;

	final List<int[]> extensionRanges;
	final Map<Integer, Field> extensions;
	final Map<String, Object> options;

	public Message(String name, Message parentMessage, Proto proto, Map<String, Message> nestedMessages, Map<String, EnumGroup> nestedEnumGroups, List<Field> fields,
			List<Extension> nestedExtensions, List<int[]> extensionRanges, Map<Integer, Field> extensions, Map<String, Object> options) {
		this.name = name;
		this.parentMessage = parentMessage;
		this.proto = proto;
		this.nestedMessages = nestedMessages;
		this.nestedEnumGroups = nestedEnumGroups;
		this.fields = fields;
		this.nestedExtensions = nestedExtensions;
		this.extensionRanges = extensionRanges;
		this.extensions = extensions;
		this.options = options;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Proto getProto() {
		return proto;
	}

	public Message getRootMessage() {
		return parentMessage == null ? null : getRoot(parentMessage);
	}

	public Message getParentMessage() {
		return parentMessage;
	}

	public boolean isNested() {
		return parentMessage != null;
	}

	public boolean hasNestedMessages() {
		return !nestedMessages.isEmpty();
	}

	public boolean hasNestedEnumGroups() {
		return !nestedEnumGroups.isEmpty();
	}

	public Map<String, Message> getNestedMessageMap() {
		return nestedMessages;
	}

	public Collection<Message> getNestedMessages() {
		return nestedMessages.values();
	}

	public Message getNestedMessage(String name) {
		return nestedMessages.get(name);
	}

	/* ================================================== */

	public Map<String, EnumGroup> getNestedEnumGroupMap() {
		return nestedEnumGroups;
	}

	public Collection<EnumGroup> getNestedEnumGroups() {
		return nestedEnumGroups.values();
	}

	public EnumGroup getNestedEnumGroup(String name) {
		return nestedEnumGroups.get(name);
	}

	@Override
	public List<Field> getFields() {
		return fields;
	}

	public boolean isDescendant(Message other) {
		if (parentMessage == null) {
			return false;
		}
		return parentMessage == other || parentMessage.isDescendant(other);
	}

	public Message getDescendant(String name) {
		if (parentMessage == null) {
			return null;
		}

		return name.equals(parentMessage.name) ? parentMessage : parentMessage.getDescendant(name);
	}

	public void defineExtensionRange(int first, int last) {
		extensionRanges.add(new int[] { first, last });
	}

	public void addNestedExtension(Extension extension) {
		this.nestedExtensions.add(extension);
	}

	public Collection<Extension> getNestedExtensions() {
		return this.nestedExtensions;
	}

	public void extend(Extension extension) {
		if (extensionRanges.isEmpty()) {
			throw err("Message " + getFullName() + " does not define extension range", getProto());
		}

		for (Field field : extension.getFields()) {
			int number = field.getNumber();
			boolean inRange = false;
			for (int[] range : extensionRanges) {
				if (number >= range[0] && number <= range[1]) {
					inRange = true;
					break;
				}
			}
			if (!inRange) {
				throw err("Extension '" + field.getName() + "' is outside extension range", getProto());
			}
			if (this.extensions.containsKey(number)) {
				throw err("Extension already defined for number '" + number + "'", getProto());
			}
			this.extensions.put(number, field);
		}
	}

	@Override
	public Map<String, Object> getOptions() {
		return options;
	}

	@Override
	public String toString() {
		return new StringBuilder().append('{').append("name:").append(name).append(',').append("enumGroups:").append(nestedEnumGroups.values()).append(',')
				.append("extensions:").append(nestedExtensions).append(',').append("fields:").append(fields).append('}').toString();
	}

	public String getFullName() {
		StringJoiner buffer = new StringJoiner(".");
		resolveFullName(this, buffer);
		return buffer.toString();
	}

	public String getJavaFullName() {
		StringJoiner buffer = new StringJoiner(".");
		resolveJavaFullName(this, buffer);
		return buffer.toString();
	}

	public String getRelativeName() {
		StringBuilder buffer = new StringBuilder();
		resolveRelativeName(this, buffer, null);
		return buffer.toString();
	}

	static void resolveFullName(Message message, StringJoiner buffer) {
		if (message.isNested()) {
			resolveFullName(message.parentMessage, buffer);
		} else {
			buffer.add(message.getProto().getPackageName());
		}
		buffer.add(message.name);
	}

	static void resolveJavaFullName(Message message, StringJoiner buffer) {
		if (message.isNested()) {
			resolveFullName(message.parentMessage, buffer);
		} else {
			buffer.add(message.getProto().getJavaPackageName());
		}
		buffer.add(message.name);
	}

	static void resolveRelativeName(Message message, StringBuilder buffer, Message descendant) {
		buffer.insert(0, message.name);
		if (message.parentMessage != null) {
			if (message.parentMessage != descendant) {
				buffer.insert(0, '.');
				resolveRelativeName(message.parentMessage, buffer, descendant);
			}
		}
	}

	static void computeName(Message message, Message owner, StringBuilder buffer) {
		if (owner == message || message.parentMessage == owner || owner.isDescendant(message)) {
			buffer.append(message.name);
		} else if (message.isDescendant(owner)) {
			Message.resolveRelativeName(message, buffer, owner);
		} else if (message.getProto().getJavaPackageName().equals(owner.getProto().getJavaPackageName())) {
			buffer.append(message.getRelativeName());
		} else {
			buffer.append(message.getJavaFullName());
		}
	}

	static Message getRoot(Message parent) {
		return parent.parentMessage == null ? parent : getRoot(parent.parentMessage);
	}

}
