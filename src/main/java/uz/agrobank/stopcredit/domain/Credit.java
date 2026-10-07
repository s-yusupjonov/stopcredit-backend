package uz.agrobank.stopcredit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "credits")
@Getter
@Setter
public class Credit extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(length = 100)
    private String middleName;

    @Column(nullable = false, length = 14)
    private String pinfl;

    @Enumerated(EnumType.STRING)
    @Column(name = "credit_type", nullable = false, length = 16)
    private CreditType type;

    @Column(nullable = false, length = 25)
    private String mfo;

    @Column(nullable = false, unique = true, length = 25)
    private String applicationNumber;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CreditStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CreditStage stage;

    private Instant stageDeadline;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private User createdBy;

    @Version
    @Setter(AccessLevel.NONE)
    @Column(nullable = false)
    private long version;
}
