# TCS_INTERNSHIP
The app as of right now:



The Java app is a middle-layer web service: it receives a preprocessed Excel (Big_Out), fetches categorization rules from an API (standing in for “rules from DB”), writes the category back into the Excel, then returns the updated Excel for download.

What the Java code is doing (step-by-step)
Runs a web server and exposes HTTP endpoints on port 8080 (e.g., / and /api/enrich).

On / it serves a tiny HTML page with a file upload form that submits a multipart POST to /api/enrich.
​

On /api/enrich it accepts the uploaded Excel as a MultipartFile, then loads it into memory using Apache POI’s XSSFWorkbook (this is how Java reads .xlsx).

It then makes an HTTP API call to get rules (rules.api.url), parses them into Ruleobjects, and applies them row-by-row to set the Category Name cell based on matching Loan Type (and optionally tenure/amount if you add those rules).

Finally, it writes the modified workbook back to the HTTP response as a downloadable .xlsx file using Content-Disposition: attachment.
​

Tech stack (what you’re using right now)
Java + Spring Boot (Spring MVC) for the web layer and endpoints.

Embedded Tomcat as the servlet container/web server (Spring Boot starts Tomcat automatically when you use spring-boot-starter-web).

Apache POI for reading/writing Excel .xlsx (XSSFWorkbook).

Maven to manage dependencies and run/build the project (mvn spring-boot:run, packaging, etc.).

Are Maven and Tomcat being used?
Yes:

Maven is being used to build and run the app and to pull libraries (Spring Boot, POI, etc.).

Tomcat is being used as the runtime web server/servlet container—but it’s embedded Tomcat inside your Spring Boot app, not a separately installed Tomcat you deploy a WAR to. Your logs showed Tomcat starting on port 8080.

Is there an API call happening to a DB?
Right now, the code is making an HTTP API call to whatever URL is configured in rules.api.url.
​

In your demo it was pointing to a mock endpoint inside the same app (/mock/rules), so it’s not contacting a real database.

In production, rules.api.url would point to a real “Rules service” API, and that servicewould query the database and return rules (your Java app should not connect directly to the DB if the design is “rules via API”).
​

What to change later (when the real DB API is ready)
Replace rules.api.url=http://localhost:8080/mock/rules with your real rules-service URL.

Update the Rule model to match the exact JSON payload the rules API returns (field names and structure).

If you paste your real rules API JSON (even one example response), I’ll tell you exactly what fields to use and adjust the Rule class + parsing accordingly





Build:

cd /workspaces/TCS_INTERNSHIP/bigout-middle-layer

mvn -q clean spring-boot:run




After DB access:

If you get access to the actual database, you don’t “import the database into the app”—you either (A) connect to it directly from your Java app using JDBC, or (B) call an existing Rules API that reads the DB and returns the rules; which one you should do depends on what your organization allows and how they want rules governed.

Option A: Direct DB connection (JDBC)
Your middle-layer app would connect straight to the DB (Oracle/SQL Server/Postgres) with a JDBC driver and a pooled DataSource, then query the rules table(s) on each request or on a cache refresh.

This is simplest technically, but it couples your app to the DB schema and puts DB credentials/network access into your app’s runtime environment (which many orgs restrict).

Option B: Call a Rules API (recommended if available)
Your app makes an HTTP call to a “rules service” endpoint, and that service is the only component that talks to the DB; your app just consumes rules as JSON and applies them to the uploaded Excel.

This tends to be preferred for security and separation of concerns, and it lets rules change without redeploying your middle-layer as long as the API contract stays stable.

What you should do in your scenario
Because you already built the middle layer to fetch rules via rules.api.url, the clean path is: keep the middle layer API-driven, and when “DB access” arrives, use it to build/enable the Rules API (or point to an existing one) rather than embedding DB connectivity into the Excel-enrichment service.

Only switch to direct JDBC if they explicitly tell you “no API, connect directly,” and then you’ll want proper pooling and config management.

Answer these two and I’ll tell you exactly which implementation to proceed with:

Will you be given DB credentials + network access from the Tomcat/Spring runtime, or only an internal API URL?

Is there already a team/service that owns “insurance categorization rules,” or are you expected to create that service?
