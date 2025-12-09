package ir.bahman.library.mapper;

import ir.bahman.library.dto.LocationDTO;
import ir.bahman.library.model.Location;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocationMapper extends BaseMapper<Location, LocationDTO> {
}
