package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.domain.Merchant
import com.mobilabsolutions.payment.data.repository.MerchantRepository
import com.mobilabsolutions.payment.model.MerchantRequestModel
import com.mobilabsolutions.server.commons.exception.ApiError
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Service
@Transactional
class MerchantService(
    private val merchantRepository: MerchantRepository
) {

    /**
     * Create merchant
     *
     * @param merchantInfo Merchant specific details
     * @return null
     */
    fun createMerchant(merchantInfo: MerchantRequestModel) {
        val merchant = Merchant(
            id = merchantInfo.merchantId,
            name = merchantInfo.merchantName,
            email = merchantInfo.merchantEmail,
            defaultCurrency = merchantInfo.merchantCurrency
        )

        if (merchantRepository.getMerchantById(merchantInfo.merchantId) == null) merchantRepository.save(merchant)
        else throw ApiError.ofMessage("Merchant with that id already exists").asBadRequest()
    }
}