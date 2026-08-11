package io.protostuff.core;

import java.io.IOException;
import java.io.OutputStream;

import io.protostuff.api.LinkedBuffer;
import io.protostuff.api.Output;

public final class ProtobufStreamOutput extends ProtobufOutput implements Output {

	private final OutputStream out;

	public ProtobufStreamOutput(OutputStream out, int bufSize) {
		this(out, LinkedBuffer.allocate(bufSize), bufSize);
	}

	public ProtobufStreamOutput(OutputStream out, LinkedBuffer head, int bufSize) {
		super(head, bufSize);
		this.out = out;
	}

	@Override
	protected LinkedBuffer writeBuffers(LinkedBuffer head) throws IOException {
		LinkedBuffer.writeTo(out, head);
		return head.clear();
	}
}
