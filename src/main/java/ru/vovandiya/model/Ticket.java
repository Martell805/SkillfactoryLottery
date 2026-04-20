package ru.vovandiya.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "ticket",
    indexes = {
        @Index(name = "idx_ticket_draw_id", columnList = "draw_id"),
        @Index(name = "idx_ticket_operation_id", columnList = "operation_id")
    }
)
public class Ticket extends PanacheEntity {
    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "draw_id", nullable = false)
    private Draw draw;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "operation_id")
    private Operation operation;

    @Column(name = "picked_numbers")
    private String pickedNumbers;

    private Integer prize;
}