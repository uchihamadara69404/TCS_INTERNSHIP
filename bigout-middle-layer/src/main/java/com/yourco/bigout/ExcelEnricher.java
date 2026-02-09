package com.yourco.bigout;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.Comparator;
import java.util.List;

public class ExcelEnricher {

  public void applyCategoryRules(XSSFWorkbook wb, List<Rule> rules) {
    if (wb.getNumberOfSheets() == 0) return;
    Sheet sheet = wb.getSheetAt(0);
    if (sheet.getLastRowNum() < 1) return;

    Row header = sheet.getRow(0);
    if (header == null) throw new IllegalArgumentException("Missing header row");

    int loanTypeIdx = findCol(header, "Loan Type");
    int tenureIdx   = findCol(header, "Loan Tenure");
    int amountIdx   = findCol(header, "Original Loan Amount");
    int categoryIdx = findCol(header, "Category Name");

    // Highest priority first
    List<Rule> sorted = rules.stream()
        .sorted(Comparator.comparing((Rule r) -> r.priority() == null ? 0 : r.priority()).reversed())
        .toList();

    DataFormatter fmt = new DataFormatter();

    for (int r = 1; r <= sheet.getLastRowNum(); r++) {
      Row row = sheet.getRow(r);
      if (row == null) continue;

      String loanType = fmt.formatCellValue(row.getCell(loanTypeIdx)).trim().toUpperCase();
      Integer tenure  = parseInt(fmt.formatCellValue(row.getCell(tenureIdx)));
      Double amount   = parseDouble(fmt.formatCellValue(row.getCell(amountIdx)));

      String chosen = chooseCategory(sorted, loanType, tenure, amount);

      Cell catCell = row.getCell(categoryIdx);
      if (catCell == null) catCell = row.createCell(categoryIdx, CellType.STRING);
      catCell.setCellValue(chosen == null ? "" : chosen);
    }
  }

  private String chooseCategory(List<Rule> rules, String loanType, Integer tenure, Double amount) {
    String txt = (loanType == null ? "" : loanType);

    for (Rule rule : rules) {
      String pat = (rule.loanTypePattern() == null ? "" : rule.loanTypePattern().trim().toUpperCase());
      if (!pat.isEmpty() && !txt.contains(pat)) continue;

      if (rule.minTenure() != null && (tenure == null || tenure < rule.minTenure())) continue;
      if (rule.maxTenure() != null && (tenure == null || tenure > rule.maxTenure())) continue;

      if (rule.minAmount() != null && (amount == null || amount < rule.minAmount())) continue;
      if (rule.maxAmount() != null && (amount == null || amount > rule.maxAmount())) continue;

      return rule.categoryName();
    }
    return "";
  }

  private int findCol(Row header, String name) {
    DataFormatter fmt = new DataFormatter();
    for (int c = 0; c < header.getLastCellNum(); c++) {
      String v = fmt.formatCellValue(header.getCell(c)).trim();
      if (name.equalsIgnoreCase(v)) return c;
    }
    throw new IllegalArgumentException("Missing required column: " + name);
  }

  private Integer parseInt(String s) {
    try {
      if (s == null) return null;
      s = s.replace(",", "").trim();
      if (s.isEmpty()) return null;
      return (int) Double.parseDouble(s);
    } catch (Exception e) {
      return null;
    }
  }

  private Double parseDouble(String s) {
    try {
      if (s == null) return null;
      s = s.replace(",", "").trim();
      if (s.isEmpty()) return null;
      return Double.parseDouble(s);
    } catch (Exception e) {
      return null;
    }
  }
}
