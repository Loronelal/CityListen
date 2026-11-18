package com.citylisten.domain.validation

import com.citylisten.domain.result.DomainError
import com.citylisten.domain.result.DomainErrorType
import com.citylisten.domain.result.DomainResult
import javax.inject.Inject

/**
 * Validates the display name of the local guest profile.
 * Returns the normalized name (trimmed, single spaces) on success.
 */
class DisplayNameValidator @Inject constructor() {

    fun validate(rawName: String): DomainResult<String> {
        val name = rawName.trim().replace(MULTIPLE_SPACES_REGEX, " ")
        return when {
            name.isEmpty() -> invalid("Введите имя")
            name.length < MIN_LENGTH -> invalid(
                "Имя должно содержать не меньше $MIN_LENGTH символов"
            )
            name.length > MAX_LENGTH -> invalid(
                "Имя должно содержать не больше $MAX_LENGTH символов"
            )
            !NAME_REGEX.matches(name) -> invalid(
                "Имя может содержать буквы, цифры, пробелы, дефис и апостроф"
            )
            else -> DomainResult.Success(name)
        }
    }

    private fun invalid(message: String): DomainResult.Failure =
        DomainResult.Failure(DomainError(DomainErrorType.INVALID_INPUT, message))

    companion object {
        const val MIN_LENGTH = 2
        const val MAX_LENGTH = 40

        private val MULTIPLE_SPACES_REGEX = Regex(" +")
        private val NAME_REGEX = Regex("^[\\p{L}\\p{N}][\\p{L}\\p{N} .,'’-]*$")
    }
}
