package io.protostuff.compiler.core;

import org.junit.Test;

import core.imports.A;
import core.imports.B;

/**
 * @author Kostiantyn Shchepanovskyi
 */
public class ImportIT {
	@Test
	public void testImportedFieldIsAccessible() throws Exception {
		A a = new A();
		a.setB(new B());
	}
}
