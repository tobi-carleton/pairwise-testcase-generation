package org.tobi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InParameterOrder extends PairWiseTestBase {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        List<Parameter> parameterList = getParameterList("oz");
        int totalNumberOfPairRequirements = getTotalNumberOfPairRequirements(parameterList);
        List<IndexedValue> indexedValues = createIndexedValuesList(parameterList);
        int[][] requirementsArray = createRequirementsArray(indexedValues);

        // Will be used for plotting incremental increase of pairwise coverage later on
        int numberOfCoveredRequirements = 0;
        List<Double> cumulativeNumOfCoveredReq = new ArrayList<>();

        List<String[]> testSuite = createInitialTestSuite(parameterList);
        numberOfCoveredRequirements += getNewRequirementCoverageOfTest(testSuite, 2, requirementsArray, indexedValues).stream().mapToInt(Integer::intValue).sum();
        cumulativeNumOfCoveredReq.add((double) numberOfCoveredRequirements / totalNumberOfPairRequirements);


        fillInRequirementsArray(requirementsArray, testSuite, 2, indexedValues);

        int parameterListSize = parameterList.size();
        for (int i = 2; i < parameterListSize; i++) {
            numberOfCoveredRequirements = horizontalExpansion(testSuite, requirementsArray, parameterList.get(i), i, i + 1,  indexedValues, cumulativeNumOfCoveredReq, numberOfCoveredRequirements, totalNumberOfPairRequirements);
            numberOfCoveredRequirements = verticalExpansion(testSuite, requirementsArray, parameterList.get(i), i, i + 1, indexedValues, parameterList, cumulativeNumOfCoveredReq, numberOfCoveredRequirements, totalNumberOfPairRequirements);
        }
        printTestSuite(testSuite);
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        // Calculate the used memory
        long memory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Used memory is bytes: " + memory);
        System.out.println("Total Time Elapsed: " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds");
        System.out.println("Test Suite Size: " + testSuite.size());
        System.out.println("Number of horizontal/vertical expansion steps = " + cumulativeNumOfCoveredReq.size());
        System.out.println("Percentage of covered requirements = " + ((double) numberOfCoveredRequirements / totalNumberOfPairRequirements) * 100 + "%");
        List<Integer> xValues = IntStream.range(1, cumulativeNumOfCoveredReq.size() + 1).boxed().collect(Collectors.toList());
        LineChart.createIDLineChart("Covered Requirements vs. Number of horizontal/vertical expansion steps", "In Parameter Order", xValues, cumulativeNumOfCoveredReq, "Horizontal/Vertical Expansion Steps", "Number of covered pair requirements");
    }

    private static List<Integer> getNewRequirementCoverageOfTest(List<String[]> kTests, int numParamsToExpand, int[][] requirementsArray, List<IndexedValue> indexedValues) {
        List<Integer> coveredReqs = new ArrayList<>();
        for (String[] test : kTests) {
            coveredReqs.add(numberOfNewlyCoveredRequirements(requirementsArray, test, numParamsToExpand, indexedValues));
        }
        return coveredReqs;
    }

    private static int verticalExpansion(List<String[]> testSuite, int[][] requirementsArray, Parameter parameter, int parameterIndex, int numParamsToExpand, List<IndexedValue> indexedValues, List<Parameter> parameterList, List<Double> cumulativeNumOfCoveredReq, int numberOfCoveredRequirements, int totalNumberOfPairRequirements) {
        // Get parameter values
        List<String> values = parameter.getValues();
        int numOfParameterValues = parameter.getValues().size();
        int lastParameterValueIndex = getIndexFromParameterValue(indexedValues, values.get(numOfParameterValues - 1), parameterIndex);
        for (String parameterValue : values) {
            // Determine uncovered requirements
            int paramValueIndex = getIndexFromParameterValue(indexedValues, parameterValue, parameterIndex);
            int[] requirementsForParamValue = requirementsArray[paramValueIndex];
            List<IndexedValue[]> missingRequirements = new ArrayList<>();
            for (int i = 0; i < lastParameterValueIndex + 1; i++) {
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
                        numberOfCoveredRequirements += getNewRequirementCoverageOfTest(Collections.singletonList(test), numParamsToExpand, requirementsArray, indexedValues).stream().mapToInt(Integer::intValue).sum();
                        cumulativeNumOfCoveredReq.add((double)  numberOfCoveredRequirements / totalNumberOfPairRequirements);
                        fillInRequirementsArray(requirementsArray, Collections.singletonList(test), numParamsToExpand, indexedValues);
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
                    numberOfCoveredRequirements += getNewRequirementCoverageOfTest(Collections.singletonList(test), numParamsToExpand, requirementsArray, indexedValues).stream().mapToInt(Integer::intValue).sum();
                    cumulativeNumOfCoveredReq.add((double)  numberOfCoveredRequirements / totalNumberOfPairRequirements);
                    fillInRequirementsArray(requirementsArray, Collections.singletonList(test), numParamsToExpand, indexedValues);
                    testSuite.add(test);
                }
            }
        }
        return numberOfCoveredRequirements;
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

    private static int horizontalExpansion(List<String[]> testSuite, int[][] requirementsArray, Parameter parameter, int parameterIndex, int numParamsToExpand, List<IndexedValue> indexedValues, List<Double> cumulativeNumOfCoveredReq, int numberOfCoveredRequirements, int totalNumberOfPairRequirements) {
        // Get parameter values
        List<String> parameterValues = parameter.getValues();
        int testSuiteSize = testSuite.size();
        for (int i = 0; i < testSuiteSize; i++) {
            String[] bestCoverageTest = getBestCoverageTest(testSuite.get(i), parameterValues, parameterIndex, numParamsToExpand, requirementsArray, indexedValues);
            testSuite.set(i, bestCoverageTest);

            numberOfCoveredRequirements += getNewRequirementCoverageOfTest(Collections.singletonList(bestCoverageTest), numParamsToExpand, requirementsArray, indexedValues).stream().mapToInt(Integer::intValue).sum();
            cumulativeNumOfCoveredReq.add((double)  numberOfCoveredRequirements / totalNumberOfPairRequirements);
            //Update requirements array
            fillInRequirementsArray(requirementsArray, Collections.singletonList(bestCoverageTest), numParamsToExpand, indexedValues);
        }
        return numberOfCoveredRequirements;
    }

    private static String[] getBestCoverageTest(String[] test, List<String> parameterValues, int parameterIndex, int numParamsToExpand, int[][] requirementsArray, List<IndexedValue> indexedValues) {
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

            fillInRequirementsArray(reqArrayClone, singleTest, numParamsToExpand, indexedValues);

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