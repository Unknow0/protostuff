package io.protostuff.core;

import java.io.IOException;
import java.io.OutputStream;

import io.protostuff.api.LinkedBuffer;
import io.protostuff.api.Output;

/**
 * Output for protostuff format into an OutputStream
 */
public final class ProtostuffStreamOutput extends ProtostuffOutput implements Output {

	private final OutputStream out;

	/**
	 * new protostuffStreamOutput
	 * @param out output to write to
	 * @param bufSize size of buffers
	 */
	public ProtostuffStreamOutput(OutputStream out, int bufSize) {
		this(out, LinkedBuffer.allocate(bufSize), bufSize);
	}

	/**
	 * new protostuffStreamOutput
	 * @param out output to write to
	 * @param head temp buffer to use
	 * @param bufSize size of next buffer
	 */
	public ProtostuffStreamOutput(OutputStream out, LinkedBuffer head, int bufSize) {
		super(head, bufSize);
		this.out = out;
	}

	@Override
	protected LinkedBuffer writeBuffers(LinkedBuffer head) throws IOException {
		LinkedBuffer.writeTo(out, head);
		return head.clear();
	}
}
