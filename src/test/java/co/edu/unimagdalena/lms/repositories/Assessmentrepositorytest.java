package co.edu.unimagdalena.lms.repositories;

import co.edu.unimagdalena.lms.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.*;


class AssessmentRepositoryTest extends AbstractRepositoryIT {

    @Autowired private AssessmentRepository assessmentRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private co.edu.unimagdalena.Ims.repositories.CourseRepository courseRepository;
    @Autowired private InstructorRepository instructorRepository;

    private Student student1;
    private Student student2;
    private Course curso;

    @BeforeEach
    void setUp() {
        Instructor instructor = instructorRepository.save(
                Instructor.builder().email("p@t.com").fullName("Prof")
                        .createdAt(Instant.now()).build()
        );

        student1 = studentRepository.save(Student.builder()
                .email("s1@t.com").fullName("Estudiante Uno")
                .createdAt(Instant.now()).build());

        student2 = studentRepository.save(Student.builder()
                .email("s2@t.com").fullName("Estudiante Dos")
                .createdAt(Instant.now()).build());

        curso = courseRepository.save(Course.builder()
                .instructor(instructor).title("Curso Test")
                .active(true).status("PUBLISHED").createdAt(Instant.now()).build());

        assessmentRepository.save(Assessment.builder()
                .student(student1).course(curso).type("QUIZ")
                .score(80).takenAt(Instant.now().minus(10, ChronoUnit.DAYS)).build());
        assessmentRepository.save(Assessment.builder()
                .student(student1).course(curso).type("QUIZ")
                .score(90).takenAt(Instant.now().minus(5, ChronoUnit.DAYS)).build());

        assessmentRepository.save(Assessment.builder()
                .student(student2).course(curso).type("QUIZ")
                .score(50).takenAt(Instant.now().minus(8, ChronoUnit.DAYS)).build());
        assessmentRepository.save(Assessment.builder()
                .student(student2).course(curso).type("FINAL_EXAM")
                .score(95).takenAt(Instant.now().minus(1, ChronoUnit.DAYS)).build());
    }


    @Test
    @DisplayName("findByStudentId() - retorna todas las evaluaciones de un estudiante")
    void findByStudentId_student1_retornaSusEvaluaciones() {

        List<Assessment> resultado = assessmentRepository.findByStudentId(student1.getId());

        assertThat(resultado).hasSize(2);
        assertThat(resultado)
                .extracting(Assessment::getScore)
                .containsExactlyInAnyOrder(80, 90);
    }

    @Test
    @DisplayName("findByCourseId() - retorna las 4 evaluaciones del curso")
    void findByCourseId_retornaTodas() {

        List<Assessment> resultado = assessmentRepository.findByCourseId(curso.getId());

        assertThat(resultado).hasSize(4);
    }

    @Test
    @DisplayName("findByStudentIdAndCourseId() - evaluaciones de un estudiante en un curso")
    void findByStudentIdAndCourseId_retornaDelEstudianteEnCurso() {

        List<Assessment> resultado = assessmentRepository
                .findByStudentIdAndCourseId(student2.getId(), curso.getId());

        assertThat(resultado).hasSize(2);
        assertThat(resultado)
                .extracting(Assessment::getType)
                .containsExactlyInAnyOrder("QUIZ", "FINAL_EXAM");
    }

    @Test
    @DisplayName("findByType() - filtra todas las evaluaciones por tipo globalmente")
    void findByType_retornaSoloDelTipoDado() {

        List<Assessment> quizzes = assessmentRepository.findByType("QUIZ");
        List<Assessment> finales = assessmentRepository.findByType("FINAL_EXAM");

        assertThat(quizzes).hasSize(3);
        assertThat(finales).hasSize(1);
    }

    @Test
    @DisplayName("findByCourseIdAndType() - evaluaciones de un curso filtradas por tipo")
    void findByCourseIdAndType_retornaQuizzesDelCurso() {

        List<Assessment> quizzesDelCurso = assessmentRepository
                .findByCourseIdAndType(curso.getId(), "QUIZ");

        assertThat(quizzesDelCurso).hasSize(3);
        assertThat(quizzesDelCurso)
                .allMatch(a -> a.getType().equals("QUIZ"));
    }

