package co.edu.unimagdalena.lms.repositories;


import co.edu.unimagdalena.lms.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class InstructorProfileRepositoryTest extends AbstractRepositoryIT {

    @Autowired
    private InstructorProfileRepository profileRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    private Instructor instructor;
    private InstructorProfile perfil;

    @BeforeEach
    void setUp() {
        instructor = instructorRepository.save(
                Instructor.builder()
                        .email("lucia@test.com")
                        .fullName("Lucía Vargas Mora")
                        .createdAt(Instant.now())
                        .build()
        );

        perfil = profileRepository.save(
                InstructorProfile.builder()
                        .instructor(instructor)
                        .phone("3109876543")
                        .bio("Ingeniera de software con 10 años de experiencia")
                        .build()
        );
    }

    @Test
    @DisplayName("save() - persiste el perfil con la FK al instructor correcta")
    void save_guardaPerfilConRelacionAlInstructor() {


        assertThat(perfil.getId())
                .isGreaterThan(0);
        assertThat(perfil.getInstructor().getId())
                .isEqualTo(instructor.getId());
    }

    @Test
    @DisplayName("findByInstructorId() - encuentra el perfil usando el ID del instructor")
    void findByInstructorId_instructorConPerfil_retornaPerfil() {

        Optional<InstructorProfile> resultado =
                profileRepository.findByInstructorId(instructor.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getPhone()).isEqualTo("3109876543");
        assertThat(resultado.get().getBio())
                .isEqualTo("Ingeniera de software con 10 años de experiencia");
    }

    @Test
    @DisplayName("findByInstructorId() - retorna vacío si el instructor no tiene perfil")
    void findByInstructorId_instructorSinPerfil_retornaVacio() {
        Instructor sinPerfil = instructorRepository.save(
                Instructor.builder()
                        .email("sinperfil@test.com")
                        .fullName("Sin Perfil")
                        .createdAt(Instant.now())
                        .build()
        );

        Optional<InstructorProfile> resultado =
                profileRepository.findByInstructorId(sinPerfil.getId());

        // Then: no existe fila en instructor_profiles con ese instructor_id
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("existsByInstructorId() - true cuando el instructor ya tiene perfil")
    void existsByInstructorId_conPerfil_retornaTrue() {
        // Given: instructor tiene perfil en @BeforeEach

        // When
        boolean existe = profileRepository.existsByInstructorId(instructor.getId());
        // ↑ Clave para la lógica de negocio: si existe → UPDATE, si no → INSERT.

        // Then
        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("existsByInstructorId() - false cuando el instructor no tiene perfil")
    void existsByInstructorId_sinPerfil_retornaFalse() {
        // Given: instructor nuevo sin perfil
        Instructor nuevo = instructorRepository.save(
                Instructor.builder()
                        .email("nuevo@test.com")
                        .fullName("Instructor Nuevo")
                        .createdAt(Instant.now())
                        .build()
        );

        // When
        boolean existe = profileRepository.existsByInstructorId(nuevo.getId());

        // Then
        assertThat(existe).isFalse();
    }

    @Test
    @DisplayName("findByPhone() - encuentra perfil por número de teléfono exacto")
    void findByPhone_telefonoExistente_retornaPerfil() {
        // Given: perfil con phone "3109876543" en @BeforeEach

        // When
        Optional<InstructorProfile> resultado =
                profileRepository.findByPhone("3109876543");

        // Then: se encuentra el perfil y se puede navegar al instructor
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getInstructor().getEmail())
                .isEqualTo("lucia@test.com");
        // ↑ Verifica que la relación @OneToOne es navegable desde el perfil.
    }

    @Test
    @DisplayName("findByPhone() - retorna vacío para teléfono no registrado")
    void findByPhone_telefonoInexistente_retornaVacio() {
        // Given: "0000000000" no pertenece a ningún perfil

        // When
        Optional<InstructorProfile> resultado =
                profileRepository.findByPhone("0000000000");

        // Then
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("save() con ID existente - actualiza el perfil (UPDATE en lugar de INSERT)")
    void save_conIdExistente_actualizaElPerfil() {
        // Given: perfil existente en @BeforeEach
        InstructorProfile existente = profileRepository
                .findByInstructorId(instructor.getId()).get();

        // When: se modifican campos y se guarda de nuevo
        existente.setBio("Bio actualizada para el test");
        existente.setPhone("3001111111");
        profileRepository.save(existente);
        // ↑ save() con ID ya existente → Hibernate hace UPDATE, no INSERT.

        // Then: los cambios se persistieron correctamente
        InstructorProfile actualizado = profileRepository
                .findByInstructorId(instructor.getId()).get();
        assertThat(actualizado.getBio())
                .isEqualTo("Bio actualizada para el test");
        assertThat(actualizado.getPhone())
                .isEqualTo("3001111111");
    }
}

