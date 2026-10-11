package mekanism.api.chemical;

import mekanism.api.Action;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Binary-compatible chemical API for addons still exposing the pre-transaction handler. */
public interface IChemicalHandler extends ResourceHandler<ChemicalResource> {
    int getChemicalTanks();
    ChemicalStack getChemicalInTank(int tank);
    void setChemicalInTank(int tank, ChemicalStack stack);
    long getChemicalTankCapacity(int tank);
    boolean isValid(int tank, ChemicalStack stack);
    ChemicalStack insertChemical(int tank, ChemicalStack stack, Action action);
    ChemicalStack extractChemical(int tank, long amount, Action action);

    default ChemicalStack insertChemical(ChemicalStack stack, Action action) {
        ChemicalStack remaining = stack;
        for (int i=0;i<getChemicalTanks() && !remaining.isEmpty();i++) remaining=insertChemical(i,remaining,action);
        return remaining;
    }
    default ChemicalStack extractChemical(ChemicalStack stack, Action action) {
        if(stack.isEmpty()) return ChemicalStack.EMPTY;
        int extracted=0;
        for(int i=0;i<getChemicalTanks() && extracted<stack.amount();i++) {
            if(ChemicalStack.isSameChemical(getChemicalInTank(i),stack)) extracted+=extractChemical(i,stack.amount()-extracted,action).amount();
        }
        return extracted==0?ChemicalStack.EMPTY:stack.copyWithAmount(extracted);
    }
    default ChemicalStack extractChemical(long amount, Action action) {
        if(amount<=0)return ChemicalStack.EMPTY;
        for(int i=0;i<getChemicalTanks();i++) {
            var stack=getChemicalInTank(i);
            if(!stack.isEmpty())return extractChemical(stack.copyWithAmount((int)Math.min(amount,Integer.MAX_VALUE)),action);
        }
        return ChemicalStack.EMPTY;
    }
    @Override default int size(){return getChemicalTanks();}
    @Override default ChemicalResource getResource(int slot){return ChemicalResource.of(getChemicalInTank(slot));}
    @Override default long getAmountAsLong(int slot){return getChemicalInTank(slot).amount();}
    @Override default long getCapacityAsLong(int slot,ChemicalResource resource){return getChemicalTankCapacity(slot);}
    @Override default boolean isValid(int slot,ChemicalResource resource){return !resource.isEmpty() && isValid(slot,resource.toStack(1));}
    @Override default int insert(int slot,ChemicalResource resource,int amount,TransactionContext transaction){
        if(amount<=0 || resource.isEmpty())return 0;
        var stack=resource.toStack(amount);
        int accepted=amount-insertChemical(slot,stack,Action.SIMULATE).amount();
        if(accepted<=0)return 0;
        LegacyChemicalTransactionSnapshot.capture(this,transaction);
        return amount-insertChemical(slot,stack,Action.EXECUTE).amount();
    }
    @Override default int extract(int slot,ChemicalResource resource,int amount,TransactionContext transaction){
        if(amount<=0 || resource.isEmpty() || !resource.equals(getResource(slot)))return 0;
        int accepted=extractChemical(slot,amount,Action.SIMULATE).amount();
        if(accepted<=0)return 0;
        LegacyChemicalTransactionSnapshot.capture(this,transaction);
        return extractChemical(slot,amount,Action.EXECUTE).amount();
    }
}
