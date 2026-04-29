package com.example.naroo.recovery.port.`out`

import com.example.naroo.diagnostic.domain.DiagnosticSessionId
import com.example.naroo.recovery.domain.RecoveryMission
import com.example.naroo.recovery.domain.RecoveryMissionId
import com.example.naroo.user.domain.UserId

interface RecoveryMissionRepositoryPort {
    fun findById(id: RecoveryMissionId): RecoveryMission?
    fun findByUserIdAndDiagnosticSessionId(userId: UserId, diagnosticSessionId: DiagnosticSessionId): RecoveryMission?
    fun findAllByUserIdAndDiagnosticSessionId(userId: UserId, diagnosticSessionId: DiagnosticSessionId): List<RecoveryMission>
    fun save(mission: RecoveryMission): RecoveryMission
}
