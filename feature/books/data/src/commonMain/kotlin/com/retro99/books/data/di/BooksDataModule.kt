package com.retro99.books.data.di

import com.retro99.books.domain.BooksRepository
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.mp.KoinPlatform.getKoin

@Module
@Configuration
@ComponentScan("com.retro99.books.data")
class BooksDataModule {

    @Single
    fun provideAllBooksRepositories(): List<BooksRepository> {
        return getKoin().getAll<BooksRepository>()
    }
}

