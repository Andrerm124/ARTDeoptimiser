package com.ljmu.andre.artdeoptimiser.Utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class was created by Andre R M (SID: 701439)
 * It and its contents are free to use by all
 */

public class CollectionUtils {
	public static <K, V> Map<K, V> toMap(Collection<V> collection, KeyBinder<K, V> binder) {
		return toMap(new HashMap<>(collection.size()), collection, binder);
	}

	public static <K, V> Map<K, V> toMap(Map<K, V> map, Collection<V> collection, KeyBinder<K, V> binder) {
		if (collection == null)
			return map;

		for (V v : collection)
			map.put(binder.getKey(v), v);

		return map;
	}

	public interface KeyBinder<K, V> {
		K getKey(V mapEntry);
	}
}
