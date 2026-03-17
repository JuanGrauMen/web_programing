package co.edu.unimagdalena.lms.repositories;

import co.edu.unimagdalena.lms.entities.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;


@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {


    List<Enrollment> findByStudentId(Long studentId);

    List<Enrollment> findByCourseId(Long courseId);

    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    List<Enrollment> findByStatus(String status);

    List<Enrollment> findByStudentIdAndStatus(Long studentId, String status);

    List<Enrollment> findByEnrolledAtBetween(Instant from, Instant to);

    long countByCourseId(Long courseId);

    @Query("SELECT e FROM Enrollment e WHERE e.course.id = :courseId ORDER BY e.enrolledAt DESC")
    List<Enrollment> findRecentEnrollmentsByCourseId(@Param("courseId") Long courseId);
}