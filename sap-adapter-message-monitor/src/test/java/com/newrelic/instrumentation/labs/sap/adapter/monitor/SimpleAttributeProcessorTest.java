package com.newrelic.instrumentation.labs.sap.adapter.monitor;

import java.util.*;

/**
 * Simple standalone test for AttributeProcessor (no JUnit/Mockito needed)
 * Run with: java SimpleAttributeProcessorTest
 */
public class SimpleAttributeProcessorTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("AttributeProcessor Standalone Tests");
        System.out.println("==============================================\n");

        // Clear cache before all tests
        AttributeProcessor.clearCacheAndBuffer();

        // Run all tests
        testExtractCompleteBusinessFields();
        testExtractCompleteBusinessFields_SkipNotReported();
        testEnrichWithCachedFields();
        testEnrichWithCachedFields_DontOverwrite();
        testFindCompleteValue_WithPrefixes();
        testFindCompleteValue_SkipNotReported();
        testCachedBusinessFields_Expiration();
        testBufferedMessageData_Timeout();
        testScenario_CacheAndEnrich();

        // Print summary
        System.out.println("\n==============================================");
        System.out.println("TEST SUMMARY");
        System.out.println("==============================================");
        System.out.println("Tests run: " + testsRun);
        System.out.println("Tests passed: " + testsPassed + " ✅");
        System.out.println("Tests failed: " + testsFailed + " ❌");
        System.out.println("==============================================");

        if (testsFailed == 0) {
            System.out.println("\n🎉 ALL TESTS PASSED! 🎉");
            System.exit(0);
        } else {
            System.out.println("\n❌ SOME TESTS FAILED ❌");
            System.exit(1);
        }
    }

    // Test 1: Extract complete business fields
    private static void testExtractCompleteBusinessFields() {
        startTest("Extract complete business fields");

        Map<String, String> attributes = new HashMap<>();
        attributes.put("opco", "GLO");
        attributes.put("process", "Commerce");
        attributes.put("subprocess", "CreditManagement");
        attributes.put("countryCodeISO", "US");

        Map<String, String> completeFields = AttributeProcessor.extractCompleteBusinessFields(attributes);

        assertNotNull(completeFields, "Should return a map");
        assertFalse(completeFields.containsValue("Not_Reported"), "Should not contain Not_Reported");

        endTest();
    }

    // Test 2: Extract complete fields - skip "Not_Reported"
    private static void testExtractCompleteBusinessFields_SkipNotReported() {
        startTest("Extract complete fields - skip Not_Reported");

        Map<String, String> attributes = new HashMap<>();
        attributes.put("opco", "Not_Reported");
        attributes.put("process", "Commerce");
        attributes.put("subprocess", "");

        Map<String, String> completeFields = AttributeProcessor.extractCompleteBusinessFields(attributes);

        assertNotNull(completeFields, "Should return a map");
        assertFalse(completeFields.containsValue("Not_Reported"), "Should not include Not_Reported");
        assertFalse(completeFields.containsValue(""), "Should not include empty values");

        endTest();
    }

    // Test 3: Enrich attributes with cached fields
    private static void testEnrichWithCachedFields() {
        startTest("Enrich attributes with cached fields");

        Map<String, String> attributes = new HashMap<>();
        attributes.put("opco", "Not_Reported");
        attributes.put("process", "Not_Reported");
        attributes.put("existingField", "KeepThis");

        Map<String, String> cachedFields = new HashMap<>();
        cachedFields.put("opco", "GLO");
        cachedFields.put("process", "Commerce");

        AttributeProcessor.enrichWithCachedFields(attributes, cachedFields);

        assertEquals("GLO", attributes.get("opco"), "Should enrich opco");
        assertEquals("Commerce", attributes.get("process"), "Should enrich process");
        assertEquals("KeepThis", attributes.get("existingField"), "Should keep existing field");

        endTest();
    }

    // Test 4: Enrich - don't overwrite existing complete values
    private static void testEnrichWithCachedFields_DontOverwrite() {
        startTest("Enrich - don't overwrite existing complete values");

        Map<String, String> attributes = new HashMap<>();
        attributes.put("opco", "EXISTING_VALUE");
        attributes.put("process", "Not_Reported");

        Map<String, String> cachedFields = new HashMap<>();
        cachedFields.put("opco", "CACHED_VALUE");
        cachedFields.put("process", "Commerce");

        AttributeProcessor.enrichWithCachedFields(attributes, cachedFields);

        assertEquals("EXISTING_VALUE", attributes.get("opco"), "Should NOT overwrite existing");
        assertEquals("Commerce", attributes.get("process"), "Should enrich Not_Reported");

        endTest();
    }

    // Test 5: Find complete value with prefixes
    private static void testFindCompleteValue_WithPrefixes() {
        startTest("Find complete value with prefixes");

        Map<String, String> attributeMapping = new HashMap<>();
        attributeMapping.put("modulecontext-opco", "GLO");
        attributeMapping.put("supplementaldata-process", "Commerce");
        attributeMapping.put("SupplementalData-subprocess", "CreditManagement");
        attributeMapping.put("countryCodeISO", "US");

        String opcoValue = AttributeProcessor.findCompleteValue("opco", attributeMapping);
        assertEquals("GLO", opcoValue, "Should find opco with modulecontext- prefix");

        String processValue = AttributeProcessor.findCompleteValue("process", attributeMapping);
        assertEquals("Commerce", processValue, "Should find process with supplementaldata- prefix");

        String subprocessValue = AttributeProcessor.findCompleteValue("subprocess", attributeMapping);
        assertEquals("CreditManagement", subprocessValue, "Should find subprocess with SupplementalData- prefix");

        String countryValue = AttributeProcessor.findCompleteValue("countryCodeISO", attributeMapping);
        assertEquals("US", countryValue, "Should find countryCodeISO with direct key");

        endTest();
    }

    // Test 6: Skip Not_Reported during search
    private static void testFindCompleteValue_SkipNotReported() {
        startTest("Skip Not_Reported during prefix search");

        Map<String, String> attributeMapping = new HashMap<>();
        attributeMapping.put("opco", "Not_Reported");
        attributeMapping.put("modulecontext-opco", "GLO");

        String value = AttributeProcessor.findCompleteValue("opco", attributeMapping);
        assertEquals("GLO", value, "Should skip Not_Reported and find GLO");

        endTest();
    }

    // Test 7: Cache expiration
    private static void testCachedBusinessFields_Expiration() {
        startTest("Cache expiration");

        try {
            Map<String, String> businessFields = new HashMap<>();
            businessFields.put("opco", "GLO");

            // Create cache entry with 1 second expiration
            AttributeProcessor.CachedBusinessFields cached =
                new AttributeProcessor.CachedBusinessFields(businessFields, 1000L);

            assertFalse(cached.isExpired(), "Should not be expired immediately");

            Thread.sleep(1100); // Wait 1.1 seconds

            assertTrue(cached.isExpired(), "Should be expired after 1 second");

            endTest();
        } catch (InterruptedException e) {
            fail("Thread interrupted");
        }
    }

    // Test 8: Buffered entry timeout
    private static void testBufferedMessageData_Timeout() {
        startTest("Buffered entry timeout");

        try {
            Map<String, String> attributes = new HashMap<>();
            attributes.put("opco", "Not_Reported");

            // Create buffered entry with 1 second timeout
            AttributeProcessor.BufferedMessageData buffered =
                new AttributeProcessor.BufferedMessageData(null, attributes, 1000L);

            assertFalse(buffered.isTimedOut(), "Should not be timed out immediately");

            Thread.sleep(1100); // Wait 1.1 seconds

            assertTrue(buffered.isTimedOut(), "Should be timed out after 1 second");

            endTest();
        } catch (InterruptedException e) {
            fail("Thread interrupted");
        }
    }

    // Test 9: Complete scenario - cache and enrich
    private static void testScenario_CacheAndEnrich() {
        startTest("Complete scenario - cache and enrich");

        // Clear cache first
        AttributeProcessor.clearCacheAndBuffer();

        String messageId = "TEST_MSG_12345";

        // Step 1: Create complete business fields (simulating Entry 2)
        Map<String, String> completeFields = new HashMap<>();
        completeFields.put("opco", "GLO");
        completeFields.put("process", "Commerce");
        completeFields.put("subprocess", "CreditManagement");

        // Cache them directly (bypassing extract since AttributeConfig not available in tests)
        AttributeProcessor.cacheBusinessFields(messageId, completeFields);

        // Verify cache populated
        AttributeProcessor.CachedBusinessFields cached =
            AttributeProcessor.getCachedBusinessFields(messageId);
        assertNotNull(cached, "Cache should be populated");
        assertFalse(cached.fields.isEmpty(), "Cache should have fields");

        // Step 2: Create incomplete attributes (like Entry 1 or 3)
        Map<String, String> incompleteAttributes = new HashMap<>();
        incompleteAttributes.put("opco", "Not_Reported");
        incompleteAttributes.put("process", "Not_Reported");
        incompleteAttributes.put("subprocess", "Not_Reported");

        // Enrich with cached fields
        AttributeProcessor.enrichWithCachedFields(incompleteAttributes, cached.fields);

        // Verify enrichment
        assertNotEquals("Not_Reported", incompleteAttributes.get("opco"), "opco should be enriched");
        assertNotEquals("Not_Reported", incompleteAttributes.get("process"), "process should be enriched");
        assertNotEquals("Not_Reported", incompleteAttributes.get("subprocess"), "subprocess should be enriched");

        // Cleanup
        AttributeProcessor.removeCachedBusinessFields(messageId);
        assertNull(AttributeProcessor.getCachedBusinessFields(messageId), "Cache should be removed");

        endTest();
    }

    // ==============================================
    // ASSERTION HELPERS
    // ==============================================

    private static void startTest(String testName) {
        testsRun++;
        System.out.println("\nTest " + testsRun + ": " + testName);
    }

    private static void endTest() {
        testsPassed++;
        System.out.println("  ✅ PASSED");
    }

    private static void fail(String message) {
        testsFailed++;
        System.out.println("  ❌ FAILED: " + message);
        throw new AssertionError(message);
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            fail(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            fail(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected == null || !expected.equals(actual)) {
            fail(message + " (expected: " + expected + ", actual: " + actual + ")");
        }
    }

    private static void assertNotEquals(Object unexpected, Object actual, String message) {
        if (unexpected != null && unexpected.equals(actual)) {
            fail(message + " (should not be: " + unexpected + ")");
        }
    }

    private static void assertNotNull(Object object, String message) {
        if (object == null) {
            fail(message);
        }
    }

    private static void assertNull(Object object, String message) {
        if (object != null) {
            fail(message + " (was: " + object + ")");
        }
    }
}
