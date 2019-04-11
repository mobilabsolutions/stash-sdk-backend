package com.mobilabsolutions.payment.bspayone.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.mobilabsolutions.payment.bspayone.configuration.BsPayoneProperties
import com.mobilabsolutions.payment.bspayone.data.enum.BsPayoneClearingType
import com.mobilabsolutions.payment.bspayone.data.enum.BsPayoneMode
import com.mobilabsolutions.payment.bspayone.data.enum.BsPayoneRequestType
import com.mobilabsolutions.payment.bspayone.exception.BsPayoneErrors
import com.mobilabsolutions.payment.bspayone.model.BsPayoneCaptureRequestModel
import com.mobilabsolutions.payment.bspayone.model.BsPayoneDeleteAliasModel
import com.mobilabsolutions.payment.bspayone.model.BsPayonePaymentRequestModel
import com.mobilabsolutions.payment.data.domain.Alias
import com.mobilabsolutions.payment.data.domain.Transaction
import com.mobilabsolutions.payment.data.enum.PaymentMethod
import com.mobilabsolutions.payment.data.enum.PaymentServiceProvider
import com.mobilabsolutions.payment.data.enum.TransactionAction
import com.mobilabsolutions.payment.data.enum.TransactionStatus
import com.mobilabsolutions.payment.data.repository.AliasRepository
import com.mobilabsolutions.payment.data.repository.TransactionRepository
import com.mobilabsolutions.payment.model.AliasExtraModel
import com.mobilabsolutions.payment.model.PaymentRequestModel
import com.mobilabsolutions.payment.model.PersonalDataModel
import com.mobilabsolutions.payment.model.PspAliasConfigModel
import com.mobilabsolutions.payment.model.PspConfigListModel
import com.mobilabsolutions.payment.model.PspConfigModel
import com.mobilabsolutions.payment.model.PspPaymentResponseModel
import com.mobilabsolutions.payment.model.PspRegisterAliasResponseModel
import com.mobilabsolutions.payment.model.SepaConfigModel
import com.mobilabsolutions.payment.service.Psp
import com.mobilabsolutions.server.commons.exception.ApiError
import com.mobilabsolutions.server.commons.util.RandomStringGenerator
import mu.KLogging
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
    private val transactionRepository: TransactionRepository,
    private val randomStringGenerator: RandomStringGenerator,
    private val jsonMapper: ObjectMapper
) : Psp {

    companion object : KLogging() {
        const val REFERENCE_LENGTH = 10
        const val AMOUNT_FOR_CANCELLING_TRANSACTION = 0
        const val DELETE = "yes"
        const val DO_NOT_DELETE = "no"
    }

    override fun getProvider(): PaymentServiceProvider {
        return PaymentServiceProvider.BS_PAYONE
    }

    override fun calculatePspConfig(pspConfigModel: PspConfigModel?, pspTestMode: Boolean?): PspAliasConfigModel? {
        logger.info { "BS Payone config calculation has been called..." }
        return if (pspConfigModel != null) PspAliasConfigModel(
            type = PaymentServiceProvider.BS_PAYONE.toString(),
            merchantId = pspConfigModel.merchantId,
            portalId = pspConfigModel.portalId,
            request = BsPayoneRequestType.CREDIT_CARD_CHECK.type,
            apiVersion = bsPayoneProperties.apiVersion,
            responseType = BsPayoneHashingService.RESPONSE_TYPE,
            hash = bsPayoneHashingService.makeCreditCardCheckHash(pspConfigModel, getPspMode(pspTestMode)),
            accountId = pspConfigModel.accountId,
            encoding = bsPayoneProperties.encoding,
            mode = getPspMode(pspTestMode),
            publicKey = null,
            privateKey = null
        ) else null
    }

    override fun registerAlias(aliasId: String, aliasExtra: AliasExtraModel?, pspTestMode: Boolean?): PspRegisterAliasResponseModel? {
        return null
    }

    override fun preauthorize(preauthorizeRequestModel: PaymentRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        val alias = getAlias(preauthorizeRequestModel.aliasId!!)
        val pspConfig = getPspConfig(alias)

        val bsPayonePreauthorizeRequest = BsPayonePaymentRequestModel(
            accountId = pspConfig.accountId,
            clearingType = getBsPayoneClearingType(alias),
            reference = randomStringGenerator.generateRandomAlphanumeric(REFERENCE_LENGTH),
            amount = preauthorizeRequestModel.paymentData!!.amount.toString(),
            currency = preauthorizeRequestModel.paymentData!!.currency,
            customerId = alias.id,
            lastName = getPersonalData(alias)?.lastName,
            country = getPersonalData(alias)?.country,
            city = getPersonalData(alias)?.city,
            pspAlias = alias.pspAlias,
            iban = null,
            bic = null
        )

        val response = bsPayoneClient.preauthorization(bsPayonePreauthorizeRequest, pspConfig, getPspMode(pspTestMode))

        if (response.hasError()) {
            logger.error { "Error during BS Payone preauthorization. Error code: ${response.errorCode}, error message: ${response.errorMessage} " }
            return PspPaymentResponseModel(response.transactionId, TransactionStatus.FAIL, response.customerId,
                BsPayoneErrors.mapResponseCode(response.errorCode!!), response.errorMessage)
        }

        return PspPaymentResponseModel(response.transactionId, TransactionStatus.SUCCESS, response.customerId, null, null)
    }

    override fun authorize(authorizeRequestModel: PaymentRequestModel, pspTestMode: Boolean?): PspPaymentResponseModel {
        val alias = getAlias(authorizeRequestModel.aliasId!!)
        val pspConfig = getPspConfig(alias)

        val bsPayoneAuthorizeRequest = BsPayonePaymentRequestModel(
            accountId = pspConfig.accountId,
            clearingType = getBsPayoneClearingType(alias),
            reference = randomStringGenerator.generateRandomAlphanumeric(REFERENCE_LENGTH),
            amount = authorizeRequestModel.paymentData!!.amount.toString(),
            currency = authorizeRequestModel.paymentData!!.currency,
            customerId = alias.id,
            lastName = getPersonalData(alias)?.lastName,
            country = getPersonalData(alias)?.country,
            city = getPersonalData(alias)?.city,
            pspAlias = alias.pspAlias,
            iban = if (getPaymentMethod(alias) == PaymentMethod.SEPA) getSepaConfigData(alias)?.iban else null,
            bic = if (getPaymentMethod(alias) == PaymentMethod.SEPA) getSepaConfigData(alias)?.bic else null
        )

        val response = bsPayoneClient.authorization(bsPayoneAuthorizeRequest, pspConfig, getPspMode(pspTestMode))

        if (response.hasError()) {
            logger.error { "Error during BS Payone authorization. Error code: ${response.errorCode}, error message: ${response.errorMessage} " }
            return PspPaymentResponseModel(response.transactionId, TransactionStatus.FAIL, response.customerId,
                BsPayoneErrors.mapResponseCode(response.errorCode!!), response.errorMessage)
        }

        return PspPaymentResponseModel(response.transactionId, TransactionStatus.SUCCESS, response.customerId, null, null)
    }

    override fun capture(transactionId: String, pspTransactionId: String?, pspTestMode: Boolean?): PspPaymentResponseModel {
        val transaction = findPreauthTransaction(transactionId)
        return executeRequest(pspTransactionId, pspTestMode, transaction, transaction.amount!!)
    }

    override fun reverse(transactionId: String, pspTransactionId: String?, pspTestMode: Boolean?): PspPaymentResponseModel {
        val transaction = findPreauthTransaction(transactionId)
        return executeRequest(pspTransactionId, pspTestMode, transaction, AMOUNT_FOR_CANCELLING_TRANSACTION)
    }

    override fun deleteAlias(aliasId: String, pspTestMode: Boolean?) {
        val alias = getAlias(aliasId)
        val paymentMethod = getAliasExtra(alias).paymentMethod
        val deleteAliasRequest = BsPayoneDeleteAliasModel(
            customerId = aliasId,
            deleteCardData = if (paymentMethod == PaymentMethod.CC) DELETE else DO_NOT_DELETE,
            deleteBankAccountData = if (paymentMethod == PaymentMethod.SEPA) DELETE else DO_NOT_DELETE
        )
        val response = bsPayoneClient.deleteAlias(deleteAliasRequest, getPspConfig(alias), getPspMode(pspTestMode))
        if (response.hasError()) {
            logger.error { "Error during BS Payone alias deletion. Error code: ${response.errorCode}, error message: ${response.errorMessage} " }
        }
    }

    private fun getPspMode(test: Boolean?): String {
        if (test == null || test == false) return BsPayoneMode.LIVE.mode
        return BsPayoneMode.TEST.mode
    }

    private fun getAlias(aliasId: String): Alias {
        return aliasRepository.getFirstByIdAndActive(aliasId, true)
            ?: throw ApiError.ofMessage("Alias ID cannot be found").asBadRequest()
    }

    private fun getBsPayoneClearingType(alias: Alias): String {
        val aliasExtra = getAliasExtra(alias)
        return when (aliasExtra.paymentMethod) {
            PaymentMethod.CC -> BsPayoneClearingType.CC.type
            PaymentMethod.SEPA -> BsPayoneClearingType.SEPA.type
            else -> throw ApiError.ofMessage("Payment method not supported for BS Payone").asBadRequest()
        }
    }

    private fun getPersonalData(alias: Alias): PersonalDataModel? {
        val aliasExtra = getAliasExtra(alias)
        return aliasExtra.personalData
            ?: throw ApiError.ofMessage("Alias not configured properly, personal data is missing").asInternalServerError()
    }

    private fun getSepaConfigData(alias: Alias): SepaConfigModel? {
        val aliasExtra = getAliasExtra(alias)
        return aliasExtra.sepaConfig
            ?: throw ApiError.ofMessage("Alias not configured properly, sepa config is missing").asInternalServerError()
    }

    private fun getPaymentMethod(alias: Alias): PaymentMethod? {
        val aliasExtra = getAliasExtra(alias)
        return aliasExtra.paymentMethod
            ?: throw ApiError.ofMessage("Alias not configured properly, payment method is missing").asInternalServerError()
    }

    private fun getAliasExtra(alias: Alias): AliasExtraModel {
        return jsonMapper.readValue(alias.extra, AliasExtraModel::class.java)
            ?: throw ApiError.ofMessage("Alias extra data cannot be found").asBadRequest()
    }

    private fun getPspConfig(alias: Alias): PspConfigModel {
        val result = jsonMapper.readValue(alias.merchant?.pspConfig, PspConfigListModel::class.java)
        return result.psp.firstOrNull { it.type == getProvider().toString() }
            ?: throw ApiError.ofMessage("PSP configuration for '${getProvider()}' cannot be found from used merchant").asBadRequest()
    }

    private fun findPreauthTransaction(transactionId: String): Transaction {
        return transactionRepository.getByTransactionIdAndAction(
            transactionId,
            TransactionAction.PREAUTH,
            TransactionStatus.SUCCESS
        ) ?: throw ApiError.ofMessage("Transaction cannot be found").asBadRequest()
    }

    private fun executeRequest(pspTransactionId: String?, pspTestMode: Boolean?, transaction: Transaction, amount: Int): PspPaymentResponseModel {
        val alias = transaction.alias ?: throw ApiError.ofMessage("Alias ID cannot be found").asBadRequest()
        val result = jsonMapper.readValue(alias.merchant?.pspConfig, PspConfigListModel::class.java)
        val pspConfig = result.psp.firstOrNull { it.type == getProvider().toString() }
            ?: throw ApiError.ofMessage("PSP configuration for '${getProvider()}' cannot be found from used merchant").asBadRequest()

        val bsPayoneCaptureRequest = BsPayoneCaptureRequestModel(
            pspTransactionId = pspTransactionId,
            amount = amount.toString(),
            currency = transaction.currencyId
        )

        val response = bsPayoneClient.capture(bsPayoneCaptureRequest, pspConfig, getPspMode(pspTestMode))

        if (response.hasError()) {
            logger.error { "Error during BS Payone capture. Error code: ${response.errorCode}, error message: ${response.errorMessage} " }
            return PspPaymentResponseModel(response.transactionId, TransactionStatus.FAIL, response.customerId,
                BsPayoneErrors.mapResponseCode(response.errorCode!!), response.errorMessage)
        }

        return PspPaymentResponseModel(response.transactionId, TransactionStatus.SUCCESS, response.customerId, null, null)
    }
}
