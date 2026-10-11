package mekanism.common.capabilities.chemical;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.chemical.*;
import mekanism.api.resource.IMekanismResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
/** Exposes modern item containers to binary consumers of the former chemical interface. */
public final class LegacyChemicalResourceHandler implements IChemicalHandler, IMekanismResourceHandler<ChemicalResource,IChemicalTank> {
 private final IMekanismResourceHandler<ChemicalResource,IChemicalTank> handler;
 public LegacyChemicalResourceHandler(IMekanismResourceHandler<ChemicalResource,IChemicalTank> handler){this.handler=handler;}
 public List<IChemicalTank> getContainers(){return handler.getContainers();}
 public int size(){return handler.size();}
 public ChemicalResource getResource(int slot){return handler.getResource(slot);}
 public long getAmountAsLong(int slot){return handler.getAmountAsLong(slot);}
 public long getCapacityAsLong(int slot,ChemicalResource resource){return handler.getCapacityAsLong(slot,resource);}
 public boolean isValid(int slot,ChemicalResource resource){return handler.isValid(slot,resource);}
 public int insert(int slot,ChemicalResource resource,int amount,TransactionContext tx){return handler.insert(slot,resource,amount,tx);}
 public int extract(int slot,ChemicalResource resource,int amount,TransactionContext tx){return handler.extract(slot,resource,amount,tx);}
 public int getChemicalTanks(){return size();}
 public ChemicalStack getChemicalInTank(int slot){return getResource(slot).toStack((int)Math.min(getAmountAsLong(slot),Integer.MAX_VALUE));}
 public long getChemicalTankCapacity(int slot){return getCapacityAsLong(slot,getResource(slot));}
 public boolean isValid(int slot,ChemicalStack stack){return !stack.isEmpty() && isValid(slot,ChemicalResource.of(stack));}
 public void setChemicalInTank(int slot,ChemicalStack stack){handler.getContainer(slot).setContents(ChemicalResource.of(stack),stack.amount(),Transaction.getCurrentOpenedTransaction());}
 public ChemicalStack insertChemical(int slot,ChemicalStack stack,Action action){if(stack.isEmpty())return ChemicalStack.EMPTY;try(var tx=Transaction.open(Transaction.getCurrentOpenedTransaction())){int moved=insert(slot,ChemicalResource.of(stack),stack.amount(),tx);if(action.execute())tx.commit();return stack.copyWithAmount(stack.amount()-moved);}}
 public ChemicalStack extractChemical(int slot,long amount,Action action){var resource=getResource(slot);if(resource.isEmpty() || amount<=0)return ChemicalStack.EMPTY;try(var tx=Transaction.open(Transaction.getCurrentOpenedTransaction())){int moved=extract(slot,resource,(int)Math.min(amount,Integer.MAX_VALUE),tx);if(action.execute())tx.commit();return resource.toStack(moved);}}
}
