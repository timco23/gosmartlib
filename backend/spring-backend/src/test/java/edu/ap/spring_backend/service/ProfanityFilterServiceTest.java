package edu.ap.spring_backend.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProfanityFilterServiceTest {

    @Test
    void containsProfanity_withBlocklist_detectsDirectMatch() {
        ProfanityFilterService service = new ProfanityFilterService("", "badword");
        assertTrue(service.containsProfanity("badword"));
    }

    @Test
    void containsProfanity_normalizesSpacingAndSymbols() {
        ProfanityFilterService service = new ProfanityFilterService("", "badword");
        assertTrue(service.containsProfanity("b a d w o r d"));
        assertTrue(service.containsProfanity("b@a@d@@word"));
        assertTrue(service.containsProfanity("baaadwooord"));
    }

    @Test
    void containsProfanity_blankOrShortInput_returnsFalse() {
        ProfanityFilterService service = new ProfanityFilterService("", "");
        assertFalse(service.containsProfanity(null));
        assertFalse(service.containsProfanity("   "));
        assertFalse(service.containsProfanity("ok"));
    }
}