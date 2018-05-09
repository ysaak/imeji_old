package ysaak.imeji.exception;

import org.springframework.http.HttpStatus;

public enum TechnicalError implements ErrorCode {
	NULL_POINTER("%s is null", "I-COMN-T-001", HttpStatus.INTERNAL_SERVER_ERROR),
	ILLEGAL_ARGUMENT("%s is invalid%s", "I-COMN-T-002", HttpStatus.INTERNAL_SERVER_ERROR)
	;

	private final String message;
	private final String code;
	private final HttpStatus status;

	TechnicalError(String message, String code, HttpStatus status) {
		this.message = message;
		this.code = code;
		this.status = status;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public HttpStatus getStatus() {
		return status;
	}

	@Override
	public boolean isTechnical() {
		return true;
	}
}
