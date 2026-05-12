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
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import io.protostuff.parser.Field.Modifier;
import junit.framework.TestCase;

/**
 * Various tests for the proto parser.
 *
 * @author David Yu
 * @created Dec 18, 2009
 */
public class ProtoParserTest extends TestCase {
	static InputStream getStream(String path) {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
	}

	static URL getResource(String path) {
		return Thread.currentThread().getContextClassLoader().getResource(path);
	}

	static File getFile(String path) throws URISyntaxException {
		URL resource = getResource(path);
		if (resource == null) {
			throw new IllegalArgumentException("Ressource introuvable : " + path);
		}

		return new File(resource.toURI());
	}

	public void testSimple() throws Exception {
		File f = getFile("TestModel.proto");
		assertTrue(f.exists());

		Proto proto = ProtoUtil.parseProto(f);
		assertEquals("simple", proto.getPackageName());
		assertEquals("com.example.simple", proto.getJavaPackageName());
		assertEquals(2, proto.getEnumGroups().size());
		assertEquals(3, proto.getMessages().size());

		Message foo = proto.getMessage("Foo");
		Message bar = proto.getMessage("Bar");
		Message baz = proto.getMessage("Baz");
		assertNotNull(foo);
		assertNotNull(bar);
		assertNotNull(baz);

		assertEquals(1, foo.getNestedEnumGroups().size());
		EnumGroup enumSample = foo.getNestedEnumGroup("EnumSample");
		assertNotNull(enumSample);
		assertEquals(5, enumSample.getFieldCount());
		assertEquals("TYPE0", enumSample.getField(0).getName());
		assertEquals("TYPE1", enumSample.getField(1).getName());
		assertEquals("TYPE2", enumSample.getField(2).getName());
		assertEquals("TYPE3", enumSample.getField(3).getName());
		assertEquals("TYPE4", enumSample.getField(4).getName());

		assertTrue(foo.getFields().size() == 9);
		Field foo_some_int = foo.getField("some_int");
		Field foo_some_string = foo.getField("some_string");
		Field foo_bar = foo.getField("bar");
		Field foo_some_enum = foo.getField("some_enum");
		Field foo_some_bytes = foo.getField("some_bytes");
		Field foo_some_boolean = foo.getField("some_boolean");
		Field foo_some_float = foo.getField("some_float");
		Field foo_some_double = foo.getField("some_double");
		Field foo_some_long = foo.getField("some_long");

		assertTrue(foo_some_int != null && foo_some_int.modifier == Modifier.REPEATED);
		assertTrue(foo_some_string != null && foo_some_string.modifier == Modifier.REPEATED);
		assertTrue(foo_bar != null && foo_bar.modifier == Modifier.REPEATED);
		assertTrue(foo_some_enum != null && foo_some_enum.modifier == Modifier.REPEATED);
		assertTrue(foo_some_bytes != null && foo_some_bytes.modifier == Modifier.REPEATED);
		assertTrue(foo_some_boolean != null && foo_some_boolean.modifier == Modifier.REPEATED);
		assertTrue(foo_some_float != null && foo_some_float.modifier == Modifier.REPEATED);
		assertTrue(foo_some_double != null && foo_some_double.modifier == Modifier.REPEATED);
		assertTrue(foo_some_long != null && foo_some_long.modifier == Modifier.REPEATED);

		Field bar_some_int = bar.getField("some_int");
		Field bar_some_string = bar.getField("some_string");
		Field bar_baz = bar.getField("baz");
		Field bar_some_enum = bar.getField("some_enum");
		Field bar_some_bytes = bar.getField("some_bytes");
		Field bar_some_boolean = bar.getField("some_boolean");
		Field bar_some_float = bar.getField("some_float");
		Field bar_some_double = bar.getField("some_double");
		Field bar_some_long = bar.getField("some_long");

		assertTrue(bar_some_int != null && bar_some_int.modifier == Modifier.OPTIONAL);
		assertTrue(bar_some_string != null && bar_some_string.modifier == Modifier.OPTIONAL);
		assertTrue(bar_baz != null && bar_baz.modifier == Modifier.OPTIONAL);
		assertTrue(bar_some_enum != null && bar_some_enum.modifier == Modifier.OPTIONAL);
		assertTrue(bar_some_bytes != null && bar_some_bytes.modifier == Modifier.OPTIONAL);
		assertTrue(bar_some_boolean != null && bar_some_boolean.modifier == Modifier.OPTIONAL);
		assertTrue(bar_some_float != null && bar_some_float.modifier == Modifier.OPTIONAL);
		assertTrue(bar_some_double != null && bar_some_double.modifier == Modifier.OPTIONAL);
		assertTrue(bar_some_long != null && bar_some_long.modifier == Modifier.OPTIONAL);

		Field baz_id = baz.getField("id");
		Field baz_name = baz.getField("name");
		Field baz_timestamp = baz.getField("timestamp");
		Field baz_data = baz.getField("data");

		assertTrue(baz_id != null && baz_id.modifier == Modifier.REQUIRED);
		assertTrue(baz_name != null && baz_name.modifier == Modifier.OPTIONAL);
		assertTrue(baz_timestamp != null && baz_timestamp.modifier == Modifier.OPTIONAL);
		assertTrue(baz_data != null && baz_data.modifier == Modifier.OPTIONAL);

//		assertEquals(bar_some_int.defaultValue, Integer.valueOf(127));
//		assertEquals(new String(bar_some_string.defaultValue.getBytes(TextFormat.ISO_8859_1), "UTF-8"), "\u1234");
//		assertEquals(bar_some_float.defaultValue, Float.valueOf(127.0f));
//		assertEquals(bar_some_double.defaultValue, Double.valueOf(45.123));
//		byte[] data = baz_data.getDefaultValue();
//		assertTrue(data != null && data.length == 2);
//		assertTrue((data[0] & 0xFF) == 0xFA);
//		assertTrue((data[1] & 0xFF) == 0xCE);
	}

