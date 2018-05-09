package ysaak.imeji.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
	String getCode();
	String getMessage();

	default HttpStatus getStatus() {
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	default boolean isTechnical() {
		return false;
	}
}
