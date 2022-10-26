package com.yl.github;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.yl.github.simplify.PointExtractor;
import com.yl.github.simplify.Simplify;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description:
 * @author: YL
 * @date: Created in 2022/8/18 16:14
 * @version: 1.0
 * @modified By:
 */
public class Main {
    private static Simplify<Position> simplify = new Simplify<>(new Position[0], new PointExtractor<Position>() {
        @Override
        public double getX(Position point) {
            return point.lat * PositionConst.XY_RATE;
        }
        @Override
        public double getY(Position point) {
            return point.lng * PositionConst.XY_RATE;
        }
    });

    public static void main(String[] args) throws IOException {
        //读取本地文件
        String posPath = "./1637573501000.pos";
        String content = Util.copyToString(posPath);
        //得到原始轨迹点
        List<Position> formatList = posDataFormat(content);
        assert formatList != null;
        //轨迹预处理
        List<Position> list = new ArrayList<>();
        Position last = formatList.get(0);
        for (Position position:formatList) {
            //速度有效
            if((position.speed != null && position.speed>5) || position==formatList.get(formatList.size()-1)){
                // 链式计算当前点
                // 计算理论速度m/s   100 米/秒=360 千米/时
                // 避免GPS点异常漂移导致里程计算结果过大
                double mile = Util.getDistance(position,last);
                double time = (position.time - last.time)/1000.0;
                if(time>0.0 && mile/time < 100){
                    position.countMile = last.countMile + mile;
                    position.countTime = last.countTime + (position.time - last.time);
                    position.maxSpeed = Math.max(position.speed==null?0:position.speed, last.maxSpeed);
                    last = position;
                    list.add(position);
                }
            }
        }
        //转换为数组方便后续简化点计算
        Position[] array = new Position[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }

        //简化点集
        Position[] simplifyList = simplify(array, 20);
        //轨迹数据
        List<Double[]> positions = new ArrayList<>(list.size());
        for(Position p : list){
            positions.add(new Double[]{p.lng,p.lat});
        }
        System.out.println("\n轨迹数据: ");
        System.out.println(JSON.toJSONString(positions));

        //简化数据
        List<Double[]> positionsS = new ArrayList<>(simplifyList.length);
        for(Position p : simplifyList){
            positionsS.add(new Double[]{p.lng,p.lat});
        }
        System.out.println("\n简化数据: ");
        System.out.println(JSON.toJSONString(positionsS));

        //弯道计算
        List<CurveData> curveDataList = AloneCurve.findCurve(simplifyList, list);
        //弯道数据
        System.out.println("\n弯道数据: ");
        System.out.println(JSON.toJSONString(curveDataList, SerializerFeature.DisableCircularReferenceDetect));
    }

    /**
     * createTime: 2020/11/11
     * author: yl
     * effect: 对定位文件进行数据筛选，并返回筛选后的总里程 和 场景合理的定位点
     * 之前dataFiltrate方法存在效率问题，此处重写
     */
    private static List<Position> posDataFormat(String str) {
        if (str == null) return null;
        List<Position> list = new LinkedList<>();
        //正则匹配
        Pattern regex = Pattern.compile("(-?\\d+\\.?\\d*,){5}\\d{13};");
        Matcher foundMatches = regex.matcher(str.trim());
        while (foundMatches.find()) {
            String result = foundMatches.group(0);
            String[] arr = result.split(",");
            Position position = new Position();
            position.lat = Double.parseDouble(arr[0]);
            position.lng = Double.parseDouble(arr[1]);
            position.direction = Double.parseDouble(arr[2]);
            position.height = Double.parseDouble(arr[3]);
            position.speed = Double.parseDouble(arr[4]);
            //去除末尾分号 ";"
            position.time = Long.parseLong(arr[5].substring(0,arr[5].length()-1));
            list.add(position);
        }
        return list;
    }

    /**
     * 使用算法简化轨迹点，保留关键拐点数据
     * @param list 轨迹点集
     * @param tolerance 舍弃阈值
     * @return 简化点集
     */
    private static Position[] simplify(Position[] list, double tolerance){
        Position[] simplifyPositions = simplify.simplify(list, tolerance, false);
        System.out.println("simplify  old: "+ list.length+"  new: "+ simplifyPositions.length);
        return simplifyPositions;
    }
}
