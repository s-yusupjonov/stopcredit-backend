package uz.agrobank.stopcredit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "cards")
@Getter
@Setter
public class Card extends BaseEntity {

    @Column(nullable = false, length = 16)
    private String cardNumber;

    @Column(length = 10)
    private String mfo;

    private LocalDate restrictionDate;

    @Column(precision = 19, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private CardRestrictionType restrictionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CardBasisCategory basisCategory;

    @Column(length = 500)
    private String basisComment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CardStatus status;

    @Column(length = 500)
    private String statusComment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "executor_id", nullable = false)
    private Executor executor;

    @Column(nullable = false, length = 150, updatable = false)
    private String senderName;

    @Column(length = 500)
    private String unblockOrderNumber;

    @Column(length = 500)
    private String unblockComment;

    private Instant unblockedAt;

    @Column(name = "unblocked_by_name", length = 150)
    private String unblockedBy;
}