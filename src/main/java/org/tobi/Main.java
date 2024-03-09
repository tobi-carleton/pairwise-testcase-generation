package org.tobi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Main {
    private static final String DONT_CARE = "DC";
    public static void main(String[] args) {
        List<Parameter> parameterList = getParameterList();
        List<IndexedValue> indexedValues = createIndexedValuesList(parameterList);
        int[][] requirementsArray = createRequirementsArray(indexedValues);

        List<String[]> testSuite = createInitialTestSuite(parameterList);

        fillInRequirementsArray(requirementsArray, testSuite, 2, indexedValues);

        for (int i = 2; i < parameterList.size(); i++) {
            testSuite = horizontalExpansion(testSuite, requirementsArray, parameterList.get(i), i, indexedValues);
            verticalExpansion(testSuite, requirementsArray, parameterList.get(i), i, indexedValues, parameterList);
            fillInRequirementsArray(requirementsArray, testSuite, i + 1, indexedValues);
        }
        printTestSuite(testSuite);
    }

    private static List<String[]> verticalExpansion(List<String[]> testSuite, int[][] requirementsArray, Parameter parameter, int parameterIndex, List<IndexedValue> indexedValues, List<Parameter> parameterList) {
        // Get parameter values
        List<String> values = parameter.getValues();
        for (String parameterValue : values) {
            // Determine uncovered requirements
            int paramValueIndex = getIndexFromParameterValue(indexedValues, parameterValue);
            int[] requirementsForParamValue = requirementsArray[paramValueIndex];
            List<IndexedValue[]> missingRequirements = new ArrayList<>();
            for (int i = 0; i < requirementsForParamValue.length; i++) {
                if (requirementsForParamValue[i] == 0) {
                    IndexedValue[] missingReq = new IndexedValue[2];
                    // Always add parameter being expanded in index 0
                    missingReq[0] = getIndexedValueFromParameterValue(indexedValues, parameterValue);
                    missingReq[1] = getIndexedValueFromIndex(indexedValues, i);
                    missingRequirements.add(missingReq);
                }
            }

            for (IndexedValue[] missingReq : missingRequirements) {
                boolean updatedExistingTest = false;
                for (String[] test : testSuite) {
                    if (existingTestCanCoverRequirement(test, missingReq)) {
                        // update existing test - replace DON'T CARE value
                        int otherParameterIndex = missingReq[1].getParameterIndex();
                        test[otherParameterIndex] = missingReq[1].getValue();
                        updatedExistingTest = true;
                        break;
                    }
                }
                if (!updatedExistingTest) {
                    // No suitable existing test found - add a new test case
                    String[] test = new String[parameterList.size()];
                    int parameterBeingExpandedIndex = missingReq[0].getParameterIndex();
                    String parameterBeingExpandedValue = missingReq[0].getValue();
                    int otherParameterIndex = missingReq[1].getParameterIndex();
                    String otherParameterValue = missingReq[1].getValue();

                    test[parameterBeingExpandedIndex] = parameterBeingExpandedValue;
                    test[otherParameterIndex] = otherParameterValue;
                    fillNullValuesWithDontCare(test);
                    testSuite.add(test);
                }
            }
        }
        return testSuite;
    }

    private static void fillNullValuesWithDontCare(String[] test) {
        for (int i = 0; i < test.length; i++) {
            if (Objects.isNull(test[i])) {
                test[i] = DONT_CARE;
            }
        }
    }

    private static boolean existingTestCanCoverRequirement(String[] existingTest, IndexedValue[] requirement) {
        int parameterBeingExpandedIndex = requirement[0].getParameterIndex();
        String parameterBeingExpandedValue = requirement[0].getValue();
        int otherParameterIndex = requirement[1].getParameterIndex();

        // If existing test contains the parameter being expanded value, and the other parameter is marked as don't care
        // We can update existing test case without affecting its existing requirement coverage
        return existingTest[parameterBeingExpandedIndex].equalsIgnoreCase(parameterBeingExpandedValue) && existingTest[otherParameterIndex].equalsIgnoreCase(DONT_CARE);
    }

    private static List<String[]> horizontalExpansion(List<String[]> testSuite, int[][] requirementsArray, Parameter parameter, int parameterIndex, List<IndexedValue> indexedValues) {
        // Get parameter values
        List<String> parameterValues = parameter.getValues();
        List<String[]> updatedTestSuite = new ArrayList<>();
        for (String[] test : testSuite) {
            updatedTestSuite.add(getBestCoverageTest(test, parameterValues, parameterIndex, requirementsArray, indexedValues));

            //Update requirements array
            fillInRequirementsArray(requirementsArray, updatedTestSuite, parameterIndex + 1, indexedValues);
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
        System.out.println("------- Test Suite -------");
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

        coveredRequirements = coveredRequirements.stream().filter(coveredReq -> !(coveredReq[0].equalsIgnoreCase(DONT_CARE) || coveredReq[1].equalsIgnoreCase(DONT_CARE))).collect(Collectors.toList());
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

    private static IndexedValue getIndexedValueFromParameterValue(List<IndexedValue> indexedValues, String parameterValue) {
        return indexedValues.stream()
                .filter(indexedValue -> indexedValue.getValue().equals(parameterValue))
                .findFirst()
                .orElseThrow(NullPointerException::new);
    }

    private static IndexedValue getIndexedValueFromIndex(List<IndexedValue> indexedValues, int parameterIndex) {
        return indexedValues.stream()
                .filter(indexedValue -> indexedValue.getIndex() == parameterIndex)
                .findFirst()
                .orElseThrow(NullPointerException::new);
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