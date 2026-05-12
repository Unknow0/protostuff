package io.protostuff.parser.builder;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import io.protostuff.WireFormat;
import io.protostuff.WireFormat.FieldType;
import io.protostuff.parser.DefaultProtoLoader;
import io.protostuff.parser.EnumGroup;
import io.protostuff.parser.Extension;
import io.protostuff.parser.Message;
import io.protostuff.parser.Proto;
import io.protostuff.parser.Proto.Loader;
import io.protostuff.parser.Service;

public class ProtoBuilder extends HasMessageBuilder<ProtoBuilder> {
	private final URI source;
	private final Loader loader;

	private final Map<String, Proto> importedProtos;
	private final List<ServiceBuilder> servicesBuilder;

	private String packageName;

	public ProtoBuilder(URI source) {
		this(source, DefaultProtoLoader.DEFAULT_INSTANCE);
	}

	public ProtoBuilder(URI source, Loader loader) {
		super(null, null);
		this.source = source;
		this.loader = loader;

		this.importedProtos = new HashMap<>();
		this.servicesBuilder = new ArrayList<>();
	}

	public URI getSource() {
		return source;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	@Override
	public String name() {
		return getJavaPackage();
	}

	public String getJavaPackage() {
		Object object = options.get("java_package");
		if (object instanceof String)
			return (String) object;
		return packageName;
	}

	public void importProto(String path) {
		Proto proto = loader.load(path, this);
		importedProtos.put(proto.getSourcePath(), proto);
	}

	public final ServiceBuilder newService() {
		return add(servicesBuilder, new ServiceBuilder(proto, this));
	}

	public Proto build() {
		Map<String, Message> messages = new HashMap<>();
		Map<String, EnumGroup> enumGroups = new HashMap<>();
		Map<String, Service> services = new HashMap<>();
		List<Extension> extensions = new ArrayList<>();

		Proto proto = new Proto(source.toString(), packageName, getJavaPackage(), importedProtos, options, messages, enumGroups, services, extensions);

		for (MessageBuilder builder : messageBuilders)
			addMessages(messages, enumGroups, builder.build(this, proto, null));
		for (EnumBuilder b : enumBuilders) {
			EnumGroup e = b.build(proto, null);
			put(enumGroups, "enum", e.getName(), e);
		}
		for (ServiceBuilder b : servicesBuilder) {
			Service s = b.build(proto);
			put(services, "service", s.getName(), s);
		}
		for (ExtendBuilder b : extendBuilders)
			extensions.add(b.build(proto, null));
		return proto;
	}

	private void addMessages(Map<String, Message> messages, Map<String, EnumGroup> enumGroups, Message m) {
		String name = getFullName(m);
		put(messages, "message", name, m);
		for (EnumGroup e : m.getNestedEnumGroups())
			put(enumGroups, "enum", name + "." + e.getName(), e);
		for (Message nested : m.getNestedMessages())
			addMessages(messages, enumGroups, nested);
	}

	@Override
	protected void resolveFullName(StringJoiner js) {
		String javaPackage = getJavaPackage();
		if (javaPackage != null)
			js.add(javaPackage);
	}

	public WireFormat.FieldType getWireType(String fullId) {
		FieldType type = getWireType(findBuilder(fullId));
		if (type != null)
			return type;
		if (fullId.startsWith(proto.getPackageName() + '.')) {
			String localName = fullId.substring(proto.getPackageName().length() + 1);
			type = getWireType(proto.findBuilder(localName));
			if (type == null)
				throw new IllegalStateException("Failed to find type " + fullId);
			return type;
		}
		for (Proto p : importedProtos.values()) {
			if (fullId.startsWith(p.getPackageName() + '.')) {
				String localName = fullId.substring(p.getPackageName().length() + 1);
				Message m = p.getMessage(localName);
				if (m != null)
					return WireFormat.FieldType.forMessage(m.getJavaFullName());
				EnumGroup e = p.getEnumGroup(localName);
				if (e != null)
					return WireFormat.FieldType.forEnum(e.getJavaFullName());
				throw new IllegalStateException("Failed to find message " + fullId);
			}
			Message m = p.getMessage(fullId);
			if (m != null)
				return WireFormat.FieldType.forMessage(m.getJavaFullName());
			EnumGroup e = p.getEnumGroup(fullId);
			if (e != null)
				return WireFormat.FieldType.forEnum(e.getJavaFullName());
		}
		throw new IllegalStateException("Failed to find message " + fullId);
	}

	private static String getFullName(Message m) {
		LinkedList<String> s = new LinkedList<>();
		while (m != null) {
			s.addFirst(m.getName());
			m = m.getParentMessage();
		}
		return String.join(".", s);
	}

	@Override
	public String toString() {
		return "ProtoBuilder " + source;
	}
}
