package com.mobilabsolutions.payment.notifications.data.repository

import com.mobilabsolutions.payment.data.configuration.BaseRepository
import com.mobilabsolutions.payment.notifications.data.Notification
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Repository
interface NotificationRepository : BaseRepository<Notification, String> {

    @Query("SELECT * FROM notification nt WHERE nt.status IN ('CREATED', 'FAIL') AND nt.psp = :psp ORDER BY nt.created_date DESC LIMIT :limit FOR UPDATE OF nt SKIP LOCKED", nativeQuery = true)
    fun findNotificationByPsp(@Param("psp") psp: String, @Param("limit") limit: Int): List<Notification>
}
