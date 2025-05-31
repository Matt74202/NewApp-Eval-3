package com.newApp.controller;

import com.newApp.model.Employee;
import com.newApp.model.PayrollEntry;
import com.newApp.service.EmployeeService;
import com.newApp.service.PdfExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PdfExportService pdfExportService;

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model) {
        String result = employeeService.authenticate(username, password);
        if (result == null) {
            return "redirect:/employees";
        } else {
            model.addAttribute("errorMessage", result);
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("frappeSid");
        return "redirect:/login";
    }

    @GetMapping("/employees")
    public String listEmployees(@RequestParam(required = false) String search, Model model) {
        try {
            List<Employee> employees = employeeService.getEmployees(search);
            model.addAttribute("employees", employees);
            model.addAttribute("search", search);
            return "employee_list";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erreur lors du chargement des employés : " + e.getMessage());
            return "employee_list";
        }
    }

    @GetMapping("/employee/{id}")
    public String employeeDetails(@PathVariable String id, Model model) {
        try {
            Employee employee = employeeService.getEmployeeById(id);
            List<PayrollEntry> salaries = employeeService.getEmployeeSalaries(id);
            model.addAttribute("employee", employee);
            model.addAttribute("salaries", salaries);
            return "employee_details";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erreur lors du chargement des détails : " + e.getMessage());
            return "employee_list";
        }
    }

    @PostMapping("/employees/import")
    public String importCsv(@RequestParam("file") MultipartFile file, Model model) {
        try {
            employeeService.importEmployeesFromCsv(file);
            model.addAttribute("message", "Importation réussie !");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erreur lors de l'importation : " + e.getMessage());
        }
        return "employee_list";
    }

    @GetMapping("/payroll/pdf/{id}/{month}")
    public ResponseEntity<byte[]> generatePayrollPdf(@PathVariable String id, @PathVariable String month) {
        try {
            Employee employee = employeeService.getEmployeeById(id);
            PayrollEntry payroll = employeeService.getEmployeeSalaries(id).stream()
                    .filter(p -> p.getPostingDate().toString().startsWith(month))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Aucune paie pour ce mois"));
            byte[] pdf = pdfExportService.generatePayrollPdf(employee, payroll);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=fiche_paie_" + id + "_" + month + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/payroll")
    public String payrollTable(@RequestParam(required = false) String month, Model model) {
        try {
            List<PayrollEntry> payrolls = employeeService.getPayrollsByMonth(month);
            model.addAttribute("payrolls", payrolls);
            model.addAttribute("month", month);
            return "payroll_table";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erreur lors du chargement des salaires : " + e.getMessage());
            return "payroll_table";
        }
    }
}