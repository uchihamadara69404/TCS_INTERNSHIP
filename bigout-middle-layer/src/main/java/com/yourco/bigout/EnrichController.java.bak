package com.yourco.bigout;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
public class EnrichController {

  @Value("${rules.api.url}")
  private String rulesApiUrl;

  private final RestClient http = RestClient.create();
  private final ExcelEnricher enricher = new ExcelEnricher();

  @GetMapping("/")
  public String home() {
    return """
      <html><body>
      <h3>Big_Out Middle Layer</h3>
      <form action="/api/enrich" method="post" enctype="multipart/form-data">
        <input type="file" name="file" accept=".xlsx" />
        <button type="submit">Upload & Enrich</button>
      </form>
      </body></html>
    """;
  }

  @PostMapping(value="/api/enrich", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<byte[]> enrich(@RequestParam("file") MultipartFile file) throws IOException {

    if (file == null || file.isEmpty()) {
      return ResponseEntity.badRequest().body("Missing file".getBytes(StandardCharsets.UTF_8));
    }

    // 1) Read workbook
    XSSFWorkbook wb;
    try (InputStream in = file.getInputStream()) {
      wb = new XSSFWorkbook(in);
    }

    // 2) Fetch rules (JSON) from API
    Rule[] rules = http.get()
        .uri(rulesApiUrl)
        .retrieve()
        .body(Rule[].class);

    List<Rule> ruleList = (rules == null) ? List.of() : Arrays.asList(rules);

    // 3) Enrich
    enricher.applyCategoryRules(wb, ruleList);

    // 4) Return enriched XLSX
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      wb.write(bos);
    } finally {
      wb.close();
    }

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Big_Out_Enriched.xlsx\"")
        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .body(bos.toByteArray());
  }

  // Temporary mock rules endpoint so you can test end-to-end right now
  @GetMapping("/mock/rules")
  public List<Rule> mockRules() {
    return List.of(
      new Rule("CREDIT CARD", null, null, null, null, "Credit Card Loan", 600),
      new Rule("MORTGAGE", null, null, null, null, "Mortgage Loan", 500),
      new Rule("HOUSING",  null, null, null, null, "Mortgage Loan", 495),
      new Rule("AUTO",     null, null, null, null, "Auto Loan", 450),
      new Rule("BUSINESS", null, null, null, null, "Business Loan", 420),
      new Rule("PERSONAL", null, null, null, null, "Personal Loan", 400),
      new Rule("",         null, null, null, null, "Uncategorized Loan", 1)
    );
  }
}
