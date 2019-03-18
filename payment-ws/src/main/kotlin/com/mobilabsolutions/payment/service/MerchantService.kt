package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.domain.Authority
import com.mobilabsolutions.payment.data.domain.Merchant
import com.mobilabsolutions.payment.data.repository.AuthorityRepository
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
    private val merchantRepository: MerchantRepository,
    private val authorityRepository: AuthorityRepository
) {

    /**
     * Create merchant
     *
     * @param merchantInfo Merchant specific details
     *
     */
    fun createMerchant(merchantInfo: MerchantRequestModel) {
        if (!checkMerchantAndAuthority(merchantInfo.merchantId)) throw ApiError.ofMessage("Merchant with id '${merchantInfo.merchantId}' already exists").asBadRequest()

        merchantRepository.save(
            Merchant(
                id = merchantInfo.merchantId,
                name = merchantInfo.merchantName,
                email = merchantInfo.merchantEmail,
                defaultCurrency = merchantInfo.merchantCurrency
            )
        )

        authorityRepository.save(Authority(name = merchantInfo.merchantId))
    }

    private fun checkMerchantAndAuthority(merchantId: String): Boolean {
        return authorityRepository.getAuthorityByName(merchantId) == null || merchantRepository.getMerchantById(merchantId) == null
    }
}