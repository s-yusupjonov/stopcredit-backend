package uz.agrobank.stopcredit.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.agrobank.stopcredit.dto.AdLookupResponse;
import uz.agrobank.stopcredit.dto.UserCreateRequest;
import uz.agrobank.stopcredit.dto.UserResponse;
import uz.agrobank.stopcredit.dto.UserUpdateRequest;
import uz.agrobank.stopcredit.security.AuthUser;
import uz.agrobank.stopcredit.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@AuthenticationPrincipal AuthUser actor, @Valid @RequestBody UserCreateRequest request) {
        return userService.create(actor, request);
    }

    @GetMapping
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    @GetMapping("/ad-lookup")
    public AdLookupResponse adLookup(@RequestParam String username) {
        return userService.lookupAdAccount(username);
    }

    @PutMapping("/{id}")
    public UserResponse update(@AuthenticationPrincipal AuthUser actor, @PathVariable Long id,
                               @Valid @RequestBody UserUpdateRequest request) {
        return userService.update(actor, id, request);
    }
}
