package com.greenrou.rouxen.feature.history.domain

import com.greenrou.rouxen.feature.history.db.ScanDao
import com.greenrou.rouxen.feature.history.db.ScanResultEntity

class SaveScanUseCase(private val dao: ScanDao) {
    suspend operator fun invoke(entity: ScanResultEntity): Long = dao.insert(entity)
}
