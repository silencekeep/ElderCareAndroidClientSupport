package com.silencekeep.eldercarewebview;

import android.util.Log;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class AcceleatorProc {
    public class AccVec3d{
        public float X;
        public float Y;

        public AccVec3d(float x, float y, float z) {
            X = x;
            Y = y;
            Z = z;
        }

        public float Z;
    }
    private int queueMaxCount;
    private int phDistCounter = 0;
    private BlockingQueue<AcceleatorProc.AccVec3d> queue;
    private BlockingQueue<Double> judgeQueue;
    public AcceleatorProc(int queueMaxCount){
        this.queueMaxCount = queueMaxCount;
        queue = new ArrayBlockingQueue<AcceleatorProc.AccVec3d>(queueMaxCount, true);
        judgeQueue = new ArrayBlockingQueue<>(queueMaxCount, true);
    }
    public void pushAcceleatorData(AccVec3d data){
        try {
            if(queue.size() < queueMaxCount)
                queue.put(data);
            else{
                queue.take();
                queue.put(data);
            }
        } catch (InterruptedException e) {
            //throw new RuntimeException(e);
        }
    }
    public void lazyEvaluateFalldown(){
        double BOUND = 30.0f;

        float modLabelMin = Float.MAX_VALUE, modLabelMax = 0;
        AccVec3d minVal = new AccVec3d(0,0,0);
        AccVec3d maxVal = new AccVec3d(0,0,0);
        int minTimeLine = 0, maxTimeLine = 0;
        if(queue.size() < queueMaxCount) return;
        Iterator<AccVec3d> iterator = queue.iterator();
        int timeLine = 0;
        while (iterator.hasNext()) {
            timeLine++;
            AccVec3d element = iterator.next();
            float acceleration = (float) Math.sqrt(element.X * element.X + element.Y * element.Y + element.Z * element.Z);
            if(acceleration < modLabelMin){
                modLabelMin = acceleration;
                minTimeLine = timeLine;
                minVal = element;
            }
            else if(acceleration > modLabelMax){
                modLabelMax = acceleration;
                maxTimeLine = timeLine;
                maxVal = element;
            }
        }
        double u = Math.sqrt((minVal.X - maxVal.X) * (minVal.X - maxVal.X) + (minVal.Y - maxVal.Y) * (minVal.Y - maxVal.Y) + (minVal.Z - maxVal.Z) * (minVal.Z - maxVal.Z));
        //Log.d("Acceleration: ", String.format("%2.2f m/s^2    dist:%d",u,maxTimeLine - minTimeLine));
        try {
            if(judgeQueue.size() < queueMaxCount)
                judgeQueue.put(u);
            else{
                judgeQueue.take();
                judgeQueue.put(u);
            }
        } catch (InterruptedException e) {
            //throw new RuntimeException(e);
        }
    }
    public BlockingQueue<Double> getRecentData(){
        return judgeQueue;
    }
}
