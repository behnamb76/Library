package ir.bahman.library.mapper;

import ir.bahman.library.dto.PenaltyDTO;
import ir.bahman.library.model.Penalty;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public abstract class PenaltyMapper implements BaseMapper<Penalty, PenaltyDTO> {
    public abstract Penalty toEntity(PenaltyDTO dto);

    public abstract PenaltyDTO toDto(Penalty penalty);

    @AfterMapping
    protected void afterToDto(Penalty penalty, @MappingTarget PenaltyDTO dto) {
        dto.setReason(penalty.getReason().toString());
        dto.setStatus(penalty.getStatus().toString());
    }
}
