package com.greenrou.rouxen.feature.history.domain

import com.greenrou.rouxen.feature.history.db.ScanDao

class DeleteScanUseCase(private val dao: ScanDao) {
    suspend operator fun invoke(id: Long) = dao.deleteById(id)
}
