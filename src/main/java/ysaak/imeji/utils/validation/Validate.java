package ysaak.imeji.utils.validation;

import ysaak.imeji.exception.TechnicalError;
import ysaak.imeji.exception.TechnicalException;

public final class Validate {
	private Validate() {
	}

	public static <T> T checkNotNull(T reference, String fieldName) throws TechnicalException {
		if (reference == null) {
			throw new TechnicalException(TechnicalError.NULL_POINTER, fieldName);
		}
		return reference;
	}

	public static void isTrue(boolean expression, String fieldName) throws TechnicalException {
		if (!expression) {
			throw new TechnicalException(TechnicalError.ILLEGAL_ARGUMENT, fieldName, null);
		}
	}

	public static void isFalse(boolean expression, String fieldName, String complement) throws TechnicalException {
		if (expression) {
			throw new TechnicalException(TechnicalError.ILLEGAL_ARGUMENT, fieldName, complement != null ? " -- " + complement : "");
		}
	}
}
