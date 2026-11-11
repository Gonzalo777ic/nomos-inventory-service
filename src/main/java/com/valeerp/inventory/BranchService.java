package com.valeerp.inventory;

import com.nomos.inventory.service.model.Branch;
import com.nomos.inventory.service.repository.BranchRepository;
import com.valeerp.inventory.BranchDtos.BranchResponse;
import com.valeerp.inventory.BranchDtos.CreateBranchRequest;
import com.valeerp.inventory.BranchDtos.UpdateBranchRequest;
import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class BranchService {

    private final BranchRepository branchRepository;
    private final Clock clock;

    public BranchService(BranchRepository branchRepository, Clock clock) {
        this.branchRepository = branchRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<BranchResponse> listByCompany(Long companyId) {
        requireCompanyId(companyId);
        return branchRepository.findByCompanyIdOrderByCodeAsc(companyId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BranchResponse get(Long id) {
        return toResponse(requireBranch(id));
    }

    @Transactional
    public BranchResponse create(CreateBranchRequest request) {
        requireCompanyId(request.companyId());
        String code = request.code().trim().toUpperCase();
        if (branchRepository.existsByCompanyIdAndCode(request.companyId(), code)) {
            throw new DomainException("Ya existe una sucursal con código " + code + " en la compañía");
        }
        Branch branch = Branch.open(request.companyId(), code, request.name(), request.address());
        return toResponse(branchRepository.save(branch));
    }

    @Transactional
    public BranchResponse update(Long id, UpdateBranchRequest request) {
        Branch branch = requireBranch(id);
        String code = request.code().trim().toUpperCase();
        if (branchRepository.existsByCompanyIdAndCodeAndIdNot(branch.getCompanyId(), code, id)) {
            throw new DomainException("Ya existe otra sucursal con código " + code + " en la compañía");
        }
        branch.rename(code, request.name(), request.address());
        return toResponse(branchRepository.save(branch));
    }

    @Transactional
    public void retire(Long id) {
        Branch branch = requireBranch(id);
        branch.retire(Actors.current(), Instant.now(clock));
        branchRepository.save(branch);
    }

    private Branch requireBranch(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new DomainException("La sucursal no existe"));
    }

    private void requireCompanyId(Long companyId) {
        if (companyId == null) {
            throw new DomainException("companyId es obligatorio");
        }
    }

    private BranchResponse toResponse(Branch branch) {
        return new BranchResponse(
                branch.getId(),
                branch.getCompanyId(),
                branch.getCode(),
                branch.getName(),
                branch.getAddress()
        );
    }
}
