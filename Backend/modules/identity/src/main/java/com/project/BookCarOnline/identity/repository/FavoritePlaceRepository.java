package com.project.BookCarOnline.identity.repository;

import com.project.BookCarOnline.identity.entity.FavoritePlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoritePlaceRepository extends JpaRepository<FavoritePlace, String> {
    List<FavoritePlace> findByCustomerId(String customerId);
    Optional<FavoritePlace> findByFavoritePlaceIdAndCustomerId(String favoritePlaceId, String customerId);
}
