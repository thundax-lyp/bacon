package com.github.thundax.bacon.upms.application.query;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.upms.application.assembler.DepartmentAssembler;
import com.github.thundax.bacon.upms.application.codec.DepartmentIdCodec;
import com.github.thundax.bacon.upms.application.dto.DepartmentDTO;
import com.github.thundax.bacon.upms.application.dto.DepartmentTreeDTO;
import com.github.thundax.bacon.upms.domain.model.entity.Department;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.repository.DepartmentRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DepartmentQueryApplicationService {

    private final DepartmentRepository departmentRepository;

    public DepartmentQueryApplicationService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public DepartmentDTO getById(DepartmentId departmentId) {
        return DepartmentAssembler.toDto(departmentRepository
                .findById(departmentId)
                .orElseThrow(() -> new NotFoundException("Department not found: " + departmentId)));
    }

    public DepartmentDTO getByCode(DepartmentCode departmentCode) {
        return DepartmentAssembler.toDto(departmentRepository
                .findByCode(departmentCode)
                .orElseThrow(() -> new NotFoundException("Department not found: " + departmentCode.value())));
    }

    public List<DepartmentDTO> listByCodes(Set<DepartmentCode> codes) {
        if (codes == null || codes.isEmpty()) {
            return List.of();
        }
        return codes.stream()
                .map(departmentRepository::findByCode)
                .flatMap(Optional::stream)
                .map(DepartmentAssembler::toDto)
                .toList();
    }

    public List<DepartmentDTO> listByIds(Set<DepartmentId> departmentIds) {
        return departmentRepository.listByIds(departmentIds).stream()
                .map(DepartmentAssembler::toDto)
                .toList();
    }

    public List<DepartmentTreeDTO> tree() {
        List<Department> departments = departmentRepository.listTree();
        Map<Long, DepartmentTreeDTO> treeNodeMap = departments.stream()
                .map(DepartmentAssembler::toTreeDto)
                .collect(Collectors.toMap(DepartmentTreeDTO::getId, Function.identity()));

        departments.forEach(department -> {
            if (department.getParentId() != null) {
                DepartmentTreeDTO parent = treeNodeMap.get(DepartmentIdCodec.toValue(department.getParentId()));
                if (parent != null) {
                    parent.getChildren().add(treeNodeMap.get(DepartmentIdCodec.toValue(department.getId())));
                }
            }
        });

        return departments.stream()
                .filter(department -> department.getParentId() == null)
                .map(department -> treeNodeMap.get(DepartmentIdCodec.toValue(department.getId())))
                .sorted(treeComparator())
                .toList();
    }

    private Comparator<DepartmentTreeDTO> treeComparator() {
        return Comparator.comparing(DepartmentTreeDTO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(DepartmentTreeDTO::getId);
    }
}
