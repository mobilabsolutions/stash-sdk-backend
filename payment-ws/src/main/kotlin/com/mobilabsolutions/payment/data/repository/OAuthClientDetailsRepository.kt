package com.mobilabsolutions.payment.data.repository

import com.mobilabsolutions.payment.data.domain.OAuthClientDetails
import org.springframework.stereotype.Repository

/**
 * @author <a href="mailto:doruk@mobilabsolutions.com">Doruk Coskun</a>
 */
@Repository
interface OAuthClientDetailsRepository : BaseRepository<OAuthClientDetails, String>