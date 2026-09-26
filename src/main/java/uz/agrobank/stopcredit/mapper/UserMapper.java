package uz.agrobank.stopcredit.mapper;

import org.springframework.stereotype.Component;
import uz.agrobank.stopcredit.domain.User;
import uz.agrobank.stopcredit.dto.UserResponse;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getFullName(),
                user.getRole(), user.isActive(), user.getAuthSource(), user.getCreatedAt());
    }
}