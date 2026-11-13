package com.valeerp.catalog;

import com.valeerp.inventory.service.model.Category;
import com.valeerp.inventory.service.repository.CategoryRepository;
import com.valeerp.catalog.CategoryDtos.CategoryResponse;
import com.valeerp.catalog.CategoryDtos.CreateCategoryRequest;
import com.valeerp.catalog.CategoryDtos.UpdateCategoryRequest;
import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final Clock clock;

    public CategoryService(CategoryRepository categoryRepository, Clock clock) {
        this.categoryRepository = categoryRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listAll() {
        return categoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listChildren(Long parentId) {
        List<Category> children = parentId == null
                ? categoryRepository.findByParentIdIsNullOrderByNameAsc()
                : categoryRepository.findByParentIdOrderByNameAsc(parentId);
        return children.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse get(Long id) {
        return toResponse(require(id));
    }

    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        if (categoryRepository.existsByName(request.name().trim())) {
            throw new DomainException("Ya existe una categoría con el nombre " + request.name());
        }
        if (request.parentId() != null) {
            require(request.parentId());
        }
        Category category = Category.create(request.name(), request.description(), request.parentId());
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long id, UpdateCategoryRequest request) {
        Category category = require(id);
        if (categoryRepository.existsByNameAndIdNot(request.name().trim(), id)) {
            throw new DomainException("Ya existe otra categoría con el nombre " + request.name());
        }
        if (request.parentId() != null) {
            require(request.parentId());
            assertNoCycle(id, request.parentId());
        }
        category.rename(request.name(), request.description());
        category.moveUnder(request.parentId());
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void retire(Long id) {
        Category category = require(id);
        if (!categoryRepository.findByParentIdOrderByNameAsc(id).isEmpty()) {
            throw new DomainException("Dale de baja primero a las categorías hijas");
        }
        category.retire(Actors.current(), Instant.now(clock));
        categoryRepository.save(category);
    }

    private void assertNoCycle(Long categoryId, Long newParentId) {
        Long cursor = newParentId;
        Set<Long> seen = new HashSet<>();
        List<Long> path = new ArrayList<>();
        while (cursor != null) {
            if (!seen.add(cursor)) {
                throw new DomainException("Jerarquía de categorías cíclica");
            }
            path.add(cursor);
            if (cursor.equals(categoryId)) {
                throw new DomainException("Mover la categoría crearía un ciclo: " + path);
            }
            cursor = categoryRepository.findById(cursor).map(Category::getParentId).orElse(null);
        }
    }

    private Category require(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new DomainException("Categoría no encontrada: " + id));
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getParentId(),
                category.isRoot()
        );
    }
}