    @Test
    @DisplayName("findByScoreGreaterThanEqual() - solo evaluaciones que superan el mínimo")
    void findByScoreGreaterThanEqual_score80_retornaTres() {

        List<Assessment> aprobadas = assessmentRepository
                .findByScoreGreaterThanEqual(80);

        assertThat(aprobadas).hasSize(3);
        assertThat(aprobadas)
                .allMatch(a -> a.getScore() >= 80);
    }

    @Test
    @DisplayName("findByTakenAtBetween() - evaluaciones dentro del rango de fechas")
    void findByTakenAtBetween_retornaLasDelRango() {
        Instant desde = Instant.now().minus(6, ChronoUnit.DAYS);
        Instant hasta = Instant.now().plus(1, ChronoUnit.HOURS);

        List<Assessment> enRango = assessmentRepository.findByTakenAtBetween(desde, hasta);

        assertThat(enRango).hasSize(2);
        assertThat(enRango)
                .extracting(Assessment::getScore)
                .containsExactlyInAnyOrder(90, 95);
    }


    @Test
    @DisplayName("findAvgScoreByStudentAndCourse() - calcula el promedio exacto de un estudiante")
    void findAvgScore_student1EnCurso_retornaPromedio85() {

        Double promedio = assessmentRepository
                .findAvgScoreByStudentAndCourse(student1.getId(), curso.getId());

        assertThat(promedio).isNotNull();
        assertThat(promedio).isEqualTo(85.0);
    }

    @Test
    @DisplayName("findAvgScoreByStudentAndCourse() - cada estudiante tiene su propio promedio")
    void findAvgScore_student2EnCurso_retornaPromedio72punto5() {

        Double promedio = assessmentRepository
                .findAvgScoreByStudentAndCourse(student2.getId(), curso.getId());

        assertThat(promedio).isEqualTo(72.5);
    }

    @Test
    @DisplayName("findAvgScoreByStudentAndCourse() - retorna null si no hay evaluaciones")
    void findAvgScore_sinEvaluaciones_retornaNull() {

        Double promedio = assessmentRepository
                .findAvgScoreByStudentAndCourse(student1.getId(), 9999L);

        assertThat(promedio).isNull();
    }

    @Test
    @DisplayName("findMaxScoreByCourseId() - retorna el score más alto del curso")
    void findMaxScoreByCourseId_retorna95() {

        Integer maxScore = assessmentRepository.findMaxScoreByCourseId(curso.getId());

        assertThat(maxScore).isNotNull();
        assertThat(maxScore).isEqualTo(95);
    }

    @Test
    @DisplayName("findMaxScoreByCourseId() - retorna null para curso sin evaluaciones")
    void findMaxScoreByCourseId_cursoVacio_retornaNull() {

        Integer maxScore = assessmentRepository.findMaxScoreByCourseId(9999L);

        assertThat(maxScore).isNull();
    }

    @Test
    @DisplayName("findTopScoresByCourseId() - retorna todas ordenadas de mayor a menor (ranking)")
    void findTopScoresByCourseId_retornaOrdenadoDescendente() {

        List<Assessment> ranking = assessmentRepository
                .findTopScoresByCourseId(curso.getId());

        assertThat(ranking).hasSize(4);
        assertThat(ranking)
                .extracting(Assessment::getScore)
                .containsExactly(95, 90, 80, 50);
        assertThat(ranking.get(0).getStudent().getEmail())
                .isEqualTo("s2@t.com");
    }

    @Test
    @DisplayName("findTopScoresByCourseId() - retorna vacío para curso sin evaluaciones")
    void findTopScoresByCourseId_cursoSinEvaluaciones_retornaVacio() {

        List<Assessment> ranking = assessmentRepository.findTopScoresByCourseId(9999L);

        assertThat(ranking).isEmpty();
    }
}



