package ysaak.imeji.exception;

import org.springframework.http.HttpStatus;

public class ImejiException extends Exception {

	private final String code;
	private final HttpStatus status;


	public ImejiException(final ErrorCode error) {
		this(error.getMessage(), error.getCode(), error.getStatus(), null);
	}

	public ImejiException(final ErrorCode error, Object...args) {
		this(String.format(error.getMessage(), args), error.getCode(), error.getStatus(), null);
	}

	public ImejiException(final ErrorCode error, final Throwable cause) {
		this(error.getMessage(), error.getCode(), error.getStatus(), cause);
	}

	public ImejiException(final ErrorCode error, final Throwable cause, Object...args) {
		this(String.format(error.getMessage(), args), error.getCode(), error.getStatus(), cause);
	}

	private ImejiException(final String message, final String code, final HttpStatus status, final Throwable cause) {
		super(message, cause);
		this.code = code;
		this.status = status;
	}

	public String getCode() {
		return code;
	}

	public HttpStatus getStatus() {
		return status;
	}
}
