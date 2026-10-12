package mekanism.common.capabilities.energy;

import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.Upgrade;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.functions.IntObjectToIntFunction;
import mekanism.api.math.MathUtils;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.tile.machine.TileEntityElectrolyticSeparator;
import org.jspecify.annotations.Nullable;

public class ElectroSeparatorEnergyContainer extends MachineEnergyContainer<TileEntityElectrolyticSeparator> {

    public static ElectroSeparatorEnergyContainer input(TileEntityElectrolyticSeparator tile, IntObjectToIntFunction<TileEntityElectrolyticSeparator> baseEnergyCalculator,
          @Nullable IContentsListener listener) {
        AttributeEnergy electricBlock = validateBlock(tile);
        return new ElectroSeparatorEnergyContainer(electricBlock.getStorage(), electricBlock.getUsage(), notExternal, ConstantPredicates.alwaysTrue(), tile, baseEnergyCalculator, listener);
    }

    private final IntObjectToIntFunction<TileEntityElectrolyticSeparator> baseEnergyCalculator;
    private long calculatedCapacity;
    private boolean capacityCalculated;

    private boolean hasCreativeUpgrade() {
        if (tile == null) {
            return false;
        }
        for (Upgrade upgrade : Upgrade.values()) {
            if ("creative".equals(upgrade.getSerializedName()) && tile.supportsUpgrade(upgrade) && tile.getUpgrades(upgrade) > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public long getCapacityAsLong() {
        return capacityCalculated && !hasCreativeUpgrade() ? calculatedCapacity : super.getCapacityAsLong();
    }

    @Override
    public void setMaxEnergy(long maxEnergy) {
        // Also follow capacity updates received by the client's normal container sync.
        calculatedCapacity = maxEnergy;
        capacityCalculated = true;
        super.setMaxEnergy(maxEnergy);
    }

    protected ElectroSeparatorEnergyContainer(long maxEnergy, int energyPerTick, Predicate<AutomationType> canExtract,
          Predicate<AutomationType> canInsert, TileEntityElectrolyticSeparator tile, IntObjectToIntFunction<TileEntityElectrolyticSeparator> baseEnergyCalculator, @Nullable IContentsListener listener) {
        super(maxEnergy, energyPerTick, canExtract, canInsert, tile, listener);
        this.baseEnergyCalculator = baseEnergyCalculator;
    }

    @Override
    public int getBaseEnergyPerTick() {
        return baseEnergyCalculator.applyAsInt(super.getBaseEnergyPerTick(), tile);
    }

    @Override
    public void updateEnergyPerTick() {
        if (tile.isMakingHydrogen()) {
            //Energy upgrades do nothing
            this.currentEnergyPerTick = getBaseEnergyPerTick();
        } else {
            super.updateEnergyPerTick();
        }
    }

    @Override
    public void updateMaxEnergy() {
        if (hasCreativeUpgrade()) {
            return;
        }
        // Calculate from the required work, never from stored energy: the superclass
        // getter preserves surplus energy, which would otherwise feed back into this buffer.
        long capacity = MathUtils.multiplyClamped((long) getEnergyPerTick(), AttributeEnergy.STORAGE_MULTIPLIER);
        if (tile.isMakingHydrogen()) {
            capacity = MathUtils.multiplyClamped(capacity,
                  MathUtils.multiplyClamped((long) tile.getBaselineMaxOperations(), AttributeEnergy.STORAGE_MULTIPLIER));
        }
        // The concrete capacity getter also lets setMaxEnergy clamp saved surplus.
        setMaxEnergy(capacity);
    }
}
