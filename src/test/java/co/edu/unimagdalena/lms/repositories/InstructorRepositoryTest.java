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

class InstructorRepositoryTest extends AbstractRepositoryIT {

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private InstructorProfileRepository profileRepository;

    @Autowired
    private co.edu.unimagdalena.Ims.repositories.CourseRepository courseRepository;

    private Instructor instructor1;
    private Instructor instructor2;

    @BeforeEach
    void setUp() {
        instructor1 = instructorRepository.save(
                Instructor.builder()
                        .email("maria@test.com")
                        .fullName("María González Ruiz")
                        .createdAt(Instant.now().minus(30, ChronoUnit.DAYS))
                        .updatedAt(Instant.now())
                        .build()
        );

        instructor2 = instructorRepository.save(
                Instructor.builder()
                        .email("carlos@test.com")
                        .fullName("Carlos Ramírez Torres")
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build()
        );
    }


    @Test
    @DisplayName("findByEmail() - encuentra instructor por email exacto")
    void findByEmail_emailExistente_retornaInstructor() {

        Optional<Instructor> resultado = instructorRepository.findByEmail("maria@test.com");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getFullName())
                .isEqualTo("María González Ruiz");
    }

    @Test
    @DisplayName("findByEmail() - retorna vacío para email no registrado")
    void findByEmail_emailInexistente_retornaVacio() {

        Optional<Instructor> resultado = instructorRepository.findByEmail("noexiste@test.com");

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("existsByEmail() - true cuando el email ya está en BD")
    void existsByEmail_emailRegistrado_retornaTrue() {

        boolean existe = instructorRepository.existsByEmail("carlos@test.com");

        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("existsByEmail() - false para email no registrado")
    void existsByEmail_emailNuevo_retornaFalse() {

        boolean existe = instructorRepository.existsByEmail("nuevo@test.com");

        assertThat(existe).isFalse();
    }

    @Test
    @DisplayName("findByFullNameContainingIgnoreCase() - busca por fragmento de nombre")
    void findByFullName_nombreParcial_retornaCoincidencias() {

        List<Instructor> resultado = instructorRepository
                .findByFullNameContainingIgnoreCase("gonzález");

        assertThat(resultado)
                .hasSize(1)
                .extracting(Instructor::getEmail)
                .containsExactly("maria@test.com");
    }

    @Test
    @DisplayName("findByCreatedAtBefore() - solo instructores registrados antes de la fecha")
    void findByCreatedAtBefore_retornaSoloLosAntiguos() {
        Instant corte = Instant.now().minus(10, ChronoUnit.DAYS);

        List<Instructor> antiguos = instructorRepository.findByCreatedAtBefore(corte);

        assertThat(antiguos).hasSize(1);
        assertThat(antiguos.get(0).getEmail()).isEqualTo("maria@test.com");
    }


    @Test
    @DisplayName("findInstructorsWithActiveCourses() - solo instructores con al menos un curso activo")
    void findInstructorsWithActiveCourses_retornaSoloConCursosActivos() {
        courseRepository.save(Course.builder()
                .instructor(instructor1).title("Curso Activo")
                .active(true).status("PUBLISHED").createdAt(Instant.now()).build());

        courseRepository.save(Course.builder()
                .instructor(instructor2).title("Curso Inactivo")
                .active(false).status("DRAFT").createdAt(Instant.now()).build());

        List<Instructor> resultado = instructorRepository.findInstructorsWithActiveCourses();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getEmail()).isEqualTo("maria@test.com");
    }

    @Test
    @DisplayName("findInstructorsWithActiveCourses() - retorna vacío si ninguno tiene cursos activos")
    void findInstructorsWithActiveCourses_sinCursosActivos_retornaVacio() {

        List<Instructor> resultado = instructorRepository.findInstructorsWithActiveCourses();

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("findByIdWithProfile() - carga instructor y su perfil juntos (LEFT JOIN FETCH)")
    void findByIdWithProfile_instructorConPerfil_retornaAmbos() {
        profileRepository.save(
                InstructorProfile.builder()
                        .instructor(instructor1)
                        .phone("3001234567")
                        .bio("Experta en desarrollo backend con Spring")
                        .build()
        );

        Optional<Instructor> resultado = instructorRepository
                .findByIdWithProfile(instructor1.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getProfile())
                .isNotNull();
        assertThat(resultado.get().getProfile().getPhone())
                .isEqualTo("3001234567");
        assertThat(resultado.get().getProfile().getBio())
                .isEqualTo("Experta en desarrollo backend con Spring");
    }

    @Test
    @DisplayName("findByIdWithProfile() - LEFT JOIN incluye instructor aunque no tenga perfil")
    void findByIdWithProfile_sinPerfil_retornaInstructorConPerfilNull() {

        Optional<Instructor> resultado = instructorRepository
                .findByIdWithProfile(instructor2.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getProfile()).isNull();
    }

    @Test
    @DisplayName("countCoursesByInstructorId() - cuenta correctamente los cursos por instructor")
    void countCoursesByInstructorId_con3Cursos_retorna3() {
        for (int i = 1; i <= 3; i++) {
            courseRepository.save(Course.builder()
                    .instructor(instructor1)
                    .title("Curso " + i)
                    .active(true).status("PUBLISHED")
                    .createdAt(Instant.now()).build());
        }

        long totalInstructor1 = instructorRepository
                .countCoursesByInstructorId(instructor1.getId());
        long totalInstructor2 = instructorRepository
                .countCoursesByInstructorId(instructor2.getId());

        assertThat(totalInstructor1).isEqualTo(3L);
        assertThat(totalInstructor2).isEqualTo(0L);
    }
}