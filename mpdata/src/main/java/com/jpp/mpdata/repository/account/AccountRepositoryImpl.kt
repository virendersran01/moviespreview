package com.jpp.mpdata.repository.account

import com.jpp.mpdomain.Session
import com.jpp.mpdomain.UserAccount
import com.jpp.mpdomain.repository.AccountRepository

class AccountRepositoryImpl(private val accountApi: AccountApi) : AccountRepository {
    override fun getUserAccount(session: Session): UserAccount? = accountApi.getUserAccountInfo(session)
}