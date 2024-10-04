package com.skladanov.VseSvoiSearchBot.bot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.util.Objects;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "users_id")
    private User user;
    private String specAge;
    private String gender;
    private String methodTherapy;
    private String clientAge;
    private String clientGender;
    private String diagnosis;
    private String clientRequest;
    private String budget;
    private String extra;

    public Request(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Ищу специалиста: " +
                "Возраст специалиста: " + specAge + '\n' +
                "Пол специалиста: " + gender + '\n' +
                "Метод работы: " + methodTherapy + '\n' +
                "Возраст клиента/пациента: " + clientAge + '\n' +
                "Пол клиента,пациента: " + clientGender + '\n' +
                "Диагнозы: " + diagnosis + '\n' +
                "Запрос: " + clientRequest + '\n' +
                "Бюджет и частота встреч: " + budget + '\n' +
                "Дополнительные сведения: " + extra + '\n' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Request request = (Request) o;
        return id != null && Objects.equals(id, request.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
