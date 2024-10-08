package com.skladanov.VseSvoiSearchBot.bot.model;

import com.skladanov.VseSvoiSearchBot.bot.RequestStages;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Entity
@NoArgsConstructor
public class User {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    private String name;

    @Enumerated(EnumType.STRING)
    private RequestStages stage = RequestStages.SPECIALIST_AGE;
    private Boolean isCreationRequest = false;
    @OneToMany
    @ToString.Exclude
    private List<Request> requests;

    public User (Long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User user = (User) o;
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
