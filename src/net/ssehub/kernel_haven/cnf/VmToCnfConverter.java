package net.ssehub.kernel_haven.cnf;

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;
import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNullArrayWithNotNullContent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.variability_model.VariabilityModel;
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
        File dimacsModel = vm.getConstraintModel();
        Cnf cnf = null;
        Map<Integer, String> vmMap = getMapOfVM(vm);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(dimacsModel));
            String line = br.readLine();
            // iterating over variability variables.
            while (!line.contains(CNF_START_INDICATOR)) {
                line = br.readLine();
                if (line == null) {
                    // we are at the end of file without finding the proper start indicator
                    throw new FormatException("Missing \"p cnf\" line");
                }
            }
            
            String[] startline = line.split(ROW_DELIMITER);
            if (startline.length == CNF_START_LINE_LENGTH) {
                throw new FormatException("Invalid \"p cnf\" line: " + line);
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
                    throw new FormatException("Too many lines found");
                }
                
                cnf.addRow(parseLine(line, vmMap, maxNumber));
                
                line = br.readLine();
            }
            
            if (count < initialLenght) {
                throw new FormatException("Too few lines found");
            }
        } catch (NumberFormatException e) {
            throw new FormatException("Error parsing number: " + e.getMessage());
        } catch (FileNotFoundException e) {
            throw new FormatException(e);
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
     * @return The CNF line.
     * @throws FormatException If the format is wrong.
     */
    private @NonNull CnfVariable @NonNull [] parseLine(@NonNull String line, @NonNull Map<Integer, String> vmMap,
            int maxNumber) throws FormatException {
        
        String[] dimacsModelLine = line.split(ROW_DELIMITER);
        CnfVariable[] cnfRow = new CnfVariable[dimacsModelLine.length - 1];
        for (int i = 0; i < dimacsModelLine.length - 1; i++) {
            int dimacsNumber = Integer.parseInt(dimacsModelLine[i]);
            int absolutDimacsNumber = Math.abs(dimacsNumber);
            boolean isNot = dimacsNumber < 0;
            
            if (absolutDimacsNumber > maxNumber) {
                throw new FormatException("Too high number: " + absolutDimacsNumber);
            }
            
            if (!vmMap.containsKey(absolutDimacsNumber)) {
                // we have no mapping, so just generate a number
                cnfRow[i] = new CnfVariable(isNot, "VARIABLE_" + absolutDimacsNumber);
            } else {
                cnfRow[i] = new CnfVariable(isNot, notNull(vmMap.get(absolutDimacsNumber)));
            }
            
            
        }
        return notNullArrayWithNotNullContent(cnfRow); // the loop makes sure that every entry is filled with non null
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
