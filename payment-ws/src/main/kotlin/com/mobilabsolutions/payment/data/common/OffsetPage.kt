package com.mobilabsolutions.payment.data.common

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

/**
 * Pageable implementation which allows mid-page offsets. If the pageable starts at a min-page offset, the page number
 * is considered the previous page number.
 *
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
class OffsetPage(private val offset: Int, private val size: Int, private var sort: Sort) : Pageable {

    constructor(offset: Int, size: Int) : this(offset, size, Sort.unsorted())

    override fun getPageNumber(): Int {
        return this.offset / this.size
    }

    override fun getPageSize(): Int {
        return this.size
    }

    override fun getOffset(): Long {
        return this.offset.toLong()
    }

    override fun getSort(): Sort {
        return this.sort
    }

    override fun next(): Pageable {
        return OffsetPage(this.offset + this.size, this.size)
    }

    override fun previousOrFirst(): Pageable {
        return OffsetPage(if (this.offset < this.size) 0 else this.offset - this.size, this.size)
    }

    override fun first(): Pageable {
        return OffsetPage(0, this.size)
    }

    override fun hasPrevious(): Boolean {
        return this.offset > 0
    }
}