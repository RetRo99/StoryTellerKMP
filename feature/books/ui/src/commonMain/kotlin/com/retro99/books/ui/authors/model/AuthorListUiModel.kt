package com.retro99.books.ui.authors.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthorListUiModel(
    val uuid: String,
    val name: String,
    val fileAs: String?,
)

