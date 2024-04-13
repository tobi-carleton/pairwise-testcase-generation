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
        int[][] requirementsArray = createRequirementsArray(indexedValues);
        List<String[]> testSuite = new ArrayList<>();

        // Will be used for plotting incremental increase of pairwise coverage later on
        List<Double> cumulativeNumOfCoveredReq = new ArrayList<>();

        int kTrials = 3;
        int maxNumOfIterations = 1000;
        while (numOfIterations < maxNumOfIterations && numberOfCoveredRequirements < totalNumberOfPairRequirements) {
            List<String[]> kRandomTestCases = createKRandomTestCases(parameterList, parameterListSize, kTrials);
            TestAndNumOfCoveredReq highestCoverageTest = getHighestCoverageTest(kRandomTestCases, parameterListSize, requirementsArray, indexedValues);

            if (highestCoverageTest.getNumOfCoveredReq() > 0) {
                testSuite.add(highestCoverageTest.getTest());
                numberOfCoveredRequirements += highestCoverageTest.getNumOfCoveredReq();
                cumulativeNumOfCoveredReq.add((double) numberOfCoveredRequirements / totalNumberOfPairRequirements);

                // Update requirements array
                fillInRequirementsArray(requirementsArray, Collections.singletonList(highestCoverageTest.getTest()), parameterListSize, indexedValues);
            }
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
        List<Integer> xValues = IntStream.range(1, testSuite.size() + 1).boxed().collect(Collectors.toList());
        LineChart.createIDLineChart("Covered Requirements vs. Test Suite Size", "Random Greedy - " + kTrials + " Trials", xValues, cumulativeNumOfCoveredReq, "Test Suite Size", "Number of covered pair requirements");
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
}
