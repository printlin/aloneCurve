package com.yl.github;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @description: 计算弯道数据
 * @author: YL
 * @date: Created in 2022/8/18 10:50
 * @version: 1.0
 * @modified By:
 */
public class AloneCurve {
    public static List<CurveData> findCurve(Position[] simplifyList, List<Position> list){
        List<CurveData> curveList = findAngle(simplifyList);
        return filterCurveData(getCurveData(curveList, list));
    }

    /**
     * 查找弯道中心点
     */
    private static List<CurveData> findAngle(Position[] simplifyList){
        List<CurveData> list = new ArrayList<>();
        for (int i = 0; i < simplifyList.length-2; i++) {
            //滑动窗口：每次取三个点计算夹角
            int angle = getAngle(simplifyList[i], simplifyList[i+1], simplifyList[i+2]);
            //角度满足条件则认为是弯道
            if(angle>PositionConst.CURVE_ANGLE_MIN && angle<PositionConst.CURVE_ANGLE_MAX){
                CurveData curveData = new CurveData();
                curveData.angle = angle;
                curveData.center = simplifyList[i+1];
                list.add(curveData);
            }
        }
        return list;
    }

    /**
     * 计算弯道数据
     * 获得速度、时间、出入弯坐标等信息
     */
    private static List<CurveData> getCurveData(List<CurveData> curveList, List<Position> list){
        int listSize = list.size();
        int curveListSize = curveList.size();
        //结果列表
        List<CurveData> curveDataList = new ArrayList<>();

        if(listSize <= 0 || curveListSize <= 0){
            return curveDataList;
        }

        Position before, after;
        for (CurveData curveData : curveList) {
            Position curr = curveData.center;
            //查找弯心点在原始数据中的位置
            int index = list.indexOf(curr);
            //排除开始和结尾的数据点
            if (index > 10 && index < listSize - 10) {
                //向前递推5秒
                int indexTemp = index - 1;
                before = list.get(indexTemp);
                double maxSpeed = before.speed;
                while (indexTemp > 0 && curr.time - before.time < 5000) {
                    indexTemp = indexTemp - 1;
                    before = list.get(indexTemp);
                    maxSpeed = (maxSpeed < before.speed) ? before.speed : maxSpeed;
                }

                //向后递推5秒
                indexTemp = index + 1;
                after = list.get(indexTemp);
                maxSpeed = (maxSpeed < after.speed) ? after.speed : maxSpeed;
                while (indexTemp < listSize - 1 && after.time - curr.time < 5000) {
                    indexTemp = indexTemp + 1;
                    after = list.get(indexTemp);
                    maxSpeed = (maxSpeed < after.speed) ? after.speed : maxSpeed;
                }

                //数据更新
                curveData.before = before;
                curveData.center = curr;
                curveData.after = after;
                curveData.maxSpeed = maxSpeed;
                curveData.countMile = after.countMile - before.countMile;
                curveData.countTime = after.countTime - before.countTime;
                curveData.speed = (curveData.countMile / (curveData.countTime / 1000.0)) * 3.6;
                curveDataList.add(curveData);
            }
        }
        return curveDataList;
    }

    /**
     * 过滤弯道数据
     * 整个多个弯道数据到一个弯道
     */
    private static List<CurveData> filterCurveData(List<CurveData> curveDataList){
        int curveListSize = curveDataList.size();
        //同一弯道数据暂存
        List<CurveData> sameCurveData = new ArrayList<>();
        //结果列表
        List<CurveData> filterList = new LinkedList<>();

        if(curveListSize <= 0){
            return filterList;
        }

        //初始化第一个弯道的方向数据
        boolean directionTemp = getDirection(curveDataList.get(0));
        sameCurveData.add(curveDataList.get(0));
        for (int i = 1; i < curveListSize; i++) {
            //获取弯道方向
            boolean direction = getDirection(curveDataList.get(i));
            //获取与上一个弯心点的距离
            double distance = Util.getDistance(curveDataList.get(i).center, curveDataList.get(i-1).center);
            //判断是否不是同一弯道：即方向不一致 且 间隔距离不满足条件
            if(directionTemp != direction || distance > PositionConst.CURVE_DISTANCE_MAX){
                //不是同一弯道则保存暂存列表中累积的弯道数据
                CurveData curveData = createBySameCurveData(sameCurveData);
                if(curveData != null){
                    filterList.add(curveData);
                }
                //清空弯道缓存
                sameCurveData.clear();
            }
            //更新暂存数据
            directionTemp = direction;
            sameCurveData.add(curveDataList.get(i));
        }
        //避免最后一个弯道丢失
        if(sameCurveData.size() > 0){
            CurveData curveData = createBySameCurveData(sameCurveData);
            if(curveData != null){
                filterList.add(curveData);
            }
        }
        return filterList;
    }

