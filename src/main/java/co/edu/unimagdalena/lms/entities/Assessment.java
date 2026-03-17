package co.edu.unimagdalena.lms.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;


@Entity
@Table(name = "assessments")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    // ↑ Clave primaria de la evaluación

    @ManyToOne
    // ↑ RELACIÓN: Muchos Assessment → Un Student (N:1)
    //   Assessment es lado DUEÑO → tiene la FK student_id
    //   Un estudiante puede tener muchas evaluaciones; cada evaluación es de un estudiante

    @JoinColumn(name = "student_id", nullable = false)
    // ↑ FK en tabla "assessments": student_id → students.id

    private Student student;

    @ManyToOne
    // ↑ RELACIÓN: Muchos Assessment → Un Course (N:1)
    //   Assessment es lado DUEÑO → tiene la FK course_id
    //   Un curso puede tener muchas evaluaciones; cada evaluación pertenece a un curso

    @JoinColumn(name = "course_id", nullable = false)
    // ↑ FK en tabla "assessments": course_id → courses.id
    //   nullable=false: toda evaluación DEBE pertenecer a un curso.
    private Course course;


    private String type;

    private int score;


    @Column(name = "taken_at")

    private Instant takenAt;

}