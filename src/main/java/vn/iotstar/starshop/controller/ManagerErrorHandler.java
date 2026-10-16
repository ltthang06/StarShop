package vn.iotstar.starshop.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(assignableTypes = {ManagerOperationsController.class, ManagerCatalogController.class,
        ManagerShopController.class, CategoryController.class, AdminUserController.class, ManagerReportController.class})
public class ManagerErrorHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView invalid(IllegalArgumentException error, HttpServletRequest request) {
        return page(request.getMethod().equals("GET") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST, error.getMessage());
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ModelAndView invalidFilter(MethodArgumentTypeMismatchException error) {
        return page(HttpStatus.BAD_REQUEST, "Thông số tìm kiếm không hợp lệ. Hãy kiểm tra bộ lọc và thử lại.");
    }
    @ExceptionHandler(ResponseStatusException.class)
    public ModelAndView missing(ResponseStatusException error) {
        var view = new ModelAndView("manager/error");
        view.setStatus(error.getStatusCode());
        view.addObject("message", error.getReason() == null ? "Không thể mở nội dung này." : error.getReason());
        return view;
    }
    private ModelAndView page(HttpStatus status, String message) {
        var view = new ModelAndView("manager/error");
        view.setStatus(status);
        view.addObject("message", message);
        return view;
    }
}

