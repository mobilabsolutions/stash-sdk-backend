package com.mobilabsolutions.payment.service

import com.mobilabsolutions.payment.data.Transaction
import com.mobilabsolutions.payment.data.repository.TransactionRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Service
class HomeService(
    private val transactionRepository: TransactionRepository
) {

    @Transactional(readOnly = true)
    @KafkaListener(topics = ["transactions"], groupId = "payment-sdk")
    fun getLiveData(@Payload transaction: Transaction) {
        println("TRANSACTION '${transaction.transactionId}")
    }
}
