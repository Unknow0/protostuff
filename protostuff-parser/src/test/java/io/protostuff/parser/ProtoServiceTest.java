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

import java.io.File;

import io.protostuff.parser.Service.RpcMethod;
import junit.framework.TestCase;

/**
 * Tests for parsing service/rpc components in the .proto file.
 *
 * @author David Yu
 * @created Jun 19, 2010
 */
public class ProtoServiceTest extends TestCase {

	public void testRpc() throws Exception {
		File f = ProtoParserTest.getFile("test_rpc.proto");
		assertTrue(f.exists());
		Proto proto = ProtoUtil.parseProto(f);

		assertTrue(proto.getImportedProtos().size() == 2);

		Proto importedProto = proto.getImportedProto(ProtoParserTest.getFile("test_imported_inner.proto"));
		assertNotNull(importedProto);

		Proto jpImportedProto = proto.getImportedProto(ProtoParserTest.getFile("test_java_package_imported_inner.proto"));
		assertNotNull(jpImportedProto);

		Message request = proto.getMessage("Request");
		assertNotNull(request);

		Message requestInner = request.getNestedMessage("Inner");
		assertNotNull(requestInner);

		Message requestDeeper = requestInner.getNestedMessage("Deeper");
		assertNotNull(requestDeeper);

		Message response = proto.getMessage("Response");
		assertNotNull(response);

		Message responseInner = response.getNestedMessage("Inner");
		assertNotNull(responseInner);

		Message responseDeeper = responseInner.getNestedMessage("Deeper");
		assertNotNull(responseDeeper);

		Message foo = importedProto.getMessage("Foo");
		assertNotNull(foo);

		Message fooInner = foo.getNestedMessage("Inner");
		assertNotNull(fooInner);

		Message fooDeeper = fooInner.getNestedMessage("Deeper");
		assertNotNull(fooDeeper);

		Message bar = importedProto.getMessage("Bar");
		assertNotNull(bar);

		Message barInner = bar.getNestedMessage("Inner");
		assertNotNull(barInner);

		Message barDeeper = barInner.getNestedMessage("Deeper");
		assertNotNull(barDeeper);

		Message jpFoo = jpImportedProto.getMessage("JPFoo");
		assertNotNull(jpFoo);

		Message jpFooInner = jpFoo.getNestedMessage("Inner");
		assertNotNull(jpFooInner);

		Message jpFooDeeper = jpFooInner.getNestedMessage("Deeper");
		assertNotNull(jpFooDeeper);

		Message jpBar = jpImportedProto.getMessage("JPBar");
		assertNotNull(jpBar);

		Message jpBarInner = jpBar.getNestedMessage("Inner");
		assertNotNull(jpBarInner);

		Message jpBarDeeper = jpBarInner.getNestedMessage("Deeper");
		assertNotNull(jpBarDeeper);

		Service service = proto.getService("SimpleRpc");
		assertNotNull(service);

		RpcMethod local = service.getRpcMethod("Local");
		assertNotNull(local);
		assertEquals(request.getRelativeName(), local.getArgType());
		assertEquals(response.getRelativeName(), local.getReturnType());

		assertEquals(Boolean.TRUE, local.getOption("(rpc.is_streaming_rpc)"));
		assertEquals("bar", local.getOption("foo"));
		assertEquals(1.0, local.getOption("bar.baz"));
		assertEquals(Long.valueOf(1), local.getOption("id"));

		RpcMethod theVoid = service.getRpcMethod("theVoid");
		assertNotNull(theVoid);
		assertTrue(theVoid.isVoidArgType());
		assertTrue(theVoid.isVoidReturnType());

		RpcMethod localFull = service.getRpcMethod("LocalFull");
		assertNotNull(localFull);
		assertEquals(request.getFullName(), localFull.getArgType());
		assertEquals(response.getFullName(), localFull.getReturnType());

		RpcMethod localInner = service.getRpcMethod("LocalInner");
		assertNotNull(localInner);
		assertEquals(requestInner.getRelativeName(), localInner.getArgType());
		assertEquals(responseInner.getRelativeName(), localInner.getReturnType());

		RpcMethod localInnerFull = service.getRpcMethod("LocalInnerFull");
		assertNotNull(localInnerFull);
		assertEquals(requestInner.getFullName(), localInnerFull.getArgType());
		assertEquals(responseInner.getFullName(), localInnerFull.getReturnType());

		RpcMethod localDeeper = service.getRpcMethod("LocalDeeper");
		assertNotNull(localDeeper);
		assertEquals(requestDeeper.getRelativeName(), localDeeper.getArgType());
		assertEquals(responseDeeper.getRelativeName(), localDeeper.getReturnType());

		RpcMethod localDeeperFull = service.getRpcMethod("LocalDeeperFull");
		assertNotNull(localDeeperFull);
		assertEquals(requestDeeper.getFullName(), localDeeperFull.getArgType());
		assertEquals(responseDeeper.getFullName(), localDeeperFull.getReturnType());

		RpcMethod foreign = service.getRpcMethod("Foreign");
		assertNotNull(foreign);
		assertEquals(foo.getRelativeName(), foreign.getArgType());
		assertEquals(bar.getRelativeName(), foreign.getReturnType());

		RpcMethod foreignFull = service.getRpcMethod("ForeignFull");
		assertNotNull(foreignFull);
		assertEquals(foo.getFullName(), foreignFull.getArgType());
		assertEquals(bar.getFullName(), foreignFull.getReturnType());

		RpcMethod foreignInner = service.getRpcMethod("ForeignInner");
		assertNotNull(foreignInner);
		assertEquals(fooInner.getRelativeName(), foreignInner.getArgType());
		assertEquals(barInner.getRelativeName(), foreignInner.getReturnType());

		RpcMethod foreignInnerFull = service.getRpcMethod("ForeignInnerFull");
		assertNotNull(foreignInnerFull);
		assertEquals(fooInner.getFullName(), foreignInnerFull.getArgType());
		assertEquals(barInner.getFullName(), foreignInnerFull.getReturnType());

		RpcMethod foreignDeeper = service.getRpcMethod("ForeignDeeper");
		assertNotNull(foreignDeeper);
		assertEquals(fooDeeper.getRelativeName(), foreignDeeper.getArgType());
		assertEquals(barDeeper.getRelativeName(), foreignDeeper.getReturnType());

		RpcMethod foreignDeeperFull = service.getRpcMethod("ForeignDeeperFull");
		assertNotNull(foreignDeeperFull);
		assertEquals(fooDeeper.getFullName(), foreignDeeperFull.getArgType());
		assertEquals(barDeeper.getFullName(), foreignDeeperFull.getReturnType());

		RpcMethod jpForeign = service.getRpcMethod("JPForeign");
		assertNotNull(jpForeign);
		assertEquals(jpFoo.getRelativeName(), jpForeign.getArgType());
		assertEquals(jpBar.getRelativeName(), jpForeign.getReturnType());

		RpcMethod jpForeignFull = service.getRpcMethod("JPForeignFull");
		assertNotNull(jpForeignFull);
		assertEquals(jpFoo.getFullName(), jpForeignFull.getArgType());
		assertEquals(jpBar.getFullName(), jpForeignFull.getReturnType());

		RpcMethod jpForeignInner = service.getRpcMethod("JPForeignInner");
		assertNotNull(jpForeignInner);
		assertEquals(jpFooInner.getRelativeName(), jpForeignInner.getArgType());
		assertEquals(jpBarInner.getRelativeName(), jpForeignInner.getReturnType());

		RpcMethod jpForeignInnerFull = service.getRpcMethod("JPForeignInnerFull");
		assertNotNull(jpForeignInnerFull);
		assertEquals(jpFooInner.getFullName(), jpForeignInnerFull.getArgType());
		assertEquals(jpBarInner.getFullName(), jpForeignInnerFull.getReturnType());

		RpcMethod jpForeignDeeper = service.getRpcMethod("JPForeignDeeper");
		assertNotNull(jpForeignDeeper);
		assertEquals(jpFooDeeper.getRelativeName(), jpForeignDeeper.getArgType());
		assertEquals(jpBarDeeper.getRelativeName(), jpForeignDeeper.getReturnType());

		RpcMethod jpForeignDeeperFull = service.getRpcMethod("JPForeignDeeperFull");
		assertNotNull(jpForeignDeeperFull);
		assertEquals(jpFooDeeper.getFullName(), jpForeignDeeperFull.getArgType());
		assertEquals(jpBarDeeper.getFullName(), jpForeignDeeperFull.getReturnType());
	}
}
