package com.bogdan_mierloiu.permissions_system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "groups", indexes = @Index(columnList = "uuid", name = "groupIndex"))
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Size(min = 3, max = 35, message = "Name must be between 3 and 35 characters")
    private String name;

    @Column(length = 512)
    @Size(min = 3, max = 512, message = "Description must be between 3 and 512 characters")
    private String description;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "parent_id")
    private Group parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private Set<Group> children;

    @ManyToMany(mappedBy = "groups", fetch = FetchType.LAZY)
    private Set<User> users;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(name = "inherit_permissions", nullable = false, columnDefinition = "boolean default false")
    private Boolean inheritPermissions;

    @OneToMany(mappedBy = "group", fetch = FetchType.EAGER, cascade = CascadeType.PERSIST, orphanRemoval = true)
    private Set<GroupPermission> groupPermissions;

    @Column(unique = true, nullable = false)
    private UUID uuid;

    @PrePersist
    public void prePersist() {
        this.uuid = UUID.randomUUID();
    }

    @Override
    public String toString() {
        return "Group: " + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Group group)) return false;
        return Objects.equals(id, group.id) && Objects.equals(uuid, group.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, uuid);
    }

}
