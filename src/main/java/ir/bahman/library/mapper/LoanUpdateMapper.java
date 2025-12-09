package ir.bahman.library.mapper;

import ir.bahman.library.dto.LoanUpdateRequest;
import ir.bahman.library.model.Loan;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoanUpdateMapper extends BaseMapper<Loan, LoanUpdateRequest> {
}
