package io.protostuff.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchemaRegistry {
	private static final Logger logger = LoggerFactory.getLogger(SchemaRegistry.class);

	private static final Comparator<SchemaFactory> CMP = (f1, f2) -> Integer.compare(f2.priority(), f2.priority());
	@SuppressWarnings("rawtypes")
	private static final ClassValue<Schema> CACHE = new SchemaClassValue();
	private static volatile SchemaFactory[] FACTORIES;

	static {
		List<SchemaFactory> list = new ArrayList<>();
		for (SchemaFactory f : ServiceLoader.load(SchemaFactory.class))
			list.add(f);
		list.sort(CMP);
		FACTORIES = list.toArray(new SchemaFactory[0]);
	}

	@SuppressWarnings("unchecked")
	public static <T> Schema<T> get(Class<T> clazz) {
		return CACHE.get(clazz);
	}

	public static synchronized void register(SchemaFactory f) {
		int l = FACTORIES.length;
		SchemaFactory[] newF = Arrays.copyOf(FACTORIES, l + 1);
		int i = 0;
		int p = f.priority();
		while (i < l && newF[i].priority() < p)
			i++;
		if (i < l)
			System.arraycopy(newF, i, newF, i + 1, l - i);
		newF[i] = f;
		FACTORIES = newF;
	}

	@SuppressWarnings("rawtypes")
	private static class SchemaClassValue extends ClassValue<Schema> {
		@Override
		protected Schema computeValue(Class<?> type) {
			SchemaFactory[] f = FACTORIES;
			for (int i = 0; i < f.length; i++) {
				Schema s = f[i].tryCreate(type);
				if (s != null) {
					checkDupe(f, i, type);
					return s;
				}
			}
			return null;
		}

		private void checkDupe(SchemaFactory[] f, int i, Class<?> type) {
			SchemaFactory o = f[i++];
			int p = o.priority();
			while (i < f.length) {
				SchemaFactory s = f[i++];
				if (s.priority() != p)
					return;
				if (s.tryCreate(type) != null) {
					logger.warn("Duplicate factory found for {}: {} and {}", type, o.getClass().getName(), s.getClass().getName());
					return;
				}
			}
		}
	}
}