package mekanism.api.chemical;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChemicalBuilderLegacyOreTest {

    @Test
    void acceptsLegacyOreTagsAndPreservesFluentBuilder() {
        ChemicalBuilder builder = ChemicalBuilder.cleanSlurry();
        assertNull(builder.getLegacyOreTag());
        assertSame(builder, builder.ore("c:ores/crimson_iron"));
        assertEquals(Identifier.parse("c:ores/crimson_iron"),
              builder.getLegacyOreTag().solidRepresentation().location());
        builder.ore("#c:ores/azure_silver");
        assertEquals(Identifier.parse("c:ores/azure_silver"),
              builder.getLegacyOreTag().solidRepresentation().location());
    }

    @Test
    void rejectsInvalidLegacyOreTags() {
        assertThrows(NullPointerException.class, () -> ChemicalBuilder.dirtySlurry().ore(null));
        assertThrows(IllegalArgumentException.class, () -> ChemicalBuilder.dirtySlurry().ore(" "));
    }
}
