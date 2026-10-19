package mekanism.common.integration.kubejs.mixin;

import dev.latvian.mods.kubejs.recipe.viewer.RecipeViewerEntryType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Retired optional ExtraDisks entries may still be hidden by older pack scripts. */
@Pseudo
@Mixin(targets="dev.latvian.mods.kubejs.integration.jei.JEIRemoveEntriesKubeEvent", remap=false)
public abstract class LegacyChemicalViewerMixin {
    @Shadow @Final private RecipeViewerEntryType type;

    @Inject(method="remove(Ldev/latvian/mods/rhino/Context;Ljava/lang/Object;)V", at=@At("HEAD"), cancellable=true, require=1)
    private void mekanism$skipRetiredChemicalEntry(@Coerce Object context, Object entry, CallbackInfo callback) {
        if (type == RecipeViewerEntryType.ITEM && entry instanceof CharSequence literal) {
            String id = literal.toString();
            if ((id.equals("extradisks:infinite_chemical_storage_block") ||
                  id.equals("extradisks:infinite_chemical_storage_disk") ||
                  id.equals("extradisks:infinite_chemical_storage_part")) &&
                  !BuiltInRegistries.ITEM.containsKey(Identifier.parse(id))) {
                callback.cancel();
            }
        }
    }
}
