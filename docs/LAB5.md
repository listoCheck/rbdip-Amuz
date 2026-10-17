# Lab 5: final verification

Validation on 2026-10-17, JDK 21, Docker 29.8.2, PostgreSQL 16:

| Check | Result |
| --- | --- |
| `mvn clean verify` | SUCCESS; 53 tests, no failures/errors/skips |
| Reference tests | All pass; sources unchanged |
| JaCoCo line coverage | 217/231 = 93.94% (minimum 60%) |
| Checkstyle | 0 violations |
| PMD | 12 warnings; combined style count below the limit of 15 |
| PIT | 16/17 mutations killed = 94.12% (minimum 60%) |
| Pricing mutation line coverage | 100%; no uncovered mutations |
| `python scripts/write_score.py` | All eight gates pass; 100/100 |

PIT now has its JUnit 5 engine and runs the pricing characterization suite.
The target class and mutation threshold are unchanged. Added order-validation,
customer-name and review tests verify invalid requests, quantity defaults,
compound names and the author/product purchase boundary. Surefire and JaCoCo
now fail the build on unmet requirements. CI preserves reports and also requires
all score gates before reporting success.

The unmodified scoring script does not recognize the PMD 7 XML namespace and
reports pmd=0 in score.json. The actual PMD XML contains 12 warnings, so the style
gate also passes with the correct count; no scoring thresholds were changed.

Reproduce from the repository root with JDK 21 and Docker running:

```text
mvn clean verify
mvn org.pitest:pitest-maven:mutationCoverage
python scripts/write_score.py
```

Reports: target/surefire-reports, target/site/jacoco/index.html,
target/pit-reports/index.html, target/checkstyle-result.xml, target/pmd.xml.
Generated score.json is ignored by Git; this document records the verified result.
The reference tests and V1 migration match main byte for byte.
