package net.ssehub.kernel_haven.cnf;

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.variability_model.VariabilityModel;
import net.ssehub.kernel_haven.variability_model.VariabilityModelDescriptor.ConstraintFileType;
import net.ssehub.kernel_haven.variability_model.VariabilityVariable;

/**
 * This class is only for converting the VariabilityModel to a CNF Table. Then
 * it can be Solved.
 * 
 * @author Johannes
 * @author malek
 */
public class VmToCnfConverter {

    public static final @NonNull String CNF_START_INDICATOR = "p cnf";

    public static final @NonNull String ROW_DELIMITER = " ";
    
    public static final int CNF_START_LINE_LENGTH = 4;
    
    /**
     * Creates a new {@link VmToCnfConverter}.
     */
    public VmToCnfConverter() {
        
    }
    
    /**
     * This method converts the vm into a cnf representation.
     * 
     * @param vm The vm. This has to have a DIMACS model as the constraint model.
     * @return returns the cnf of the vm.
     * 
     * @throws FormatException If the constraint model file of the {@link VariabilityModel} is not a DIMACS file.
     */
    public @NonNull Cnf convertVmToCnf(@NonNull VariabilityModel vm) throws FormatException {
        if (vm.getDescriptor().getConstraintFileType() != ConstraintFileType.DIMACS) {
            throw new FormatException("Descriptor says constraint file type is not DIMACS");
        }
        
        File dimacsModel = vm.getConstraintModel();
        Cnf cnf = null;
        Map<Integer, String> vmMap = getMapOfVM(vm);
        LineNumberReader br = null;
        try {
            br = new LineNumberReader(new BufferedReader(new FileReader(dimacsModel)));
            String line = br.readLine();
            // iterating over variability variables.
            while (line != null && !line.contains(CNF_START_INDICATOR)) {
                line = br.readLine();
            }
            if (line == null) {
                // we are at the end of file without finding the proper start indicator
                throw new FormatException("Missing \"p cnf\" line");
            }
            
            String[] startline = line.split(ROW_DELIMITER);
            if (startline.length != CNF_START_LINE_LENGTH) {
                throw new FormatException("Invalid \"p cnf\" line in linenumber " + br.getLineNumber() + ": " + line);
            }
            
            // p cnf 2 4
            int initialLenght = Integer.parseInt(startline[3]);
            cnf = new Cnf(initialLenght);
            
            int maxNumber = Integer.parseInt(startline[2]);
            
            line = br.readLine();
            int count = 0;
            while (line != null) {
                count++;
                if (count > initialLenght) {
                    throw new FormatException("Too many lines found (current line " + br.getLineNumber() + ")");
                }
                
                cnf.addRow(parseLine(line, vmMap, maxNumber, br.getLineNumber()));
                
                line = br.readLine();
            }
            
            if (count < initialLenght) {
                throw new FormatException("Reached end of file with too few lines found");
            }
        } catch (NumberFormatException e) {
            if (br != null) {
                throw new FormatException("Error parsing number in line " + br.getLineNumber() + ": " + e.getMessage());
            } else {
                throw new FormatException("Error parsing number: " + e.getMessage());
            }
        } catch (IOException e) {
            throw new FormatException(e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }
        return cnf;
    }

    /**
     * Reads a single line and converts it to a CNF line. This converts the numbers to the appropriate
     * variable names.
     * 
     * @param vmMap The mapping of number -> variable name.
     * @param line The line to parse.
     * @param maxNumber The maximum allowed variable number.
     * @param currentLineNumber The current line number in the DIMACS file (for error messages).
     * @return The CNF line.
     * @throws FormatException If the format is wrong.
     */
    private @NonNull CnfVariable @NonNull [] parseLine(@NonNull String line, @NonNull Map<Integer, String> vmMap,
            int maxNumber, int currentLineNumber) throws FormatException {
        
        String[] dimacsModelLine = line.split(ROW_DELIMITER);
        // the loop makes sure that every entry is filled with non null
        @NonNull CnfVariable[] cnfRow = new  @NonNull CnfVariable[dimacsModelLine.length - 1];
        
        for (int i = 0; i < dimacsModelLine.length - 1; i++) {
            int dimacsNumber = Integer.parseInt(dimacsModelLine[i]);
            int absolutDimacsNumber = Math.abs(dimacsNumber);
            boolean isNot = dimacsNumber < 0;
            
            if (absolutDimacsNumber > maxNumber) {
                throw new FormatException("Too high number in line " + currentLineNumber + ": " + absolutDimacsNumber);
            }
            
            if (!vmMap.containsKey(absolutDimacsNumber)) {
                // we have no mapping, so just generate a number
                cnfRow[i] = new CnfVariable(isNot, "VARIABLE_" + absolutDimacsNumber);
            } else {
                cnfRow[i] = new CnfVariable(isNot, notNull(vmMap.get(absolutDimacsNumber)));
            }
        }
        
        return cnfRow; 
    }
    

    /**
     * This Method converts a Map from a VariabilityModel.
     * This Method could be included in the VariabilityModel.
     * @param vm the Variability Model.
     * @return a Map with the DimacsNumber as Key and the VariableName as Value.
     */
    private @NonNull Map<Integer, String> getMapOfVM(@NonNull VariabilityModel vm) {
        Set<VariabilityVariable> set = vm.getVariables();
        HashMap<Integer, String> map = new HashMap<>();
        for (VariabilityVariable variabilityVariable : set) {
            variabilityVariable.getDimacsMapping(map);
        }
        return map;
    }
    
    
}
