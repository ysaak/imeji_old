package ysaak.imeji.exception;

import org.springframework.http.HttpStatus;

public class TechnicalException extends RuntimeException {

	private final String code;
	private final HttpStatus status;


	public TechnicalException(final ErrorCode error) {
		this(error.getMessage(), error.getCode(), error.getStatus(), null);
	}

	public TechnicalException(final ErrorCode error, Object...args) {
		this(String.format(error.getMessage(), args), error.getCode(), error.getStatus(), null);
	}

	public TechnicalException(final ErrorCode error, final Throwable cause) {
		this(error.getMessage(), error.getCode(), error.getStatus(), cause);
	}

	public TechnicalException(final ErrorCode error, final Throwable cause, Object...args) {
		this(String.format(error.getMessage(), args), error.getCode(), error.getStatus(), cause);
	}

	private TechnicalException(final String message, final String code, final HttpStatus status, final Throwable cause) {
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
