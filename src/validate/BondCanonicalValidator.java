package validate;

import java.util.SortedSet;

import group.BondDiscretePartitionRefiner;
import group.Partition;
import group.Permutation;

import org.openscience.cdk.interfaces.IAtomContainer;

public class BondCanonicalValidator {

    public boolean isCanonical(IAtomContainer atomContainer) {
        BondDiscretePartitionRefiner refiner = new BondDiscretePartitionRefiner();
        refiner.getAutomorphismGroup(atomContainer);
        Permutation labelling = refiner.getBest();
        Partition autPartition = refiner.getAutomorphismPartition();
        int size = atomContainer.getBondCount() - 1;
        int del = labelling.get(size);
        boolean canonical = (del == size || inSameCell(autPartition, del, size));
//        System.out.println(test.AtomContainerPrinter.toString(atomContainer)
//                +"\t" + labelling + "\t" + autPartition 
//                + "\t" + size+ "\t" + del + "\t" + canonical);
        return canonical;
    }
    
    private boolean inSameCell(Partition partition, int i, int j) {
        for (int cellIndex = 0; cellIndex < partition.size(); cellIndex++) {
            SortedSet<Integer> cell = partition.getCell(cellIndex);
            if (cell.contains(i) && cell.contains(j)) {
                return true;
            }
        }
        return false;
    }

}