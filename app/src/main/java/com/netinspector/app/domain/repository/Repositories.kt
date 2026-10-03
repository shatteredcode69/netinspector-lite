package com.netinspector.app.domain.repository

import com.netinspector.app.domain.model.FirewallAudit
import com.netinspector.app.domain.model.IspDetails
import com.netinspector.app.domain.model.WifiInfoModel
import kotlinx.coroutines.flow.Flow

interface WifiRepository { fun observe(): Flow<WifiInfoModel?>; fun gateway(): String? }
interface IspRepository { suspend fun get(): IspDetails }
interface FirewallRepository { suspend fun scan(): FirewallAudit }