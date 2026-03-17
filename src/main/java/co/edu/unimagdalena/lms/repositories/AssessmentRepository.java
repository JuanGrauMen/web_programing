package co.edu.unimagdalena.lms.repositories;

import co.edu.unimagdalena.lms.entities.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;



@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    List<Assessment> findByStudentId(Long studentId);

    List<Assessment> findByCourseId(Long courseId);

    List<Assessment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    List<Assessment> findByType(String type);

    List<Assessment> findByCourseIdAndType(Long courseId, String type);

    List<Assessment> findByScoreGreaterThanEqual(int minScore);

    List<Assessment> findByTakenAtBetween(Instant from, Instant to);

    @Query("SELECT AVG(a.score) FROM Assessment a WHERE a.student.id = :studentId AND a.course.id = :courseId")
    Double findAvgScoreByStudentAndCourse(@Param("studentId") Long studentId,
                                          @Param("courseId") Long courseId);

    @Query("SELECT MAX(a.score) FROM Assessment a WHERE a.course.id = :courseId")
    Integer findMaxScoreByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT a FROM Assessment a WHERE a.course.id = :courseId ORDER BY a.score DESC")
    List<Assessment> findTopScoresByCourseId(@Param("courseId") Long courseId);
}