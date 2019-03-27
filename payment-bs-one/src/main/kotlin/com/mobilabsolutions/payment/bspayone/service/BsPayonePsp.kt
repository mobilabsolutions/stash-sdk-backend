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
import com.mobilabsolutions.payment.model.PersonalDataModel
import com.mobilabsolutions.payment.model.PreauthorizeRequestModel
import com.mobilabsolutions.payment.model.PspAliasConfigModel
import com.mobilabsolutions.payment.model.PspConfigListModel
import com.mobilabsolutions.payment.model.PspConfigModel
import com.mobilabsolutions.payment.model.PspPaymentResponseModel
import com.mobilabsolutions.payment.service.Psp
import com.mobilabsolutions.server.commons.exception.ApiError
import mu.KLogging
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.stereotype.Component

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Component
class BsPayonePsp(
    private val bsPayoneHashingService: BsPayoneHashingService,
    private val bsPayoneProperties: BsPayoneProperties,
    private val bsPayoneClient: BsPayoneClient,
    private val aliasRepository: AliasRepository,
    private val jsonMapper: ObjectMapper
) : Psp {
    companion object : KLogging() {
        const val REFERENCE_LENGTH = 10
    }

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

    override fun preauthorize(preauthorizeRequestModel: PreauthorizeRequestModel): PspPaymentResponseModel {
        val alias = aliasRepository.getFirstById(preauthorizeRequestModel.aliasId) ?: throw ApiError.ofMessage("Alias ID cannot be found").asBadRequest()
        val result = jsonMapper.readValue(alias.merchant?.pspConfig, PspConfigListModel::class.java)
        val pspConfig = result.psp.firstOrNull { it.type == getProvider().toString() }
            ?: throw ApiError.ofMessage("PSP configuration for '${getProvider()}' cannot be found from used merchant").asBadRequest()

        val bsPayonePreauthorizeRequest = BsPayonePaymentRequestModel(
            accountId = pspConfig.accountId,
            clearingType = getBsPayoneClearingType(alias),
            reference = RandomStringUtils.randomAlphanumeric(REFERENCE_LENGTH),
            amount = preauthorizeRequestModel.paymentData.amount.toString(),
            currency = preauthorizeRequestModel.paymentData.currency,
            lastName = getPersonalData(alias)?.lastName,
            country = getPersonalData(alias)?.country,
            city = getPersonalData(alias)?.city,
            pspAlias = alias.pspAlias,
            iban = null,
            bic = null
        )

        val response = bsPayoneClient.preauthorization(bsPayonePreauthorizeRequest, pspConfig)

        if (response.hasError()) {
            logger.error { "Error during BS Payone preauthorization. Error code: ${response.errorCode}, error message: ${response.errorMessage} " }
            return PspPaymentResponseModel(response.transactionId, TransactionStatus.FAIL,
                response.customerId, BsPayoneErrors.mapResponseCode(response.errorCode!!))
        }

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