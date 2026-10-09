package com.horsemanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "roles", uniqueConstraints = @UniqueConstraint(name = "uq_roles_code", columnNames = "role_code"))
@Getter
@Setter
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;

    @Nationalized
    @Column(name = "role_code", length = 50, nullable = false)
    private String roleCode;

    @Nationalized
    @Column(name = "role_name", length = 100, nullable = false)
    private String roleName;

    @Nationalized
    @Column(name = "description", length = 255)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "datetime2")
    private LocalDateTime createdAt;

}
