package io.protostuff.parser;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.UnbufferedCharStream;
import org.antlr.v4.runtime.UnbufferedTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.Test;

public class HeaderParserTest {

	private static String tryParse(CharStream input) {
		int marker = input.mark();
		ProtoHeaderLexer hlexer = new ProtoHeaderLexer(input);
		ProtoHeaderParser hparser = null;
		UnbufferedTokenStream<Token> htokens = new UnbufferedTokenStream<>(hlexer);
		hparser = new ProtoHeaderParser(htokens);
		try {
			hparser.getInterpreter().setPredictionMode(PredictionMode.SLL);
			hparser.setErrorHandler(new BailErrorStrategy());
			hparser.file();
		} catch (@SuppressWarnings("unused") ParseCancellationException e) { // ok
		}
		input.release(marker);
		return hparser.version;
	}

	@Test
	public void testDefault() {
		CharStream input = new UnbufferedCharStream(new StringReader("message test{}"));

		String version = tryParse(input);
		assertEquals("proto2", version);
		assertEquals("m", Character.toString((char) input.LA(1)));
	}

	@Test
	public void testVersion() {
		CharStream input = new UnbufferedCharStream(new StringReader("// comment\n\n \tsyntax =\t \"proto3\";message test{}"));

		String version = tryParse(input);
		assertEquals("proto3", version);
		assertEquals("m", Character.toString((char) input.LA(1)));
	}

	@Test
	public void testEmpty() {
		CharStream input = new UnbufferedCharStream(new StringReader("// comment\n\n"));

		String version = tryParse(input);
		assertEquals("proto2", version);
	}
}
