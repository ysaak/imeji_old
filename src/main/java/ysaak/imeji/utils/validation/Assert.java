package ysaak.imeji.utils.validation;

import ysaak.imeji.exception.ErrorCode;
import ysaak.imeji.exception.ImejiException;

public final class Assert {
	private Assert() {
	}

	public static void checkNotNull(Object reference, ErrorCode errorCode, Object... args) throws ImejiException {
		if (reference == null) {
			throw new ImejiException(errorCode, args);
		}
	}

	public static void isTrue(boolean expression, ErrorCode errorCode, Object... args) throws ImejiException {
		if (!expression) {
			throw new ImejiException(errorCode, args);
		}
	}

	public static void isFalse(boolean expression, ErrorCode errorCode, Object... args) throws ImejiException {
		if (expression) {
			throw new ImejiException(errorCode, args);
		}
	}
}
