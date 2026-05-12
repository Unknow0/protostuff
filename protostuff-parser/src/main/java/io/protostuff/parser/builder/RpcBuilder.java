package io.protostuff.parser.builder;

import io.protostuff.parser.Service;
import io.protostuff.parser.Service.RpcMethod;

public class RpcBuilder extends AbstractBuilder implements WithName<RpcBuilder> {

	private String name;
	private String argType;
	private String retType;

	protected RpcBuilder(ProtoBuilder proto, AbstractBuilder parent) {
		super(proto, parent);
	}

	@Override
	public RpcBuilder name(String name) {
		this.name = name;
		return this;
	}

	@Override
	public String name() {
		return name;
	}

	public RpcBuilder argType(String argType) {
		this.argType = argType;
		return this;
	}

	public RpcBuilder retType(String retType) {
		this.retType = retType;
		return this;
	}

	public RpcMethod build(Service service) {
		return new RpcMethod(service, name, argType, getWireType(argType, true), retType, getWireType(retType, true), options);
	}

	@Override
	public String toString() {
		return "RpcBuilder " + name;
	}
}
