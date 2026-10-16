package vn.iotstar.starshop.config;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
@ControllerAdvice @RequiredArgsConstructor
public class ViewModelAdvice {
    private final ViewFormat format;
    @ModelAttribute("viewFormat")
    public ViewFormat format() { return format; }
}

