package com.skladanov.VseSvoiSearchBot.bot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.util.Objects;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class Response {
    @ManyToOne
    @JoinColumn(name = "users_id")
    User user;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "contacts")
    private String contacts;

    @Column(name = "content")
    private String content;

    @ManyToOne
    @JoinColumn(name = "requests_id")
    private Request request;

    public Response(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return content + "Контактные данные: " + contacts;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Response response = (Response) o;
        return id != null && Objects.equals(id, response.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
