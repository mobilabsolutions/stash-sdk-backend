/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.notifications.data.repository

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant

/**
 * Test the custom repository configuration and implementation. Also ensures that obtaining the result type with
 * reflection at runtime works as expected.
 *
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
class BaseRepositoryTest : AbstractRepositoryTest() {

    @Autowired
    lateinit var testRecordRepository: TestRecordRepository

    @Test
    fun testSaveAndUpdate() {
        var record: TestRecord = recordWithQuantity(0)
        val createdAt: Instant? = record.createdDate

        Assertions.assertNotNull(createdAt)
        Assertions.assertEquals(createdAt, record.lastModifiedDate)

        Thread.sleep(1)

        record.quantity = 1
        record = testRecordRepository.saveAndFlush(record)

        if (createdAt != null) {
            Assertions.assertTrue(createdAt.isBefore(record.lastModifiedDate))
        }
    }

    @Test
    fun testFindAll() {
        recordWithNameAndOrder("Water", 1)
        recordWithNameAndOrder("Burger", 2)
        recordWithNameAndOrder("Pomes", 2)

        val records: List<TestRecord> = testRecordRepository.findAll()
        Assertions.assertEquals(3, records.size)
    }

    @Test
    fun testDelete() {
        val record1 = recordWithNameAndOrder("Water", 1)
        val record2 = recordWithNameAndOrder("Burger", 2)
        recordWithNameAndOrder("Pomes", 3)

        testRecordRepository.delete(record1)
        Assertions.assertEquals(testRecordRepository.count(), 2)

        testRecordRepository.delete(record2)
        Assertions.assertEquals(testRecordRepository.count(), 1)
    }

    private fun recordWithQuantity(quantity: Int): TestRecord {
        val testRecord = TestRecord()
        testRecord.quantity = quantity
        return testRecordRepository.save(testRecord)
    }

    private fun recordWithNameAndOrder(name: String, order: Int): TestRecord {
        val record = TestRecord()
        record.name = name
        record.order = order
        return testRecordRepository.save(record)
    }
}
