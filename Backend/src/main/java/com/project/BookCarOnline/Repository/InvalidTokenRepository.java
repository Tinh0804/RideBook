package com.project.BookCarOnline.Repository;

import com.project.BookCarOnline.Entity.InvalidToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidTokenRepository extends JpaRepository<InvalidToken, String> {
}
