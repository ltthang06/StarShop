package vn.iotstar.starshop.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.entity.Category;
import vn.iotstar.starshop.repository.CategoryRepository;
import vn.iotstar.starshop.service.CategoryService;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Category> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return categoryRepository.findAll(org.springframework.data.domain.Sort.by("name").ascending());
        }
        return categoryRepository.findByNameContainingIgnoreCaseOrderByNameAsc(keyword.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục: " + id));
    }

    @Override
    @Transactional
    public Category save(Category category) {
        String normalizedName = category.getName().trim();
        boolean duplicated = category.getId() == null
                ? categoryRepository.existsByNameIgnoreCase(normalizedName)
                : categoryRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, category.getId());
        if (duplicated) {
            throw new IllegalArgumentException("Tên danh mục đã tồn tại");
        }
        category.setName(normalizedName);
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void toggleActive(Long id) {
        Category category = findById(id);
        category.setActive(!category.isActive());
        categoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return categoryRepository.count();
    }
}
