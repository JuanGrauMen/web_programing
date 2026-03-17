package co.edu.unimagdalena.lms.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.Set;

@Entity

@Table(name = "instructors")
//   Nombre de la tabla en la base de datos: "instructors".

@NoArgsConstructor

@AllArgsConstructor

@Builder

@Getter

@Setter

public class Instructor {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    //   PK auto-generada. JPA elige la estrategia según el motor de BD.
    private long id;
    //   Clave primaria del instructor.

    @Column(nullable = false)
    //   Columna "email" en BD. NOT NULL → obligatorio.
    private String email;
    //   Email del instructor, usado para login e identificación única.

    @Column(name = "full_name", nullable = false)

    private String fullName;
    //  Nombre completo del instructor.

    @Column(name = "created_at")
    //   Fecha de creación del registro. Nullable (sin nullable=false).
    private Instant createdAt;
    //   Momento en que el instructor fue registrado en el sistema.

    @Column(name = "updated_at")
    //   Fecha de última modificación.
    private Instant updatedAt;

    @OneToMany(mappedBy = "instructor")
    // ↑ RELACIÓN: Un Instructor → Muchos Course (1:N)
    //   mappedBy="instructor": la FK instructor_id vive en la tabla courses.
    //   Este lado (Instructor) es el lado INVERSO; Course es el lado DUEÑO.
    private Set<Course> courses;
    //   Todos los cursos que ha creado/dicta este instructor.

    @OneToOne(mappedBy = "instructor")
    //   RELACIÓN: Un Instructor → Un InstructorProfile (1:1)
    //   mappedBy="instructor": la FK instructor_id vive en la tabla instructor_profiles.
    private InstructorProfile profile;
}