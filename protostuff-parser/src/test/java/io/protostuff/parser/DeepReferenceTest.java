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

import junit.framework.TestCase;

/**
 * Test for deep references in the message's fields
 *
 * @author David Yu
 * @created Jun 20, 2010
 */
public class DeepReferenceTest extends TestCase {

	public void testIt() throws Exception {
		File f = ProtoParserTest.getFile("test_deep_reference.proto");
		assertTrue(f.exists());

		Proto proto = ProtoUtil.parseProto(f);

		assertEquals(proto.getImportedProtos().size(), 2);

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

		assertEquals(getMessageField("foo1", request), foo.getJavaFullName());
		assertEquals(getMessageField("foo2", request), foo.getJavaFullName());
		assertEquals(getMessageField("foo3", request), jpFoo.getJavaFullName());

		assertEquals(getMessageField("inner1", request), fooInner.getJavaFullName());
		assertEquals(getMessageField("inner2", request), fooInner.getJavaFullName());
		assertEquals(getMessageField("inner3", request), jpFooInner.getJavaFullName());

		assertEquals(getMessageField("deeper1", request), fooDeeper.getJavaFullName());
		assertEquals(getMessageField("deeper2", request), fooDeeper.getJavaFullName());
		assertEquals(getMessageField("deeper3", request), jpFooDeeper.getJavaFullName());

		assertEquals(getMessageField("foo1", requestInner), foo.getJavaFullName());
		assertEquals(getMessageField("foo2", requestInner), foo.getJavaFullName());
		assertEquals(getMessageField("foo3", requestInner), jpFoo.getJavaFullName());

		assertEquals(getMessageField("inner1", requestInner), fooInner.getJavaFullName());
		assertEquals(getMessageField("inner2", requestInner), fooInner.getJavaFullName());
		assertEquals(getMessageField("inner3", requestInner), jpFooInner.getJavaFullName());

		assertEquals(getMessageField("deeper1", requestInner), fooDeeper.getJavaFullName());
		assertEquals(getMessageField("deeper2", requestInner), fooDeeper.getJavaFullName());
		assertEquals(getMessageField("deeper3", requestInner), jpFooDeeper.getJavaFullName());

		assertEquals(getMessageField("foo1", requestDeeper), foo.getJavaFullName());
		assertEquals(getMessageField("foo2", requestDeeper), foo.getJavaFullName());
		assertEquals(getMessageField("foo3", requestDeeper), jpFoo.getJavaFullName());

		assertEquals(getMessageField("inner1", requestDeeper), fooInner.getJavaFullName());
		assertEquals(getMessageField("inner2", requestDeeper), fooInner.getJavaFullName());
		assertEquals(getMessageField("inner3", requestDeeper), jpFooInner.getJavaFullName());

		assertEquals(getMessageField("deeper1", requestDeeper), fooDeeper.getJavaFullName());
		assertEquals(getMessageField("deeper2", requestDeeper), fooDeeper.getJavaFullName());
		assertEquals(getMessageField("deeper3", requestDeeper), jpFooDeeper.getJavaFullName());

		assertEquals(getMessageField("bar1", response), bar.getJavaFullName());
		assertEquals(getMessageField("bar2", response), bar.getJavaFullName());
		assertEquals(getMessageField("bar3", response), jpBar.getJavaFullName());

		assertEquals(getMessageField("inner1", response), barInner.getJavaFullName());
		assertEquals(getMessageField("inner2", response), barInner.getJavaFullName());
		assertEquals(getMessageField("inner3", response), jpBarInner.getJavaFullName());

		assertEquals(getMessageField("deeper1", response), barDeeper.getJavaFullName());
		assertEquals(getMessageField("deeper2", response), barDeeper.getJavaFullName());
		assertEquals(getMessageField("deeper3", response), jpBarDeeper.getJavaFullName());

		assertEquals(getMessageField("bar1", responseInner), bar.getJavaFullName());
		assertEquals(getMessageField("bar2", responseInner), bar.getJavaFullName());
		assertEquals(getMessageField("bar3", responseInner), jpBar.getJavaFullName());

		assertEquals(getMessageField("inner1", responseInner), barInner.getJavaFullName());
		assertEquals(getMessageField("inner2", responseInner), barInner.getJavaFullName());
		assertEquals(getMessageField("inner3", responseInner), jpBarInner.getJavaFullName());

		assertEquals(getMessageField("deeper1", responseInner), barDeeper.getJavaFullName());
		assertEquals(getMessageField("deeper2", responseInner), barDeeper.getJavaFullName());
		assertEquals(getMessageField("deeper3", responseInner), jpBarDeeper.getJavaFullName());

		assertEquals(getMessageField("bar1", responseDeeper), bar.getJavaFullName());
		assertEquals(getMessageField("bar2", responseDeeper), bar.getJavaFullName());
		assertEquals(getMessageField("bar3", responseDeeper), jpBar.getJavaFullName());

		assertEquals(getMessageField("inner1", responseDeeper), barInner.getJavaFullName());
		assertEquals(getMessageField("inner2", responseDeeper), barInner.getJavaFullName());
		assertEquals(getMessageField("inner3", responseDeeper), jpBarInner.getJavaFullName());

		assertEquals(getMessageField("deeper1", responseDeeper), barDeeper.getJavaFullName());
		assertEquals(getMessageField("deeper2", responseDeeper), barDeeper.getJavaFullName());
		assertEquals(getMessageField("deeper3", responseDeeper), jpBarDeeper.getJavaFullName());
	}

	static String getMessageField(String name, Message msg) {
		return msg.getField(name).type.getJavaType();
	}

}
