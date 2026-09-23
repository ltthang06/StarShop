package vn.iotstar.starshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.dto.CategoryForm;
import vn.iotstar.starshop.entity.Category;
import vn.iotstar.starshop.service.CategoryService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manager/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public String list(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("categories", categoryService.search(keyword));
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        return "manager/categories";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("categoryForm", new CategoryForm());
        model.addAttribute("pageTitle", "Thêm danh mục");
        return "manager/category-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Category category = categoryService.findById(id);
        CategoryForm form = new CategoryForm();
        form.setId(category.getId());
        form.setName(category.getName());
        form.setDescription(category.getDescription());
        form.setImage(category.getImage());
        form.setActive(category.isActive());
        model.addAttribute("categoryForm", form);
        model.addAttribute("pageTitle", "Sửa danh mục");
        return "manager/category-form";
    }

    @PostMapping("/save")
    public String save(@Valid CategoryForm form, BindingResult bindingResult,
            RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", form.getId() == null ? "Thêm danh mục" : "Sửa danh mục");
            return "manager/category-form";
        }
        try {
            Category category = new Category();
            category.setId(form.getId());
            category.setName(form.getName());
            category.setDescription(form.getDescription());
            category.setImage(form.getImage());
            category.setActive(form.isActive());
            categoryService.save(category);
            redirectAttributes.addFlashAttribute("success", "Đã lưu danh mục thành công");
        } catch (IllegalArgumentException ex) {
            model.addAttribute("pageTitle", form.getId() == null ? "Thêm danh mục" : "Sửa danh mục");
            model.addAttribute("error", ex.getMessage());
            return "manager/category-form";
        }
        return "redirect:/manager/categories";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        categoryService.toggleActive(id);
        redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái danh mục");
        return "redirect:/manager/categories";
    }
}
