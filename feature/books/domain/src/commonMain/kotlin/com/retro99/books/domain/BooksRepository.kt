package com.retro99.books.domain

import com.retro99.base.repository.BaseRepository
import com.retro99.books.domain.model.BookDomainModel
import kotlinx.coroutines.flow.Flow

interface BooksRepository : BaseRepository {

    fun getBooks(): Flow<List<BookDomainModel>>

    fun getBook(uuid: String): Flow<BookDomainModel>
}

