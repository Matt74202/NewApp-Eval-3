package com.newApp.model;

import java.time.LocalDate;
import java.util.List;

public class PayrollEntry {
    private String employee;
    private String employee_name;
    private LocalDate posting_date;
    private List<SalaryComponent> earnings;
    private List<SalaryComponent> deductions;
    private double gross_pay;
    private double net_pay;

    public String getEmployee() { return employee; }
    public void setEmployee(String employee) { this.employee = employee; }
    public String getEmployeeName() { return employee_name; }
    public void setEmployeeName(String employee_name) { this.employee_name = employee_name; }
    public LocalDate getPostingDate() { return posting_date; }
    public void setPostingDate(LocalDate posting_date) { this.posting_date = posting_date; }
    public List<SalaryComponent> getEarnings() { return earnings; }
    public void setEarnings(List<SalaryComponent> earnings) { this.earnings = earnings; }
    public List<SalaryComponent> getDeductions() { return deductions; }
    public void setDeductions(List<SalaryComponent> deductions) { this.deductions = deductions; }
    public double getGrossPay() { return gross_pay; }
    public void setGrossPay(double gross_pay) { this.gross_pay = gross_pay; }
    public double getNetPay() { return net_pay; }
    public void setNetPay(double net_pay) { this.net_pay = net_pay; }
}