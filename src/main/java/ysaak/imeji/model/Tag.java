package ysaak.imeji.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import ysaak.imeji.data.TagType;

@Document(indexName = "tag")
public class Tag {

	@Id
	private String id;

	@Field(type = FieldType.keyword)
	private String code;

	private TagType type;

	private int wallpaperCount;

	public Tag() {
	}

	public Tag(String code) {
		this.code = code;
		this.type = TagType.GENERAL;
		this.wallpaperCount = 0;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public TagType getType() {
		return type;
	}

	public void setType(TagType type) {
		this.type = type;
	}

	public int getWallpaperCount() {
		return wallpaperCount;
	}

	public void setWallpaperCount(int wallpaperCount) {
		this.wallpaperCount = wallpaperCount;
	}
}
