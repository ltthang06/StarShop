package vn.iotstar.starshop.service;

import java.util.List;

import vn.iotstar.starshop.entity.Category;

public interface CategoryService {

    List<Category> search(String keyword);

    Category findById(Long id);

    Category save(Category category);

    void toggleActive(Long id);

    long count();
}
