package io.protostuff.compiler;

import org.antlr.stringtemplate.StringTemplateGroup;

import junit.framework.TestCase;

/**
 * @author Ryan Rawson
 */
public class ProtoToJavaBeanCompilerTest extends TestCase {

	public void testSimpleLoad() {

		StringTemplateGroup group = STCodeGenerator.getSTG("java_bean_primitives");

		group.getInstanceOf("message_block");

		assertEquals(0, STCodeGenerator.errorCount);
	}
}
