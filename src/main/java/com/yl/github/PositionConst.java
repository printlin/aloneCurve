package com.yl.github;

/**
 * @description: 轨迹计算相关常量
 * @author: YL
 * @date: Created in 2020/12/29 11:23
 * @version: 1.0
 * @modified By:
 */
public class PositionConst {
    public static final int XY_RATE = 100000; //经纬度精度扩大比例
    public static final int CURVE_ANGLE_MIN = 2;   //弯道最小角度
    public static final int CURVE_ANGLE_MAX = 150; //弯道最大角度
    public static final int CURVE_DISTANCE_MAX = 100; //弯道合并最大距离（米）
}
