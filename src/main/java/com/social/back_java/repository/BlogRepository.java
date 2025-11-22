package com.social.back_java.repository;

import com.social.back_java.model.Blog;
import com.social.back_java.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {
    Optional<Blog> findBySlug(String slug);
    List<Blog> findByAuthor(User author);
    List<Blog> findByStatus(String status);
    List<Blog> findByCategory(String category);
}
