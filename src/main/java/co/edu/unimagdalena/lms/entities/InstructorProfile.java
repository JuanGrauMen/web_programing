package co.edu.unimagdalena.lms.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity

@Table(name = "instructor_profiles")
// ↑ Nombre de la tabla: "instructor_profiles".

@NoArgsConstructor

@AllArgsConstructor

@Builder

@Getter

@Setter

public class InstructorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    //   PK auto-generada.
    private long id;
    //   Clave primaria propia del perfil (distinta al id del instructor).

    @OneToOne
    //   RELACIÓN: Un InstructorProfile → Un Instructor (1:1)
    //   Esta entidad es el lado DUEÑO porque tiene la FK físicamente.

    @JoinColumn(name = "instructor_id", nullable = false)
    //   @JoinColumn: especifica la columna FK en esta tabla.
    private Instructor instructor;
    //   El instructor al que pertenece este perfil.

    private String phone;
    //   Número de teléfono de contacto del instructor.

    private String bio;
}