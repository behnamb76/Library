package ir.bahman.library.service.impl;

import ir.bahman.library.Repository.LocationRepository;
import ir.bahman.library.exception.AlreadyExistsException;
import ir.bahman.library.exception.EntityNotFoundException;
import ir.bahman.library.model.Location;
import ir.bahman.library.service.LocationService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class LocationServiceImpl extends BaseServiceImpl<Location, Long> implements LocationService {
    private final LocationRepository locationRepository;

    public LocationServiceImpl(JpaRepository<Location, Long> repository, LocationRepository locationRepository) {
        super(repository);
        this.locationRepository = locationRepository;
    }

    @Override
    public void prePersist(Location location) {
        if (locationRepository.existsBySectionAndShelfAndRow(location.getSection(), location.getShelf(), location.getRow())) {
            throw new AlreadyExistsException("Location already exists");
        }
    }

    @Override
    public Location update(Long id, Location location) {
        if (locationRepository.existsDuplicateOnUpdate(location.getSection(), location.getShelf(), location.getRow(), id)) {
            throw new AlreadyExistsException("Another location with same position exists");
        }

        Location existing = locationRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Location not found!"));

        existing.setSection(location.getSection());
        existing.setShelf(location.getShelf());
        existing.setRow(location.getRow());

        return locationRepository.save(existing);
    }
}
