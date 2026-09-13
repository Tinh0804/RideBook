package com.project.BookCarOnline.identity.service;

import com.project.BookCarOnline.identity.dto.request.FavoritePlaceRequest;
import com.project.BookCarOnline.identity.entity.FavoritePlace;
import com.project.BookCarOnline.identity.repository.FavoritePlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoritePlaceService {
    private final FavoritePlaceRepository favoritePlaceRepository;

    public List<FavoritePlace> getMyFavoritePlaces(String customerId) {
        return favoritePlaceRepository.findByCustomerId(customerId);
    }

    public FavoritePlace addFavoritePlace(String customerId, FavoritePlaceRequest request) {
        FavoritePlace place = FavoritePlace.builder()
                .customerId(customerId)
                .label(request.getLabel())
                .address(request.getAddress())
                .lat(request.getLat())
                .lng(request.getLng())
                .icon(request.getIcon())
                .build();
        return favoritePlaceRepository.save(place);
    }

    public FavoritePlace updateFavoritePlace(String customerId, String placeId, FavoritePlaceRequest request) {
        FavoritePlace place = favoritePlaceRepository.findByFavoritePlaceIdAndCustomerId(placeId, customerId)
                .orElseThrow(() -> new RuntimeException("Favorite place not found"));
        
        place.setLabel(request.getLabel());
        place.setAddress(request.getAddress());
        place.setLat(request.getLat());
        place.setLng(request.getLng());
        place.setIcon(request.getIcon());
        
        return favoritePlaceRepository.save(place);
    }

    public void deleteFavoritePlace(String customerId, String placeId) {
        FavoritePlace place = favoritePlaceRepository.findByFavoritePlaceIdAndCustomerId(placeId, customerId)
                .orElseThrow(() -> new RuntimeException("Favorite place not found"));
        favoritePlaceRepository.delete(place);
    }
}
