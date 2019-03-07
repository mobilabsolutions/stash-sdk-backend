package com.mobilabsolutions.payment.data.repository

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests
import org.springframework.test.context.web.WebAppConfiguration

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@ExtendWith(SpringExtension::class)
@WebAppConfiguration
@ContextConfiguration(locations = ["classpath:spring/dao-test.xml"])
class AbstractRepositoryTest : AbstractTransactionalJUnit4SpringContextTests()