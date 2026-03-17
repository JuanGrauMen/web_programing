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

class CourseRepositoryTest extends AbstractRepositoryIT {

    @Autowired private co.edu.unimagdalena.Ims.repositories.CourseRepository courseRepository;
    @Autowired private InstructorRepository instructorRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private EnrollmentRepository enrollmentRepository;
    @Autowired private LessonRepository lessonRepository;

    private Instructor instructor;
    private Course cursoActivo;
    private Course cursoBorrador;

    @BeforeEach
    void setUp() {
        instructor = instructorRepository.save(
                Instructor.builder()
                        .email("prof@test.com").fullName("Prof Test")
                        .createdAt(Instant.now()).build()
        );

        cursoActivo = courseRepository.save(
                Course.builder()
                        .instructor(instructor)
                        .title("Programación en Java")
                        .status("PUBLISHED")
                        .active(true)
                        .createdAt(Instant.now().minus(5, ChronoUnit.DAYS))
                        .updatedAt(Instant.now())
                        .build()
        );

        cursoBorrador = courseRepository.save(
                Course.builder()
                        .instructor(instructor)
                        .title("Bases de Datos Avanzadas")
                        .status("DRAFT")
                        .active(false)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build()
        );
    }

    @Test
    @DisplayName("findByActiveTrue() - solo retorna los cursos con active=true")
    void findByActiveTrue_retornaSoloCursosActivos() {

        List<Course> activos = courseRepository.findByActiveTrue();

        assertThat(activos).hasSize(1);
        assertThat(activos.get(0).getTitle()).isEqualTo("Programación en Java");
    }

    @Test
    @DisplayName("findByStatus() - filtra por cada valor de status correctamente")
    void findByStatus_retornaCursosDelStatusDado() {

        List<Course> publicados = courseRepository.findByStatus("PUBLISHED");
        List<Course> borradores = courseRepository.findByStatus("DRAFT");
        List<Course> archivados = courseRepository.findByStatus("ARCHIVED");

        assertThat(publicados).hasSize(1);
        assertThat(borradores).hasSize(1);
        assertThat(archivados).isEmpty();
    }

    @Test
    @DisplayName("findByTitleContainingIgnoreCase() - encuentra por fragmento de título")
    void findByTitle_textoParcial_retornaCoincidencias() {

        List<Course> resultado = courseRepository
                .findByTitleContainingIgnoreCase("java");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getStatus()).isEqualTo("PUBLISHED");
    }

    @Test
    @DisplayName("findByInstructorId() - retorna todos los cursos del instructor dado")
    void findByInstructorId_retornaTodosLosCursosDelInstructor() {

        List<Course> cursos = courseRepository.findByInstructorId(instructor.getId());

        assertThat(cursos)
                .hasSize(2)
                .extracting(Course::getTitle)
                .containsExactlyInAnyOrder(
                        "Programación en Java",
                        "Bases de Datos Avanzadas"
                );
    }

    @Test
    @DisplayName("findByInstructorIdAndActiveTrue() - solo los cursos activos del instructor")
    void findByInstructorIdAndActiveTrue_retornaSoloActivos() {

        List<Course> activosDelInstructor = courseRepository
                .findByInstructorIdAndActiveTrue(instructor.getId());

        assertThat(activosDelInstructor).hasSize(1);
        assertThat(activosDelInstructor.get(0).getTitle())
                .isEqualTo("Programación en Java");
    }

    @Test
    @DisplayName("findByCreatedAtBetween() - solo cursos creados dentro del rango")
    void findByCreatedAtBetween_retornaSoloLosDelRango() {
        Instant desde = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant hasta = Instant.now().plus(1, ChronoUnit.HOURS);

        List<Course> enRango = courseRepository.findByCreatedAtBetween(desde, hasta);

        assertThat(enRango).hasSize(1);
        assertThat(enRango.get(0).getTitle()).isEqualTo("Bases de Datos Avanzadas");
    }

    @Test
    @DisplayName("findByIdWithLessons() - carga el curso con sus lecciones (LEFT JOIN FETCH)")
    void findByIdWithLessons_cursoConLecciones_retornaConLecciones() {
        lessonRepository.save(Lesson.builder()
                .course(cursoActivo).title("Lección 1: Introducción")
                .orderIndex(1).build());
        lessonRepository.save(Lesson.builder()
                .course(cursoActivo).title("Lección 2: Variables")
                .orderIndex(2).build());

        Optional<Course> resultado = courseRepository
                .findByIdWithLessons(cursoActivo.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getLessons())
                .hasSize(2)
                .extracting(Lesson::getTitle)
                .containsExactlyInAnyOrder(
                        "Lección 1: Introducción",
                        "Lección 2: Variables"
                );
    }

    @Test
    @DisplayName("findByIdWithLessons() - LEFT JOIN funciona aunque el curso no tenga lecciones")
    void findByIdWithLessons_sinLecciones_retornaCursoConSetVacio() {

        Optional<Course> resultado = courseRepository
                .findByIdWithLessons(cursoBorrador.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getLessons()).isEmpty();
    }

    @Test
    @DisplayName("findCoursesByStudentId() - cursos en los que el estudiante está inscrito")
    void findCoursesByStudentId_conInscripcion_retornaCursos() {
        Student estudiante = studentRepository.save(
                Student.builder()
                        .email("est@test.com").fullName("Estudiante Test")
                        .createdAt(Instant.now()).build()
        );
        enrollmentRepository.save(Enrollment.builder()
                .student(estudiante).course(cursoActivo)
                .status("ACTIVE").enrolledAt(Instant.now()).build());

        List<Course> cursosDelEstudiante = courseRepository
                .findCoursesByStudentId(estudiante.getId());

        assertThat(cursosDelEstudiante).hasSize(1);
        assertThat(cursosDelEstudiante.get(0).getTitle())
                .isEqualTo("Programación en Java");
    }

    @Test
    @DisplayName("findCoursesWithMoreThanNEnrollments() - filtra por cantidad de inscritos")
    void findCoursesWithMoreThanN_retornaSoloLosSuficientementePopulares() {
        for (int i = 1; i <= 3; i++) {
            Student est = studentRepository.save(
                    Student.builder()
                            .email("est" + i + "@test.com")
                            .fullName("Estudiante " + i)
                            .createdAt(Instant.now()).build()
            );
            enrollmentRepository.save(Enrollment.builder()
                    .student(est).course(cursoActivo)
                    .status("ACTIVE").enrolledAt(Instant.now()).build());
        }

        List<Course> conMasDe2 = courseRepository
                .findCoursesWithMoreThanNEnrollments(2);
        List<Course> conMasDe5 = courseRepository
                .findCoursesWithMoreThanNEnrollments(5);

        assertThat(conMasDe2).hasSize(1);
        assertThat(conMasDe5).isEmpty();
    }
}
