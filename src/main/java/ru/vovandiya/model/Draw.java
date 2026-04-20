package ru.vovandiya.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@AllArgsConstructor
@Builder
@Table(name = "draw")
public class Draw extends PanacheEntity {
    @Column(nullable = false)
    private String format;

    @Column(name = "is_instantaneous", nullable = false)
    private Boolean isInstantaneous;

    @Column(name = "is_scheduled", nullable = false)
    private Boolean isScheduled;

    @Column(name = "draw_date")
    private LocalDateTime drawDate;

    @Column(name = "prise_pool")
    private Integer prisePool;
}