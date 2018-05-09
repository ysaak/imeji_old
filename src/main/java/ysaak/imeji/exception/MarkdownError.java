package ysaak.imeji.exception;

import org.springframework.http.HttpStatus;

public enum MarkdownError implements ErrorCode {
	FILE_NOT_FOUND("File '%s' is not found", "I-MARK-T-001", HttpStatus.INTERNAL_SERVER_ERROR),
	PARSE_ERROR("Error while parsing file '%s'", "I-MARK-T-002", HttpStatus.INTERNAL_SERVER_ERROR),
	;

	private final String message;
	private final String code;
	private final HttpStatus status;

	MarkdownError(String message, String code, HttpStatus status) {
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
}
