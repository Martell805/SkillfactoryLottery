package ru.vovandiya.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.vovandiya.dto.DrawStatus;

@Getter
@Setter
@Entity
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "draw_result",
    indexes = @Index(name = "idx_draw_result_draw_id", columnList = "draw_id")
)
public class DrawResult extends PanacheEntity {
    @ToString.Exclude
    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "draw_id")
    private Draw draw;

    @Column(name = "drawn_numbers")
    private String drawnNumbers;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DrawStatus status;
}