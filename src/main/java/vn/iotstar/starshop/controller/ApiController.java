package vn.iotstar.starshop.controller;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import vn.iotstar.starshop.service.*;
@RestController @RequiredArgsConstructor @RequestMapping("/api")
public class ApiController {
    private final JwtTokenService tokens;
    private final ManagerReportService reports;
    public record Login(@NotBlank @Email @Size(max=150) String email,@NotBlank @Size(max=200) String password) {}
    @PostMapping("/auth/token")
    public ResponseEntity<?> token(@Valid @RequestBody Login login) {
        try {return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(tokens.issue(login.email(),login.password()));}
        catch (AuthenticationException ex) {return ResponseEntity.status(401).body(Map.of("error","Email hoặc mật khẩu không hợp lệ"));}
    }
    @GetMapping("/me")
    public Map<String,Object> me(Authentication auth) {
        return Map.of("email",auth.getName(),"authorities",auth.getAuthorities().stream().map(a -> a.getAuthority()).toList());
    }
    @GetMapping("/manager/report")
    public ResponseEntity<?> report(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to) {
        try {return ResponseEntity.ok(reports.report(from,to));}
        catch (IllegalArgumentException ex) {return ResponseEntity.badRequest().body(Map.of("error",ex.getMessage()));}
    }
}
