package io.protostuff.parser.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.protostuff.parser.Proto;
import io.protostuff.parser.Service;
import io.protostuff.parser.Service.RpcMethod;

public class ServiceBuilder extends AbstractBuilder implements WithName<ServiceBuilder> {
	private final List<RpcBuilder> rpcBuilders;
	private String name;

	public ServiceBuilder(ProtoBuilder proto, AbstractBuilder parent) {
		super(proto, parent);
		this.rpcBuilders = new ArrayList<>();
	}

	@Override
	public ServiceBuilder name(String name) {
		this.name = name;
		return this;
	}

	@Override
	public String name() {
		return name;
	}

	public RpcBuilder newRpc() {
		return add(rpcBuilders, new RpcBuilder(proto, this));
	}

	public Service build(Proto proto) {
		Map<String, RpcMethod> rpc = new HashMap<>();
		Service s = new Service(proto, name, rpc, options);
		for (RpcBuilder b : rpcBuilders) {
			RpcMethod r = b.build(s);
			put(rpc, "rpc", r.getName(), r);
		}
		return s;
	}

	@Override
	public String toString() {
		return "ServiceBuilder " + name;
	}
}
