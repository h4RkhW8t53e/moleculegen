package appbranch.handler;

import java.io.PrintStream;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.signature.MoleculeSignature;
import org.openscience.cdk.smiles.SmilesGenerator;

import io.AtomContainerPrinter;



/**
 * Prints the generated molecules in a string form to a print 
 * stream, defaulting to System out. 
 * 
 * @author maclean
 *
 */
public class PrintStreamStringHandler implements Handler {
	
	private PrintStream printStream;
	
	private SmilesGenerator smilesGenerator;
	
	private int count;
	
	private DataFormat format;
	
	private boolean shouldNumberLines;
	
	public PrintStreamStringHandler() {
		this(System.out, DataFormat.SMILES);
	}
	
	public PrintStreamStringHandler(PrintStream printStream, DataFormat format) {
	    this(printStream, format, false);
	}
	
	public PrintStreamStringHandler(PrintStream printStream, 
	                                DataFormat format, 
	                                boolean numberLines) {
		this.printStream = printStream;
		this.format = format;
		if (format == DataFormat.SMILES) {
		    smilesGenerator = SmilesGenerator.unique();
		}
		count = 0;
		this.shouldNumberLines = numberLines;
	}

	@Override
	public void handle(IAtomContainer atomContainer) {
	    String stringForm;
	    try {
	        stringForm = getStringForm(atomContainer);
	    } catch (CDKException cdke) {
	        stringForm = "EXCEPTION";  // XXX
	    }
	  
	    if (shouldNumberLines) { 
	        printStream.println(count + "\t" + stringForm);
	    } else {
	        printStream.println(stringForm);
	    }
	    count++;
	}
	
	@Override
    public void finish() {
	    printStream.close();
    }

    private String getStringForm(IAtomContainer atomContainer) throws CDKException {
	    if (format == DataFormat.SMILES) {
	        return smilesGenerator.create(atomContainer);
	    } else if (format == DataFormat.SIGNATURE) {
	        MoleculeSignature childSignature = new MoleculeSignature(atomContainer);
	        return childSignature.toCanonicalString();
	    } else if (format == DataFormat.ACP) {
	        return AtomContainerPrinter.toString(atomContainer);
        } else {
            return "";  // XXX
        }
	}
}
