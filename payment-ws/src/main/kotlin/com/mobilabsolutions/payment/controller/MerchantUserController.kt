/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.controller

import com.mobilabsolutions.payment.model.request.MerchantUserEditPasswordRequestModel
import com.mobilabsolutions.payment.model.request.MerchantUserEditRequestModel
import com.mobilabsolutions.payment.model.request.MerchantUserRequestModel
import com.mobilabsolutions.payment.service.UserDetailsServiceImpl
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@RestController
@RequestMapping(MerchantUserController.BASE_URL)
class MerchantUserController(private val userDetailsServiceImpl: UserDetailsServiceImpl) {

    @ApiOperation(value = "Create the merchant user by given data")
    @ApiResponses(
        ApiResponse(code = 201, message = "Successfully created a merchant user"),
        ApiResponse(code = 400, message = "Request model validation failed"),
        ApiResponse(code = 401, message = "Authentication failed"),
        ApiResponse(code = 403, message = "User doesn't have the required rights for this operation")
    )
    @RequestMapping(
        method = [RequestMethod.POST],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('admin')")
    fun createUser(
        @PathVariable("Merchant-Id") merchantId: String,
        @Valid @ApiParam(name = "Merchant-User-Info", value = "Merchant User Info Model") @RequestBody merchantUserCreateModel: MerchantUserRequestModel
    ) {
        userDetailsServiceImpl.createMerchantUser(merchantId, merchantUserCreateModel)
    }

    @ApiOperation(value = "Update the given merchant user by user id")
    @ApiResponses(
        ApiResponse(code = 204, message = "Successfully updated merchant user"),
        ApiResponse(code = 400, message = "Request model validation failed"),
        ApiResponse(code = 401, message = "Authentication failed"),
        ApiResponse(code = 403, message = "User doesn't have the required rights for this operation")
    )
    @RequestMapping(
        UPDATE_USER_URL, method = [RequestMethod.PUT],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun updateUser(
        @PathVariable("Merchant-Id") merchantId: String,
        @PathVariable("User-Id") userId: String,
        @Valid @ApiParam(name = "Merchant-User-Info", value = "Merchant User Edit Model") @RequestBody merchantUserUpdateModel: MerchantUserEditRequestModel
    ) {
        val principal = SecurityContextHolder.getContext().authentication.principal as String
        userDetailsServiceImpl.updateMerchantUser(userId, principal, merchantUserUpdateModel)
    }

    @ApiOperation(value = "Change the password of the given merchant user by user id")
    @ApiResponses(
        ApiResponse(code = 204, message = "Successfully updated merchant user's password"),
        ApiResponse(code = 400, message = "Request model validation failed"),
        ApiResponse(code = 401, message = "Authentication failed"),
        ApiResponse(code = 403, message = "User doesn't have the required rights for this operation")
    )
    @RequestMapping(
        CHANGE_USER_PASSWORD_URL, method = [RequestMethod.PUT],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority(#merchantId) or hasAuthority('admin')")
    fun changeUserPassword(
        @PathVariable("Merchant-Id") merchantId: String,
        @PathVariable("User-Id") userId: String,
        @Valid @ApiParam(name = "Merchant-User-Password-Model", value = "Merchant User Password Change Model") @RequestBody merchantUserEditPasswordModel: MerchantUserEditPasswordRequestModel
    ) {
        val principal = SecurityContextHolder.getContext().authentication.principal as String
        userDetailsServiceImpl.changePasswordMerchantUser(userId, principal, merchantUserEditPasswordModel)
    }

    @ApiOperation(value = "Send forgot password email")
    @ApiResponses(
        ApiResponse(code = 201, message = "Successfully sent email"),
        ApiResponse(code = 400, message = "Request model validation failed"),
        ApiResponse(code = 401, message = "Authentication failed"),
        ApiResponse(code = 403, message = "User doesn't have the required rights for this operation")
    )
    @RequestMapping(
        FORGOT_PASSWORD_URL,
        method = [RequestMethod.POST]
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('admin')")
    fun sendForgotPasswordEmail(
        request: HttpServletRequest,
        @RequestParam email: String,
        @PathVariable("Merchant-Id") merchantId: String
    ) = userDetailsServiceImpl.sendForgotPasswordEmail(email, request, merchantId)

    @ApiOperation(value = "Validate token and reset password")
    @ApiResponses(
        ApiResponse(code = 204, message = "Successfully reset merchant user's password"),
        ApiResponse(code = 400, message = "Request model validation failed"),
        ApiResponse(code = 401, message = "Authentication failed"),
        ApiResponse(code = 403, message = "User doesn't have the required rights for this operation")
    )
    @RequestMapping(
        RESET_PASSWORD_URL, method = [RequestMethod.PUT],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('admin')")
    fun validateTokenAndResetPassword(
        @RequestParam token: String,
        @RequestParam email: String,
        @Valid @ApiParam(name = "Merchant-User-Password-Model", value = "Merchant User Password Change Model") @RequestBody merchantUserEditPasswordModel: MerchantUserEditPasswordRequestModel
    ) = userDetailsServiceImpl.validateTokenAndResetPassword(token, email, merchantUserEditPasswordModel)

    companion object {
        const val BASE_URL = "/merchant/{Merchant-Id}/user"
        const val UPDATE_USER_URL = "/{User-Id}"
        const val CHANGE_USER_PASSWORD_URL = "/{User-Id}/change-password"
        const val FORGOT_PASSWORD_URL = "/forgot-password"
        const val RESET_PASSWORD_URL = "/reset-password"
    }
}
