package com.jpp.mpdata.datasources.account

import com.jpp.mpdomain.Session
import com.jpp.mpdomain.UserAccount

interface AccountApi {
    fun getUserAccountInfo(session: Session): UserAccount?
}