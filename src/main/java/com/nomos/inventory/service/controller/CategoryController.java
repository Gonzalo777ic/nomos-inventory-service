package com.nomos.inventory.service.controller;

import com.nomos.inventory.service.model.Category;
import com.nomos.inventory.service.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/inventory/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    // 1. GET (READ ALL) - Incluye todas las categorías (padres e hijos)
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public ResponseEntity<List<Category>> getAllCategories() {
        // Al usar @JsonIgnoreProperties en el modelo, evitamos bucles de serialización
        // a pesar de la relación recursiva (parent).
        return ResponseEntity.ok(categoryRepository.findAll());
    }

    // 2. GET (READ ONE)
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. POST (CREATE) - Permite crear categorías raíz (parent=null) o subcategorías (parent definido)
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody Category category) {
        // Nota: Spring Boot y JPA manejarán automáticamente la referencia 'parent' si se envía
        // en el cuerpo del JSON con solo el ID de la categoría padre.
        Category savedCategory = categoryRepository.save(category);
        return ResponseEntity.ok(savedCategory);
    }

    // 4. PUT (UPDATE)
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @Valid @RequestBody Category categoryDetails) {
        return categoryRepository.findById(id).map(category -> {

            category.setName(categoryDetails.getName());
            category.setDescription(categoryDetails.getDescription());

            // Actualiza la referencia al padre. Si categoryDetails.getParent() es null, la relación se anula.
            category.setParent(categoryDetails.getParent());

            Category updatedCategory = categoryRepository.save(category);
            return ResponseEntity.ok(updatedCategory);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 5. DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        if (categoryRepository.existsById(id)) {
            // Nota: En un sistema real, deberías implementar lógica para:
            // a) Verificar si hay productos que aún usan esta categoría (Constraint check).
            // b) Reasignar los productos a una categoría diferente (ej. "Sin Categoría").
            // c) Eliminar recursivamente las subcategorías si existen (requiere manejo manual o configuración JPA).

            categoryRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
