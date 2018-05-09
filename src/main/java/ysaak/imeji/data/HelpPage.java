package ysaak.imeji.data;

import java.util.Objects;

public class HelpPage {
	private final String title;
	private final String content;

	public HelpPage(String title, String content) {
		this.title = title;
		this.content = content;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		HelpPage helpPage = (HelpPage) o;
		return Objects.equals(title, helpPage.title) &&
				Objects.equals(content, helpPage.content);
	}

	@Override
	public int hashCode() {
		return Objects.hash(title, content);
	}

	@Override
	public String toString() {
		return "HelpPage{" +
				"title='" + title + '\'' +
				'}';
	}
}
