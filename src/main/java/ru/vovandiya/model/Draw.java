package ru.vovandiya.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@ToString
@RequiredArgsConstructor
@Table(name = "draw")
public class Draw extends PanacheEntity {
    @Column(nullable = false)
    private String format;

    @Column(name = "draw_date")
    private LocalDateTime drawDate;
}