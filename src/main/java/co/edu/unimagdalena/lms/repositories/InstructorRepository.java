package co.edu.unimagdalena.lms.repositories;

import co.edu.unimagdalena.lms.entities.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;


@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Long> {

    Optional<Instructor> findByEmail(String email);

    List<Instructor> findByFullNameContainingIgnoreCase(String fullName);

    boolean existsByEmail(String email);

    List<Instructor> findByCreatedAtBefore(Instant date);


    @Query("SELECT DISTINCT i FROM Instructor i JOIN i.courses c WHERE c.active = true")
    List<Instructor> findInstructorsWithActiveCourses();

    @Query("SELECT i FROM Instructor i LEFT JOIN FETCH i.profile WHERE i.id = :id")
    Optional<Instructor> findByIdWithProfile(@Param("id") Long id);

    @Query("SELECT COUNT(c) FROM Course c WHERE c.instructor.id = :instructorId")
    long countCoursesByInstructorId(@Param("instructorId") Long instructorId);
}