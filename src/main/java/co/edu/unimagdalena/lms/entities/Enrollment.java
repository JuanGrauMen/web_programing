package co.edu.unimagdalena.lms.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;



@Entity
@Table(name = "enrollments")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    //   Clave primaria propia de la inscripción


    @ManyToOne
    //   RELACIÓN: Muchos Enrollment → Un Student (N:1)
    //   Enrollment es lado DUEÑO → tiene la FK student_id.

    @JoinColumn(name = "student_id", nullable = false)
    //   FK en tabla "enrollments": student_id → students.id
    //   nullable=false: toda inscripción DEBE tener un estudiante.
    private Student student;
    //   El estudiante que está inscrito.

    //   REFERENCIA INVERSA EN: Student.java (@OneToMany private Set<Enrollment> enrollments)


    @ManyToOne
    //   RELACIÓN: Muchos Enrollment → Un Course (N:1)
    //   Enrollment es lado DUEÑO → tiene la FK course_id

    @JoinColumn(name = "course_id", nullable = false)
    //   FK en tabla "enrollments": course_id → courses.id
    //   nullable=false: toda inscripción DEBE pertenecer a un curso
    private Course course;
    //   El curso en que está inscrito el estudiante

    //   REFERENCIA INVERSA EN: Course.java (@OneToMany private Set<Enrollment> enrollments)


    private String status;
    //   Estado de la inscripción: "ACTIVE", "COMPLETED", "CANCELLED", "PENDING", etc


    @Column(name = "enrolled_at")
    //   enrolled_at en BD, enrolledAt en Java (snake_case ↔ camelCase).
    private Instant enrolledAt;
    //   Fecha/hora exacta en que se realizó la inscripción.

}