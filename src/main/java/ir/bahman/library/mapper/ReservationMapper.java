package ir.bahman.library.mapper;

import ir.bahman.library.Repository.BookRepository;
import ir.bahman.library.Repository.PersonRepository;
import ir.bahman.library.dto.ReservationDTO;
import ir.bahman.library.exception.EntityNotFoundException;
import ir.bahman.library.model.Book;
import ir.bahman.library.model.Person;
import ir.bahman.library.model.Reservation;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class ReservationMapper {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private PersonRepository personRepository;

    public abstract Reservation toEntity(ReservationDTO dto);

    public abstract ReservationDTO toDto(Reservation reservation);

    @AfterMapping
    protected void afterToEntity(ReservationDTO dto, @MappingTarget Reservation reservation) {
        Book book = bookRepository.findById(dto.getBookId())
                .orElseThrow(() -> new EntityNotFoundException("Book not found!"));
        reservation.setBook(book);

        Person member = personRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new EntityNotFoundException("Member not found!"));
        reservation.setMember(member);
    }

    @AfterMapping
    protected void afterToDto(Reservation reservation, @MappingTarget ReservationDTO dto) {
        dto.setBookId(reservation.getBook().getId());
        dto.setMemberId(reservation.getMember().getId());
    }
}
