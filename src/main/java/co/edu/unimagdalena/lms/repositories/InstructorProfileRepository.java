package co.edu.unimagdalena.lms.repositories;

import co.edu.unimagdalena.lms.entities.InstructorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface InstructorProfileRepository extends JpaRepository<InstructorProfile, Long> {

    Optional<InstructorProfile> findByInstructorId(Long instructorId);

    boolean existsByInstructorId(Long instructorId);

    Optional<InstructorProfile> findByPhone(String phone);
}