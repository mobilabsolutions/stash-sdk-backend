/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.server.commons.exception

import mu.KLogging
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE
import org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY
import org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.FieldError
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.multipart.MultipartException
import org.springframework.web.servlet.NoHandlerFoundException
import java.io.IOException
import java.net.UnknownHostException
import java.util.Arrays
import java.util.stream.Collectors
import javax.servlet.http.HttpServletRequest
import javax.validation.ConstraintViolationException

@ResponseBody
@ControllerAdvice
class CommonExceptionHandler {

    companion object : KLogging()

    @ResponseStatus(FORBIDDEN)
    @ExceptionHandler(AccessDeniedException::class)
    fun accessDeniedException(): ApiError {
        return ApiError.ofErrorCode(ApiErrorCode.INSUFFICIENT_RIGHTS)
    }

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFoundException(exception: NoHandlerFoundException): ApiError {
        logger.error(
            "No mapping found for request: {} {}", exception.httpMethod, exception.requestURL)
        return ApiError.builder().withMessage("nothing.here").build()
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(exception: ConstraintViolationException): ApiError {
        logger.error("Bad request.", exception)
        return ApiError.builder()
            .withMessage(exception.message ?: "non readable message")
            .withError(ApiErrorCode.CONSTRAINT_VALIDATION_FAILED.name.toLowerCase())
            .withErrorCode(ApiErrorCode.CONSTRAINT_VALIDATION_FAILED)
            .build()
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MissingRequestHeaderException::class)
    fun handleMissingRequestHeaderException(exception: MissingRequestHeaderException): ApiError {
        logger.error("Bad request.", exception)
        return ApiError.builder()
            .withMessage(exception.message)
            .withError(ApiErrorCode.MISSING_REQUEST_HEADER.name.toLowerCase())
            .withErrorCode(ApiErrorCode.MISSING_REQUEST_HEADER)
            .build()
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(exception: MethodArgumentNotValidException): ApiError {
        logger.error("Bad request.", exception)
        val bindingResult = exception.bindingResult
        val errorMessage = bindingResult
            .allErrors
            .stream()
            .map { (it as FieldError).field + ": " + it.defaultMessage }
            .collect(Collectors.joining(", "))
        return ApiError.builder()
            .withMessage(errorMessage)
            .withError(ApiErrorCode.ARGUMENT_NOT_VALID.name.toLowerCase())
            .withErrorCode(ApiErrorCode.ARGUMENT_NOT_VALID)
            .build()
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(exception: HttpMessageNotReadableException): ApiError {
        logger.error("Bad request.", exception)
        return ApiError.builder()
            .withMessage("non readable message")
            .withError(ApiErrorCode.MESSAGE_NOT_READABLE.name.toLowerCase())
            .withErrorCode(ApiErrorCode.MESSAGE_NOT_READABLE)
            .build()
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MultipartException::class)
    fun handleMultipartException(exception: MultipartException): ApiError {
        if (exception is MaxUploadSizeExceededException) {
            logger.error("Multipart request is bigger than max allowed limit.", exception)
            val maxUploadSize = exception.maxUploadSize
            return ApiError.builder()
                .withMessage("max.allowed.size.in.bytes: $maxUploadSize")
                .withError(ApiErrorCode.MULTIPART_NOT_VALID.name.toLowerCase())
                .withErrorCode(ApiErrorCode.MULTIPART_NOT_VALID)
                .build()
        }

        logger.error("Multipart request expected, but not a multipart.", exception)
        return ApiError.builder()
            .withMessage("request.not.multipart")
            .withErrorCode(ApiErrorCode.MULTIPART_NOT_VALID)
            .build()
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchExceptionException(
        exception: MethodArgumentTypeMismatchException
    ): ApiError {
        logger.error("Method argument type mismatch.", exception)
        return ApiError.builder()
            .withMessage("argumentName: '${exception.name}', requiredType: '${exception.requiredType?.simpleName}' value: '${exception.value}'")
            .withError(ApiErrorCode.ARGUMENT_TYPE_MISMATCH.name.toLowerCase())
            .withErrorCode(ApiErrorCode.ARGUMENT_TYPE_MISMATCH)
            .build()
    }

    @ResponseStatus(METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(
        exception: HttpRequestMethodNotSupportedException
    ): ApiError {
        val message = "Used method: ['${exception.method}'] Supported methods: '${Arrays.toString(exception.supportedMethods)}'"
        logger.error("Method not allowed. {}", message)
        return ApiError.builder().withMessage(message).withErrorCode(ApiErrorCode.VALIDATION_ERROR).build()
    }

    @ResponseStatus(UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleHttpMediaTypeNotSupportedException(
        exception: HttpMediaTypeNotSupportedException
    ): ApiError {
        val message = "Unsupported media type: '${exception.contentType}'"
        logger.error(message, exception)
        return ApiError.builder().withMessage(message).withErrorCode(ApiErrorCode.VALIDATION_ERROR).build()
    }

    /**
     * Handles the exception that is thrown when there is a missing request query parameter. <br></br>
     * See [here](http://stackoverflow.com/a/10323055/1005102).
     *
     * @param exception the thrown exception
     * @return the error response DTO
     */
    @ResponseStatus(UNPROCESSABLE_ENTITY)
    @ExceptionHandler(UnsatisfiedServletRequestParameterException::class)
    fun handleUnsatisfiedServletRequestParameterException(
        exception: UnsatisfiedServletRequestParameterException
    ): ApiError {
        logger.error("Unprocessable entity.", exception)
        return ApiError.builder().withMessage("Missing request parameter").withErrorCode(ApiErrorCode.VALIDATION_ERROR).build()
    }

    @ResponseStatus(UNPROCESSABLE_ENTITY)
    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameterException(
        exception: MissingServletRequestParameterException
    ): ApiError {
        logger.error("Unprocessable entity.", exception)
        return ApiError.builder().withMessage("Missing request parameter").withErrorCode(ApiErrorCode.VALIDATION_ERROR).build()
    }

    @ExceptionHandler(ApiException::class)
    fun handleApiException(apiException: ApiException): ResponseEntity<ApiError> {
        val status = apiException.status()
        when {
            status.is4xxClientError -> logger.error("Api error", apiException)
            status.is5xxServerError -> logger.error("Api error", apiException)
            else -> logger.warn(
                "Api exception with non-error http status code. Should never happen!", apiException
            )
        }
        return ResponseEntity(apiException.apiError(), status)
    }

    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception::class)
    fun handleException(request: HttpServletRequest, exception: Exception?): ApiError {
        logger.error(
            "Error on request: [{} {}]. Message: {}",
            request.method,
            request.requestURI,
            exception?.message,
            exception
        )
        return ApiError.builder()
            .withErrorCode(ApiErrorCode.SDK_GENERAL_ERROR)
            .withMessage(if (exception?.message != null) exception.message!! else "Unknown exception occurred.")
            .build()
    }

    @ResponseStatus(SERVICE_UNAVAILABLE)
    @ExceptionHandler(ResourceAccessException::class)
    fun handleResourceAccessException(exception: ResourceAccessException): ApiError {
        logger.error("Service unavailable.", exception)
        return ApiError.builder()
            .withMessage(exception.message!!)
            .build()
    }

    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler(UnknownHostException::class)
    fun handleUnknownHostException(exception: UnknownHostException): ApiError {
        logger.error("Unknown host exception.", exception)
        return ApiError.builder()
            .withErrorCode(ApiErrorCode.SDK_GENERAL_ERROR)
            .withMessage(exception.message!!)
            .build()
    }

    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler(IOException::class)
    fun handleIOException(exception: IOException): ApiError {
        logger.error("I/O exception.", exception)
        return ApiError.builder()
            .withErrorCode(ApiErrorCode.SDK_GENERAL_ERROR)
            .withMessage(exception.message!!)
            .build()
    }
}
