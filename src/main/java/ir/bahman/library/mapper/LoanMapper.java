package ir.bahman.library.mapper;

import ir.bahman.library.Repository.BookCopyRepository;
import ir.bahman.library.Repository.PersonRepository;
import ir.bahman.library.dto.LoanResponseDTO;
import ir.bahman.library.dto.ReservationDTO;
import ir.bahman.library.exception.EntityNotFoundException;
import ir.bahman.library.model.BookCopy;
import ir.bahman.library.model.Loan;
import ir.bahman.library.model.Person;
import ir.bahman.library.model.Reservation;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class LoanMapper implements BaseMapper<Loan, LoanResponseDTO> {
    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    public abstract Loan toEntity(LoanResponseDTO dto);

    public abstract LoanResponseDTO toDto(Loan loan);

    @AfterMapping
    protected void afterToEntity(LoanResponseDTO dto, @MappingTarget Loan loan) {
        Person member = personRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new EntityNotFoundException("Member not found!"));
        loan.setMember(member);

        BookCopy bookCopy = bookCopyRepository.findById(dto.getBookCopyId())
                .orElseThrow(() -> new EntityNotFoundException("Copy not found!"));
        loan.setBookCopy(bookCopy);
    }

    @AfterMapping
    protected void afterToDto(Loan loan, @MappingTarget LoanResponseDTO dto) {
        dto.setMemberId(loan.getMember().getId());
        dto.setBookCopyId(loan.getBookCopy().getId());
    }
}
