package com.yl.github.simplify;

/**
 * @description:
 * @author: YL
 * @date: Created in 2020/12/24 11:10
 * @version: 1.0
 * @modified By:
 */

public class Simplify<T> extends AbstractSimplify<T> {

    private final PointExtractor<T> pointExtractor;

    public Simplify(T[] sampleArray, PointExtractor<T> pointExtractor) {
        super(sampleArray);
        this.pointExtractor = pointExtractor;
    }

    /**
     * 两点之间的平方距离
     */
    @Override
    public double getSquareDistance(T p1, T p2) {

        double dx = pointExtractor.getX(p1) - pointExtractor.getX(p2);
        double dy = pointExtractor.getY(p1) - pointExtractor.getY(p2);

        return dx * dx + dy * dy;
    }

    /**
     * 弓形中间点与斜边的直线距离
     */
    @Override
    public double getSquareSegmentDistance(T p0, T p1, T p2) {
        double x0, y0, x1, y1, x2, y2, dx, dy, t;

        x1 = pointExtractor.getX(p1);
        y1 = pointExtractor.getY(p1);
        x2 = pointExtractor.getX(p2);
        y2 = pointExtractor.getY(p2);
        x0 = pointExtractor.getX(p0);
        y0 = pointExtractor.getY(p0);

        dx = x2 - x1;
        dy = y2 - y1;

        if (dx != 0.0d || dy != 0.0d) {
            t = ((x0 - x1) * dx + (y0 - y1) * dy)
                    / (dx * dx + dy * dy);

            if (t > 1.0d) {
                x1 = x2;
                y1 = y2;
            } else if (t > 0.0d) {
                x1 += dx * t;
                y1 += dy * t;
            }
        }

        dx = x0 - x1;
        dy = y0 - y1;

        return dx * dx + dy * dy;
    }
}
