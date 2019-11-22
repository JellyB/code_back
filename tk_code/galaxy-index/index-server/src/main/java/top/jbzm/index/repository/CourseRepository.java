package top.jbzm.index.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import top.jbzm.index.entity.videoEntity.Course;

/**
 * @author: wangjian
 * @create: 2018-04-03 10:56
 **/
public interface CourseRepository extends JpaRepository<Course, String> {

    Page<Course> findAllByCateId(int cateId, Pageable page);
}
