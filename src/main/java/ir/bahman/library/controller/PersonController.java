package ir.bahman.library.controller;

import ir.bahman.library.dto.AssignRoleRequest;
import ir.bahman.library.dto.PersonDTO;
import ir.bahman.library.dto.RegisterRequest;
import ir.bahman.library.mapper.PersonMapper;
import ir.bahman.library.model.Person;
import ir.bahman.library.model.Role;
import ir.bahman.library.service.PersonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/person")
public class PersonController {
    private final PersonService personService;
    private final PersonMapper personMapper;

    public PersonController(PersonService personService, PersonMapper personMapper) {
        this.personService = personService;
        this.personMapper = personMapper;
    }

    @PostMapping("/member-register")
    public ResponseEntity<PersonDTO> memberRegister(@Valid @RequestBody RegisterRequest request) {
        Person person = personService.register(request);
        personService.assignRoleToPerson("member" , person.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(personMapper.toDto(person));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/librarian-register")
    public ResponseEntity<PersonDTO> librarianRegister(@Valid @RequestBody RegisterRequest request) {
        Person person = personService.register(request);
        personService.assignRoleToPerson("librarian" , person.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(personMapper.toDto(person));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add-role")
    public ResponseEntity<Void> assignRoleToPerson(@Valid @RequestBody AssignRoleRequest request) {
        personService.assignRoleToPerson(request.getRole(), request.getPersonId());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN','MEMBER','USER')")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateProfile(@Valid @RequestBody PersonDTO dto, @PathVariable Long id) {
        personService.update(id, personMapper.toEntity(dto));
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/search/{keyword}")
    public ResponseEntity<List<PersonDTO>> searchPeople(@PathVariable String keyword) {
        List<PersonDTO> people = personService.search(keyword)
                .stream().map(personMapper::toDto).toList();
        return ResponseEntity.ok().body(people);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/person-roles")
    public ResponseEntity<List<String>> getPersonRoles(Principal principal) {
        List<String> roles = personService.getPersonRoles(principal).stream()
                .map(Role::getName)
                .toList();
        return ResponseEntity.status(HttpStatus.OK).body(roles);
    }
}
