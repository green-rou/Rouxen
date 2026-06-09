package com.greenrou.rouxen.feature.history.domain

import com.greenrou.rouxen.feature.history.db.ScanDao
import com.greenrou.rouxen.feature.history.db.ScanResultEntity
import kotlinx.coroutines.flow.Flow

class GetScanHistoryUseCase(private val dao: ScanDao) {
    operator fun invoke(): Flow<List<ScanResultEntity>> = dao.getAll()
}
