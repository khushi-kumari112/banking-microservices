package com.banking.user_service.repository;

import com.banking.user_service.entity.Mpin;
import com.banking.user_service.enums.MpinStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MpinRepository extends JpaRepository<Mpin, Long> {
    Optional<Mpin> findByInternalUserId(String internalUserId);
    boolean existsByInternalUserId(String internalUserId);
}
