package ir.bahman.library.util;

import ir.bahman.library.Repository.AccountRepository;
import ir.bahman.library.Repository.PersonRepository;
import ir.bahman.library.Repository.RoleRepository;
import ir.bahman.library.model.Account;
import ir.bahman.library.model.Person;
import ir.bahman.library.model.Role;
import ir.bahman.library.model.enums.AccountStatus;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;

    public DataInitializer(RoleRepository roleRepository, PersonRepository personRepository, PasswordEncoder passwordEncoder, AccountRepository accountRepository) {
        this.roleRepository = roleRepository;
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
    }


    @Override
    public void run(String... args) {
        createRoles();
        createAdmin();
    }

    public void createRoles() {
        Optional<Role> user = roleRepository.findByName("USER");
        Optional<Role> admin = roleRepository.findByName("ADMIN");
        Optional<Role> librarian = roleRepository.findByName("LIBRARIAN");
        Optional<Role> member = roleRepository.findByName("MEMBER");
        if (member.isEmpty() && librarian.isEmpty() && admin.isEmpty() && user.isEmpty()) {
            roleRepository.save(Role.builder().name("USER").build());
            roleRepository.save(Role.builder().name("ADMIN").build());
            roleRepository.save(Role.builder().name("LIBRARIAN").build());
            roleRepository.save(Role.builder().name("MEMBER").build());
        }
    }

    public void createAdmin() {
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new EntityNotFoundException("Role not found!"));
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new EntityNotFoundException("Role not found!"));


        if (!personRepository.existsByRolesContains(adminRole)) {

            Person admin = Person.builder()
                    .firstName("Admin")
                    .lastName("Admin")
                    .nationalCode("123456789")
                    .phoneNumber("09123324213")
                    .roles(List.of(adminRole, userRole))
                    .build();

            personRepository.save(admin);


            Account account = Account.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .status(AccountStatus.ACTIVE)
                    .person(admin)
                    .activeRole(adminRole)
                    .build();

            admin.setAccount(account);
            accountRepository.save(account);
        }
    }
}
