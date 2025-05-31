package com.newApp.util;

import com.newApp.model.Employee;
import com.opencsv.CSVReader;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class CsvParser {

    public List<Employee> parseEmployees(MultipartFile file) throws Exception {
        List<Employee> employees = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] line;
            reader.readNext(); // Ignorer l'en-tête
            int lineNumber = 1;
            while ((line = reader.readNext()) != null) {
                lineNumber++;
                if (line.length < 5) {
                    throw new IllegalArgumentException("Ligne " + lineNumber + " : format CSV invalide, 5 colonnes attendues");
                }
                Employee employee = new Employee();
                employee.setName(line[0].trim());
                employee.setFirstName(line[1].trim());
                employee.setLastName(line[2].trim());
                try {
                    LocalDate date = LocalDate.parse(line[3].trim(), formatter);
                    employee.setEmployeeName(employee.getFirstName() + " " + employee.getLastName());
                    employee.setStatus("Active");
                    employee.setDepartment(line[4].trim());
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Ligne " + lineNumber + " : format de date invalide pour " + line[0] + ", attendu yyyy-MM-dd");
                }
                employees.add(employee);
            }
        }
        return employees;
    }
}