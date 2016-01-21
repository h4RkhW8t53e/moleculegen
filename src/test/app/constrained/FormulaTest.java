package test.app.constrained;

import java.util.List;
import java.util.Map;

import org.openscience.cdk.interfaces.IAtomContainer;

import augment.constrained.BondGenerator;
import handler.DuplicateCountingHandler;

public class FormulaTest {
    
    public int countNFromAtom(String elementFormula) {
        return countNFromAtom(elementFormula, false);
    }
    
    public int countNFromAtom(String elementFormula, boolean printDupMap) {
        DuplicateCountingHandler handler = new DuplicateCountingHandler();
        BondGenerator gen = new BondGenerator(elementFormula, handler);
        gen.run();
        Map<String, List<IAtomContainer>> map = handler.getDupMap();
        if (printDupMap) { printDupMap(map); }
        return map.keySet().size(); 
    }
    
    private void printDupMap(Map<String, List<IAtomContainer>> map) {
        int i = 0;
        for (String s : map.keySet()) {
            System.out.println(i++ + " " + s + " = " + io.AtomContainerPrinter.toString(map.get(s).get(0)));
        }
    }

}
