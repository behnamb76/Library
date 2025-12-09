package ir.bahman.library.controller;

import ir.bahman.library.dto.PenaltyDTO;
import ir.bahman.library.mapper.PenaltyMapper;
import ir.bahman.library.model.Penalty;
import ir.bahman.library.model.enums.PenaltyReason;
import ir.bahman.library.service.PenaltyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/Penalty")
public class PenaltyController {
    private final PenaltyService penaltyService;
    private final PenaltyMapper penaltyMapper;

    public PenaltyController(PenaltyService penaltyService, PenaltyMapper penaltyMapper) {
        this.penaltyService = penaltyService;
        this.penaltyMapper = penaltyMapper;
    }

    @PostMapping("/{loanId}")
    public ResponseEntity<PenaltyDTO> createPenaltyByLoanIdAndReason(@PathVariable Long loanId, @RequestParam String reason) {
        Penalty penalty = penaltyService.createPenaltyForReason(loanId, PenaltyReason.valueOf(reason));
        return ResponseEntity.status(HttpStatus.CREATED).body(penaltyMapper.toDto(penalty));
    }

    @PostMapping("/create-overdue-penalties")
    public ResponseEntity<Void> CreatePenaltiesForOverdueLoans() {
        penaltyService.autoCreatePenaltiesForOverdueLoans();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/freeze-penalty/{loanId}")
    public ResponseEntity<Void> freezePenaltyForLoan(@PathVariable Long loanId) {
        penaltyService.freezePenaltyForLoan(loanId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePenalty(@PathVariable Long id, @Valid @RequestBody PenaltyDTO dto) {
        penaltyService.update(id, penaltyMapper.toEntity(dto));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PenaltyDTO> getPenalty(@PathVariable Long id) {
        Penalty penalty = penaltyService.findById(id);
        return ResponseEntity.ok().body(penaltyMapper.toDto(penalty));
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<PenaltyDTO>> getPenaltiesByMemberId(@PathVariable Long memberId) {
        List<PenaltyDTO> dtoList = penaltyService.findPenaltyByMemberId(memberId)
                .stream().map(penaltyMapper::toDto).toList();

        return ResponseEntity.ok().body(dtoList);
    }
}
