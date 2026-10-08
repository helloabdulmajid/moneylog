package in.abdulmajid.moneylog.admin.repository;

import in.abdulmajid.moneylog.admin.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID>, JpaSpecificationExecutor<AuditLog> {

    List<AuditLog> findTop5ByOrderByCreatedAtDesc();

    @Query("SELECT a FROM AuditLog a JOIN FETCH a.admin ORDER BY a.createdAt DESC")
    List<AuditLog> findRecentWithAdmin(Pageable pageable);

    Page<AuditLog> findByAdminId(UUID adminId, Pageable pageable);
}
