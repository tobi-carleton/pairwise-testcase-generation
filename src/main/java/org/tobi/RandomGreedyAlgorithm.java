package org.tobi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomGreedyAlgorithm extends PairWiseTestBase {

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        List<Parameter> parameterList = getParameterList();
        int totalNumberOfPairRequirements = getTotalNumberOfPairRequirements(parameterList);
        int numberOfCoveredRequirements = 0;
        int numOfIterations = 0;
        int parameterListSize = parameterList.size();
        List<IndexedValue> indexedValues = createIndexedValuesList(parameterList);
        int[][] requirementsArray = createRequirementsArray(indexedValues);
        List<String[]> testSuite = new ArrayList<>();

        // Will be used for plotting incremental increase of pairwise coverage later on
        List<Integer> numOfCoveredReqs = new ArrayList<>();

        int kTrials = 3;
        while (numOfIterations < 1000 && numberOfCoveredRequirements < totalNumberOfPairRequirements) {
            List<String[]> kRandomTestCases = createKRandomTestCases(parameterList, parameterListSize, kTrials);
            TestAndNumOfCoveredReq highestCoverageTest = getHighestCoverageTest(kRandomTestCases, parameterListSize, requirementsArray, indexedValues);

            if (highestCoverageTest.getNumOfCoveredReq() > 0) {
                testSuite.add(highestCoverageTest.getTest());
                numOfCoveredReqs.add(highestCoverageTest.getNumOfCoveredReq());
                numberOfCoveredRequirements += highestCoverageTest.getNumOfCoveredReq();

                // Update requirements array
                fillInRequirementsArray(requirementsArray, Collections.singletonList(highestCoverageTest.getTest()), parameterListSize, indexedValues);
            }
            numOfIterations++;
        }
        printTestSuite(testSuite);
        System.out.println((System.currentTimeMillis() - startTime) / 1000.0);
        System.out.println("Number of iterations = " + numOfIterations);
        System.out.println("Percentage of covered requirements = " + ((double) numberOfCoveredRequirements / totalNumberOfPairRequirements) * 100 + "%");
        System.out.println("Test Suite Size = " + testSuite.size());
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

    private static int numberOfNewlyCoveredRequirements(int[][] requirementsArray, String[] test, int noOfParameters, List<IndexedValue> indexedValues) {
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
                coveredRequirements.add(pairRequirement);
                parameterIndexes.add(parameterIndex);
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
