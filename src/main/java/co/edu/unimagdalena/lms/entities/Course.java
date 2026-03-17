package co.edu.unimagdalena.lms.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.Set;



@Entity
@Table(name = "courses")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    //  PK auto-generada por JPA/Hibernate
    private long id;
    //   Clave primaria del curso.
    //   REFERENCIADO EN: Lesson.course_id, Enrollment.course_id, Assessment.course_id

    @ManyToOne
    //   RELACIÓN: Muchos Course → Un Instructor (N:1)
    //   Esta entidad (Course) es el lado DUEÑO porque tiene la FK física.
    //   Un instructor puede tener múltiples cursos, pero cada curso tiene UN instructor.

    @JoinColumn(name = "instructor_id", nullable = false)

    private Instructor instructor;
    //   El instructor que dicta/creó este curso
    //   REFERENCIA INVERSA EN: Instructor.java (@OneToMany private Set<Course> courses)


    @Column(nullable = false)

    private String title;


    private String status;


    private boolean active;


    @Column(name = "created_at")

    private Instant createdAt;


    @Column(name = "updated_at")

    private Instant updatedAt;


    @OneToMany(mappedBy = "course")
    //   RELACIÓN: Un Course → Muchos Lesson (1:N)
    //   mappedBy="course": la FK course_id vive en la tabla lessons
    //   Course es el lado INVERSO; Lesson es el lado DUEÑO
    private Set<Lesson> lessons;
    //   Todas las lecciones que componen este curso.
    //   ORIGEN: Lesson.java (@ManyToOne private Course course)


    @OneToMany(mappedBy = "course")
    //   RELACIÓN: Un Course → Muchos Enrollment (1:N)
    //   mappedBy="course": la FK course_id vive en la tabla enrollments.
    private Set<Enrollment> enrollments;
    //   Todas las inscripciones de estudiantes en este curso.
    //   ORIGEN: Enrollment.java (@ManyToOne private Course course)

    @OneToMany(mappedBy = "course")
    //   RELACIÓN: Un Course → Muchos Assessment (1:N)
    //   mappedBy="course": la FK course_id vive en la tabla assessments.
    private Set<Assessment> assessments;
    //   Todas las evaluaciones realizadas en este curso.
    //   ORIGEN: Assessment.java (@ManyToOne private Course course)

}