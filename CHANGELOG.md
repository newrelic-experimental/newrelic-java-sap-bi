## 1.8.1 - 2026-03-09
- **Bug Fix:** Resolved a critical performance regression causing agent shutdowns and high CPU usage.
  - Replaced 6-minute sleep in `AttributeChecker` with efficient 1-second polling.
  - Fixed memory leak caused by excessive String allocation in `AdapterMonitorLogger` (circuit breaker trip fix).
  - Validated against official New Relic Java Agent architecture.

## Installation

To install:

1. Download the latest release jar files.
2. In the New Relic Java directory (the one containing newrelic.jar), create a directory named extensions if it does not already exist.
3. Copy the downloaded jars into the extensions directory.
4. Restart the application.   

