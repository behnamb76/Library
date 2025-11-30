package ir.bahman.library.service.impl;

import ir.bahman.library.Repository.AccountRepository;
import ir.bahman.library.Repository.PersonRepository;
import ir.bahman.library.Repository.RoleRepository;
import ir.bahman.library.dto.RegisterRequest;
import ir.bahman.library.exception.AlreadyExistsException;
import ir.bahman.library.exception.EntityNotFoundException;
import ir.bahman.library.model.Account;
import ir.bahman.library.model.Person;
import ir.bahman.library.model.Role;
import ir.bahman.library.model.enums.AccountStatus;
import ir.bahman.library.service.PersonService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PersonServiceImpl extends BaseServiceImpl<Person, Long> implements PersonService {

    private final PersonRepository personRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;

    public PersonServiceImpl(JpaRepository<Person, Long> repository, PersonRepository personRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, AccountRepository accountRepository) {
        super(repository);
        this.personRepository = personRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
    }

    @Override
    protected void prePersist(Person person) {
        Role role = roleRepository.findByName("USER")
                .orElseThrow(() -> new EntityNotFoundException("Role not found!"));
        if (personRepository.existsByNationalCodeAndPhoneNumber(person.getNationalCode() , person.getPhoneNumber())) {
            throw new AlreadyExistsException("This person already exists!");
        }

        List<Role> roles = new ArrayList<>();
        roles.add(role);
        person.setRoles(roles);
    }

    @Override
    protected void postPersist(Person person) {
        Role role = roleRepository.findByName("USER")
                .orElseThrow(() -> new EntityNotFoundException("Role not found!"));

        Account account = person.getAccount();

        if (account != null) {
            String rawPassword = account.getPassword();
            account.setPassword(passwordEncoder.encode(rawPassword));
            account.setStatus(AccountStatus.PENDING);
            account.setPerson(person);
            account.setActiveRole(role);
        }

        Person savedPerson = personRepository.save(person);

        if (account != null && account.getId() == null) {
            accountRepository.save(account);
        }
    }

    @Override
    public Person register(RegisterRequest request) {
        Account account = Account.builder()
                .username(request.getUsername())
                .password(request.getPassword()).build();

        Person person = Person.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .nationalCode(request.getNationalCode())
                .phoneNumber(request.getPhoneNumber())
                .birthday(request.getBirthday())
                .account(account).build();

        return persist(person);
    }

    @Override
    public Person update(Long id, Person person) {
        Person foundedPerson = personRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Person not found!"));

        foundedPerson.setFirstName(person.getFirstName());
        foundedPerson.setLastName(person.getLastName());
        foundedPerson.setNationalCode(person.getNationalCode());
        foundedPerson.setPhoneNumber(person.getPhoneNumber());
        foundedPerson.setBirthday(person.getBirthday());

        return personRepository.save(foundedPerson);
    }

    @Override
    public void assignRoleToPerson(String role, Long personId) {
        Role founded = roleRepository.findByName(role.toUpperCase())
                .orElseThrow(() -> new EntityNotFoundException("Role not found!"));

        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new EntityNotFoundException("Person not found!"));
        person.getRoles().add(founded);
        personRepository.save(person);
    }

    @Override
    public List<Person> search(String keyword) {
        return personRepository.searchByKeyword(keyword);
    }

    @Override
    public List<Role> getPersonRoles(Principal principal) {
        String username = principal.getName();
        Person person= personRepository.findByAccountUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Person not found"));
        return person.getRoles();
    }
}
