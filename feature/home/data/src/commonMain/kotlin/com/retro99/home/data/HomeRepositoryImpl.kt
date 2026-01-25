package com.retro99.home.data

import com.retro99.home.domain.HomeRepository
import org.koin.core.annotation.Single

@Single(binds = [HomeRepository::class])
class HomeRepositoryImpl : HomeRepository {
    // Implement repository methods here
}

