package com.retro99.books.ui.authors.model

import com.retro99.books.domain.model.PersonDomainModel

fun PersonDomainModel.toListUiModel(): AuthorListUiModel {
    return AuthorListUiModel(
        uuid = uuid,
        name = name,
        fileAs = fileAs,
    )
}

