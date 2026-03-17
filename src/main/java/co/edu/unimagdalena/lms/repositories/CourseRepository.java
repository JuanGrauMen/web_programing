package co.edu.unimagdalena.Ims.repositories;

import co.edu.unimagdalena.lms.entities.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;


@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByTitleContainingIgnoreCase(String title);

    List<Course> findByActiveTrue();

    List<Course> findByStatus(String status);

    List<Course> findByInstructorId(Long instructorId);

    List<Course> findByInstructorIdAndActiveTrue(Long instructorId);

    List<Course> findByCreatedAtBetween(Instant from, Instant to);

    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.lessons WHERE c.id = :id")
    Optional<Course> findByIdWithLessons(@Param("id") Long id);

    @Query("SELECT c FROM Course c JOIN c.enrollments e WHERE e.student.id = :studentId")
    List<Course> findCoursesByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT c FROM Course c WHERE SIZE(c.enrollments) > :minEnrollments")
    List<Course> findCoursesWithMoreThanNEnrollments(@Param("minEnrollments") int minEnrollments);
}