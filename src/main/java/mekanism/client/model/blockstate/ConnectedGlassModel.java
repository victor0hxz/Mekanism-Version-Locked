package mekanism.client.model.blockstate;

import com.mojang.serialization.MapCodec;
import java.util.List;
import mekanism.common.Mekanism;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

@EventBusSubscriber(modid = Mekanism.MODID, value = Dist.CLIENT)
public final class ConnectedGlassModel implements DynamicBlockStateModel {
    private final BlockStateModelPart[][] faces;

    private ConnectedGlassModel(BlockStateModelPart[][] faces) {
        this.faces = faces;
    }

    @SubscribeEvent
    public static void register(RegisterBlockStateModels event) {
        event.registerModel(Mekanism.rl("special/connected_glass"), Unbaked.MAP_CODEC);
    }

    // UV left, top, right, bottom for Minecraft's six cube faces.
    public static Direction[] neighbors(Direction face) {
        return switch (face) {
            case NORTH -> new Direction[]{Direction.EAST, Direction.UP, Direction.WEST, Direction.DOWN};
            case SOUTH -> new Direction[]{Direction.WEST, Direction.UP, Direction.EAST, Direction.DOWN};
            case EAST -> new Direction[]{Direction.SOUTH, Direction.UP, Direction.NORTH, Direction.DOWN};
            case WEST -> new Direction[]{Direction.NORTH, Direction.UP, Direction.SOUTH, Direction.DOWN};
            case UP -> new Direction[]{Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH};
            case DOWN -> new Direction[]{Direction.WEST, Direction.SOUTH, Direction.EAST, Direction.NORTH};
        };
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        for (Direction face : Direction.values()) {
            int mask = 0;
            Direction[] directions = neighbors(face);
            for (int i = 0; i < directions.length; i++) {
                if (level.getBlockState(pos.relative(directions[i])).is(state.getBlock())) {
                    mask |= 1 << i;
                }
            }
            parts.add(faces[face.ordinal()][mask]);
        }
    }

    @Override
    public Material.Baked particleMaterial() {
        return faces[0][0].particleMaterial();
    }

    @Override
    public int materialFlags() {
        return faces[0][0].materialFlags();
    }

    public record Unbaked(String modelPrefix) implements CustomUnbakedBlockStateModel {
        public static final MapCodec<Unbaked> MAP_CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(in -> in.group(
                com.mojang.serialization.Codec.STRING.optionalFieldOf("model_prefix", "mekanism:block/connected_glass").forGetter(Unbaked::modelPrefix)
        ).apply(in, Unbaked::new));

        private Identifier model(Direction face, int mask) {
            return Identifier.parse(modelPrefix + "/" + face.getSerializedName() + "_" + mask);
        }

        @Override
        public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
            return MAP_CODEC;
        }

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            BlockStateModelPart[][] parts = new BlockStateModelPart[6][16];
            for (Direction face : Direction.values()) {
                for (int mask = 0; mask < 16; mask++) {
                    parts[face.ordinal()][mask] = SimpleModelWrapper.bake(baker, baker.getModel(model(face, mask)), Variant.SimpleModelState.DEFAULT.asModelState());
                }
            }
            return new ConnectedGlassModel(parts);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            for (Direction face : Direction.values()) {
                for (int mask = 0; mask < 16; mask++) {
                    resolver.markDependency(model(face, mask));
                }
            }
        }
    }
}
