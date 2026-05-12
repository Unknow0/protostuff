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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenFactory;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.UnbufferedCharStream;
import org.antlr.v4.runtime.UnbufferedTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import io.protostuff.parser.Proto.Loader;
import io.protostuff.parser.builder.ProtoBuilder;

/**
 * Utility for loading protos from various input.
 *
 * @author David Yu
 * @created Dec 24, 2009
 */
public final class ProtoUtil {

	private ProtoUtil() {
	}

	private static <T extends Parser> T setup(T parser) {
		parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
		parser.setErrorHandler(new BailErrorStrategy());
		parser.setBuildParseTree(false);
		return parser;
	}

	public static Proto loadFrom(CharStream input, ProtoBuilder target) {
		try {
			int marker = input.mark();
			ProtoHeaderLexer hlexer = new ProtoHeaderLexer(input);
			UnbufferedTokenStream<Token> htokens = new UnbufferedTokenStream<>(hlexer);
			ProtoHeaderParser hparser = setup(new ProtoHeaderParser(htokens));
			try {
				hparser.file();
			} catch (@SuppressWarnings("unused") ParseCancellationException e) { // ok
			}
			input.release(marker);
			Proto2Listener listener = new Proto2Listener(target);
			AbstractParser setup = setup(getParser(hparser.version, input, hlexer.getLine()));
			setup.addParseListener(listener);
			setup.load();
			return target.build();
		} catch (IllegalStateException e) {
			throw e;
		} catch (Exception e) {
			throw ProtoParserException.build(target, e);
		}
	}

	private static AbstractParser getParser(String version, CharStream input, int line) {
		ProtoLexer lexer = new ProtoLexer(input);
		lexer.setTokenFactory(new CommonTokenFactory(true));
		lexer.getInterpreter().setLine(line);
		UnbufferedTokenStream<Token> htokens = new UnbufferedTokenStream<>(lexer);
		if ("proto2".equals(version))
			return new Proto2Parser(htokens);
		throw new IllegalStateException("Invalid version " + version);
	}

	/**
	 * Loads the proto from a {@link InputStream}.
	 */
	public static Proto loadFrom(InputStream in, ProtoBuilder target) {
		UnbufferedCharStream input = new UnbufferedCharStream(in);
		input.name = target.getSource().toString();
		return loadFrom(input, target);
	}

	/**
	 * Loads the proto from a {@link Reader}.
	 */
	public static Proto loadFrom(Reader reader, ProtoBuilder target) {
		UnbufferedCharStream input = new UnbufferedCharStream(reader);
		input.name = target.getSource().toString();
		return loadFrom(input, target);
	}

	public static Proto parseProto(Path file) {
		ProtoBuilder proto = new ProtoBuilder(file.toUri());
		try (InputStream in = Files.newInputStream(file)) {
			return loadFrom(in, proto);
		} catch (IOException e) {
			throw ProtoParserException.build(proto, e);
		}
	}

	public static Proto parseProto(File file) {
		return parseProto(file, DefaultProtoLoader.DEFAULT_INSTANCE);
	}

	public static Proto parseProto(File file, Loader loader) {
		ProtoBuilder proto = new ProtoBuilder(file.toURI(), loader);
		try (InputStream in = new FileInputStream(file)) {
			return loadFrom(in, proto);
		} catch (IOException e) {
			throw ProtoParserException.build(proto, e);
		}
	}

	public static Proto parseProto(URL resource) {
		return parseProto(resource, DefaultProtoLoader.DEFAULT_INSTANCE);
	}

	public static Proto parseProto(URL resource, Loader loader) {
		try (InputStream in = resource.openStream()) {
			ProtoBuilder proto = new ProtoBuilder(resource.toURI(), loader);
			return loadFrom(in, proto);
		} catch (IOException | URISyntaxException e) {
			throw new ProtoParserException("Failed to parse " + resource, e);
		}
	}

	public static StringBuilder toCamelCase(String name) {
		StringBuilder buffer = new StringBuilder();
		int toUpper = 0;
		char c;
		for (int i = 0, len = name.length(); i < len;) {
			c = name.charAt(i++);
			if (c == '_') {
				if (i == len) {
					break;
				}
				if (buffer.length() != 0) {
					toUpper++;
				}
				continue;
			} else if (toUpper != 0) {
				if (c > 96 && c < 123) {
					buffer.append((char) (c - 32));
					toUpper = 0;
				} else if (c > 64 && c < 91) {
					buffer.append(c);
					toUpper = 0;
				} else {
					while (toUpper > 0) {
						buffer.append('_');
						toUpper--;
					}
					buffer.append(c);
				}
			} else {
				if (buffer.length() == 0 && c > 64 && c < 91) {
					buffer.append((char) (c + 32));
				} else {
					buffer.append(c);
				}
			}
		}
		return buffer;
	}

	public static StringBuilder toPascalCase(String name) {
		StringBuilder buffer = toCamelCase(name);
		char c = buffer.charAt(0);
		if (c > 96 && c < 123) {
			buffer.setCharAt(0, (char) (c - 32));
		}

		return buffer;
	}

	public static StringBuilder toUnderscoreCase(String name) {
		StringBuilder buffer = new StringBuilder();
		boolean toLower = false, appendUnderscore = false;
		for (int i = 0, len = name.length(); i < len;) {
			char c = name.charAt(i++);
			if (c == '_') {
				if (i == len) {
					break;
				}
				if (buffer.length() != 0) {
					appendUnderscore = true;
				}

				continue;
			}

			if (appendUnderscore) {
				buffer.append('_');
			}

			if (c > 96 && c < 123) {
				buffer.append(c);
				toLower = true;
			} else if (c > 64 && c < 91) {
				if (toLower) {
					// avoid duplicate underscore
					if (!appendUnderscore) {
						buffer.append('_');
					}
					toLower = false;
				}
				buffer.append((char) (c + 32));
			} else {
				buffer.append(c);
				toLower = false;
			}
			appendUnderscore = false;
		}
		return buffer;
	}

	public static IllegalStateException err(String msg, Proto proto) {
		if (proto == null) {
			return new IllegalStateException(msg);
		}

		return new IllegalStateException(msg + " [" + proto.getSourcePath() + "]");
	}
}
