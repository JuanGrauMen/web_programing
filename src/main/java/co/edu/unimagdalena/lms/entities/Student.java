
package co.edu.unimagdalena.lms.entities;

import jakarta.persistence.*;
        import lombok.*;
        import java.time.Instant;
import java.util.Set;

@Entity

@Table(name = "students")
// ↑ Especifica el nombre EXACTO de la tabla en la base de datos.

@NoArgsConstructor

@AllArgsConstructor

@Builder

@Getter

@Setter

public class Student {

    @Id
    //   Marca este campo como la CLAVE PRIMARIA de la entidad.

    @GeneratedValue(strategy = GenerationType.AUTO)
    // ↑ JPA generará el valor del ID automáticamente.
    private long id;

    @Column(nullable = false)
    private String email;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "student")
    //   RELACIÓN: Un Student → Muchos Assessment (1:N)
    //   Esto significa que la FK (student_id) vive en la tabla assessments, NO en students.
    private Set<Assessment> assessments;

    @OneToMany(mappedBy = "student")
    //   RELACIÓN: Un Student → Muchas Enrollment (1:N)
    private Set<Enrollment> enrollments;
}
