package com.mobilabsolutions.payment.bspayone.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.bspayone.configuration.BsPayoneProperties
import com.mobilabsolutions.payment.bspayone.data.enum.BsPayoneClearingType
import com.mobilabsolutions.payment.bspayone.data.enum.BsPayoneRequestType
import com.mobilabsolutions.payment.bspayone.exception.BsPayoneErrors
import com.mobilabsolutions.payment.bspayone.model.BsPayonePaymentRequestModel
import com.mobilabsolutions.payment.data.domain.Alias
import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import com.mobilabsolutions.payment.data.repository.AliasRepository
import com.mobilabsolutions.payment.model.AliasExtraModel
import com.mobilabsolutions.payment.model.AuthorizeRequestModel
import com.mobilabsolutions.payment.model.PersonalDataModel
import com.mobilabsolutions.payment.model.PspAliasConfigModel
import com.mobilabsolutions.payment.model.PspPaymentResponseModel
import com.mobilabsolutions.payment.model.PspConfigListModel
import com.mobilabsolutions.payment.model.PspConfigModel
import com.mobilabsolutions.payment.service.Psp
import com.mobilabsolutions.server.commons.exception.ApiError
import mu.KLogging
import org.springframework.stereotype.Component

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Component
class BsPayonePsp(
    private val bsPayoneHashingService: BsPayoneHashingService,
    private val bsPayoneProperties: BsPayoneProperties,
    private val bsPayoneService: BSPayoneService,
    private val aliasRepository: AliasRepository,
    private val jsonMapper: ObjectMapper
) : Psp {
    companion object : KLogging()

    override fun getProvider(): PaymentServiceProvider {
        return PaymentServiceProvider.BS_PAYONE
    }

    override fun calculatePspConfig(pspConfigModel: PspConfigModel?): PspAliasConfigModel? {
        logger.info { "Random config calculation has been called..." }
        return if (pspConfigModel != null) PspAliasConfigModel(
            type = PaymentServiceProvider.BS_PAYONE.toString(),
            merchantId = pspConfigModel.merchantId,
            portalId = pspConfigModel.portalId,
            request = BsPayoneRequestType.CREDIT_CARD_CHECK.type,
            apiVersion = bsPayoneProperties.apiVersion,
            responseType = BsPayoneHashingService.RESPONSE_TYPE,
            hash = bsPayoneHashingService.makeCreditCardCheckHash(pspConfigModel),
            accountId = pspConfigModel.accountId,
            encoding = bsPayoneProperties.encoding,
            mode = bsPayoneProperties.mode,
            publicKey = null,
            privateKey = null
        ) else null
    }

    override fun authorize(authorizeRequestModel: AuthorizeRequestModel): PspPaymentResponseModel {
        val alias = aliasRepository.getFirstById(authorizeRequestModel.aliasId) ?: throw ApiError.ofMessage("Alias ID cannot be found").asBadRequest()
        val result = jsonMapper.readValue(alias.merchant?.pspConfig, PspConfigListModel::class.java)
        val pspConfig = result.psp.firstOrNull { it.type == getProvider().toString() }
            ?: throw ApiError.ofMessage("PSP configuration for '${getProvider()}' cannot be found from used merchant").asBadRequest()

        val bsPayoneAuthorizeRequest = BsPayonePaymentRequestModel(
            accountId = pspConfig.accountId,
            clearingType = getBsPayoneClearingType(alias),
            reference = authorizeRequestModel.paymentData.reason,
            amount = authorizeRequestModel.paymentData.amount.toString(),
            lastName = getPersonalData(alias)?.lastName,
            country = getPersonalData(alias)?.country,
            city = getPersonalData(alias)?.city,
            pspAlias = alias.pspAlias,
            iban = null,
            bic = null
        )

        val response = bsPayoneService.authorization(bsPayoneAuthorizeRequest, pspConfig)

        if (response.hasError()) return PspPaymentResponseModel(response.transactionId, TransactionStatus.FAIL,
            response.customerId, BsPayoneErrors.mapResponseCode(response.errorCode, response.errorMessage))

        return PspPaymentResponseModel(response.transactionId, TransactionStatus.SUCCESS, response.customerId, null)
    }

    private fun getBsPayoneClearingType(alias: Alias): String {
        val aliasExtra = jsonMapper.readValue(alias.extra, AliasExtraModel::class.java)
            ?: throw ApiError.ofMessage("Alias extra data cannot be found").asBadRequest()
        return when (aliasExtra.paymentMethod) {
            PaymentMethod.CC -> BsPayoneClearingType.CC.type
            PaymentMethod.SEPA -> BsPayoneClearingType.SEPA.type
            else -> throw ApiError.ofMessage("Payment method not supported for BS Payone").asBadRequest()
        }
    }

    private fun getPersonalData(alias: Alias): PersonalDataModel? {
        val aliasExtra = jsonMapper.readValue(alias.extra, AliasExtraModel::class.java)
        ?: throw ApiError.ofMessage("Alias extra data cannot be found").asBadRequest()
        return aliasExtra.personalData ?: throw ApiError.ofMessage("Alias not configured properly, personal data is missing").asInternalServerError()
    }
}