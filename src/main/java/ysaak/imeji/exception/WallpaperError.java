package ysaak.imeji.exception;

import org.springframework.http.HttpStatus;

public enum WallpaperError implements ErrorCode {
	WALL_UPLOAD_IMAGE_LOADING_ERROR("Error while loading uploaded image", "I-WALL-UPLOAD-T-001", HttpStatus.INTERNAL_SERVER_ERROR),
	WALL_UPLOAD_IMAGE_WRITING_ERROR("Error while writing image on disk", "I-WALL-UPLOAD-T-002", HttpStatus.INTERNAL_SERVER_ERROR),
	WALL_UPLOAD_FILE_TYPE_DETECTION_ERROR("Error while detecting file type", "I-WALL-UPLOAD-T-003", HttpStatus.INTERNAL_SERVER_ERROR),
	WALL_DELETE_IMAGE_ERROR("Error while deleting image on disk", "I-WALL-UPLOAD-T-004", HttpStatus.INTERNAL_SERVER_ERROR),

	WALL_UPLOAD_EXTENSION_NOT_ALLOWED_ERROR("Extension '%s' is not allowed", "I-WALL-UPLOAD-B-001", HttpStatus.BAD_REQUEST),
	WALL_UPLOAD_FILE_TYPE_NOT_ALLOWED_ERROR("File type '%s' is not allowed", "I-WALL-UPLOAD-B-002", HttpStatus.BAD_REQUEST),

	WALL_UPLOAD_SIMILAR_WALLPAPER_UPLOADED_ERROR("A similar wallpaper (%s) is already uploaded", "I-WALL-UPLOAD-B-003", HttpStatus.BAD_REQUEST),
	;

	private final String message;
	private final String code;
	private final HttpStatus status;

	WallpaperError(String message, String code, HttpStatus status) {
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
