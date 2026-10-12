package vn.iotstar.starshop.controller;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import vn.iotstar.starshop.service.ManagerReportService;
@Controller @RequiredArgsConstructor @RequestMapping("/manager/reports")
public class ManagerReportController {
    private final ManagerReportService service;
    @GetMapping
    public String report(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to, Model model) {
        try {model.addAttribute("report",service.report(from,to));}
        catch (IllegalArgumentException ex) {model.addAttribute("error",ex.getMessage());model.addAttribute("report",service.report(null,null));}
        return "manager/reports";
    }
    @GetMapping(value="/export",produces="text/csv;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> export(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to) {
        try {return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=starshop-report.csv").body(service.csv(service.report(from,to)));}
        catch (IllegalArgumentException ex) {return ResponseEntity.badRequest().body(ex.getMessage());}
    }
}
