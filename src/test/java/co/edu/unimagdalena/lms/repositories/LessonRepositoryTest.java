package co.edu.unimagdalena.lms.repositories;

import co.edu.unimagdalena.lms.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class LessonRepositoryTest extends AbstractRepositoryIT {

    @Autowired private LessonRepository lessonRepository;
    @Autowired private co.edu.unimagdalena.Ims.repositories.CourseRepository courseRepository;
    @Autowired private InstructorRepository instructorRepository;

    private Course curso;

    @BeforeEach
    void setUp() {
        Instructor instructor = instructorRepository.save(
                Instructor.builder()
                        .email("prof@test.com").fullName("Prof Test")
                        .createdAt(Instant.now()).build()
        );

        curso = courseRepository.save(
                Course.builder()
                        .instructor(instructor).title("Curso Test")
                        .active(true).status("PUBLISHED").createdAt(Instant.now()).build()
        );

        lessonRepository.save(Lesson.builder()
                .course(curso).title("Lección 3: POO Avanzado").orderIndex(3).build());
        lessonRepository.save(Lesson.builder()
                .course(curso).title("Lección 1: Introducción a Java").orderIndex(1).build());
        lessonRepository.save(Lesson.builder()
                .course(curso).title("Lección 2: Variables y Tipos").orderIndex(2).build());
    }

    @Test
    @DisplayName("findByCourseIdOrderByOrderIndexAsc() - retorna lecciones ordenadas de 1 a N")
    void findByCourseIdOrderByOrderIndex_retornaEnOrdenCorrecto() {

        List<Lesson> lecciones = lessonRepository
                .findByCourseIdOrderByOrderIndexAsc(curso.getId());

        assertThat(lecciones).hasSize(3);
        assertThat(lecciones)
                .extracting(Lesson::getOrderIndex)
                .containsExactly(1, 2, 3);
        assertThat(lecciones.get(0).getTitle())
                .isEqualTo("Lección 1: Introducción a Java");
        assertThat(lecciones.get(2).getTitle())
                .isEqualTo("Lección 3: POO Avanzado");
    }

    @Test
    @DisplayName("findByCourseIdOrderByOrderIndexAsc() - retorna vacío para curso sin lecciones")
    void findByCourseId_cursoSinLecciones_retornaVacio() {
        Instructor otroInstructor = instructorRepository.save(
                Instructor.builder().email("otro@test.com").fullName("Otro Prof")
                        .createdAt(Instant.now()).build()
        );
        Course cursoVacio = courseRepository.save(
                Course.builder().instructor(otroInstructor).title("Curso Vacío")
                        .active(true).status("DRAFT").createdAt(Instant.now()).build()
        );

        List<Lesson> resultado = lessonRepository
                .findByCourseIdOrderByOrderIndexAsc(cursoVacio.getId());

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("findByCourseIdAndTitle() - encuentra lección por título exacto dentro del curso")
    void findByCourseIdAndTitle_tituloExacto_retornaLeccion() {

        Optional<Lesson> resultado = lessonRepository
                .findByCourseIdAndTitle(curso.getId(), "Lección 2: Variables y Tipos");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getOrderIndex()).isEqualTo(2);
    }

    @Test
    @DisplayName("findByCourseIdAndTitle() - retorna vacío si el título no existe en el curso")
    void findByCourseIdAndTitle_tituloInexistente_retornaVacio() {

        Optional<Lesson> resultado = lessonRepository
                .findByCourseIdAndTitle(curso.getId(), "Título que no existe");

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("findByTitleContainingIgnoreCase() - búsqueda global en todos los cursos")
    void findByTitle_busquedaGlobal_retornaTodasLasCoincidencias() {

        List<Lesson> resultado = lessonRepository
                .findByTitleContainingIgnoreCase("lección");

        assertThat(resultado).hasSize(3);
    }

    @Test
    @DisplayName("findByTitleContainingIgnoreCase() - solo retorna coincidencias exactas del fragmento")
    void findByTitle_busquedaEspecifica_retornaSoloCoincidentes() {

        List<Lesson> resultado = lessonRepository
                .findByTitleContainingIgnoreCase("POO");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getOrderIndex()).isEqualTo(3);
    }

    @Test
    @DisplayName("countByCourseId() - cuenta exactamente las lecciones del curso")
    void countByCourseId_retornaCantidadCorrecta() {

        long total = lessonRepository.countByCourseId(curso.getId());

        assertThat(total).isEqualTo(3L);
    }

    @Test
    @DisplayName("findByCourseIdAndOrderIndexGreaterThan() - lecciones después de un índice dado")
    void findByOrderIndexGreaterThan_retornaLeccionesPosteriores() {

        List<Lesson> posteriores = lessonRepository
                .findByCourseIdAndOrderIndexGreaterThan(curso.getId(), 1);

        assertThat(posteriores).hasSize(2);
        assertThat(posteriores)
                .extracting(Lesson::getOrderIndex)
                .containsExactlyInAnyOrder(2, 3);
    }

    @Test
    @DisplayName("findFirstLessonByCourseId() - retorna la lección con menor orderIndex")
    void findFirstLessonByCourseId_retornaPrimera() {

        Optional<Lesson> primera = lessonRepository
                .findFirstLessonByCourseId(curso.getId());

        assertThat(primera).isPresent();
        assertThat(primera.get().getOrderIndex()).isEqualTo(1);
        assertThat(primera.get().getTitle())
                .isEqualTo("Lección 1: Introducción a Java");
    }

    @Test
    @DisplayName("findFirstLessonByCourseId() - retorna vacío si el curso no tiene lecciones")
    void findFirstLessonByCourseId_cursoSinLecciones_retornaVacio() {
        Instructor otro = instructorRepository.save(
                Instructor.builder().email("v@v.com").fullName("V")
                        .createdAt(Instant.now()).build()
        );
        Course vacio = courseRepository.save(
                Course.builder().instructor(otro).title("Vacío")
                        .active(true).status("DRAFT").createdAt(Instant.now()).build()
        );

        Optional<Lesson> primera = lessonRepository.findFirstLessonByCourseId(vacio.getId());

        assertThat(primera).isEmpty();
    }
}