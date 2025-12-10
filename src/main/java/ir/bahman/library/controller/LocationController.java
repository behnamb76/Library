package ir.bahman.library.controller;

import ir.bahman.library.dto.LocationDTO;
import ir.bahman.library.mapper.LocationMapper;
import ir.bahman.library.model.Location;
import ir.bahman.library.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/location")
public class LocationController {
    private final LocationService locationService;
    private final LocationMapper locationMapper;

    public LocationController(LocationService locationService, LocationMapper locationMapper) {
        this.locationService = locationService;
        this.locationMapper = locationMapper;
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @PostMapping
    public ResponseEntity<LocationDTO> createLocation(@Valid @RequestBody LocationDTO dto) {
        Location location = locationService.persist(locationMapper.toEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(locationMapper.toDto(location));
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateLocation(@PathVariable Long id, @Valid @RequestBody LocationDTO dto) {
        locationService.update(id, locationMapper.toEntity(dto));
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @GetMapping("/{id}")
    public ResponseEntity<LocationDTO> getLocation(@PathVariable Long id) {
        Location location = locationService.findById(id);
        return ResponseEntity.ok().body(locationMapper.toDto(location));
    }
}
