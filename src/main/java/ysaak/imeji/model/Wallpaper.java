package ysaak.imeji.model;

import com.google.common.base.MoreObjects;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import ysaak.framework.data.Resource;
import ysaak.imeji.data.Color;
import ysaak.imeji.data.Rating;

import java.util.Date;
import java.util.List;

@Document(indexName = "wallpaper")
public class Wallpaper implements Resource {
	@Id
	private String id;

	private String type;

	private int width;
	private int height;

	private long fileSize;

	@Field(type = FieldType.Nested)
	private List<Color> palette;

	private List<String> tags;

	private Rating rating;

	private Date uploadDate;

	private long hash;

	@Override
	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(final int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(final int height) {
		this.height = height;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(final long fileSize) {
		this.fileSize = fileSize;
	}

	public List<Color> getPalette() {
		return palette;
	}

	public void setPalette(final List<Color> palette) {
		this.palette = palette;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(final List<String> tags) {
		this.tags = tags;
	}

	public Rating getRating() {
		return rating;
	}

	public void setRating(final Rating rating) {
		this.rating = rating;
	}

	public Date getUploadDate() {
		return uploadDate;
	}

	public void setUploadDate(final Date uploadDate) {
		this.uploadDate = uploadDate;
	}

	public long getHash() {
		return hash;
	}

	public void setHash(long hash) {
		this.hash = hash;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("id", id)
				.add("type", type)
				.add("width", width)
				.add("height", height)
				.add("fileSize", fileSize)
				.add("palette", palette)
				.add("rating", rating)
				.add("rating", rating)
				.add("uploadDate", uploadDate)
				.add("hash", hash)
				.toString();
	}
}
