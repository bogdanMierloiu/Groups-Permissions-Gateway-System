package com.bogdan_mierloiu.permissions_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "groups_permissions", indexes = @Index(columnList = "uuid"))
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    private Group group;

    @ManyToOne(fetch = FetchType.EAGER)
    private Permission permission;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "group_permission_action_relation",
            joinColumns = @JoinColumn(name = "group_permission_id"),
            inverseJoinColumns = @JoinColumn(name = "action_id"))
    private Set<Action> actions;

    @Column(unique = true, nullable = false)
    private UUID uuid;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
    }

    @Override
    public String toString() {
        return "GroupPermission{" +
                "group=" + group.getName() +
                ", permission=" + permission.getName() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupPermission that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

