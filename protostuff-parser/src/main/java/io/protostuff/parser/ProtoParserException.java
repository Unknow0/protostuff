package io.protostuff.parser;

import io.protostuff.parser.builder.ProtoBuilder;

public class ProtoParserException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ProtoParserException() {
		super();
	}

	public ProtoParserException(String msg) {
		super(msg);
	}

	public ProtoParserException(Throwable cause) {
		super(cause);
	}

	public ProtoParserException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public static ProtoParserException build(ProtoBuilder proto, Throwable cause) {
		if (cause instanceof ProtoParserException)
			return (ProtoParserException) cause;
		return new ProtoParserException("Failed to parse " + proto.getSource(), cause);
	}
}
