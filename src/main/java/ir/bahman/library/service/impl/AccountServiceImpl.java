package ir.bahman.library.service.impl;

import ir.bahman.library.Repository.AccountRepository;
import ir.bahman.library.Repository.RoleRepository;
import ir.bahman.library.dto.ChangePasswordRequest;
import ir.bahman.library.exception.AccessDeniedException;
import ir.bahman.library.model.Account;
import ir.bahman.library.model.Role;
import ir.bahman.library.model.enums.AccountStatus;
import ir.bahman.library.service.AccountService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl extends BaseServiceImpl<Account, Long> implements AccountService {
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountServiceImpl(JpaRepository<Account, Long> repository, AccountRepository accountRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        super(repository);
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void changeRole(String username, String roleName) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Account not found!"));

        Role role = roleRepository.findByName(roleName.toUpperCase())
                .orElseThrow(() -> new EntityNotFoundException("Role not found!"));

        if (account.getPerson().getRoles().contains(role)) {
            account.setActiveRole(role);
            accountRepository.save(account);
        } else
            throw new AccessDeniedException("Role is not assigned to this person!");
    }

    @Override
    public void activateAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Account not found!"));
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);
    }

    @Override
    public void deactivateAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Account not found!"));
        account.setStatus(AccountStatus.INACTIVE);
        accountRepository.save(account);
    }

    @Override
    public void changePassword(ChangePasswordRequest dto, String username) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Account not found!"));

        if (!passwordEncoder.matches(dto.getOldPassword(), account.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect!");
        }

        account.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        accountRepository.save(account);
    }

    @Override
    public Account update(Long id, Account account) {
        Account foundedAccount = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Account not found!"));

        foundedAccount.setUsername(account.getUsername());
        foundedAccount.setPassword(account.getPassword());
        foundedAccount.setStatus(account.getStatus());
        foundedAccount.setActiveRole(account.getActiveRole());
        return accountRepository.save(foundedAccount);
    }
}
