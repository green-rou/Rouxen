package com.greenrou.rouxen.feature.history.domain

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { prettyPrint = true }

class ExportScanUseCase {
    operator fun invoke(model: ScanExportModel): String = json.encodeToString(model)
}
