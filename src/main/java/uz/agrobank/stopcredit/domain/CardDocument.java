package uz.agrobank.stopcredit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "card_documents")
@Getter
@Setter
public class CardDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_id", nullable = false, updatable = false)
    private Card card;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false, unique = true)
    private String objectKey;

    @Column(nullable = false)
    private long sizeBytes;

    @Column(name = "uploaded_by_name", nullable = false, length = 150, updatable = false)
    private String uploadedBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant uploadedAt;
}
