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

    @Test
    void containsProfanity_blocklistedWord_returnsTrue() {
        ProfanityFilterService service = new ProfanityFilterService("", "badword,foo");
        assertTrue(service.containsProfanity("This contains badword"));
    }

    @Test
    void containsProfanity_shortInput_returnsFalse() {
        ProfanityFilterService service = new ProfanityFilterService("", "badword");
        assertFalse(service.containsProfanity("hi"));
    }

    @Test
    void containsProfanity_null_returnsFalse() {
        ProfanityFilterService service = new ProfanityFilterService("", "badword");
        assertFalse(service.containsProfanity(null));
    }
}