package test.branch;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openscience.cdk.group.AtomContainerPrinter;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

import branch.AtomAugmentation;

/**
 * Test augmentation of molecules by single atoms and sets of bonds.
 * 
 * @author maclean
 *
 */
public class TestAtomAugmentation {
    
    private IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
    
    private IAtomContainer make(String acpString) {
        return AtomContainerPrinter.fromString(acpString, builder);
    }
    
    private AtomAugmentation makeAugmentation(IAtomContainer mol, String elementSymbol, int... points) {
        return new AtomAugmentation(mol, builder.newInstance(IAtom.class, elementSymbol), points);
    }
    
    @Test
    public void testCanonical() {
        test("C0C1C2C3 0:1(1),0:2(1),1:3(1),2:3(1)", "C", 1, 1, 0, 0);
        test("C0C1C2C3 0:1(1),0:2(1),0:3(1),1:2(1)", "C", 0, 1, 0, 1);
    }
    
    private void test(String start, String atom, int... points) {
        IAtomContainer mol = make(start);
        AtomAugmentation aug = makeAugmentation(mol, atom, points);
        IAtomContainer augMol = aug.getAugmentedMolecule();
        System.out.println(aug.isCanonical() + "\t" + AtomContainerPrinter.toString(augMol));
    }
    
    @Test
    public void testCanonicalFromSingle() {
        test("C0C1 0:1(1)", "C", 2, 1);
        test("C0C1 0:1(2)", "C", 1, 1);
    }
    
    @Test
    public void testFailingPair() {
        test("C0C1C2 0:1(1),0:2(1),1:2(1)", "C", 2, 0, 0);
        test("C0C1C2 0:1(2),0:2(1)", "C", 1, 0, 1);
    }
    
    @Test
    public void testGetAugmentedMolecule() {
        IAtomContainer mol = make("C0C1C2 0:1(1),0:2(1)");
        AtomAugmentation augmentation = makeAugmentation(mol, "C", 1, 0, 1);
        IAtomContainer augmentedMol = augmentation.getAugmentedMolecule();
        AtomContainerPrinter.print(augmentedMol);
        IBond addedBond = augmentedMol.getBond(2);
        assertEquals("Added bond 1", IBond.Order.SINGLE, addedBond.getOrder());
    }

}
