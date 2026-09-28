package uz.agrobank.stopcredit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "executors")
@Getter
@Setter
public class Executor extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 50)
    private String phone = "";

    @Column(nullable = false, length = 50)
    private String extension = "";
}
