package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.payment.model.MerchantResponseModel
import com.mobilabsolutions.server.commons.exception.ApiError
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Service
@Transactional
class MerchantService(
    private val merchantRepository: MerchantRepository
) {

    fun getMerchant(merchantId: String): MerchantResponseModel {
        val merchant = merchantRepository.getMerchantById(merchantId)
            ?: throw ApiError.ofMessage("Merchant with id '$merchantId'").asBadRequest()
        return MerchantResponseModel(merchant?.name, merchant?.email, merchant?.defaultCurrency)
    }

    companion object : KLogging()
}