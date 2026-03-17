package co.edu.unimagdalena.lms.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lessons")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    // ↑ PK auto-generada.
    private long id;
    //   Clave primaria de la lección.

    @ManyToOne
    //   RELACIÓN: Muchos Lesson → Un Course (N:1)
    //   Esta entidad (Lesson) es el lado DUEÑO → tiene la FK física en su tabla.
    //   Un curso tiene muchas lecciones, pero cada lección pertenece a un solo curso.

    @JoinColumn(name = "course_id", nullable = false)
    //   Columna FK en la tabla "lessons": course_id → courses.id
    private Course course;
    //   El curso al que pertenece esta lección.

    @Column(nullable = false)

    private String title;

    @Column(name = "order_index")
    private int orderIndex;
}