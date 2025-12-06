package ir.bahman.library.service;

import ir.bahman.library.dto.ChangePasswordRequest;
import ir.bahman.library.model.Account;

public interface AccountService extends BaseService<Account, Long> {
    void changeRole(String username, String roleName);

    void activateAccount(Long id);

    void deactivateAccount(Long id);

    void changePassword(ChangePasswordRequest dto, String username);
}
