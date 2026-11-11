package com.example.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SkillTest {

    @Test
    void gettersAndSetters() {
        Skill skill = new Skill();
        skill.setName("Crossover");
        skill.setDescription("Ball handling move");
        skill.setVideoFileName("crossover.mp4");

        assertEquals("Crossover", skill.getName());
        assertEquals("Ball handling move", skill.getDescription());
        assertEquals("crossover.mp4", skill.getVideoFileName());
    }

    @Test
    void newSkillHasNullFields() {
        Skill skill = new Skill();

        assertNull(skill.getName());
        assertNull(skill.getDescription());
        assertNull(skill.getVideoFileName());
        assertNull(skill.getId());
        assertEquals(0, skill.getVersion());
    }

    // ---- AbstractEntity (id, hashCode, equals) ----

    @Test
    void idGetterAndSetter() {
        Skill skill = new Skill();
        skill.setId(5L);

        assertEquals(5L, skill.getId());
    }

    @Test
    void hashCode_usesIdWhenSet() {
        Skill skill = new Skill();
        skill.setId(5L);

        assertEquals(Long.valueOf(5L).hashCode(), skill.hashCode());
    }

    @Test
    void hashCode_withoutIdIsIdentityBased() {
        Skill skill = new Skill();

        assertEquals(System.identityHashCode(skill), skill.hashCode());
    }

    @Test
    void equals_sameIdIsEqual() {
        Skill a = new Skill();
        Skill b = new Skill();
        a.setId(1L);
        b.setId(1L);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_differentIdIsNotEqual() {
        Skill a = new Skill();
        Skill b = new Skill();
        a.setId(1L);
        b.setId(2L);

        assertNotEquals(a, b);
    }

    @Test
    void equals_nullAndOtherTypesAreNotEqual() {
        Skill skill = new Skill();
        skill.setId(1L);

        assertFalse(skill.equals(null));
        assertFalse(skill.equals("not an entity"));
    }

    @Test
    void equals_withoutIdOnlyEqualToItself() {
        Skill a = new Skill();
        Skill b = new Skill();

        assertEquals(a, a);
        assertNotEquals(a, b);
    }
}
