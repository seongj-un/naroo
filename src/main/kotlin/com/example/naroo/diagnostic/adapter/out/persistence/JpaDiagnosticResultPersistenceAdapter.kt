package com.example.naroo.diagnostic.adapter.`out`.persistence

import com.example.naroo.diagnostic.domain.DiagnosticResult
import com.example.naroo.diagnostic.domain.DiagnosticSessionId
import com.example.naroo.diagnostic.domain.MathArea
import com.example.naroo.diagnostic.port.`out`.DiagnosticResultRepositoryPort
import com.example.naroo.user.domain.UserId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class JpaDiagnosticResultPersistenceAdapter(
    private val repository: SpringDataDiagnosticResultJpaRepository,
) : DiagnosticResultRepositoryPort {
    override fun findByDiagnosticSessionId(diagnosticSessionId: DiagnosticSessionId): DiagnosticResult? {
        return repository.findById(diagnosticSessionId.value).map(DiagnosticResultJpaEntity::toDomain).orElse(null)
    }

    override fun findLatestByUserId(userId: UserId): DiagnosticResult? {
        return repository.findFirstByUserIdOrderByCreatedAtDesc(userId.value)?.toDomain()
    }

    override fun save(result: DiagnosticResult): DiagnosticResult {
        return repository.saveAndFlush(DiagnosticResultJpaEntity.from(result)).toDomain()
    }
}

interface SpringDataDiagnosticResultJpaRepository : JpaRepository<DiagnosticResultJpaEntity, String> {
    fun findFirstByUserIdOrderByCreatedAtDesc(userId: String): DiagnosticResultJpaEntity?
}

@Entity
@Table(name = "diagnostic_results")
class DiagnosticResultJpaEntity(
    @Id
    @Column(name = "diagnostic_session_id", nullable = false, length = 36)
    var diagnosticSessionId: String = "",

    @Column(name = "user_id", nullable = false, length = 36)
    var userId: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "math_area", nullable = false, length = 64)
    var mathArea: MathArea = MathArea.FUNCTION,

    @Column(name = "total_question_count", nullable = false)
    var totalQuestionCount: Int = 0,

    @Column(name = "correct_count", nullable = false)
    var correctCount: Int = 0,

    @Column(name = "wrong_count", nullable = false)
    var wrongCount: Int = 0,

    @Column(name = "unknown_count", nullable = false)
    var unknownCount: Int = 0,

    @Column(name = "weak_links", nullable = false, length = 500)
    var weakLinks: String = "",

    @Column(name = "primary_recovery_concept", nullable = false, length = 100)
    var primaryRecoveryConcept: String = "",

    @Column(name = "summary", nullable = false, length = 500)
    var summary: String = "",

    @Column(name = "created_at", nullable = false, columnDefinition = "datetime(6)")
    var createdAt: Instant = Instant.EPOCH,
) {
    fun toDomain(): DiagnosticResult {
        return DiagnosticResult(
            diagnosticSessionId = DiagnosticSessionId(diagnosticSessionId),
            userId = UserId(userId),
            mathArea = mathArea,
            totalQuestionCount = totalQuestionCount,
            correctCount = correctCount,
            wrongCount = wrongCount,
            unknownCount = unknownCount,
            weakLinks = weakLinks.split(",").filter { it.isNotBlank() },
            primaryRecoveryConcept = primaryRecoveryConcept,
            summary = summary,
            createdAt = createdAt,
        )
    }

    companion object {
        fun from(result: DiagnosticResult): DiagnosticResultJpaEntity {
            return DiagnosticResultJpaEntity(
                diagnosticSessionId = result.diagnosticSessionId.value,
                userId = result.userId.value,
                mathArea = result.mathArea,
                totalQuestionCount = result.totalQuestionCount,
                correctCount = result.correctCount,
                wrongCount = result.wrongCount,
                unknownCount = result.unknownCount,
                weakLinks = result.weakLinks.joinToString(","),
                primaryRecoveryConcept = result.primaryRecoveryConcept,
                summary = result.summary,
                createdAt = result.createdAt,
            )
        }
    }
}
