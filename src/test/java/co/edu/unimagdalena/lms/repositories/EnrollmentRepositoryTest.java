package co.edu.unimagdalena.lms.repositories;

import co.edu.unimagdalena.lms.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class EnrollmentRepositoryTest extends AbstractRepositoryIT {

    @Autowired private EnrollmentRepository enrollmentRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private co.edu.unimagdalena.Ims.repositories.CourseRepository courseRepository;
    @Autowired private InstructorRepository instructorRepository;

    private Student student1;
    private Student student2;
    private Course curso1;
    private Course curso2;

    @BeforeEach
    void setUp() {
        // Given base: 2 estudiantes, 2 cursos y 3 inscripciones entre ellos
        Instructor instructor = instructorRepository.save(
                Instructor.builder().email("prof@test.com").fullName("Prof")
                        .createdAt(Instant.now()).build()
        );

        student1 = studentRepository.save(Student.builder()
                .email("s1@test.com").fullName("Estudiante Uno")
                .createdAt(Instant.now()).build());

        student2 = studentRepository.save(Student.builder()
                .email("s2@test.com").fullName("Estudiante Dos")
                .createdAt(Instant.now()).build());

        curso1 = courseRepository.save(Course.builder()
                .instructor(instructor).title("Curso Uno")
                .active(true).status("PUBLISHED").createdAt(Instant.now()).build());

        curso2 = courseRepository.save(Course.builder()
                .instructor(instructor).title("Curso Dos")
                .active(true).status("PUBLISHED").createdAt(Instant.now()).build());

        // Las 3 inscripciones con distintos status y fechas
        enrollmentRepository.save(Enrollment.builder()
                .student(student1).course(curso1).status("ACTIVE")
                .enrolledAt(Instant.now().minus(5, ChronoUnit.DAYS)).build());

        enrollmentRepository.save(Enrollment.builder()
                .student(student1).course(curso2).status("COMPLETED")
                .enrolledAt(Instant.now().minus(1, ChronoUnit.DAYS)).build());

        enrollmentRepository.save(Enrollment.builder()
                .student(student2).course(curso1).status("ACTIVE")
                .enrolledAt(Instant.now().minus(2, ChronoUnit.DAYS)).build());
    }

    @Test
    @DisplayName("findByStudentId() - retorna todas las inscripciones de un estudiante")
    void findByStudentId_student1_retornaSusDosInscripciones() {
        // Given: student1 inscrito en curso1 y curso2 en @BeforeEach

        // When
        List<Enrollment> inscripciones = enrollmentRepository
                .findByStudentId(student1.getId());

        // Then: student1 tiene exactamente 2 inscripciones
        assertThat(inscripciones).hasSize(2);
        assertThat(inscripciones)
                .extracting(e -> e.getCourse().getTitle())
                .containsExactlyInAnyOrder("Curso Uno", "Curso Dos");
    }

    @Test
    @DisplayName("findByCourseId() - retorna todas las inscripciones de un curso")
    void findByCourseId_curso1_retornaDosEstudiantes() {
        // Given: student1 y student2 inscritos en curso1

        // When
        List<Enrollment> inscritos = enrollmentRepository
                .findByCourseId(curso1.getId());

        // Then: curso1 tiene 2 estudiantes inscritos
        assertThat(inscritos).hasSize(2);
        assertThat(inscritos)
                .extracting(e -> e.getStudent().getEmail())
                .containsExactlyInAnyOrder("s1@test.com", "s2@test.com");
    }

    @Test
    @DisplayName("findByStudentIdAndCourseId() - encuentra la inscripción específica")
    void findByStudentIdAndCourseId_combinacionExistente_retornaInscripcion() {
        // Given: student1 está inscrito en curso1 con status "ACTIVE"

        // When
        Optional<Enrollment> resultado = enrollmentRepository
                .findByStudentIdAndCourseId(student1.getId(), curso1.getId());

        // Then: la combinación existe y tiene el status correcto
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("findByStudentIdAndCourseId() - retorna vacío para combinación no existente")
    void findByStudentIdAndCourseId_combinacionInexistente_retornaVacio() {
        // Given: student2 NO está inscrito en curso2

        // When
        Optional<Enrollment> resultado = enrollmentRepository
                .findByStudentIdAndCourseId(student2.getId(), curso2.getId());

        // Then: no existe esa combinación de student_id + course_id
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("existsByStudentIdAndCourseId() - ⭐ true si la inscripción ya existe (anti-duplicados)")
    void existsByStudentIdAndCourseId_inscripcionExistente_retornaTrue() {
        // Given: student1 ya inscrito en curso1

        // When: se verifica antes de intentar inscribir de nuevo
        boolean existe = enrollmentRepository
                .existsByStudentIdAndCourseId(student1.getId(), curso1.getId());
        // ↑ En la capa de servicio: if (existe) → throw AlreadyEnrolledException

        // Then: devuelve true → el servicio debe rechazar la inscripción duplicada
        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("existsByStudentIdAndCourseId() - false si el estudiante no está inscrito")
    void existsByStudentIdAndCourseId_noInscrito_retornaFalse() {
        // Given: student2 NO está en curso2

        // When
        boolean existe = enrollmentRepository
                .existsByStudentIdAndCourseId(student2.getId(), curso2.getId());

        // Then: false → el servicio puede proceder con la inscripción
        assertThat(existe).isFalse();
    }

    @Test
    @DisplayName("findByStatus() - filtra todas las inscripciones por su estado")
    void findByStatus_retornaInscripcionesPorEstado() {
        // Given: 2 inscripciones ACTIVE (s1→c1, s2→c1), 1 COMPLETED (s1→c2)

        // When
        List<Enrollment> activas    = enrollmentRepository.findByStatus("ACTIVE");
        List<Enrollment> completadas = enrollmentRepository.findByStatus("COMPLETED");
        List<Enrollment> canceladas  = enrollmentRepository.findByStatus("CANCELLED");

        // Then
        assertThat(activas).hasSize(2);
        assertThat(completadas).hasSize(1);
        assertThat(canceladas).isEmpty();
    }

    @Test
    @DisplayName("findByStudentIdAndStatus() - inscripciones de un estudiante filtradas por estado")
    void findByStudentIdAndStatus_student1Active_retornaSoloSuActiva() {
        // Given: student1 tiene 1 ACTIVE (curso1) y 1 COMPLETED (curso2)

        // When: busca solo las ACTIVE de student1
        List<Enrollment> activasDeStudent1 = enrollmentRepository
                .findByStudentIdAndStatus(student1.getId(), "ACTIVE");

        // Then: solo aparece la inscripción a curso1
        assertThat(activasDeStudent1).hasSize(1);
        assertThat(activasDeStudent1.get(0).getCourse().getTitle())
                .isEqualTo("Curso Uno");
    }

    @Test
    @DisplayName("findByEnrolledAtBetween() - inscripciones dentro del rango de fechas")
    void findByEnrolledAtBetween_retornaLasDelRango() {
        // Given: inscripciones hace 5 días, 2 días y 1 día
        //        rango: últimos 3 días
        Instant desde = Instant.now().minus(3, ChronoUnit.DAYS);
        Instant hasta = Instant.now().plus(1, ChronoUnit.HOURS);
        // ↑ Dentro del rango: s1→c2 (1 día) y s2→c1 (2 días) → 2 inscripciones.
        //   Fuera del rango: s1→c1 (5 días) → NO aparece.

        // When
        List<Enrollment> enRango = enrollmentRepository
                .findByEnrolledAtBetween(desde, hasta);

        // Then
        assertThat(enRango).hasSize(2);
    }

    @Test
    @DisplayName("countByCourseId() - cuenta los estudiantes inscritos en un curso")
    void countByCourseId_retornaCantidadCorrecta() {
        // Given: curso1 tiene 2 inscripciones, curso2 tiene 1

        // When
        long totalCurso1 = enrollmentRepository.countByCourseId(curso1.getId());
        long totalCurso2 = enrollmentRepository.countByCourseId(curso2.getId());

        // Then
        assertThat(totalCurso1).isEqualTo(2L);
        assertThat(totalCurso2).isEqualTo(1L);
    }

    @Test
    @DisplayName("findRecentEnrollmentsByCourseId() - más reciente primero (ORDER BY DESC)")
    void findRecentEnrollmentsByCourseId_retornaOrdenadoMasRecientePrimero() {
        // Given: curso1 tiene s1 (hace 5 días) y s2 (hace 2 días)
        //        → s2 es el más reciente de curso1

        // When
        List<Enrollment> recientes = enrollmentRepository
                .findRecentEnrollmentsByCourseId(curso1.getId());

        // Then: el primer elemento (índice 0) debe ser el más reciente (s2)
        assertThat(recientes).hasSize(2);
        assertThat(recientes.get(0).getStudent().getEmail())
                .isEqualTo("s2@test.com");
        // ↑ s2 se inscribió hace 2 días → más reciente que s1 (hace 5 días).
        assertThat(recientes.get(1).getStudent().getEmail())
                .isEqualTo("s1@test.com");
        // ↑ s1 aparece de segundo por ser el más antiguo de curso1.
    }
}


