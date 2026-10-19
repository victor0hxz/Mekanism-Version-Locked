package mekanism.common.integration.kubejs.mixin;

import java.util.Set;
import mekanism.common.integration.kubejs.ChemicalAliasCompatibility;
import java.util.concurrent.ConcurrentHashMap;
import mekanism.common.Mekanism;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Pseudo
@Mixin(targets = "dev.latvian.mods.kubejs.script.ScriptFile", remap = false)
public abstract class ScriptFileChemicalAliasMixin {
    private static final Set<String> mekanism$warnedScripts = ConcurrentHashMap.newKeySet();

    @ModifyArgs(method = "load(Ldev/latvian/mods/kubejs/script/KubeJSContext;)V", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/script/KubeJSContext;evaluateString(Ldev/latvian/mods/rhino/Scriptable;Ljava/lang/String;Ljava/lang/String;ILjava/lang/Object;)Ljava/lang/Object;"), require = 1)
    private void mekanism$normalizeChemicalAliases(Args args) {
        String source = args.get(1);
        String sourceName = args.get(2);
        String normalized = ChemicalAliasCompatibility.normalize(sourceName, source);
        if (!normalized.equals(source)) {
            args.set(1, normalized);
            if (mekanism$warnedScripts.add(sourceName)) {
                Mekanism.logger.warn("Applied Rhino block-const compatibility to Mekanism Chemical/ChemicalBuilder aliases in {} in memory; original script files are unchanged.", sourceName);
            }
        }
    }
}
