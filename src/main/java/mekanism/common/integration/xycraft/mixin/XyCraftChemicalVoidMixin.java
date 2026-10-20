package mekanism.common.integration.xycraft.mixin;

import mekanism.api.chemical.ChemicalResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Bridge XyCraft's placeholder empty chemical to the actual shared chemical resource. */
@Pseudo
@Mixin(targets="tv.soaryn.xycraft.machines.compat.mekanism.ChemicalCapabilityRegistry",remap=false)
public abstract class XyCraftChemicalVoidMixin {
    @ModifyArg(method="<clinit>",at=@At(value="INVOKE",target="Lnet/neoforged/neoforge/transfer/VoidingResourceHandler;<init>(Lnet/neoforged/neoforge/transfer/resource/Resource;)V"),index=0,require=0)
    private static Resource mekanism$validEmptyChemical(Resource original) {
        if (!original.isEmpty() && original.getClass().getName().equals("tv.soaryn.xycraft.core.compat.mekanism.ChemicalResource")) {
            return ChemicalResource.EMPTY;
        }
        return original;
    }
}
