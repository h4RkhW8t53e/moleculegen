package app;

import generate.AtomAugmentingGenerator;
import handler.DataFormat;
import handler.GenerateHandler;
import handler.PrintStreamHandler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IElement;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.io.iterator.IIteratingChemObjectReader;
import org.openscience.cdk.io.iterator.IteratingSMILESReader;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

public class AMG {
    
    public static void main(String[] args) throws CDKException, IOException {
        ArgumentHandler argsH = new ArgumentHandler();
        argsH.processArguments(args);
        run(argsH);
    }
    
    public static void run(ArgumentHandler argsH) throws CDKException, IOException {
        if (argsH.isHelp()) {
            argsH.printHelp();
            return;
        }
        
        String formula = argsH.getFormula();
        if (formula == null) {
            error("Please supply a formula");
        }
        
        IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
        AtomAugmentingGenerator generator;
        GenerateHandler handler;
        DataFormat format = argsH.getOutputFormat();
        
        PrintStream outStream;
        if (argsH.isStdOut()) {
            outStream = System.out;
        } else {
            String outputFilename = argsH.getOutputFilepath();
            outStream = new PrintStream(new FileOutputStream(outputFilename));
        }
        handler = new PrintStreamHandler(outStream, format);
        generator = new AtomAugmentingGenerator(handler);
        
        int heavyAtomCount = setParamsFromFormula(formula, generator);
        if (heavyAtomCount < 3) {
            error("Please specify more than 3 heavy atoms");
        }
        
        if (argsH.isAugmentingFile()) {
            String inputFile = argsH.getInputFilepath();
            if (inputFile == null) {
                error("No input file specified");
            } else {
                // TODO : single-molecule file?
                IIteratingChemObjectReader<IAtomContainer> reader = getInputReader(argsH, builder);
                if (reader != null) {
                    while (reader.hasNext()) {
                        IAtomContainer parent = reader.next();
                        int currentAtomIndex = parent.getAtomCount();   // XXX what about Hs?
                        generator.extend(parent, currentAtomIndex, heavyAtomCount);
                    }
                    reader.close();
                } else {
                    error("Problem with the input");    // XXX
                }
                
            }
        } else if (argsH.isStartingFromScratch()) {
            List<String> symbols = generator.getElementSymbols();
            
            // XXX until generation from a single atom is fixed, have to do this...
            String firstE  = symbols.get(0);
            String secondE = symbols.get(1);
            
            IAtomContainer singleBond = makeEdge(firstE, secondE, IBond.Order.SINGLE, builder);
            generator.extend(singleBond, 2, heavyAtomCount);
            
            IAtomContainer doubleBond = makeEdge(firstE, secondE, IBond.Order.DOUBLE, builder);
            generator.extend(doubleBond, 2, heavyAtomCount);
            
            // XXX - if there are less than 2 carbons, this might be inefficient?
            IAtomContainer tripleBond = makeEdge(firstE, secondE, IBond.Order.TRIPLE, builder);
            generator.extend(tripleBond, 2, heavyAtomCount);
        }
    }
    
    /**
     * Get a suitable reader based on the program arguments or null if there is
     * a problem.
     * 
     * @param argsH
     * @params builder a chem object builder
     * @return
     * @throws FileNotFoundException 
     */
    private static IIteratingChemObjectReader<IAtomContainer> getInputReader(
            ArgumentHandler argsH, IChemObjectBuilder builder) throws FileNotFoundException {
        DataFormat inputFormat = argsH.getInputFormat();
        IIteratingChemObjectReader<IAtomContainer> reader;
        String filepath = argsH.getInputFilepath();
        InputStream in = new FileInputStream(filepath);
        switch (inputFormat) {
            case SMILES: reader = new IteratingSMILESReader(in, builder); break;
            case SIGNATURE: reader = new IteratingSignatureReader(in, builder); break;
            default: reader = null; error("Unrecognised format"); break;
        }
        return reader;
    }

    /**
     * Set the hydrogen count and the heavy atom symbol string from the formula, 
     * returning the count of heavy atoms.
     * 
     * @param formulaString
     * @param generator
     * @return
     */
    private static int setParamsFromFormula(
            String formulaString, AtomAugmentingGenerator generator) {
        IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
        IMolecularFormula formula = 
            MolecularFormulaManipulator.getMolecularFormula(formulaString, builder);
        List<IElement> elements = MolecularFormulaManipulator.elements(formula);
        
        // count the number of non-heavy atoms
        int hCount = 0;
        List<String> elementSymbols = new ArrayList<String>();
        for (IElement element : elements) {
            String symbol = element.getSymbol();
            int count = MolecularFormulaManipulator.getElementCount(formula, element);
            if (symbol.equals("H")) {
                hCount = count;
            } else {
                for (int i = 0; i < count; i++) {
                    elementSymbols.add(symbol);
                }
            }
        }
        generator.setHCount(hCount);
        
        // could just pass in the list of strings...
        Collections.sort(elementSymbols);
        StringBuffer buffer = new StringBuffer();
        for (String e : elementSymbols) {
            buffer.append(e);
        }
        generator.setElementString(buffer.toString());
        return elementSymbols.size();
    }
    
    private static void error(String text) {
        System.out.println(text);
    }
    
    private static IAtomContainer makeEdge(
            String elementA, String elementB, IBond.Order order, IChemObjectBuilder builder) {
        IAtomContainer ac = builder.newInstance(IAtomContainer.class);
        ac.addAtom(builder.newInstance(IAtom.class,(elementA)));
        ac.addAtom(builder.newInstance(IAtom.class,(elementA)));
        ac.addBond(0, 1, order);
        return ac;
    }

}