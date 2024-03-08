package org.tobi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Parameter> parameterList = getParameterList();
        List<IndexedValue> indexedValues = createIndexedValuesList(parameterList);
        int[][] requirementsArray = createRequirementsArray(indexedValues);

        printRequirementsArray(requirementsArray);

        List<String[]> initialTestSuite = createInitialTestSuite(parameterList);

        printTestSuite(initialTestSuite);

        fillInRequirementsArray(requirementsArray, initialTestSuite, 2, indexedValues);

        printRequirementsArray(requirementsArray);
    }

    private static List<Parameter> getParameterList() {
        List<Parameter> parameterList = new ArrayList<>();

        Parameter x = new Parameter("x", List.of("x1", "x2", "x3"));
        Parameter y = new Parameter("y", List.of("y1", "y2"));
        Parameter z = new Parameter("z", List.of("z1", "z2", "z3", "z4"));

        parameterList.add(x);
        parameterList.add(y);
        parameterList.add(z);

        return parameterList;
    }

    private static List<IndexedValue> createIndexedValuesList(List<Parameter> parameterList) {
        List<IndexedValue> indexedValuesList = new ArrayList<>();
        int index = 0;
        for (Parameter parameter : parameterList) {
            String parameterName = parameter.getName();
            for (String value : parameter.getValues()) {
                IndexedValue indexedValue = new IndexedValue(index, parameterName, value);
                indexedValuesList.add(indexedValue);
                index++;
            }

        }
        return indexedValuesList;
    }

    private static int[][] createRequirementsArray(List<IndexedValue> indexedValueList) {
        int[][] requirementsArray = new int[indexedValueList.size()][indexedValueList.size()];
        // Create requirements array row
        for (IndexedValue rowIndexedValue : indexedValueList) {
            // Create requirements array column
            for (IndexedValue columnIndexedValue : indexedValueList) {
                if (rowIndexedValue.getParameter().equals(columnIndexedValue.getParameter())) {
                    requirementsArray[rowIndexedValue.getIndex()][columnIndexedValue.getIndex()] = 1;
                }
            }
        }
        return requirementsArray;
    }

    private static void printRequirementsArray(int[][] requirementsArray) {
        for (int[] row : requirementsArray) {
            System.out.println(Arrays.toString(row));
        }
    }

    private static void printTestSuite(List<String[]> testSuite) {
        for (String[] row : testSuite) {
            System.out.println(Arrays.toString(row));
        }
    }

    private static List<String[]> createInitialTestSuite(List<Parameter> parameterList) {
        List<String> firstParameterValues = parameterList.get(0).getValues();
        List<String> secondParameterValues = parameterList.get(1).getValues();
        List<String[]> testSuite = new ArrayList<>();

        for (String firstValue : firstParameterValues) {
            for (String secondValue : secondParameterValues) {
                String[] test = new String[parameterList.size()];
                test[0] = firstValue;
                test[1] = secondValue;
                testSuite.add(test);
            }
        }
        return  testSuite;
    }

    private static void fillInRequirementsArray(int[][] requirementsArray, List<String[]> testSuite, int noOfParameters, List<IndexedValue> indexedValues) {
        List<String[]> coveredRequirements = new ArrayList<>();
        for (String[] test : testSuite) {
            for (int i = 0; i < noOfParameters - 1; i++) {
                for (int j = i + 1; j < noOfParameters; j++) {
                    String[] pairRequirement = new String[2];
                    pairRequirement[0] = test[i];
                    pairRequirement[1] = test[j];
                    coveredRequirements.add(pairRequirement);
                }
            }
        }

        for (String[] coveredRequirement : coveredRequirements) {
            int firstParamIndex = getIndexFromParameterValue(indexedValues, coveredRequirement[0]);
            int secondParamIndex = getIndexFromParameterValue(indexedValues, coveredRequirement[1]);

            requirementsArray[firstParamIndex][secondParamIndex] = 1;
            requirementsArray[secondParamIndex][firstParamIndex] = 1;
        }
    }

    private static int getIndexFromParameterValue(List<IndexedValue> indexedValues, String parameterValue) {
        IndexedValue filteredIndexValue = indexedValues.stream()
                .filter(indexedValue -> indexedValue.getValue().equals(parameterValue))
                .findFirst()
                .orElseThrow(NullPointerException::new);
        return filteredIndexValue.getIndex();
    }
}