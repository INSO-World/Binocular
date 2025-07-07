package com.inso_world.binocular.core.persistence.model

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

/**
 * Custom pagination response wrapper.
 * This class wraps a collection of items with pagination metadata.
 *
 * @param T The type of items in the page
 * @property content The actual items in the current page
 * @property totalElements The total number of items across all pages
 * @property totalPages The total number of pages
 * @property size The number of items per page
 * @property number The current page number (zero-based)
 * @property sort The sort information
 * @property first Whether this is the first page
 * @property last Whether this is the last page
 * @property numberOfElements The number of items in the current page
 */
data class Page<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val size: Int,
    val number: Int,
    val sort: Sort,
    val first: Boolean,
    val last: Boolean,
    val numberOfElements: Int,
) : Iterable<T> {
    /**
     * Returns an iterator over the elements in the content list.
     * This allows Page<T> to be used as an Iterable<T>.
     *
     * @return an Iterator over the elements in the content list
     */
    override fun iterator(): Iterator<T> = content.iterator()

    /**
     * Secondary constructor that creates a Page from a list of items and a Pageable object.
     *
     * @param content The actual items in the current page
     * @param totalElements The total number of items across all pages
     * @param pageable The Spring Pageable object
     */
    constructor(content: List<T>, totalElements: Long, pageable: Pageable) : this(
        content = content,
        totalElements = totalElements,
        totalPages = if (pageable.pageSize > 0) Math.ceil(totalElements.toDouble() / pageable.pageSize).toInt() else 0,
        size = pageable.pageSize,
        number = pageable.pageNumber,
        sort = pageable.sort,
        first = pageable.pageNumber == 0,
        last =
            pageable.pageNumber == (if (pageable.pageSize > 0) Math.ceil(totalElements.toDouble() / pageable.pageSize).toInt() - 1 else 0),
        numberOfElements = content.size,
    )
}
