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

import java.nio.ByteBuffer;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.TokenStream;

import io.protostuff.parser.Proto2Parser.LoadContext;

/**
 * Base parser
 *
 * @author David Yu
 * @created Dec 16, 2009
 */
public abstract class AbstractParser extends Parser {

	static final boolean SUPPRESS_WARNINGS = System.getProperty("parser.suppress_warnings") != null;

	protected AbstractParser(TokenStream input) {
		super(input);
	}

	public abstract LoadContext load();

	static String getString(String value) {
		return TextFormat.unescapeText(value);
	}

	static byte[] getBytesFromStringLiteral(String literal) {
		return getBytes(literal.substring(1, literal.length() - 1));
	}

	static byte[] getBytes(String value) {
		ByteBuffer buffer = TextFormat.unescapeBytes(value, 0, value.length());
		byte[] buf = new byte[buffer.limit()];
		buffer.get(buf);
		return buf;
	}

	static byte[] getBytesFromHexString(String value) {
		int start = value.startsWith("0x") ? 2 : 0;
		int len = value.length() - start;
		if (len % 2 != 0) {
			throw new IllegalArgumentException("malformed hex string: " + value);
		}

		byte[] out = new byte[len / 2];
		for (int i = 0; i < out.length;) {
			int left = decimalFromHex(value.charAt(start++));
			int right = decimalFromHex(value.charAt(start++));
			out[i++] = (byte) ((right & 0x0F) | (left << 4 & 0xF0));
		}
		return out;
	}

	static int decimalFromHex(char c) {
		switch (c) {
			case '0':
				return 0;
			case '1':
				return 1;
			case '2':
				return 2;
			case '3':
				return 3;
			case '4':
				return 4;
			case '5':
				return 5;
			case '6':
				return 6;
			case '7':
				return 7;
			case '8':
				return 8;
			case '9':
				return 9;
			case 'a':
			case 'A':
				return 10;
			case 'b':
			case 'B':
				return 11;
			case 'c':
			case 'C':
				return 12;
			case 'd':
			case 'D':
				return 13;
			case 'e':
			case 'E':
				return 14;
			case 'f':
			case 'F':
				return 15;
			default:
				throw new IllegalArgumentException("Not a hex character: " + c);
		}
	}

	static void info(String msg) {
		System.out.println(msg);
	}

	static void warn(String msg) {
		if (!SUPPRESS_WARNINGS) {
			System.err.println(msg);
		}
	}
}
