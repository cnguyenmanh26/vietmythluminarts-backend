package com.social.back_java.repository;

import com.social.back_java.model.Story;
import com.social.back_java.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {
    Optional<Story> findBySlug(String slug);
    List<Story> findByStatus(String status);
    List<Story> findByAuthor(User author);
}
