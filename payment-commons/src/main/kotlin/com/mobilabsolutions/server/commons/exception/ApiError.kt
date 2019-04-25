package com.mobilabsolutions.server.commons.exception

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.google.common.base.MoreObjects
import com.google.common.base.Preconditions.checkNotNull
import com.google.common.collect.ImmutableMap
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception
import java.util.Objects
import java.util.Optional

class ApiError private constructor(details: Map<String, Any>) {

    private val details: ImmutableMap<String, Any>
    private lateinit var httpStatus: HttpStatus

    init {
        this.details = ImmutableMap.copyOf(checkNotNull(details, DETAILS_PROPERTY))
    }

    private constructor(details: Map<String, Any>, httpStatus: HttpStatus) : this(details) {
        this.httpStatus = httpStatus
    }

    companion object {

        private const val MESSAGE_PROPERTY = "error_description"
        private const val CODE_PROPERTY = "error_code"
        private const val ERROR_PROPERTY = "error"
        private const val DETAILS_PROPERTY = "details"

        fun ofMessage(message: String): ApiError {
            return ApiError(ImmutableMap.of<String, Any>(MESSAGE_PROPERTY, message))
        }

        fun ofError(error: String): ApiError {
            return ApiError(ImmutableMap.of<String, Any>(ERROR_PROPERTY, error))
        }

        fun ofErrorCode(errorCode: ApiErrorCode, message: String? = null): ApiError {
            return ApiError(ImmutableMap.of<String, Any>(CODE_PROPERTY, errorCode.code, MESSAGE_PROPERTY, message ?: errorCode.message), errorCode.httpStatus)
        }

        fun ofDetails(details: Map<String, Any>): ApiError {
            return ApiError(details)
        }

        fun builder(): Builder {
            return Builder()
        }
    }

    fun asBadRequest(): ApiException {
        return ApiException(HttpStatus.BAD_REQUEST, this)
    }

    fun asBadConfiguration(): ApiException {
        return ApiException(HttpStatus.UNPROCESSABLE_ENTITY, this)
    }

    fun asUnauthorized(): ApiException {
        return ApiException(HttpStatus.UNAUTHORIZED, this)
    }

    fun asForbidden(): ApiException {
        return ApiException(HttpStatus.FORBIDDEN, this)
    }

    fun asNotFound(): ApiException {
        return ApiException(HttpStatus.NOT_FOUND, this)
    }

    fun asConflict(): ApiException {
        return ApiException(HttpStatus.CONFLICT, this)
    }

    fun asInternalServerError(): ApiException {
        return ApiException(HttpStatus.INTERNAL_SERVER_ERROR, this)
    }

    fun asInternalServerError(cause: Throwable): ApiException {
        return ApiException(cause, HttpStatus.INTERNAL_SERVER_ERROR, this)
    }

    fun asInternalServerError(exceptionMessage: String, cause: Throwable): ApiException {
        return ApiException(exceptionMessage, cause, HttpStatus.INTERNAL_SERVER_ERROR, this)
    }

    fun asInternalServerError(exceptionMessage: String): ApiException {
        return ApiException(exceptionMessage, HttpStatus.INTERNAL_SERVER_ERROR, this)
    }

    fun asException(): ApiException {
        return ApiException(this)
    }

    fun asException(status: HttpStatus): ApiException {
        return ApiException(status, this)
    }

    fun asException(status: HttpStatus, exceptionMessage: String): ApiException {
        return ApiException(exceptionMessage, status, this)
    }

    fun asException(
        status: HttpStatus,
        exceptionMessage: String,
        cause: Throwable
    ): ApiException {
        return ApiException(exceptionMessage, cause, status, this)
    }

    fun asException(status: HttpStatus, cause: Throwable): ApiException {
        return ApiException(cause, status, this)
    }

    fun asOAuth2Exception(): OAuth2Exception {
        return OAuth2Exception(this.toString())
    }

    @JsonAnyGetter
    fun details(): Map<String, Any> {
        return details
    }

    @JsonIgnore
    fun message(): Optional<String> {
        val message = details[MESSAGE_PROPERTY]
        return if (message is String) Optional.of(message) else Optional.empty()
    }

    @JsonIgnore
    fun httpStatus(): HttpStatus {
        return httpStatus
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val apiError = other as ApiError?
        return details == apiError!!.details
    }

    override fun hashCode(): Int {
        return Objects.hash(details)
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add(DETAILS_PROPERTY, details)
            .toString()
    }

    class Builder {

        private val builder = ImmutableMap.builder<String, Any>()

        fun withMessage(message: String): Builder {
            builder.put(MESSAGE_PROPERTY, message)
            return this
        }

        fun withErrorCode(errorCode: ApiErrorCode): Builder {
            builder.put(CODE_PROPERTY, errorCode.code)
            return this
        }

        fun withError(error: String): Builder {
            builder.put(ERROR_PROPERTY, error)
            return this
        }

        fun withProperty(name: String, value: Any): Builder {
            builder.put(name, value)
            return this
        }

        fun build(): ApiError {
            return ApiError(builder.build())
        }
    }
}
