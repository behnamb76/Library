package ir.bahman.library.mapper;

import ir.bahman.library.dto.PersonDTO;
import ir.bahman.library.model.Person;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PersonMapper extends BaseMapper<Person, PersonDTO> {
}