	@SuppressWarnings("null")
	public void testImport() throws Exception {
		File f = getFile("unittest.proto");
		assertTrue(f.exists());

		Proto proto = ProtoUtil.parseProto(f);

		Proto iProto = proto.getImportedProto(getFile("google/protobuf/unittest_import.proto"));
		assertNotNull(iProto);
		assertEquals("protobuf_unittest_import", iProto.getPackageName());
		assertEquals("com.google.protobuf.test", iProto.getJavaPackageName());
		assertTrue(iProto.getMessages().size() == 1);
		assertTrue(iProto.getEnumGroups().size() == 1);

		EnumGroup importEnum = iProto.getEnumGroup("ImportEnum");
		assertNotNull(importEnum);
		assertTrue(importEnum.getFieldCount() == 3);
		assertTrue(importEnum.getField("IMPORT_FOO").number == 7);
		assertTrue(importEnum.getField("IMPORT_BAR").number == 8);
		assertTrue(importEnum.getField("IMPORT_BAZ").number == 9);

		Message importMessage = iProto.getMessage("ImportMessage");
		assertNotNull(importMessage);

		assertTrue(importMessage.getFields().size() == 1);
		Field import_message_d = importMessage.getField("d");
		assertTrue(import_message_d != null);
		assertTrue(import_message_d.modifier == Modifier.OPTIONAL);
		assertTrue(import_message_d.number == 1);
//		assertTrue(import_message_d.defaultValue == null);

		// unittest.proto

		assertEquals("protobuf_unittest", proto.getPackageName());
		assertEquals(proto.getJavaPackageName(), proto.getPackageName());

		assertEquals(5, proto.getEnumGroups().size());

		EnumGroup foreignEnum = proto.getEnumGroup("ForeignEnum");
		assertNotNull(foreignEnum);
		assertTrue(foreignEnum.getFieldCount() == 3);

		EnumGroup testEnumWithDupValue = proto.getEnumGroup("TestEnumWithDupValue");
		assertNotNull(testEnumWithDupValue);
		assertTrue(testEnumWithDupValue.getFieldCount() == 5);
		assertEquals("FOO1", testEnumWithDupValue.getField(0).name);
		assertTrue(testEnumWithDupValue.getField(0).number == 1);
		assertEquals("FOO2", testEnumWithDupValue.getField(1).name);
		assertTrue(testEnumWithDupValue.getField(1).number == 1);
		assertEquals("BAR1", testEnumWithDupValue.getField(2).name);
		assertTrue(testEnumWithDupValue.getField(2).number == 2);
		assertEquals("BAR2", testEnumWithDupValue.getField(3).name);
		assertTrue(testEnumWithDupValue.getField(3).number == 2);
		assertEquals("BAZ", testEnumWithDupValue.getField(4).name);
		assertTrue(testEnumWithDupValue.getField(4).number == 3);

		EnumGroup testSparseEnum = proto.getEnumGroup("TestSparseEnum");
		assertNotNull(testSparseEnum);
		assertTrue(testSparseEnum.getFieldCount() == 7);
		assertTrue(testSparseEnum.getField(0).name.equals("SPARSE_E"));
		assertTrue(testSparseEnum.getField(0).number == -53452);
		assertTrue(testSparseEnum.getField(1).name.equals("SPARSE_D"));
		assertTrue(testSparseEnum.getField(1).number == -15);
		assertTrue(testSparseEnum.getField(2).name.equals("SPARSE_F"));
		assertTrue(testSparseEnum.getField(2).number == 0);
		assertTrue(testSparseEnum.getField(3).name.equals("SPARSE_G"));
		assertTrue(testSparseEnum.getField(3).number == 2);
		assertTrue(testSparseEnum.getField(4).name.equals("SPARSE_A"));
		assertTrue(testSparseEnum.getField(4).number == 123);
		assertTrue(testSparseEnum.getField(5).name.equals("SPARSE_B"));
		assertTrue(testSparseEnum.getField(5).number == 62374);
		assertTrue(testSparseEnum.getField(6).name.equals("SPARSE_C"));
		assertTrue(testSparseEnum.getField(6).number == 12589234);

		Message testAllTypes = proto.getMessage("TestAllTypes");
		assertNotNull(testAllTypes);
		assertTrue(testAllTypes.getNestedMessages().size() == 1);
		assertTrue(testAllTypes.getNestedEnumGroups().size() == 1);

		Field defaultStringPiece = testAllTypes.getField("default_string_piece");
		Field defaultCord = testAllTypes.getField("default_cord");

		assertNotNull(defaultStringPiece);
		assertEquals("STRING_PIECE", defaultStringPiece.getOption("ctype"));
		assertEquals("abc", defaultStringPiece.getOption("default"));
//		assertEquals("abc", defaultStringPiece.defaultValue);

		assertNotNull(defaultCord);
		assertEquals("CORD", defaultCord.getOption("ctype"));
		assertEquals("123", defaultCord.getOption("default"));
//		assertEquals("123", defaultCord.defaultValue);

		Message nestedMessage = testAllTypes.getNestedMessage("NestedMessage");
		assertNotNull(nestedMessage);
		EnumGroup nestedEnum = testAllTypes.getNestedEnumGroup("NestedEnum");
		assertNotNull(nestedEnum);

		Message foreignMessage = proto.getMessage("ForeignMessage");
		assertNotNull(foreignMessage);

		Field optional_nested_enum = testAllTypes.getField("optional_nested_enum");
		assertNotNull(optional_nested_enum);
//		assertTrue(nestedEnum == optional_nested_enum.getEnumGroup());

		Field optional_foreign_enum = testAllTypes.getField("optional_foreign_enum");
		assertNotNull(optional_foreign_enum);
//		assertTrue(foreignEnum == optional_foreign_enum.getEnumGroup());

		Field optional_import_enum = testAllTypes.getField("optional_import_enum");
		assertNotNull(optional_import_enum);
//		assertTrue(importEnum == optional_import_enum.getEnumGroup());

		Field optional_nested_message = testAllTypes.getField("optional_nested_message");
		assertNotNull(optional_nested_message);
//		assertTrue(nestedMessage == optional_nested_message.getMessage());

		Field optional_foreign_message = testAllTypes.getField("optional_foreign_message");
		assertNotNull(optional_foreign_message);
//		assertTrue(foreignMessage == optional_foreign_message.getMessage());

		Field optional_import_message = testAllTypes.getField("optional_import_message");
		assertNotNull(optional_import_message);
//		assertTrue(importMessage == optional_import_message.getMessage());

		Message testRequiredForeign = proto.getMessage("TestRequiredForeign");
		assertNotNull(testRequiredForeign);
		assertTrue(testRequiredForeign.getFields().size() == 3);

		Field test_required_foreign_optional_message = testRequiredForeign.getField("optional_message");
		assertNotNull(test_required_foreign_optional_message);
		assertTrue(test_required_foreign_optional_message.modifier == Modifier.OPTIONAL);

		Field test_required_foreign_repeated_message = testRequiredForeign.getField("repeated_message");
		assertNotNull(test_required_foreign_repeated_message);
		assertTrue(test_required_foreign_repeated_message.modifier == Modifier.REPEATED);

		Field dummy = testRequiredForeign.getField("dummy");
		assertNotNull(dummy);
		assertTrue(dummy.modifier == Modifier.OPTIONAL && dummy.number == 3);

		Message testForeignNested = proto.getMessage("TestForeignNested");
		assertNotNull(testForeignNested);
		Field foreign_nested = testForeignNested.getField("foreign_nested");
		assertNotNull(foreign_nested);
//		assertTrue(nestedMessage == foreign_nested.getMessage());

		Message testEmptyMessage = proto.getMessage("TestEmptyMessage");
		assertNotNull(testEmptyMessage);
		assertTrue(testEmptyMessage.getFields().size() == 0);
		assertTrue(testEmptyMessage.getNestedEnumGroups().size() == 0);
		assertTrue(testEmptyMessage.getNestedMessages().size() == 0);

		Message testReallyLargeTagNumber = proto.getMessage("TestReallyLargeTagNumber");
		assertNotNull(testReallyLargeTagNumber);

		Field a = testReallyLargeTagNumber.getField("a");
		assertNotNull(a);
		assertTrue(a.number == 1);
		Field bb = testReallyLargeTagNumber.getField("bb");
		assertNotNull(bb);
		assertTrue(bb.number == 268435455);

		Message testRecursiveMessage = proto.getMessage("TestRecursiveMessage");
		assertNotNull(testRecursiveMessage);

		Message testMutualRecursionA = proto.getMessage("TestMutualRecursionA");
		assertNotNull(testMutualRecursionA);
		Message testMutualRecursionB = proto.getMessage("TestMutualRecursionB");
		assertNotNull(testMutualRecursionB);

		Field testMutualRecursionA_bb = testMutualRecursionA.getField("bb");
		assertNotNull(testMutualRecursionA_bb);
		Field testMutualRecursionB_a = testMutualRecursionB.getField("a");
		assertNotNull(testMutualRecursionB_a);

		Message testNestedMessageHasBits = proto.getMessage("TestNestedMessageHasBits");
		assertNotNull(testNestedMessageHasBits);
		Message tnmhb_nestedMessage = testNestedMessageHasBits.getNestedMessage("NestedMessage");
		assertNotNull(tnmhb_nestedMessage);

		Field tnmhb_optional_nested_message = testNestedMessageHasBits.getField("optional_nested_message");
		assertNotNull(tnmhb_optional_nested_message);
//		assertTrue(tnmhb_nestedMessage == tnmhb_optional_nested_message.getMessage());

		Field nestedmessage_repeated_foreignmessage = tnmhb_nestedMessage.getField("nestedmessage_repeated_foreignmessage");
		assertNotNull(nestedmessage_repeated_foreignmessage);
//		assertTrue(foreignMessage == nestedmessage_repeated_foreignmessage.getMessage());

		Message testFieldOrderings = proto.getMessage("TestFieldOrderings");
		assertNotNull(testFieldOrderings);
		assertTrue(testFieldOrderings.getFields().size() == 3);
		assertEquals("my_int", testFieldOrderings.getFields().get(0).name);
		assertEquals("my_string", testFieldOrderings.getFields().get(1).name);
		assertEquals("my_float", testFieldOrderings.getFields().get(2).name);

		Message testExtremeDefaultValues = proto.getMessage("TestExtremeDefaultValues");
		assertNotNull(testExtremeDefaultValues);

		Field large_uint32 = testExtremeDefaultValues.getField("large_uint32");
		assertNotNull(large_uint32);
//		assertTrue((large_uint32.getDefaultValue().intValue() & 0xFFFFFFFF) == 0xFFFFFFFF);

		Field large_uint64 = testExtremeDefaultValues.getField("large_uint64");
		assertNotNull(large_uint64);
//		assertTrue(-1 == large_uint64.getDefaultValue().longValue());

		Field small_int32 = testExtremeDefaultValues.getField("small_int32");
		assertNotNull(small_int32);
//		assertTrue(small_int32.getDefaultValue().intValue() == -0x7FFFFFFF);

		Field small_int64 = testExtremeDefaultValues.getField("small_int64");
		assertNotNull(small_int64);
//		assertTrue(-Long.MAX_VALUE == small_int64.getDefaultValue().longValue());

		assertNotNull(proto.getExtensions());
		assertTrue(proto.getExtensions().size() > 0);
		Extension extension = proto.getExtensions().iterator().next();
		assertNotNull(extension.getFields());
		assertTrue(extension.getFields().size() > 0);

		Message testNestedExtension = proto.getMessage("TestNestedExtension");
		assertNotNull(testNestedExtension.getNestedExtensions());
		assertEquals(1, testNestedExtension.getNestedExtensions().size());
		extension = testNestedExtension.getNestedExtensions().iterator().next();
		assertTrue(extension.isNested());

		Message testMultipleExtensionRanges = proto.getMessage("TestMultipleExtensionRanges");
		assertNotNull(testMultipleExtensionRanges);
		assertEquals(3, testMultipleExtensionRanges.extensionRanges.size());
		int[] first = testMultipleExtensionRanges.extensionRanges.get(0);
		int[] second = testMultipleExtensionRanges.extensionRanges.get(1);
		int[] third = testMultipleExtensionRanges.extensionRanges.get(2);

		assertEquals(42, first[0]);
		assertEquals(42, first[1]);
		assertEquals(4143, second[0]);
		assertEquals(4243, second[1]);
		assertEquals(65536, third[0]);
		assertEquals(536870911, third[1]);
	}

	public static void main(String[] arg) throws Exception {
		new ProtoParserTest().testDescriptorProto();
	}

	public void testEnumWithTrailingSemicolon() throws Exception {
		File f = getFile("enum_with_semicolon.proto");
		assertTrue(f.exists());

		Proto proto = ProtoUtil.parseProto(f);
		assertEquals(proto.getPackageName(), "rpc");
	}

	public void testDescriptorProto() throws Exception {
		File f = getFile("descriptor.proto");
		assertTrue(f.exists());

		Proto proto = ProtoUtil.parseProto(f);
		assertEquals(proto.getPackageName(), "google.protobuf");
	}

	/*
	 * public static void main(String[] args) throws Exception { File f = getFile("unittest.proto");
	 * assertTrue(f.exists());
	 *
	 * Proto proto = new Proto(f); ProtoUtil.loadFrom(f, proto); System.err.println(Float.parseFloat("6.13e5")); byte[]
	 * b = "123".getBytes(); for(int i=0; i<b.length; i++) System.err.println(b[i]);
	 *
	 * }
	 */

}
