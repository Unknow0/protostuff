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
import java.util.Map;

import io.protostuff.api.FieldType;

/**
 * Represents a service defined in the proto (for generating rpc services).
 *
 * @author David Yu
 * @created Jun 18, 2010
 */
public class Service implements HasName, HasOptions, HasProto {

	final Proto proto;
	final String name;

	final Map<String, RpcMethod> rpcMethods;

	final Map<String, Object> options;

	public Service(Proto proto, String name, Map<String, RpcMethod> rpcMethods, Map<String, Object> options) {
		this.proto = proto;
		this.name = name;
		this.rpcMethods = rpcMethods;
		this.options = options;
	}

	@Override
	public String getName() {
		return name;
	}

	public String getFullName() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(getProto().getPackageName()).append('.').append(name);
		return buffer.toString();
	}

	public String getJavaFullName() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(getProto().getJavaPackageName()).append('.').append(name);
		return buffer.toString();
	}

	public String getRelativeName() {
		return name;
	}

	/* ================================================== */

	@Override
	public Proto getProto() {
		return proto;
	}

	/* ================================================== */

	public Collection<RpcMethod> getRpcMethods() {
		return rpcMethods.values();
	}

	public Map<String, RpcMethod> getRpcMethodMap() {
		return rpcMethods;
	}

	public RpcMethod getRpcMethod(String name) {
		return rpcMethods.get(name);
	}

	@Override
	public Map<String, Object> getOptions() {
		return options;
	}

	public static class RpcMethod implements HasName, HasOptions, HasProto {

		final Map<String, Object> options;

		final String name;
		final Service service;

		final String argType;
		final FieldType argWire;
		final String retType;
		final FieldType retWire;

		public RpcMethod(Service service, String name, String argType, FieldType argWire, String retType, FieldType retWire,
				Map<String, Object> options) {
			this.service = service;
			this.name = name;
			this.argType = argType;
			this.argWire = argWire;
			this.retType = retType;
			this.retWire = retWire;
			this.options = options;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Proto getProto() {
			return service.getProto();
		}

		public Service getService() {
			return service;
		}

		public Service getOwner() {
			return service;
		}

		public String getArgType() {
			return argType;
		}

		public String getReturnType() {
			return retType;
		}

		public boolean isVoidArgType() {
			return argType.equals("void");
		}

		public boolean isVoidReturnType() {
			return retType.equals("void");
		}

		public FieldType getWireArgType() {
			return argWire;
		}

		public FieldType getWireReturnType() {
			return retWire;
		}

		@Override
		public Map<String, Object> getOptions() {
			return options;
		}

	}

}
