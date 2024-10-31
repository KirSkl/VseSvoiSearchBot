package com.skladanov.VseSvoiSearchBot.bot.model;

import com.skladanov.VseSvoiSearchBot.bot.ResponseStages;
import com.skladanov.VseSvoiSearchBot.bot.RequestStages;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Entity
@Table(name = "users")
@NoArgsConstructor
public class User {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RequestStages requestStage = RequestStages.SPECIALIST_AGE;

    @Enumerated(EnumType.STRING)
    private ResponseStages responseStages = ResponseStages.NUMBER;

    @Column(name = "isCreationRequest")
    private Boolean isCreationRequest = false;

    @Column(name = "isAnswering")
    private Boolean isAnswering = false;

    @Column(name = "isStoping")
    private Boolean isStop = false;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    @ToString.Exclude
    private List<Request> requests;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    @ToString.Exclude
    private List<Response> responses;

    public User (Long id) {
        this.id = id;
    }

    public String requestsToMessage() {
        StringJoiner joiner = new StringJoiner("\n\n");
        for (Request request : requests) {
            joiner.add(request.toStringToRemind());
        }
        return joiner.toString();
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
