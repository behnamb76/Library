package ir.bahman.library.controller;

import ir.bahman.library.dto.ChangePasswordRequest;
import ir.bahman.library.dto.ChangeRoleRequest;
import ir.bahman.library.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/activate/{id}")
    public ResponseEntity<Void> activateAccount(@PathVariable Long id) {
        accountService.activateAccount(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/deactivate/{id}")
    public ResponseEntity<Void> deactivateAccount(@PathVariable Long id) {
        accountService.deactivateAccount(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STUDENT','TEACHER','USER')")
    @PutMapping("/change-role")
    public ResponseEntity<Void> changeRole(@Valid @RequestBody ChangeRoleRequest request, Principal principal) {
        accountService.changeRole(principal.getName(), request.getRole());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STUDENT','TEACHER','USER')")
    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request, Principal principal) {
        accountService.changePassword(request, principal.getName());
        return ResponseEntity.ok().build();
    }
}
