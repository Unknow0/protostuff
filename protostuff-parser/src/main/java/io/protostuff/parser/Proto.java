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

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.protostuff.parser.builder.ProtoBuilder;

/**
 * Contains the metadata of parsed protos. Basically repesents the .proto file.
 *
 * @author David Yu
 * @created Dec 18, 2009
 */
public class Proto implements HasOptions, HasProto {
	final String sourcePath;
	final String packageName;
	final String javaPackageName;
	final Map<String, Proto> importedProtos;
	final Map<String, Object> options;
	final Map<String, Message> messages;
	final Map<String, EnumGroup> enumGroups;
	final Map<String, Service> services;
	final List<Extension> extensions;

	public Proto(String sourcePath, String packageName, String javaPackageName, Map<String, Proto> importedProtos, Map<String, Object> options, Map<String, Message> messages,
			Map<String, EnumGroup> enumGroups, Map<String, Service> services, List<Extension> extensions) {
		this.sourcePath = sourcePath;
		this.packageName = packageName;
		this.javaPackageName = javaPackageName;
		this.importedProtos = importedProtos;
		this.options = options;
		this.messages = messages;
		this.enumGroups = enumGroups;
		this.services = services;
		this.extensions = extensions;
	}

	@Override
	public Proto getProto() {
		return this;
	}

	public ErrorMap getError() {
		return ErrorMap.INSTANCE;
	}

	public String getSourcePath() {
		return sourcePath;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getJavaPackageName() {
		return javaPackageName;
	}

	public Map<String, Object> getStandardOptions() {
		return options;
	}

	public Object getStandardOption(String name) {
		return options.get(name);
	}

	public Map<String, Object> getExtraOptions() {
		return options;
	}

	public Map<String, Object> getO() {
		return getOptions();
	}

	@Override
	public Map<String, Object> getOptions() {
		return options;
	}

	public Map<String, Message> getMessageMap() {
		return messages;
	}

	public Collection<Message> getMessages() {
		return messages.values();
	}

	public Message getMessage(String name) {
		return messages.get(name);
	}

	public Map<String, EnumGroup> getEnumGroupMap() {
		return enumGroups;
	}

	public Collection<EnumGroup> getEnumGroups() {
		return enumGroups.values();
	}

	public EnumGroup getEnumGroup(String name) {
		return enumGroups.get(name);
	}

	public Map<String, Service> getServiceMap() {
		return services;
	}

	public Collection<Service> getServices() {
		return services.values();
	}

	public Service getService(String name) {
		return services.get(name);
	}

	void addService(Service service) {
		if (services.put(service.name, service) != null) {
			throw err("Duplicate service: " + service.name, this);
		}
	}

	public void addExtension(Extension extension) {
		extensions.add(extension);
	}

	public Collection<Extension> getExtensions() {
		return extensions;
	}

	public Collection<Proto> getImportedProtos() {
		return importedProtos.values();
	}

	public Proto getImportedProto(File file) {
		return importedProtos.get(file.toURI().toString());
	}

	public Proto getImportedProto(URL url) {
		return importedProtos.get(url.toString());
	}

	public Proto getImportedProto(String url) {
		return importedProtos.get(url);
	}

	@Override
	public String toString() {
		return new StringBuilder().append('{').append("packageName:").append(packageName).append(',').append("standardOptions:").append(options).append(',')
				.append("extraOptions:").append(options).append(',').append("messages:").append(getMessages()).append('}').toString();
	}

	public interface Loader {
		public Proto load(String path, ProtoBuilder importer) throws ProtoParserException;
	}

}
