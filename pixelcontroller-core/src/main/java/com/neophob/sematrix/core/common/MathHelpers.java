package com.neophob.sematrix.core.common;

/**
 * Contains math related helper functions.
 *
 */
public class MathHelpers
{
    /**
     * linearly interpolation of values between first and last.
     * Same as numpy.linspace() or eigen::LinSpaced()
     * @return numValue values between first and last. Including first, including last.
     */
    public static double[] linspace(final double first, final double last, final int numValues)
    {
        assert(numValues > 0);
        final double step = (last - first) / (numValues - 1);
        double[] values = new double[numValues];
        for(int i = 0; i < numValues; ++i)
        {
            values[i] =first + i * step;
        }
        return values;
    }
}
