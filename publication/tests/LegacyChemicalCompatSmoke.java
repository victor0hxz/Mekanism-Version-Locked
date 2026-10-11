package atm11.compat;
import com.supermartijn642.trashcans.*;
import mekanism.api.chemical.*;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.util.ResourceUtils;
import net.minecraft.core.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import java.util.List;
@Mod("atm11_compat_test")
@EventBusSubscriber(modid="atm11_compat_test")
public class CrashSmoke {
 private static void check(boolean ok,String label) { if(!ok)throw new AssertionError(label);System.out.println("CRASH PASS: "+label); }
 @SubscribeEvent public static void start(ServerStartedEvent e) {
  try {
   var level=e.getServer().overworld();
   for(var block:List.of(TrashCans.liquid_trash_can,TrashCans.ultimate_trash_can)) {
    var pos=new BlockPos(block==TrashCans.liquid_trash_can?0:4,100,0);level.removeBlock(pos,false);level.setBlockAndUpdate(pos,block.defaultBlockState());
    var tile=(TrashCanBlockEntity)level.getBlockEntity(pos);
    if(tile.isFluidFilterWhitelist())tile.toggleFluidFilterWhitelist();
    var handler=level.getCapability(Capabilities.CHEMICAL.block(),pos,Direction.UP);
    check(handler!=null,"modern chemical capability loads");
    check(handler instanceof IChemicalHandler,"legacy chemical capability preserved");
    var resource=ChemicalResource.of(MekanismChemicals.HYDROGEN);
    var source=BasicChemicalTank.createAllValid(1000,null);try(var seed=Transaction.openRoot()){source.setContents(resource,1000,seed);seed.commit();}
    try(var tx=Transaction.openRoot()) {check(ResourceUtils.emit(List.of(handler),resource,100,tx)==100,"crash path single-target emit accepts hydrogen");}
    check(source.amountAsLong()==1000,"preview leaves source unchanged");
    try(var tx=Transaction.openRoot()) {check(ResourceUtils.emit(handler,source,250,tx)==250,"transactional source emits chemical");}
    check(source.amountAsLong()==1000,"aborted source transfer restores amount");
    check(ResourceUtils.emit(handler,source,250,null)==250,"committed source transfer accepted");check(source.amountAsLong()==750,"committed transfer removes exactly 250");
    tile.toggleFluidFilterWhitelist();check(ResourceUtils.emit(handler,source,250,null)==0,"whitelist rejects unmatched chemicals");check(source.amountAsLong()==750,"rejected chemical preserved");
   }
   var hydrogen=ChemicalResource.of(MekanismChemicals.HYDROGEN);
   var display=com.supermartijn642.trashcans.compat.Compatibility.MEKANISM.getChemicalTankForGasStack(hydrogen.toStack(1));
   check(!display.isEmpty(),"chemical filter display tank created");
   check(com.supermartijn642.trashcans.compat.Compatibility.MEKANISM.doesItemHaveGasStored(display),"modern item chemical detection");
   var filter=new com.supermartijn642.trashcans.compat.mekanism.GasFilterManager().createFilter(display);
   check(filter.isValid() && filter.matches(hydrogen.toStack(1)),"chemical filter matches stored resource");
   check(!filter.matches(ChemicalResource.of(MekanismChemicals.OXYGEN).toStack(1)),"chemical filter rejects different chemical");
   var legacy=new IChemicalHandler(){
    ChemicalStack stored=hydrogen.toStack(100);
    public int getChemicalTanks(){return 1;}
    public ChemicalStack getChemicalInTank(int slot){return stored;}
    public void setChemicalInTank(int slot,ChemicalStack stack){stored=stack;}
    public long getChemicalTankCapacity(int slot){return 1000;}
    public boolean isValid(int slot,ChemicalStack stack){return true;}
    public ChemicalStack insertChemical(int slot,ChemicalStack stack,mekanism.api.Action action){if(action.execute())stored=hydrogen.toStack(stored.amount()+stack.amount());return ChemicalStack.EMPTY;}
    public ChemicalStack extractChemical(int slot,long amount,mekanism.api.Action action){int n=(int)Math.min(stored.amount(),amount);if(action.execute())stored=hydrogen.toStack(stored.amount()-n);return hydrogen.toStack(n);}
   };
   try(var tx=Transaction.openRoot()){legacy.insert(0,hydrogen,50,tx);legacy.extract(0,hydrogen,25,tx);check(legacy.getAmountAsLong(0)==125,"legacy storage changes during transaction");}
   check(legacy.getAmountAsLong(0)==100,"multiple legacy operations roll back in order");
   try(var root=Transaction.openRoot()){legacy.insert(0,hydrogen,50,root);try(var child=Transaction.open(root)){legacy.extract(0,hydrogen,25,child);}check(legacy.getAmountAsLong(0)==150,"nested rollback preserves parent changes");root.commit();}
   check(legacy.getAmountAsLong(0)==150,"legacy committed state retained");
   check(!level.getCapability(Capabilities.CHEMICAL.block(),new BlockPos(0,100,0),Direction.UP).isValid(0,ChemicalResource.of(MekanismChemicals.POLONIUM)),"radioactive chemicals rejected with nuclear voiding disabled");
   System.out.println("CRASH FUNCTIONAL COMPLETE");
  } catch(Throwable t) {System.out.println("CRASH FUNCTIONAL FAILED");t.printStackTrace();}finally{e.getServer().halt(false);}
 }
}
