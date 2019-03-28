package com.mobilabsolutions.payment.bspayone.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Maps
import com.mobilabsolutions.payment.bspayone.configuration.BsPayoneProperties
import com.mobilabsolutions.payment.bspayone.data.enum.BsPayoneRequestType
import com.mobilabsolutions.payment.bspayone.model.BsPayonePaymentRequestModel
import com.mobilabsolutions.payment.bspayone.model.BsPayonePaymentResponseModel
import com.mobilabsolutions.payment.bspayone.model.BsPayoneStandardParametersModel
import com.mobilabsolutions.payment.model.PspConfigModel
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Service
class BsPayoneClient(
    private val restTemplate: RestTemplate,
    private val jsonMapper: ObjectMapper,
    private val bsPayoneProperties: BsPayoneProperties,
    private val bsPayoneHashingService: BsPayoneHashingService
) {

        /**
         * Makes preauthorization request to BS Payone.
         *
         * @param paymentRequest BS Payone payment request
         * @param pspConfigModel BS Payone configuration
         * @return BS Payone payment response
         */
        fun preauthorization(paymentRequest: BsPayonePaymentRequestModel, pspConfigModel: PspConfigModel): BsPayonePaymentResponseModel {
            val request = createStandardRequest(paymentRequest, pspConfigModel, BsPayoneRequestType.PREAUTHORIZATION.type)
            val response = restTemplate.postForEntity(bsPayoneProperties.baseUrl, request, String::class.java)
            return convertToResponse(response.body!!, BsPayonePaymentResponseModel::class.java)
        }

        /**
         * Creates the BS Payone request with standard parameters.
         *
         * @param request BS Payone request
         * @param pspConfigModel BS Payone configuration
         * @param requestType BS Payone request type
         * @return BS Payone request as key/ value map
         */
        private fun createStandardRequest(
            request: Any,
            pspConfigModel: PspConfigModel,
            requestType: String
        ): MultiValueMap<String, Any> {
            val result = convertToKeyValue(request)
            result.putAll(convertToKeyValue(getBsPayoneStandardParameters(pspConfigModel, requestType)))
            return result
        }

        /**
         * Converts BS Payone response body to internal response.
         *
         * @param body response body
         * @param response response class
         * @return internal response
         */
        private fun <T> convertToResponse(body: String, response: Class<T>): T {
            val params = body.split("\\r?\\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val map = params.map { s ->
                val parts = s.split("=".toRegex(), 2).toTypedArray()
                Maps.immutableEntry(parts[0], parts[1])
            }.map { it.key to it.value }.toMap()
            return jsonMapper.convertValue(map, response)
        }

        /**
         * Converts the request to key/ value map
         *
         * @param request BS Payone request
         * @return key/ value map
         */
        private fun convertToKeyValue(request: Any): MultiValueMap<String, Any> {
            val params: Map<String, Any> = jsonMapper.convertValue(request, object : TypeReference<Map<String, Any>>() {})
            val map = LinkedMultiValueMap<String, Any>()
            map.setAll(params)
            return map
        }

        /**
         * Returns BS Payone standard parameters.
         *
         * @param pspConfigModel PSP Configuration
         * @param bsPayoneRequestType BS payone request type
         * @return BS Payone standard parameters model
         */
        private fun getBsPayoneStandardParameters(pspConfigModel: PspConfigModel, bsPayoneRequestType: String): BsPayoneStandardParametersModel {
            return BsPayoneStandardParametersModel(pspConfigModel.merchantId, pspConfigModel.portalId,
                bsPayoneHashingService.hashKey(pspConfigModel.key), bsPayoneProperties.apiVersion, bsPayoneProperties.mode,
                bsPayoneRequestType, bsPayoneProperties.encoding)
        }
    }