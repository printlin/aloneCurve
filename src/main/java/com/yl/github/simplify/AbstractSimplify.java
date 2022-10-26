package com.yl.github.simplify;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * @description:
 * @author: YL
 * @date: Created in 2020/12/24 11:10
 * @version: 1.0
 * @modified By:
 */
abstract class AbstractSimplify<T> {

    private T[] sampleArray;

    protected AbstractSimplify(T[] sampleArray) {
        this.sampleArray = sampleArray;
    }

    /**
     * Simplifies a list of points to a shorter list of points.
     * @param points original list of points
     * @param tolerance tolerance in the same measurement as the point coordinates
     * @param highestQuality <tt>true</tt> for using Douglas-Peucker only,
     *                       <tt>false</tt> for using Radial-Distance algorithm before
     *                       applying Douglas-Peucker (should be a bit faster)
     * @return simplified list of points
     */
    public T[] simplify(T[] points,
                        double tolerance,
                        boolean highestQuality) {

        if (points == null || points.length <= 2) {
            return points;
        }

        double sqTolerance = tolerance * tolerance;

        if (!highestQuality) {
            points = simplifyRadialDistance(points, sqTolerance);
        }

        points = simplifyDouglasPeucker(points, sqTolerance);

        return points;
    }

    T[] simplifyRadialDistance(T[] points, double sqTolerance) {
        T point = null;
        T prevPoint = points[0];

        List<T> newPoints = new ArrayList<T>();
        newPoints.add(prevPoint);

        for (int i = 1; i < points.length; ++i) {
            point = points[i];

            if (getSquareDistance(point, prevPoint) > sqTolerance) {
                newPoints.add(point);
                prevPoint = point;
            }
        }

        if (prevPoint != point) {
            newPoints.add(point);
        }

        return newPoints.toArray(sampleArray);
    }

    private static class Range {
        private Range(int first, int last) {
            this.first = first;
            this.last = last;
        }

        int first;
        int last;
    }

    /**
     * Ramer–Douglas–Peucker算法
     * https://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm
     * 该算法以递归方式划分线。最初，它给出了第一个点和最后一个点之间的所有点。它会自动标记要保留的第一个和最后一个点。然后找到以第一和最后一个点为终点的距线段最远的点。该点显然在曲线上离端点之间的近似线段最远。如果该点比ε靠近线段，则可以丢弃当前未标记为要保留的任何点，而简化曲线的质量不会比ε差。
     * 如果距离线段最远的点大于近似值的ε，则必须保留该点。该算法以第一个点和最远的点递归调用自身，然后以最远的点和最后一个点进行递归调用，最后一个点包括被标记为保留的最远点。
     * 递归完成后，可以生成一条新的输出曲线，该曲线包括所有标记点和仅那些标记为保留的点。
     */
    T[] simplifyDouglasPeucker(T[] points, double sqTolerance) {

        BitSet bitSet = new BitSet(points.length);
        bitSet.set(0);
        bitSet.set(points.length - 1);

        List<Range> stack = new ArrayList<Range>();
        stack.add(new Range(0, points.length - 1));

        while (!stack.isEmpty()) {
            Range range = stack.remove(stack.size() - 1);

            int index = -1;
            double maxSqDist = 0f;

            // find index of point with maximum square distance from first and last point
            for (int i = range.first + 1; i < range.last; ++i) {
                double sqDist = getSquareSegmentDistance(points[i], points[range.first], points[range.last]);

                if (sqDist > maxSqDist) {
                    index = i;
                    maxSqDist = sqDist;
                }
            }

            if (maxSqDist > sqTolerance) {
                bitSet.set(index);

                stack.add(new Range(range.first, index));
                stack.add(new Range(index, range.last));
            }
        }

        List<T> newPoints = new ArrayList<T>(bitSet.cardinality());
        for (int index = bitSet.nextSetBit(0); index >= 0; index = bitSet.nextSetBit(index + 1)) {
            newPoints.add(points[index]);
        }

        return newPoints.toArray(sampleArray);
    }


    public abstract double getSquareDistance(T p1, T p2);

    public abstract double getSquareSegmentDistance(T p0, T p1, T p2);
}
