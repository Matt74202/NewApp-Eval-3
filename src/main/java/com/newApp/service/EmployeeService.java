package com.newApp.service;

import com.newApp.model.Employee;
import com.newApp.model.PayrollEntry;
import com.newApp.util.CsvParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public class EmployeeService {

    @Autowired
    private FrappeApiService frappeApiService;

    @Autowired
    private CsvParser csvParser;

    @Autowired
    private ObjectMapper objectMapper;

    public String authenticate(String username, String password) {
        return frappeApiService.validateUserCredentials(username, password);
    }

    public boolean isSessionValid() {
        return frappeApiService.isSessionValid();
    }

    public List<Employee> getEmployees(String search) {
        if (!isSessionValid()) {
            throw new RuntimeException("Session non valide, veuillez vous reconnecter");
        }
        String filters = search != null ? "[['employee_name','like','%" + search + "%']]" : null;
        Map<String, Object> response = frappeApiService.getResource("Employee", filters, Map.class);
        System.out.println("API response for getEmployees: " + response.get("data")); // Debug log
        return objectMapper.convertValue(response.get("data"), new TypeReference<List<Employee>>() {});
    }

    public Employee getEmployeeById(String id) {
        if (!isSessionValid()) {
            throw new RuntimeException("Session non valide, veuillez vous reconnecter");
        }
        Map<String, Object> response = frappeApiService.getResource("Employee/" + id, null, Map.class);
        System.out.println("API response for getEmployeeById: " + response.get("data")); // Debug log
        return objectMapper.convertValue(response.get("data"), Employee.class);
    }

    public List<PayrollEntry> getEmployeeSalaries(String employeeId) {
        if (!isSessionValid()) {
            throw new RuntimeException("Session non valide, veuillez vous reconnecter");
        }
        String filters = "[['employee','=','" + employeeId + "']]";
        Map<String, Object> response = frappeApiService.getResource("Salary Slip", filters, Map.class);
        System.out.println("API response for getEmployeeSalaries: " + response.get("data")); // Debug log
        return objectMapper.convertValue(response.get("data"), new TypeReference<List<PayrollEntry>>() {});
    }

    public List<PayrollEntry> getPayrollsByMonth(String month) {
        if (!isSessionValid()) {
            throw new RuntimeException("Session non valide, veuillez vous reconnecter");
        }
        String filters = month != null ? "[['posting_date','like','" + month + "%']]" : null;
        Map<String, Object> response = frappeApiService.getResource("Salary Slip", filters, Map.class);
        System.out.println("API response for getPayrollsByMonth: " + response.get("data")); // Debug log
        return objectMapper.convertValue(response.get("data"), new TypeReference<List<PayrollEntry>>() {});
    }

    public void importEmployeesFromCsv(MultipartFile file) throws Exception {
        if (!isSessionValid()) {
            throw new RuntimeException("Session non valide, veuillez vous reconnecter");
        }
        List<Employee> employees = csvParser.parseEmployees(file);
        for (Employee employee : employees) {
            frappeApiService.postResource("Employee", employee, Map.class);
        }
    }
}