package mekanism.common.integration.kubejs;

import java.util.List;
import org.slf4j.LoggerFactory;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

/** Enable optional adapters only when their precise runtime API is available. */
public final class ScriptCompatibilityMixinPlugin implements IMixinConfigPlugin {
    private static final String EVALUATE = "(Ldev/latvian/mods/rhino/Scriptable;Ljava/lang/String;Ljava/lang/String;ILjava/lang/Object;)Ljava/lang/Object;";
    public void onLoad(String mixinPackage) { }
    public String getRefMapperConfig() { return null; }
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        try {
            ClassNode target = MixinService.getService().getBytecodeProvider().getClassNode(targetClassName);
            if (mixinClassName.endsWith("ScriptFileChemicalAliasMixin")) {
                boolean supported = target.methods.stream().filter(method -> method.name.equals("load") && method.desc.equals("(Ldev/latvian/mods/kubejs/script/KubeJSContext;)V")).anyMatch(method -> {
                    for (var instruction : method.instructions) {
                        if (instruction instanceof MethodInsnNode call && call.owner.equals("dev/latvian/mods/kubejs/script/KubeJSContext") && call.name.equals("evaluateString") && call.desc.equals(EVALUATE)) return true;
                    }
                    return false;
                });
                return report(supported, targetClassName);
            }
            if (mixinClassName.endsWith("LegacyChemicalViewerMixin")) {
                ClassNode entryType = MixinService.getService().getBytecodeProvider().getClassNode("dev.latvian.mods.kubejs.recipe.viewer.RecipeViewerEntryType");
                if (!entryType.fields.stream().anyMatch(field -> field.name.equals("ITEM") && field.desc.equals("Ldev/latvian/mods/kubejs/recipe/viewer/RecipeViewerEntryType;") && (field.access & org.objectweb.asm.Opcodes.ACC_STATIC) != 0)) {
                    return report(false, entryType.name);
                }
                return report(target.methods.stream().anyMatch(method -> method.name.equals("remove") && method.desc.equals("(Ldev/latvian/mods/rhino/Context;Ljava/lang/Object;)V")) &&
                      target.fields.stream().anyMatch(field -> field.name.equals("type") && field.desc.equals("Ldev/latvian/mods/kubejs/recipe/viewer/RecipeViewerEntryType;")), targetClassName);
            }
        } catch (Exception | LinkageError unavailable) {
            // Optional mod absent or incompatible; never force-load optional Java classes.
        }
        return false;
    }
    private static boolean report(boolean supported, String target) {
        if (!supported) LoggerFactory.getLogger("MekanismScriptCompatibility").warn("Skipping optional script compatibility for {}: runtime API signature changed.", target);
        return supported;
    }
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) { }
    public List<String> getMixins() { return null; }
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }
}
