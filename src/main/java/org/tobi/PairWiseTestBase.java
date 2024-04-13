package org.tobi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PairWiseTestBase {

    protected static final String DONT_CARE = "DC";

    protected static List<Parameter> getParameterList(String parameterListToGet) {
        List<Parameter> parameterList = new ArrayList<>();

        switch (parameterListToGet) {
            case "demo":
                Parameter x = new Parameter("x", List.of("x1", "x2", "x3"));
                Parameter y = new Parameter("y", List.of("y1", "y2"));
                Parameter z = new Parameter("z", List.of("z1", "z2", "z3", "z4"));

                parameterList.add(x);
                parameterList.add(y);
                parameterList.add(z);
                break;
            case "oz":
                Parameter noise_Y = new Parameter("noise_Y", List.of("0.0", "1.67", "3.33"));
                Parameter two_pass = new Parameter("two_pass", List.of("true", "false"));
                Parameter is_track_side_at_left = new Parameter("is_track_side_at_left", List.of("true", "false"));
                Parameter noise_X = new Parameter("noise_X", List.of("0.0", "1.67", "3.33"));
                Parameter length = new Parameter("length", List.of("10.0", "40.0", "70.0"));
                Parameter vegetable = new Parameter("vegetable", List.of("cabbage", "leek"));
                Parameter grass_density = new Parameter("grass_density", List.of("0", "1", "2", "3", "4"));
                Parameter is_first_uturn_right_side = new Parameter("is_first_uturn_right_side", List.of("true", "false"));
                Parameter roughness = new Parameter("roughness", List.of("0.0", "0.33", "0.67"));
                Parameter final_track_outer = new Parameter("final_track_outer", List.of("true", "false"));
                Parameter weed_area = new Parameter("weed_area", List.of("2.0", "35.0", "68.0"));
                Parameter gap = new Parameter("gap", List.of("55", "91", "127"));
                Parameter is_first_track_outer = new Parameter("is_first_track_outer", List.of("true", "false"));
                Parameter vegetable_density = new Parameter("vegetable_density", List.of("1", "2", "3"));
                Parameter persistence = new Parameter("persistence", List.of("0.0", "0.23", "0.47"));
                Parameter inner_track_width = new Parameter("inner_track_width", List.of("0", "33", "66"));
                Parameter row = new Parameter("row", List.of("1.0", "34.0", "67.0"));
                Parameter disappearance_probability = new Parameter("disappearance_probability", List.of("0.0", "10.0", "20.0"));

                parameterList.add(noise_Y);
                parameterList.add(two_pass);
                parameterList.add(is_track_side_at_left);
                parameterList.add(noise_X);
                parameterList.add(length);
                parameterList.add(vegetable);
                parameterList.add(grass_density);
                parameterList.add(is_first_uturn_right_side);
                parameterList.add(roughness);
                parameterList.add(final_track_outer);
                parameterList.add(weed_area);
                parameterList.add(gap);
                parameterList.add(is_first_track_outer);
                parameterList.add(vegetable_density);
                parameterList.add(persistence);
                parameterList.add(inner_track_width);
                parameterList.add(row);
                parameterList.add(disappearance_probability);
                break;
            default:
                throw new RuntimeException("Select valid option of either 'demo' or 'oz'");
        }
        return parameterList;
    }

    protected static List<IndexedValue> createIndexedValuesList(List<Parameter> parameterList) {
        List<IndexedValue> indexedValuesList = new ArrayList<>();
        int index = 0;
        int parameterIndex = 0;
        for (Parameter parameter : parameterList) {
            String parameterName = parameter.getName();
            for (String value : parameter.getValues()) {
                IndexedValue indexedValue = new IndexedValue(index, parameterIndex, parameterName, value);
                indexedValuesList.add(indexedValue);
                index++;
            }
            parameterIndex++;
        }
        return indexedValuesList;
    }

    protected static int[][] createRequirementsArray(List<IndexedValue> indexedValues) {
        int[][] requirementsArray = new int[indexedValues.size()][indexedValues.size()];
        // Create requirements array row
        for (IndexedValue rowIndexedValue : indexedValues) {
            // Create requirements array column
            for (IndexedValue columnIndexedValue : indexedValues) {
                if (rowIndexedValue.getParameter().equals(columnIndexedValue.getParameter())) {
                    requirementsArray[rowIndexedValue.getIndex()][columnIndexedValue.getIndex()] = 1;
                }
            }
        }
        return requirementsArray;
    }

    protected static void fillInRequirementsArray(int[][] requirementsArray, List<String[]> testSuite, int noOfParameters, List<IndexedValue> indexedValues) {
        List<String[]> coveredRequirements = new ArrayList<>();
        List<int[]> parameterIndexes = new ArrayList<>();
        for (String[] test : testSuite) {
            for (int i = 0; i < noOfParameters - 1; i++) {
                for (int j = i + 1; j < noOfParameters; j++) {
                    String[] pairRequirement = new String[2];
                    int[] parameterIndex = new int[2];
                    pairRequirement[0] = test[i];
                    pairRequirement[1] = test[j];
                    parameterIndex[0] = i;
                    parameterIndex[1] = j;
                    if (!(pairRequirement[0] == null || pairRequirement[1] == null || pairRequirement[0].equalsIgnoreCase(DONT_CARE) || pairRequirement[1].equalsIgnoreCase(DONT_CARE))) {
                        coveredRequirements.add(pairRequirement);
                        parameterIndexes.add(parameterIndex);
                    }
                }
            }
        }

        int coveredRequirementsSize = coveredRequirements.size();
        for (int i = 0; i < coveredRequirementsSize; i++) {
            int firstParamIndex = getIndexFromParameterValue(indexedValues, coveredRequirements.get(i)[0], parameterIndexes.get(i)[0]);
            int secondParamIndex = getIndexFromParameterValue(indexedValues, coveredRequirements.get(i)[1], parameterIndexes.get(i)[1]);

            requirementsArray[firstParamIndex][secondParamIndex] = 1;
            requirementsArray[secondParamIndex][firstParamIndex] = 1;
        }
    }

    protected static void printRequirementsArray(int[][] requirementsArray) {
        for (int[] row : requirementsArray) {
            System.out.println(Arrays.toString(row));
        }
    }

    protected static void printTestSuite(List<String[]> testSuite) {
        System.out.println("------- Test Suite -------");
        for (String[] row : testSuite) {
            System.out.println(Arrays.toString(row));
        }
    }

    protected static int getIndexFromParameterValue(List<IndexedValue> indexedValues, String parameterValue, int parameterIndex) {
        IndexedValue filteredIndexValue = indexedValues.stream()
                .filter(indexedValue -> indexedValue.getParameterIndex() == parameterIndex)
                .filter(indexedValue -> indexedValue.getValue().equals(parameterValue))
                .findFirst()
                .orElseThrow(NullPointerException::new);
        return filteredIndexValue.getIndex();
    }

    protected static String getParameterValueFromIndex(List<IndexedValue> indexedValues, int parameterIndex) {
        IndexedValue filteredIndexValue = indexedValues.stream()
                .filter(indexedValue -> indexedValue.getIndex() == parameterIndex)
                .findFirst()
                .orElseThrow(NullPointerException::new);
        return filteredIndexValue.getValue();
    }

    protected static IndexedValue getIndexedValueFromParameterValue(List<IndexedValue> indexedValues, String parameterValue, int parameterIndex) {
        return indexedValues.stream()
                .filter(indexedValue -> indexedValue.getParameterIndex() == parameterIndex)
                .filter(indexedValue -> indexedValue.getValue().equals(parameterValue))
                .findFirst()
                .orElseThrow(NullPointerException::new);
    }

    protected static IndexedValue getIndexedValueFromIndex(List<IndexedValue> indexedValues, int parameterIndex) {
        return indexedValues.stream()
                .filter(indexedValue -> indexedValue.getIndex() == parameterIndex)
                .findFirst()
                .orElseThrow(NullPointerException::new);
    }

    protected static int[][] copyIntArray(int[][] arrayToCopy) {
        int noOfRows = arrayToCopy.length;
        int noOfColumns = arrayToCopy[0].length;

        int[][] copy = new int[noOfRows][noOfColumns];

        for (int i = 0; i < noOfRows; i++) {
            System.arraycopy(arrayToCopy[i], 0, copy[i], 0, noOfColumns);
        }

        return copy;
    }

    protected static int getTotalNumberOfPairRequirements(List<Parameter> parameterList) {
        int totalNumOfPairRequirements = 0;
        int noOfParameters = parameterList.size();
        for (int i = 0; i < noOfParameters - 1; i++) {
            for (int j = i + 1; j < noOfParameters; j++) {
                totalNumOfPairRequirements = totalNumOfPairRequirements + (parameterList.get(i).getValues().size() * parameterList.get(j).getValues().size());
            }
        }

        return totalNumOfPairRequirements;
    }

    protected static int numberOfNewlyCoveredRequirements(int[][] requirementsArray, String[] test, int noOfParameters, List<IndexedValue> indexedValues) {
        int numOfNewlyCoveredRequirements = 0;
        List<String[]> coveredRequirements = new ArrayList<>();
        List<int[]> parameterIndexes = new ArrayList<>();
        for (int i = 0; i < noOfParameters - 1; i++) {
            for (int j = i + 1; j < noOfParameters; j++) {
                String[] pairRequirement = new String[2];
                int[] parameterIndex = new int[2];
                pairRequirement[0] = test[i];
                pairRequirement[1] = test[j];
                parameterIndex[0] = i;
                parameterIndex[1] = j;
                if (!(pairRequirement[0] == null || pairRequirement[1] == null || pairRequirement[0].equalsIgnoreCase(DONT_CARE) || pairRequirement[1].equalsIgnoreCase(DONT_CARE))) {
                    coveredRequirements.add(pairRequirement);
                    parameterIndexes.add(parameterIndex);
                }
            }
        }

        int coveredRequirementsSize = coveredRequirements.size();
        for (int i = 0; i < coveredRequirementsSize; i++) {
            int firstParamIndex = getIndexFromParameterValue(indexedValues, coveredRequirements.get(i)[0], parameterIndexes.get(i)[0]);
            int secondParamIndex = getIndexFromParameterValue(indexedValues, coveredRequirements.get(i)[1], parameterIndexes.get(i)[1]);

            if (requirementsArray[firstParamIndex][secondParamIndex] == 0 && requirementsArray[secondParamIndex][firstParamIndex] == 0) {
                numOfNewlyCoveredRequirements++;
            }
        }

        return numOfNewlyCoveredRequirements;
    }

}
