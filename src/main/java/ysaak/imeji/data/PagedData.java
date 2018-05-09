package ysaak.imeji.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Iterator;
import java.util.List;

public class PagedData<T> {

	private final long totalElements;
	private final Pageable pageable;
	private final List<T> content;

	public PagedData(final Page<T> springPage, final Pageable pageable) {
		content = springPage.getContent();
		this.pageable = pageable;
		totalElements = springPage.getTotalElements();
	}

	public int getTotalPages() {
		return pageable.getPageSize() > 0 ? (int) Math.ceil(((double) totalElements) / pageable.getPageSize()) : 0;
	}

	public long getTotalElements() {
		return totalElements;
	}

	public int getNumber() {
		return pageable.isPaged() ? pageable.getPageNumber() : 0;
	}

	public int getPreviousNumber() {
		return hasPrevious() ? previousPageable().getPageNumber() : getNumber();
	}

	public int getNextNumber() {
		return hasNext() ? nextPageable().getPageNumber() : getNumber();
	}

	public int getSize() {
		return pageable.isPaged() ? pageable.getPageSize() : 0;
	}

	public int getNumberOfElements() {
		return content.size();
	}

	public List<T> getContent() {
		return content;
	}

	public boolean hasContent() {
		return !content.isEmpty();
	}

	public Sort getSort() {
		return pageable.getSort();
	}

	public boolean isFirst() {
		return !hasPrevious();
	}

	public boolean isLast() {
		return !hasNext();
	}

	public boolean hasNext() {
		return getNumber() + 1 < getTotalPages();
	}

	public boolean hasPrevious() {
		return getNumber() > 0;
	}

	public Pageable nextPageable() {
		return hasNext() ? pageable.next() : Pageable.unpaged();
	}

	public Pageable previousPageable() {
		return hasPrevious() ? pageable.previousOrFirst() : Pageable.unpaged();
	}

	public Iterator<T> iterator() {
		return content.iterator();
	}

	@Override
	public String toString() {
		String contentType = "UNKNOWN";
		List<T> content = getContent();

		if (content.size() > 0) {
			contentType = content.get(0).getClass().getName();
		}

		return String.format("Page %s of %d containing %s instances", getNumber() + 1, getTotalPages(), contentType);
	}
}
