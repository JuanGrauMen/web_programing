package co.edu.unimagdalena.lms.repositories;

import co.edu.unimagdalena.lms.entities.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {


    List<Lesson> findByCourseIdOrderByOrderIndexAsc(Long courseId);

    Optional<Lesson> findByCourseIdAndTitle(Long courseId, String title);

    List<Lesson> findByTitleContainingIgnoreCase(String title);

    long countByCourseId(Long courseId);

    List<Lesson> findByCourseIdAndOrderIndexGreaterThan(Long courseId, int orderIndex);


    @Query("SELECT l FROM Lesson l WHERE l.course.id = :courseId ORDER BY l.orderIndex ASC LIMIT 1")
    Optional<Lesson> findFirstLessonByCourseId(@Param("courseId") Long courseId);
}