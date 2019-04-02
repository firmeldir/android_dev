package com.example.test_view;

public class LinearInterpolation {


    public static double[] interpolateArray(double[] source, int destinationLength){
        double[] destination = new double[destinationLength];

        destination[0] = source[0];
        int jPrevious = 0;
        for (int i = 1; i < source.length; i++)
        {
            int j = i * (destination.length - 1)/(source.length - 1);
            interpolate(destination, jPrevious, j, source[i - 1], source[i]);

            jPrevious = j;
        }
        return destination;
    }

    private static void interpolate(double[] destination, int destFrom, int destTo, double valueFrom, double valueTo)
    {
        int destLength = destTo - destFrom;
        double valueLength = valueTo - valueFrom;
        for (int i = 0; i <= destLength; i++)
            destination[destFrom + i] = valueFrom + (valueLength * i)/destLength;
    }
}