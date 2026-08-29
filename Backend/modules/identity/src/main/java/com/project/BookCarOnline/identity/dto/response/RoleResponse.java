package com.project.BookCarOnline.identity.dto.response;

import com.project.BookCarOnline.identity.entity.enums.PredefinedRole;

public record RoleResponse(PredefinedRole roleName, String description) {
}
