package org.tobi;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InParameterOrder extends PairWiseTestBase {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
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
        System.out.println((System.currentTimeMillis() - startTime) / 1000.0);
        System.out.println(testSuite.size());
    }

    private static List<String[]> verticalExpansion(List<String[]> testSuite, int[][] requirementsArray, Parameter parameter, int parameterIndex, List<IndexedValue> indexedValues, List<Parameter> parameterList) {
        // Get parameter values
        List<String> values = parameter.getValues();
        for (String parameterValue : values) {
            // Determine uncovered requirements
            int paramValueIndex = getIndexFromParameterValue(indexedValues, parameterValue, parameterIndex);
            int[] requirementsForParamValue = requirementsArray[paramValueIndex];
            List<IndexedValue[]> missingRequirements = new ArrayList<>();
            for (int i = 0; i < requirementsForParamValue.length; i++) {
                if (requirementsForParamValue[i] == 0) {
                    IndexedValue[] missingReq = new IndexedValue[2];
                    // Always add parameter being expanded in index 0
                    missingReq[0] = getIndexedValueFromParameterValue(indexedValues, parameterValue, parameterIndex);
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
        try{
            return existingTest[parameterBeingExpandedIndex].equalsIgnoreCase(parameterBeingExpandedValue) && existingTest[otherParameterIndex].equalsIgnoreCase(DONT_CARE);
        } catch (NullPointerException e) {
            return false;
        }

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

    private static String[] getBestCoverageTest(String[] test, List<String> parameterValues, int parameterIndex, int[][] requirementsArray, List<IndexedValue> indexedValues) {
        String[] bestTest = new String[0];
        int max = Integer.MIN_VALUE;
        for (String parameterValue : parameterValues) {
            // Create test with new parameter added
            String[] cloneTest = test.clone();
            cloneTest[parameterIndex] = parameterValue;

            List<String[]> singleTest = new ArrayList<>();
            singleTest.add(cloneTest);

            int[][] reqArrayClone = copyIntArray(requirementsArray);

            // Get Index of new parameter
            int parameterValueIndex = getIndexFromParameterValue(indexedValues, parameterValue, parameterIndex);

            // Get missing requirements for this parameter
            int missingReqs = 0;
            int[] paramRequirements = reqArrayClone[parameterValueIndex];
            for (int i = 0; i < indexedValues.size(); i++) {
                if (paramRequirements[i] == 0) {
                    missingReqs++;
                }
            }

            fillInRequirementsArray(reqArrayClone, singleTest, parameterIndex + 1, indexedValues);

            int missingReqsAfterAddingTest = 0;
            paramRequirements = reqArrayClone[parameterValueIndex];
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
}