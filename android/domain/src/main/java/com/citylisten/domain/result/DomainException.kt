package com.citylisten.domain.result

class DomainException(
    val error: DomainError
) : RuntimeException(error.message, error.cause)
