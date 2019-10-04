/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.bspayone.service

import com.google.common.base.Charsets
import com.google.common.hash.Hashing
import com.mobilabsolutions.payment.bspayone.configuration.BsPayoneProperties
import com.mobilabsolutions.payment.bspayone.data.enum.BsPayoneRequestType
import com.mobilabsolutions.payment.model.PspConfigModel
import com.mobilabsolutions.server.commons.exception.ApiError
import com.mobilabsolutions.server.commons.exception.ApiErrorCode
import mu.KLogging
import org.apache.tomcat.util.buf.HexUtils
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Service
class BsPayoneHashingService(private val bsPayoneProperties: BsPayoneProperties) {
    companion object : KLogging() {
        const val RESPONSE_TYPE = "JSON"
        const val STORE_CARD_DATA_PARAM_VALUE = "yes"
        const val HASH_ALGORITHM = "HmacSHA384"
    }

    /**
     * Creates a hash for the client's credit card check request
     *
     * @param pspConfigModel PSP Configuration Model
     * @return hash
     */
    fun createCreditCardCheckHash(pspConfigModel: PspConfigModel, mode: String): String {
        pspConfigModel.key ?: throw ApiError.ofErrorCode(ApiErrorCode.PSP_MODULE_ERROR, "`Key` configuration should be defined in BS_PAYONE PSP configuration").asException()
        return calculateHash(pspConfigModel.key, pspConfigModel.accountId + bsPayoneProperties.apiVersion + pspConfigModel.merchantId +
            mode + pspConfigModel.portalId + BsPayoneRequestType.CREDIT_CARD_CHECK.type + RESPONSE_TYPE +
            STORE_CARD_DATA_PARAM_VALUE)
    }

    /**
     * Creates a key hash which will be used for server requests
     *
     * @param key Key to be hashed
     * @return hashed key
     */
    fun hashKey(key: String?): String {
        return Hashing.md5().hashString(key!!, Charsets.UTF_8).toString()
    }

    private fun calculateHash(key: String?, data: String): String {
        try {
            val mac = Mac.getInstance(HASH_ALGORITHM)
            val secretKeySpec = SecretKeySpec(key?.toByteArray(StandardCharsets.UTF_8), HASH_ALGORITHM)
            mac.init(secretKeySpec)
            return HexUtils.toHexString(mac.doFinal(data.toByteArray(StandardCharsets.UTF_8)))
        } catch (e: NoSuchAlgorithmException) {
            logger.error(e.message)
            throw RuntimeException(e)
        } catch (e: InvalidKeyException) {
            logger.error(e.message)
            throw RuntimeException(e)
        }
    }
}
