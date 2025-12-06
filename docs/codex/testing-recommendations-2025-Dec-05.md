# Testing Recommendations – 2025-Dec-05

## Layered Automation Strategy
- Keep unit and slice tests close to the code: `@DataJpaTest` for repositories, `@WebMvcTest` or `GraphQlTester` for controllers/resolvers. Running `mvn clean test` remains the baseline signal that JPA, REST, and GraphQL flows still behave against the H2 profile.
- For broader coverage, add integration tests gated behind `mvn verify` so context-heavy scenarios only run when explicitly requested.

## Python Regression Harness
- Maintain a versioned Python client (e.g., `scripts/regression/runner.py`) that uses `requests` to chain complex REST and GraphQL calls. Parameterize base URLs, tenants, and sample payloads via a `config.yaml` so multiple environments are supported.
- Encode reusable assertions (status, schema snippets) to turn each scenario into a quick smoke suite. Document usage in `docs/testing.md`.

## Workflow After Large Changes
1. Implement or update granular Java tests for the touched modules.
2. Run `mvn clean test` locally; chase any failures immediately.
3. Execute the Python regression harness to cover multi-step or data-heavy flows.
4. When applicable, finish with a manual GraphiQL/REST sanity check for new features.

## Reporting
- Capture successful `mvn clean test` output and Python harness summaries, include them in PR descriptions to show regression evidence.
- Periodically review gaps between the harness scenarios and automated tests; promote stable Python cases into Spring-based tests so CI gains the same coverage.
