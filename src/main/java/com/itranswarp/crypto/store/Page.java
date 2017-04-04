package com.itranswarp.crypto.store;

import java.util.Collections;
import java.util.List;

public class Page<T> {

	public static int DEFAULT_PAGE_SIZE = 10;

	private final int pageNumber;
	private final int pageSize;
	private final int totalPages;
	private final int totalElements;
	private final List<T> content;

	public static <K> Page<K> empty(int pageSize) {
		return new Page<>(1, pageSize, 0, Collections.emptyList());
	}

	public Page(int pageNumber, int pageSize, int totalElements, List<T> content) {
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
		this.totalElements = totalElements;
		this.totalPages = totalElements / pageSize + (totalElements % pageSize > 0 ? 1 : 0);
		this.content = content;
	}

	/**
	 * Returns the number (starts from 1) of the current Page.
	 */
	public int getPageNumber() {
		return this.pageNumber;
	}

	/**
	 * Returns the size of the Page.
	 */
	public int getPageSize() {
		return this.pageSize;
	}

	/**
	 * Returns the number of total pages.
	 */
	public int getTotalPages() {
		return this.totalPages;
	}

	/**
	 * Returns the total amount of elements.
	 */
	public int getTotalElements() {
		return this.totalElements;
	}

	/**
	 * Returns the page content as {@link List}.
	 */
	public List<T> getContent() {
		return this.content;
	}

	/**
	 * Returns if there is a next Page.
	 */
	public boolean hasNext() {
		return this.pageNumber < this.totalPages;
	}

	/**
	 * Returns if there is a previous Page.
	 */
	public boolean hasPrevious() {
		return this.pageNumber > 1;
	}

}
