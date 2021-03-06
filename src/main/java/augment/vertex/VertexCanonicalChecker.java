package augment.vertex;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import augment.CanonicalChecker;
import group.Partition;
import group.Permutation;
import group.graph.GraphDiscretePartitionRefiner;
import model.Graph;
import util.graph.CutCalculator;

public class VertexCanonicalChecker implements CanonicalChecker<ByVertexAugmentation> {

    public boolean isCanonical(ByVertexAugmentation atomAugmentation) {
        Graph augmentedMolecule = atomAugmentation.getAugmentedObject();
        
        if (augmentedMolecule.getVertexCount() <= 2) {
            return true;
        }
        
        Set<Integer> nonSeparatingAtoms = getNonSeparatingAtoms(augmentedMolecule);
//        System.out.println(nonSeparatingAtoms);
        if (nonSeparatingAtoms.size() == 0) {
            return true;
        }

        GraphDiscretePartitionRefiner refiner = new GraphDiscretePartitionRefiner();
        refiner.getAutomorphismGroup(augmentedMolecule);

        int chosen = getChosen(nonSeparatingAtoms, refiner.getBest());
        int last = augmentedMolecule.getVertexCount() - 1;
        return inOrbit(chosen, last, refiner.getAutomorphismPartition());
    }
    
    private boolean inOrbit(int chosen, int last, Partition orbits) {
//        System.out.println("chosen " + chosen + " last " + last + " " + orbits);
        for (int cellIndex = 0; cellIndex < orbits.size(); cellIndex++) {
            SortedSet<Integer> orbit = orbits.getCell(cellIndex);
            if (orbit.contains(chosen) && orbit.contains(last)) {
                return true;
            }
        }
        return false;   
    }
    
    // TODO : combine this method with get non separating atoms
    private int getChosen(Set<Integer> nonSeparatingAtoms, Permutation labelling) {
        for (int index = labelling.size() - 1; index >= 0; index--) {
            int label = labelling.get(index);
            if (nonSeparatingAtoms.contains(label)) {
                return label;
            } else {
                continue;
            }
        }
        return -1;  // XXX shouldn't happen...
    }
    
    private Set<Integer> getNonSeparatingAtoms(Graph mol) {
        Set<Integer> nonSeparatingVertices = new HashSet<>();
        Set<Integer> cutVertices = CutCalculator.getCutVertices(mol);
//        System.out.println(cutVertices);
        for (int index = 0; index < mol.getVertexCount(); index++) {
            if (cutVertices.contains(index)) {
                continue;
            } else {
                nonSeparatingVertices.add(index);
            }
        }
//        System.out.println(io.AtomContainerPrinter.toString(mol) + " " + nonSeparatingVertices);
        return nonSeparatingVertices;
    }
 
}
