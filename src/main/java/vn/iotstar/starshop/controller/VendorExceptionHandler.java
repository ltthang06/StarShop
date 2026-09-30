package vn.iotstar.starshop.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice(assignableTypes = {
        VendorController.class,
        VendorProductController.class,
        VendorOrderController.class,
        VendorPromotionController.class,
        VendorStatisticController.class
})
public class VendorExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(
            IllegalArgumentException exception,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute(
                "errorMessage",
                exception.getMessage() != null
                        && !exception.getMessage().isBlank()
                        ? exception.getMessage()
                        : "Không tìm thấy dữ liệu hoặc bạn không có quyền truy cập."
        );

        return "redirect:/vendor/dashboard";
    }
}