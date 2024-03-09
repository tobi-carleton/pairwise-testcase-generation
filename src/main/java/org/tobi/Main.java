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

        List<String[]> testSuite = createInitialTestSuite(parameterList);

        printTestSuite(testSuite);

        fillInRequirementsArray(requirementsArray, testSuite, 2, indexedValues);

        printRequirementsArray(requirementsArray);

        testSuite = horizontalExpansion(testSuite, requirementsArray, parameterList.get(2), 2, indexedValues);
        printTestSuite(testSuite);
        printRequirementsArray(requirementsArray);
    }

    private static List<String[]> horizontalExpansion(List<String[]> testSuite, int[][] requirementsArray, Parameter parameter, int parameterIndex, List<IndexedValue> indexedValues) {
        // Get parameter values
        List<String> parameterValues = parameter.getValues();
        List<String[]> updatedTestSuite = new ArrayList<>();
        for (String[] test : testSuite) {
            updatedTestSuite.add(getBestCoverageTest(test, parameterValues, parameterIndex, requirementsArray, indexedValues));

            //Update requirements array
            fillInRequirementsArray(requirementsArray, updatedTestSuite, parameterIndex + 1, indexedValues);
            printRequirementsArray(requirementsArray);
        }

        return updatedTestSuite;
    }

    private static String[] getBestCoverageTest(String[] test, List<String> parameterValues, int testCaseIndex, int[][] requirementsArray, List<IndexedValue> indexedValues) {
        String[] bestTest = new String[0];
        int max = Integer.MIN_VALUE;
        for (String parameterValue : parameterValues) {
            // Create test with new parameter added
            String[] cloneTest = test.clone();
            cloneTest[testCaseIndex] = parameterValue;

            List<String[]> singleTest = new ArrayList<>();
            singleTest.add(cloneTest);

            int[][] reqArrayClone = copyIntArray(requirementsArray);

            // Get Index of new parameter
            int parameterIndex = getIndexFromParameterValue(indexedValues, parameterValue);

            // Get missing requirements for this parameter
            int missingReqs = 0;
            int[] paramRequirements = reqArrayClone[parameterIndex];
            for (int i = 0; i < indexedValues.size(); i++) {
                if (paramRequirements[i] == 0) {
                    missingReqs++;
                }
            }

            fillInRequirementsArray(reqArrayClone, singleTest, testCaseIndex + 1, indexedValues);

            int missingReqsAfterAddingTest = 0;
            paramRequirements = reqArrayClone[parameterIndex];
            for (int i = 0; i < indexedValues.size(); i++) {
                if (paramRequirements[i] == 0) {
                    missingReqsAfterAddingTest++;
                }
            }

            int numberOfExercisedRequirements = missingReqs - missingReqsAfterAddingTest;
            System.out.println("Number of exercised requirements with test " + Arrays.toString(cloneTest) + " is " + numberOfExercisedRequirements);
            if (numberOfExercisedRequirements > max) {
                max = numberOfExercisedRequirements;
                bestTest = cloneTest.clone();
            }
        }
        return bestTest;
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

    private static int[][] createRequirementsArray(List<IndexedValue> indexedValues) {
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

    private static String getParameterValueFromIndex(List<IndexedValue> indexedValues, int parameterIndex) {
        IndexedValue filteredIndexValue = indexedValues.stream()
                .filter(indexedValue -> indexedValue.getIndex() == parameterIndex)
                .findFirst()
                .orElseThrow(NullPointerException::new);
        return filteredIndexValue.getValue();
    }

    private static int[][] copyIntArray(int[][] arrayToCopy) {
        int noOfRows = arrayToCopy.length;
        int noOfColumns = arrayToCopy[0].length;

        int[][] copy = new int[noOfRows][noOfColumns];

        for (int i = 0; i < noOfRows; i++) {
            System.arraycopy(arrayToCopy[i], 0, copy[i], 0, noOfColumns);
        }

        return copy;
    }
}