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

class StudentRepositoryTest extends AbstractRepositoryIT {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private co.edu.unimagdalena.Ims.repositories.CourseRepository courseRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    private Student student1;
    private Student student2;
    private Course course;

    @BeforeEach
    void setUp() {
        Instructor instructor = instructorRepository.save(
                Instructor.builder()
                        .email("prof@test.com")
                        .fullName("Profesor Test")
                        .createdAt(Instant.now())
                        .build()
        );

        course = courseRepository.save(
                Course.builder()
                        .title("Spring Boot Avanzado")
                        .instructor(instructor)
                        .active(true)
                        .status("PUBLISHED")
                        .createdAt(Instant.now())
                        .build()
        );

        student1 = studentRepository.save(
                Student.builder()
                        .email("juan@test.com")
                        .fullName("Juan Pérez García")
                        .createdAt(Instant.now().minus(10, ChronoUnit.DAYS))
                        .build()
        );

        student2 = studentRepository.save(
                Student.builder()
                        .email("ana@test.com")
                        .fullName("Ana Martínez López")
                        .createdAt(Instant.now())
                        .build()
        );
    }


    @Test
    @DisplayName("save() - persiste un nuevo estudiante y genera ID automático")
    void save_debeGuardarEstudianteYGenerarId() {
        Student nuevo = Student.builder()
                .email("nuevo@test.com")
                .fullName("Estudiante Nuevo")
                .createdAt(Instant.now())
                .build();

        Student guardado = studentRepository.save(nuevo);

        assertThat(guardado.getId())
                .isGreaterThan(0);
        assertThat(guardado.getEmail())
                .isEqualTo("nuevo@test.com");
    }

    @Test
    @DisplayName("findById() - retorna el estudiante cuando el ID existe")
    void findById_conIdExistente_retornaEstudiante() {

        Optional<Student> resultado = studentRepository.findById(student1.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getEmail())
                .isEqualTo("juan@test.com");
    }

    @Test
    @DisplayName("findById() - retorna Optional vacío cuando el ID no existe")
    void findById_conIdInexistente_retornaVacio() {

        Optional<Student> resultado = studentRepository.findById(9999L);

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("findAll() - retorna todos los estudiantes guardados en BD")
    void findAll_retornaTodosLosEstudiantes() {

        List<Student> todos = studentRepository.findAll();

        assertThat(todos)
                .hasSize(2)
                .extracting(Student::getEmail)
                .containsExactlyInAnyOrder("juan@test.com", "ana@test.com");
    }

    @Test
    @DisplayName("deleteById() - elimina el estudiante y reduce el conteo")
    void deleteById_conIdExistente_eliminaEstudiante() {

        studentRepository.deleteById(student1.getId());

        assertThat(studentRepository.findById(student1.getId()))
                .isEmpty();
        assertThat(studentRepository.count())
                .isEqualTo(1L);
    }


    @Test
    @DisplayName("findByEmail() - encuentra el estudiante con ese email exacto")
    void findByEmail_emailExistente_retornaEstudiante() {

        Optional<Student> resultado = studentRepository.findByEmail("juan@test.com");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getFullName())
                .isEqualTo("Juan Pérez García");
    }

    @Test
    @DisplayName("findByEmail() - retorna vacío si el email no está registrado")
    void findByEmail_emailInexistente_retornaVacio() {

        Optional<Student> resultado = studentRepository.findByEmail("fantasma@test.com");

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("findByFullNameContainingIgnoreCase() - encuentra por fragmento de nombre")
    void findByFullName_textoParcial_retornaCoincidencias() {

        List<Student> resultado = studentRepository
                .findByFullNameContainingIgnoreCase("pérez");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getEmail()).isEqualTo("juan@test.com");
    }

    @Test
    @DisplayName("findByFullNameContainingIgnoreCase() - funciona con mayúsculas también")
    void findByFullName_conMayusculas_encuentraIgual() {

        List<Student> resultado = studentRepository
                .findByFullNameContainingIgnoreCase("MARTÍNEZ");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getEmail()).isEqualTo("ana@test.com");
    }

    @Test
    @DisplayName("findByFullNameContainingIgnoreCase() - retorna lista vacía si no hay coincidencias")
    void findByFullName_sinCoincidencias_retornaListaVacia() {

        List<Student> resultado = studentRepository
                .findByFullNameContainingIgnoreCase("zzznombreinexistentezzz");

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("existsByEmail() - retorna true cuando el email ya está registrado")
    void existsByEmail_emailRegistrado_retornaTrue() {

        boolean existe = studentRepository.existsByEmail("juan@test.com");

        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("existsByEmail() - retorna false para email no registrado")
    void existsByEmail_emailNuevo_retornaFalse() {

        boolean existe = studentRepository.existsByEmail("nuevo@test.com");

        assertThat(existe).isFalse();
    }

    @Test
    @DisplayName("findByCreatedAtAfter() - solo estudiantes creados después de la fecha dada")
    void findByCreatedAtAfter_retornaSoloLosRecientes() {
        Instant fechaCorte = Instant.now().minus(5, ChronoUnit.DAYS);

        List<Student> recientes = studentRepository.findByCreatedAtAfter(fechaCorte);

        assertThat(recientes).hasSize(1);
        assertThat(recientes.get(0).getEmail()).isEqualTo("ana@test.com");
    }


    @Test
    @DisplayName("findStudentsByCourseId() - retorna estudiantes inscritos en el curso")
    void findStudentsByCourseId_conInscripcion_retornaEstudiante() {
        enrollmentRepository.save(
                Enrollment.builder()
                        .student(student1)
                        .course(course)
                        .status("ACTIVE")
                        .enrolledAt(Instant.now())
                        .build()
        );

        List<Student> inscritos = studentRepository.findStudentsByCourseId(course.getId());

        assertThat(inscritos).hasSize(1);
        assertThat(inscritos.get(0).getEmail()).isEqualTo("juan@test.com");
    }

    @Test
    @DisplayName("findStudentsByCourseId() - retorna vacío si nadie está inscrito")
    void findStudentsByCourseId_sinInscripciones_retornaVacio() {

        List<Student> resultado = studentRepository.findStudentsByCourseId(course.getId());

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("findStudentsByAvgScoreInCourse() - filtra por promedio mínimo de score")
    void findStudentsByAvgScore_conScoresDistintos_soloApareceElQueSupera() {
        assessmentRepository.save(Assessment.builder()
                .student(student1).course(course).type("QUIZ")
                .score(80).takenAt(Instant.now()).build());
        assessmentRepository.save(Assessment.builder()
                .student(student1).course(course).type("QUIZ")
                .score(90).takenAt(Instant.now()).build());
        assessmentRepository.save(Assessment.builder()
                .student(student2).course(course).type("QUIZ")
                .score(40).takenAt(Instant.now()).build());
        assessmentRepository.save(Assessment.builder()
                .student(student2).course(course).type("QUIZ")
                .score(50).takenAt(Instant.now()).build());

        List<Student> destacados = studentRepository
                .findStudentsByAvgScoreInCourse(course.getId(), 70.0);

        assertThat(destacados).hasSize(1);
        assertThat(destacados.get(0).getEmail()).isEqualTo("juan@test.com");
    }
}