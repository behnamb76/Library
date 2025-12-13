package ir.bahman.library.service;

import ir.bahman.library.dto.RegisterRequest;
import ir.bahman.library.model.Person;
import ir.bahman.library.model.Role;

import java.security.Principal;
import java.util.List;

public interface PersonService extends BaseService<Person, Long> {
    Person register(RegisterRequest request);

    void assignRoleToPerson(String role, Long personId);

    List<Person> search(String keyword);

    List<Role> getPersonRoles(Principal principal);

    Person findByUsername(String username);
}
