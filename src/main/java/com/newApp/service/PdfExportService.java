package com.newApp.service;

import com.newApp.model.Employee;
import com.newApp.model.PayrollEntry;
import com.newApp.model.SalaryComponent;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfExportService {

    public byte[] generatePayrollPdf(Employee employee, PayrollEntry payroll) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        Paragraph title = new Paragraph("Fiche de Paie")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);

        document.add(new Paragraph("Employé: " + employee.getEmployeeName()));
        document.add(new Paragraph("Département: " + employee.getDepartment()));
        document.add(new Paragraph("Mois: " + payroll.getPostingDate().format(DateTimeFormatter.ofPattern("MMMM yyyy"))));

        document.add(new Paragraph("Gains").setFontSize(14).setBold());
        Table earningsTable = new Table(new float[]{2, 1, 1});
        earningsTable.addHeaderCell("Élément");
        earningsTable.addHeaderCell("Montant");
        earningsTable.addHeaderCell("Type");
        for (SalaryComponent component : payroll.getEarnings()) {
            earningsTable.addCell(component.getName());
            earningsTable.addCell(String.format("%.2f", component.getAmount()));
            earningsTable.addCell(component.getType());
        }
        document.add(earningsTable);

        document.add(new Paragraph("Déductions").setFontSize(14).setBold().setMarginTop(10));
        Table deductionsTable = new Table(new float[]{2, 1, 1});
        deductionsTable.addHeaderCell("Élément");
        deductionsTable.addHeaderCell("Montant");
        deductionsTable.addHeaderCell("Type");
        for (SalaryComponent component : payroll.getDeductions()) {
            deductionsTable.addCell(component.getName());
            deductionsTable.addCell(String.format("%.2f", component.getAmount()));
            deductionsTable.addCell(component.getType());
        }
        document.add(deductionsTable);

        document.add(new Paragraph("Salaire brut: " + String.format("%.2f", payroll.getGrossPay())).setMarginTop(10));
        document.add(new Paragraph("Salaire net: " + String.format("%.2f", payroll.getNetPay())).setBold());

        document.close();
        return baos.toByteArray();
    }
}