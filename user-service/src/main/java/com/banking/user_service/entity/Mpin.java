package com.banking.user_service.entity;

import com.banking.user_service.enums.MpinStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "mpin", indexes = {
        @Index(name = "idx_internal_user_id_mpin", columnList = "internal_user_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mpin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "internal_user_id", nullable = false, length = 50)
    private String internalUserId;

    @Column(name = "hashed_mpin", nullable = false)
    private String hashedMpin;

    @Enumerated(EnumType.STRING)
    @Column(name = "mpin_status", nullable = false, length = 20)
    private MpinStatus mpinStatus = MpinStatus.ACTIVE;

    @Column(name = "wrong_attempts", nullable = false)
    private Integer wrongAttempts = 0;

    @Column(name = "lock_until")
    private LocalDateTime lockUntil;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}

