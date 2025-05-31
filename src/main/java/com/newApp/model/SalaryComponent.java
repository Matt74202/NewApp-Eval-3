package com.newApp.model;

public class SalaryComponent {
    private String salary_component;
    private double amount;
    private String type; // Earning or Deduction

    public String getName() { return salary_component; }
    public void setName(String salary_component) { this.salary_component = salary_component; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}