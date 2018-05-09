package ysaak.imeji.dto;

import ysaak.imeji.data.Rating;

public class WallpaperDto {
	private String id;
	private String tags;
	private Rating rating;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public Rating getRating() {
		return rating;
	}

	public void setRating(Rating rating) {
		this.rating = rating;
	}

	@Override
	public String toString() {
		return "WallpaperDto{" +
				"id='" + id + '\'' +
				", tags='" + tags + '\'' +
				", rating=" + rating +
				'}';
	}
}
