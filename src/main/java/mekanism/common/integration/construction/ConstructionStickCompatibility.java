package mekanism.common.integration.construction;

import java.util.Set;
import mekanism.api.MekanismAPI;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.TriState;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Let Construction Sticks use their own item action instead of opening our block menus. */
@EventBusSubscriber(modid = MekanismAPI.MEKANISM_MODID)
public final class ConstructionStickCompatibility {
    private static final TagKey<Item> STICKS = TagKey.create(Registries.ITEM,
          Identifier.fromNamespaceAndPath("constructionstick", "construction_sticks"));
    private static final Set<String> PORT_NAMESPACES = Set.of("mekanism", "mekanismgenerators", "mekanism_extras",
          "mekmm", "meklm", "mekaf", "genextras", "mekanismadditions");

    private ConstructionStickCompatibility() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void rightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.isCanceled() || !event.getItemStack().is(STICKS)) {
            return;
        }
        var block = event.getLevel().getBlockState(event.getPos()).getBlock();
        if (PORT_NAMESPACES.contains(BuiltInRegistries.BLOCK.getKey(block).getNamespace())) {
            // Do not force item permission: claims and the stick's own restrictions remain authoritative.
            event.setUseBlock(TriState.FALSE);
        }
    }
}
