package edu.ap.spring_backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BarcodeServiceTest {

    private BarcodeService barcodeService;

    @BeforeEach
    void setUp() {
        barcodeService = new BarcodeService();
    }

    @Test
    void generateBarcodePng_validValue_returnsPngBytes() {
        byte[] result = barcodeService.generateBarcodePng("BC000071");

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void generateBarcodePng_returnsPngSignature() {
        byte[] result = barcodeService.generateBarcodePng("BC000071");

        // PNG files start with the bytes: 137 80 78 71 (= \x89PNG)
        assertEquals((byte) 0x89, result[0]);
        assertEquals((byte) 0x50, result[1]); // P
        assertEquals((byte) 0x4E, result[2]); // N
        assertEquals((byte) 0x47, result[3]); // G
    }

    @Test
    void generateBarcodePng_differentValues_returnsDifferentImages() {
        byte[] result1 = barcodeService.generateBarcodePng("BC000001");
        byte[] result2 = barcodeService.generateBarcodePng("BC000002");

        assertFalse(java.util.Arrays.equals(result1, result2));
    }

    @Test
    void generateBarcodePng_sameValue_returnsSameImage() {
        byte[] result1 = barcodeService.generateBarcodePng("BC000071");
        byte[] result2 = barcodeService.generateBarcodePng("BC000071");

        assertArrayEquals(result1, result2);
    }

    @Test
    void generateBarcodePng_emptyString_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> barcodeService.generateBarcodePng(""));
    }

    @Test
    void generateBarcodePng_longValue_returnsPngBytes() {
        byte[] result = barcodeService.generateBarcodePng("BC123456789");

        assertNotNull(result);
        assertTrue(result.length > 0);
    }
}