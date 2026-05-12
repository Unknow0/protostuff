package io.protostuff.parser.builder;

import java.util.ArrayList;
import java.util.List;

public class RangesBuilder extends AbstractBuilder {
	protected final List<int[]> ranges;

	public RangesBuilder(ProtoBuilder proto, AbstractBuilder parent) {
		super(proto, parent);
		this.ranges = new ArrayList<>();
	}

	public void addRange(int start, int end) {
		ranges.add(new int[] { start, end });
	}
}
