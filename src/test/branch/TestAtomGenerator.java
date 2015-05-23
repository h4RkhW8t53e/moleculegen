package test.branch;

import static io.AtomContainerPrinter.fromString;
import io.AtomContainerPrinter;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

import branch.AtomGenerator;
import branch.CountingHandler;
import branch.DuplicateHandler;
import branch.Handler;
import branch.PrintStreamHandler;

public class TestAtomGenerator {
    
    private IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
    
    private void run(String elementFormula, String fromString, Handler handler) {
        AtomGenerator gen = new AtomGenerator(elementFormula, handler);
        gen.run(AtomContainerPrinter.fromString(fromString, builder));
    }
    
    private void printFrom(String elementFormula, String fromString) {
        run(elementFormula, fromString, new PrintStreamHandler(System.out));
    }
    
    private int countFrom(String elementFormula, String fromString) {
        CountingHandler handler = new CountingHandler();
        AtomGenerator gen = new AtomGenerator(elementFormula, handler);
        gen.run(fromString(fromString, builder));
        return handler.getCount();
    }
    
    @Test
    public void testFromCCSingle() {
        printFrom("C4H6", "C0C1 0:1(1)");
    }
    
    @Test
    public void testFromCCDouble() {
        printFrom("CCCC", "C0C1 0:1(2)");
    }
    
    @Test
    public void testToFours() {
        int count  = countFrom("C4H6", "C0C1 0:1(1)");
            count += countFrom("C4H6", "C0C1 0:1(2)");
            count += countFrom("C4H6", "C0C1 0:1(3)");
        System.out.println(count);
    }
    
    @Test
    public void testC4H8() {
        AtomGenerator gen = new AtomGenerator("C4H8", new PrintStreamHandler(System.out));
        gen.run();
    }
    
    @Test
    public void testCH5N() {
        AtomGenerator gen = new AtomGenerator("CH5N", new PrintStreamHandler(System.out));
        gen.run();
    }
    
    @Test
    public void testCH5NFromAtom() {
        AtomGenerator gen = new AtomGenerator("CH5N", new PrintStreamHandler(System.out));
        IAtomContainer a = builder.newInstance(IAtomContainer.class);
        a.addAtom(builder.newInstance(IAtom.class, "C"));
        gen.run(a);
    }
    
    @Test
    public void testDups() {
        DuplicateHandler handler = new DuplicateHandler();
        run("C4H6", "C0C1 0:1(1)", handler);
        run("C4H6", "C0C1 0:1(2)", handler);
        run("C4H6", "C0C1 0:1(3)", handler);
        Map<String, List<IAtomContainer>> map = handler.getDupMap();
        int count = 0;
        for (String key : map.keySet()) {
            List<IAtomContainer> dups = map.get(key);
            if (dups.size() == 1) {
                System.out.println(count + "\t1\t" + AtomContainerPrinter.toString(dups.get(0)));
            } else {
                System.out.println(count + "\t" + dups.size());
            }
            count++;
        }
    }

}
