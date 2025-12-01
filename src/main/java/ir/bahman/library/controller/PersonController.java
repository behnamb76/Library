package ir.bahman.library.controller;

import ir.bahman.library.dto.PersonDTO;
import ir.bahman.library.dto.RegisterRequest;
import ir.bahman.library.mapper.PersonMapper;
import ir.bahman.library.model.Person;
import ir.bahman.library.service.PersonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
