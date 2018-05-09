package ysaak.imeji.utils;

import java.util.Collection;

public final class CollectionUtils {
	private CollectionUtils() {
	}

	/**
	 * Check if the collection is not null and not empty
	 * @param collection Collection to test
	 * @return TRUE if the collection is not null and not empty - FALSE otherwise
	 */
	public static boolean notEmpty(Collection<?> collection) {
		return collection != null && !collection.isEmpty();
	}
}
