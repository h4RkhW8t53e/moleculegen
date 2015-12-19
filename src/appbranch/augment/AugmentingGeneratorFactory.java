package appbranch.augment;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import appbranch.ArgumentHandler;
import appbranch.handler.CountingHandler;
import appbranch.handler.DataFormat;
import appbranch.handler.Handler;
import appbranch.handler.PrintStreamStringHandler;
import appbranch.handler.SDFHandler;
import appbranch.handler.ZipDecoratingHandler;
import generate.AugmentationMethod;

public class AugmentingGeneratorFactory {
    
    public static AugmentingGenerator build(ArgumentHandler argsH) throws IOException {
        if (argsH.isHelp()) {
            argsH.printHelp();
            return null;
        }
        
        String formula = argsH.getFormula();
        if (formula == null) {
            error("Please supply a formula");
            return null;
        }
        
        AugmentationMethod augmentationMethod = (argsH.getAugmentationMethod() == null)? AugmentationMethod.ATOM : argsH.getAugmentationMethod();
        if (augmentationMethod == AugmentationMethod.ATOM) {
            return new AtomGenerator(formula, getHandler(argsH));
        } else {
            // TODO
            System.err.println("ERROR : augmentation method " + augmentationMethod + " not yet implemented");
            return null;
        }
    }
    
    private static Handler getHandler(ArgumentHandler argsH) throws IOException {
        DataFormat format = argsH.getOutputFormat();
        
        if (format == DataFormat.NONE && ! argsH.isComparingToFile()) {
            return new CountingHandler(argsH.isTiming());
        } else {
            PrintStream outStream;
            boolean shouldNumberLines = argsH.isShouldNumberLines();
            if (argsH.isStdOut()) {
                if (format == DataFormat.SDF) {
                    return new SDFHandler();
                } else {
                    outStream = System.out;
                    return new PrintStreamStringHandler(outStream, format, shouldNumberLines);
                }
            } else {
                String outputFilename = argsH.getOutputFilepath();
                if (format == DataFormat.SDF) {
                    return new SDFHandler(outputFilename);
                } else {
                    if (argsH.isZipOutput()) {
                        String zipEntryName = argsH.getFormula() + ".txt"; // TODO?
                        return new ZipDecoratingHandler(
                                outputFilename, zipEntryName, format, shouldNumberLines);
                    } else {
                        PrintStream printStream = new PrintStream(new FileOutputStream(outputFilename)); 
                        return new PrintStreamStringHandler(printStream, format, shouldNumberLines);
                    }
                }
            }
        }
    }
    
    // TODO : convert to throwing a custom exception - GeneratorBuildException?
    private static void error(String text) {
        System.out.println(text);
    }

}
