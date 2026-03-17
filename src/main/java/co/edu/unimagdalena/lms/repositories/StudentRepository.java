package co.edu.unimagdalena.lms.repositories;

import co.edu.unimagdalena.lms.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository

public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findByFullNameContainingIgnoreCase(String fullName);

    Optional<Student> findByEmail(String email);

    List<Student> findByCreatedAtAfter(Instant date);

    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT s FROM Student s JOIN s.enrollments e WHERE e.course.id = :courseId")
    List<Student> findStudentsByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT s FROM Student s JOIN s.assessments a WHERE a.course.id = :courseId " +
            "GROUP BY s HAVING AVG(a.score) >= :minScore")
    List<Student> findStudentsByAvgScoreInCourse(@Param("courseId") Long courseId,
                                                 @Param("minScore") double minScore);
}