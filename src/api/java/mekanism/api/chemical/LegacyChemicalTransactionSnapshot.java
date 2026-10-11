package mekanism.api.chemical;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** One journal per handler preserves root/nested rollback order without retaining unloaded handlers. */
final class LegacyChemicalTransactionSnapshot extends SnapshotJournal<List<ChemicalStack>> {
    private static final ReferenceQueue<IChemicalHandler> QUEUE = new ReferenceQueue<>();
    private static final Map<IdentityKey, WeakReference<LegacyChemicalTransactionSnapshot>> JOURNALS = new HashMap<>();
    private final IChemicalHandler handler;

    private LegacyChemicalTransactionSnapshot(IChemicalHandler handler) { this.handler = handler; }

    static void capture(IChemicalHandler handler, TransactionContext transaction) {
        LegacyChemicalTransactionSnapshot journal;
        synchronized (JOURNALS) {
            for (var key = QUEUE.poll(); key != null; key = QUEUE.poll()) JOURNALS.remove(key);
            var reference = JOURNALS.get(new IdentityKey(handler, null));
            journal = reference == null ? null : reference.get();
            if (journal == null) {
                journal = new LegacyChemicalTransactionSnapshot(handler);
                JOURNALS.put(new IdentityKey(handler, QUEUE), new WeakReference<>(journal));
            }
        }
        journal.updateSnapshots(transaction);
    }

    @Override protected List<ChemicalStack> createSnapshot() {
        return IntStream.range(0, handler.getChemicalTanks()).mapToObj(i -> handler.getChemicalInTank(i).copy()).toList();
    }

    @Override protected void revertToSnapshot(List<ChemicalStack> snapshot) {
        for (int i = 0; i < snapshot.size(); i++) {
            if (!handler.getChemicalInTank(i).equals(snapshot.get(i))) handler.setChemicalInTank(i, snapshot.get(i));
        }
    }

    private static final class IdentityKey extends WeakReference<IChemicalHandler> {
        private final int hash;
        private IdentityKey(IChemicalHandler handler, ReferenceQueue<IChemicalHandler> queue) { super(handler, queue); hash = System.identityHashCode(handler); }
        @Override public int hashCode() { return hash; }
        @Override public boolean equals(Object other) { return this == other || other instanceof IdentityKey key && get() != null && get() == key.get(); }
    }
}