    /**
     * 将相同弯道数据合并
     * @param sameCurveData 弯道列表
     * @return 弯道
     */
    private static CurveData createBySameCurveData(List<CurveData> sameCurveData){
        CurveData curveData = new CurveData();
        curveData.maxSpeed = 0;
        curveData.speed = 0;
        //计算暂存列表中统计数据
        sameCurveData.forEach(it->{
            curveData.maxSpeed = Math.max(curveData.maxSpeed, it.maxSpeed);
            curveData.speed += it.speed;
        });
        curveData.speed = curveData.speed / sameCurveData.size();
        //第一个弯的起始点
        curveData.before = sameCurveData.get(0).before;
        //中间弯的中间点
        curveData.center = sameCurveData.get(sameCurveData.size()/2).center;
        //最后一个弯的结尾点
        curveData.after = sameCurveData.get(sameCurveData.size()-1).after;
        //默认第一个弯的角度
        curveData.angle = sameCurveData.get(0).angle;
        if(sameCurveData.size() > 1){
            //只有两个弯道时，取弯心中间点，避免弯心偏向某一边
            if(sameCurveData.size() == 2){
                Position position = pointsCenter(sameCurveData.get(0).center, sameCurveData.get(1).center);
                curveData.center.lat = position.lat;
                curveData.center.lng = position.lng;
            }
            //有多个弯时重新计算角度
            curveData.angle = getAngle(sameCurveData.get(0).before, sameCurveData.get(0).center,
                    sameCurveData.get(sameCurveData.size()-1).center, sameCurveData.get(sameCurveData.size()-1).after);
            return curveData;
        }else if(curveData.angle < PositionConst.CURVE_ANGLE_MAX){
            //只有一个弯时如果角度满足才加入结果集
            return curveData;
        }
        return null;
    }

    private static boolean getDirection(CurveData curveData){
        return getDirection(curveData.before, curveData.center, curveData.after);
    }

    /**
     * 获取弯道弯心朝向，同一个弯道朝向应该相同 - 正负
     */
    private static boolean getDirection(Position a, Position b, Position c){
        //根据直线方程计算x对应的y
        //lat对应x lng对应y
        double y = (c.lat - a.lat)/(b.lat - a.lat)*(b.lng - a.lng)+a.lng;
        //以x轴判断方向，判断y处于直线的上方还是下方
        if(b.lat > a.lat){
            return c.lng > y;
        }else{
            return c.lng <= y;
        }
    }

    /**
     * 三点获取夹角
     */
    private static int getAngle(Position a, Position b, Position c){
        return getAngle(a, b, b, c);
    }

    /**
     * 获取两向量的夹角
     */
    private static int getAngle(Position a, Position a1, Position b, Position b1){
        return Math.round(
                (float) (
                        Math.acos(
                                ((b1.lat-b.lat)*(a.lat-a1.lat) + (b1.lng-b.lng)*(a.lng-a1.lng))
                                /
                                (getDistanceMath(a, a1) * getDistanceMath(b, b1))
                        ) / 0.0174533
                )
        );
    }
    /**
     * 数学方法计算两点间距离 - 经纬度当成普通数值
     */
    private static double getDistanceMath(Position a, Position b){
        return Math.sqrt((a.lat-b.lat)*(a.lat-b.lat) + (a.lng-b.lng)*(a.lng-b.lng));
    }

    /**
     * 获得两点之间的中间点
     * @param begin 起始点
     * @param end 终止点
     * @return 中间点
     */
    private static Position pointsCenter(Position begin, Position end) {
        Position[] positions = {begin, end};
        double X = 0, Y = 0, Z = 0;
        for (Position g : positions) {
            double lat = g.lat * Math.PI / 180;
            double lon = g.lng * Math.PI / 180;
            X += Math.cos(lat) * Math.cos(lon);
            Y += Math.cos(lat) * Math.sin(lon);
            Z += Math.sin(lat);
        }
        X = X / 2;
        Y = Y / 2;
        Z = Z / 2;
        double Lon = Math.atan2(Y, X);
        double Hyp = Math.sqrt(X * X + Y * Y);
        double Lat = Math.atan2(Z, Hyp);
        return new Position(Lat * 180 / Math.PI, Lon * 180 / Math.PI);
    }
}
