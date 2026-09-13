package com.project.BookCarOnline.identity.controller;

import com.project.BookCarOnline.identity.dto.request.FavoritePlaceRequest;
import com.project.BookCarOnline.identity.entity.FavoritePlace;
import com.project.BookCarOnline.identity.service.FavoritePlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorite-places")
@RequiredArgsConstructor
@Tag(name = "Favorite Places API", description = "Customer favorite places management")
public class FavoritePlaceController {

    private final FavoritePlaceService favoritePlaceService;

    @Operation(summary = "Get my favorite places")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @GetMapping
    public ResponseEntity<List<FavoritePlace>> getMyFavoritePlaces(
            @RequestHeader(value = "X-Account-Id", required = false) String accountIdHeader,
            @RequestAttribute(value = "accountId", required = false) String accountIdAttr) {
        
        String accountId = accountIdHeader != null ? accountIdHeader : 
                           (accountIdAttr != null ? accountIdAttr : 
                           com.project.BookCarOnline.shared.security.SecurityUtils.getCurrentAccountId().orElse(null));
        if (accountId == null) return ResponseEntity.badRequest().build();
        
        return ResponseEntity.ok(favoritePlaceService.getMyFavoritePlaces(accountId));
    }

    @Operation(summary = "Add a new favorite place")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @PostMapping
    public ResponseEntity<FavoritePlace> addFavoritePlace(
            @RequestHeader(value = "X-Account-Id", required = false) String accountIdHeader,
            @RequestAttribute(value = "accountId", required = false) String accountIdAttr,
            @RequestBody FavoritePlaceRequest request) {
        
        String accountId = accountIdHeader != null ? accountIdHeader : 
                           (accountIdAttr != null ? accountIdAttr : 
                           com.project.BookCarOnline.shared.security.SecurityUtils.getCurrentAccountId().orElse(null));
        if (accountId == null) return ResponseEntity.badRequest().build();
        
        return ResponseEntity.ok(favoritePlaceService.addFavoritePlace(accountId, request));
    }

    @Operation(summary = "Update a favorite place")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @PutMapping("/{id}")
    public ResponseEntity<FavoritePlace> updateFavoritePlace(
            @PathVariable("id") String placeId,
            @RequestHeader(value = "X-Account-Id", required = false) String accountIdHeader,
            @RequestAttribute(value = "accountId", required = false) String accountIdAttr,
            @RequestBody FavoritePlaceRequest request) {
        
        String accountId = accountIdHeader != null ? accountIdHeader : 
                           (accountIdAttr != null ? accountIdAttr : 
                           com.project.BookCarOnline.shared.security.SecurityUtils.getCurrentAccountId().orElse(null));
        if (accountId == null) return ResponseEntity.badRequest().build();
        
        return ResponseEntity.ok(favoritePlaceService.updateFavoritePlace(accountId, placeId, request));
    }

    @Operation(summary = "Delete a favorite place")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFavoritePlace(
            @PathVariable("id") String placeId,
            @RequestHeader(value = "X-Account-Id", required = false) String accountIdHeader,
            @RequestAttribute(value = "accountId", required = false) String accountIdAttr) {
        
        String accountId = accountIdHeader != null ? accountIdHeader : 
                           (accountIdAttr != null ? accountIdAttr : 
                           com.project.BookCarOnline.shared.security.SecurityUtils.getCurrentAccountId().orElse(null));
        if (accountId == null) return ResponseEntity.badRequest().build();
        
        favoritePlaceService.deleteFavoritePlace(accountId, placeId);
        return ResponseEntity.noContent().build();
    }
}
