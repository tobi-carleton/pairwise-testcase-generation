package org.tobi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RandomGreedyAlgorithm extends PairWiseTestBase {

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        List<Parameter> parameterList = getParameterList("oz");
        int totalNumberOfPairRequirements = getTotalNumberOfPairRequirements(parameterList);
        int numberOfCoveredRequirements = 0;
        int numOfIterations = 0;
        int parameterListSize = parameterList.size();
        List<IndexedValue> indexedValues = createIndexedValuesList(parameterList);
        int parameterValuesListSize = indexedValues.size();
        int[][] requirementsArray = createRequirementsArray(indexedValues);
        List<String[]> testSuite = new ArrayList<>();

        // Will be used for plotting incremental increase of pairwise coverage later on
        List<Double> cumulativeNumOfCoveredReq = new ArrayList<>();

        int kTrials = 3;
        int maxNumOfIterations = 1000;
        int numOfIterationsWithNoNewRequirementsCovered = 0;
        while (numOfIterations < maxNumOfIterations && numberOfCoveredRequirements < totalNumberOfPairRequirements) {
            List<String[]> kRandomTestCases;
            TestAndNumOfCoveredReq highestCoverageTest;
            if (numOfIterationsWithNoNewRequirementsCovered > 1) {
                // If random approach does not increase coverage after 2 iterations, constrain random test case generation to create test case that covers a missing pair requirement
                int[] indexesOfFirstUncoveredRequirement = getIndexesOfFirstUncoveredRequirement(requirementsArray, parameterValuesListSize);
                IndexedValue rowParameter = getParameterFromValueIndex(indexedValues, indexesOfFirstUncoveredRequirement[0]);
                IndexedValue columnParameter = getParameterFromValueIndex(indexedValues, indexesOfFirstUncoveredRequirement[1]);
                kRandomTestCases = createKRandomConstrainedTestCases(parameterList, parameterListSize, kTrials, rowParameter.getParameterIndex(), rowParameter.getValue(), columnParameter.getParameterIndex(), columnParameter.getValue());
            } else {
                kRandomTestCases = createKRandomTestCases(parameterList, parameterListSize, kTrials);
            }
            highestCoverageTest = getHighestCoverageTest(kRandomTestCases, parameterListSize, requirementsArray, indexedValues);

            if (highestCoverageTest.getNumOfCoveredReq() > 0) {
                testSuite.add(highestCoverageTest.getTest());
                numberOfCoveredRequirements += highestCoverageTest.getNumOfCoveredReq();
                numOfIterationsWithNoNewRequirementsCovered = 0;

                // Update requirements array
                fillInRequirementsArray(requirementsArray, Collections.singletonList(highestCoverageTest.getTest()), parameterListSize, indexedValues);
            } else {
                numOfIterationsWithNoNewRequirementsCovered++;
            }
            cumulativeNumOfCoveredReq.add((double) numberOfCoveredRequirements / totalNumberOfPairRequirements);
            numOfIterations++;
        }
        printTestSuite(testSuite);
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        // Calculate the used memory
        long memory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Used memory is bytes: " + memory);
        System.out.println("Total Time Elapsed: " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds");
        System.out.println("Test Suite Size = " + testSuite.size());
        System.out.println("Number of iterations taken is " + numOfIterations + " out of maximum allowed of " + maxNumOfIterations);
        System.out.println("Percentage of covered requirements = " + ((double) numberOfCoveredRequirements / totalNumberOfPairRequirements) * 100 + "%");
        List<Integer> xValues = IntStream.range(1, numOfIterations + 1).boxed().collect(Collectors.toList());
        LineChart.createIDLineChart("Covered Requirements vs. Number of Iterations", "Random Guided Greedy - " + kTrials + " Trials", xValues, cumulativeNumOfCoveredReq, "Number of Iterations", "Percentage of Covered Pair Requirements");
    }

    private static List<String[]> createKRandomTestCases(List<Parameter> parameterList, int parameterListSize, int kTrials) {
        List<String[]> kRandomTests = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < kTrials; i++) {
            String[] test = new String[parameterListSize];

            for (int j = 0; j < parameterListSize; j++) {
                Parameter parameter = parameterList.get(j);
                List<String> parameterValues = parameter.getValues();
                int parameterValuesSize = parameterValues.size();

                // Create test case by selecting random values for each parameter
                test[j] = parameterValues.get(random.nextInt(parameterValuesSize));
            }

            // Add newly created random test to list
            kRandomTests.add(test);
        }
        return kRandomTests;
    }

    private static List<String[]> createKRandomConstrainedTestCases(List<Parameter> parameterList, int parameterListSize, int kTrials, int rowParameterIndex, String rowParameterValue, int columnParameterIndex, String columnParameterValue) {
        List<String[]> kRandomTests = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < kTrials; i++) {
            String[] test = new String[parameterListSize];

            for (int j = 0; j < parameterListSize; j++) {
                if (j != rowParameterIndex && j!= columnParameterIndex) {
                    Parameter parameter = parameterList.get(j);
                    List<String> parameterValues = parameter.getValues();
                    int parameterValuesSize = parameterValues.size();

                    // Create test case by selecting random values for the parameter
                    test[j] = parameterValues.get(random.nextInt(parameterValuesSize));
                } else if (j == rowParameterIndex) {
                    // constrain parameter value
                    test[j] = rowParameterValue;
                } else {
                    // constrain parameter value
                    test[j] = columnParameterValue;
                }
            }

            // Add newly created random test to list
            kRandomTests.add(test);
        }
        return kRandomTests;
    }

    private static TestAndNumOfCoveredReq getHighestCoverageTest(List<String[]> kTests, int parameterListSize, int[][] requirementsArray, List<IndexedValue> indexedValues) {
        int max = Integer.MIN_VALUE;
        String[] bestTest = new String[parameterListSize];
        for (String[] test : kTests) {
            int numberOfNewlyCoveredRequirements = numberOfNewlyCoveredRequirements(requirementsArray, test, parameterListSize, indexedValues);

            if (numberOfNewlyCoveredRequirements > max) {
                max = numberOfNewlyCoveredRequirements;
                bestTest = test;
            }
        }

        return new TestAndNumOfCoveredReq(bestTest, max);
    }

    private static int[] getIndexesOfFirstUncoveredRequirement(int[][] requirementsArray, int size) {
        // Iterate over each row
        for (int i = 0; i < size; i++) {
            // Iterate over each column
            for (int j = 0; j < size; j++) {
                if (requirementsArray[i][j] == 0) {
                    return new int[]{i, j};
                }
            }
        }
        throw new RuntimeException("We should not reach this point, This method should not be called in a case where all requirements have already been satisfied");
    }
}
