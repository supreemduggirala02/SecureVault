// package com.securevault.backend.repository;

// import com.securevault.backend.entity.AuditLog;
// import org.springframework.data.jpa.repository.JpaRepository;

// import java.util.List;

// public interface AuditLogRepository
//         extends JpaRepository<AuditLog, Long> {

//     List<AuditLog> findByUserEmailOrderByTimestampDesc(
//             String userEmail
//     );

//     List<AuditLog> findAllByOrderByTimestampDesc();
// }









package com.securevault.backend.repository;

import com.securevault.backend.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserEmailOrderByTimestampDesc(
            String userEmail
    );

    List<AuditLog> findAllByOrderByTimestampDesc();

    List<AuditLog> findByUserEmailAndTimestampBetweenOrderByTimestampDesc(
            String userEmail,
            LocalDateTime start,
            LocalDateTime end
    );

    long countByUserEmailAndLoginStatusAndTimestampBetween(
            String userEmail,
            String loginStatus,
            LocalDateTime start,
            LocalDateTime end
    );

    long countByUserEmailAndActionAndTimestampBetween(
            String userEmail,
            String action,
            LocalDateTime start,
            LocalDateTime end
    );
}