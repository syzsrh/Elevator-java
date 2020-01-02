/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Assignment;

import java.io.*;
import java.sql.Timestamp;
import java.util.*;
/**
 *
 * @author sarahsyazwina
 */
public class Elevator implements Runnable {
    
    private enum Direction {UP, DOWN, STATIONARY};
    private enum Action {IDLE, OFFLOADING, MOVING, GROUND};
    private static Direction direction;
    private static Action state;

    private static ArrayList<Order> pax;
    public static ArrayList<Order> list;
    private static ArrayList<Integer> q;
    private static PriorityQueue<Integer> upQ;
    private static PriorityQueue<Integer> downQ;
    
    private final long startTime;
    private static int requestID;
    private static int paxServed;
    private static int totalFloors;
    private static int currentFloor;
    private static boolean running;
    
    private static final int GROUND = 0;
    private static final int TOP = 10;
    private static final int CAPACITY = 10;
    
    public static StringBuilder log;
    public static StringBuilder report;
    public static StringBuilder orders;
    
    public Panel panel;
    
    public Elevator(){
        panel = new Panel();
        log = new StringBuilder();
        report = new StringBuilder();
        upQ = new PriorityQueue<>();
        downQ = new PriorityQueue<>(Collections.reverseOrder());
        pax = new ArrayList<>(CAPACITY);
        list = new ArrayList<>(); 
        q = new ArrayList<>();
        
        requestID = 0;
        currentFloor = 0;
        paxServed = 0;
        totalFloors = 0;
        startTime = System.nanoTime() / 1000000000;
        
        direction = Direction.STATIONARY;
        state = Action.IDLE;
        running = true;
        
        log.append("Elevator Log:\n");
        logger("Elevator ready for requests.", new Timestamp(System.currentTimeMillis()).toString());
        System.out.println(new Timestamp(System.currentTimeMillis()) + " | Elevator ready for requests.");
    }
    
    public long totalTime(){
        return (System.nanoTime() / 1000000000) - startTime;
    }
    
    public int totalFloors(){
        return totalFloors;
    }
    
    public int paxServed(){
        return paxServed;
    }
    
    public int requestID(){
        return requestID;
    }
    
    public void turnOff(){
        logger("Elevator is shutting down.", new Timestamp(System.currentTimeMillis()).toString());
        try {
            BufferedWriter l = new BufferedWriter(new FileWriter("Log File.txt"));
            l.write(log.toString());
            l.close();
            BufferedWriter r = new BufferedWriter(new FileWriter("Report File.txt"));
            report.append("##Elevator Statistics##\n");
            report.append("Service request processed:\t").append(requestID());
            report.append("\n");
            report.append("Passengers served:\t").append(paxServed());
            report.append("\n");
            report.append("Total floors travelled:\t").append(totalFloors());
            report.append("\n");
            report.append("Total time taken:\t").append(totalTime());
            report.append("\n");
            report.append(list.toString());
            r.write(report.toString());
            r.close();
        } catch (IOException ex) {
        }
        setStopRunning();
        System.exit(0);
    }
    
    private void setStopRunning(){
        running = false;
    }
    
    private void setMoving(){
        state = Action.MOVING;
    }
    
    private void setIdle(){
        q.clear();
        upQ.clear();
        downQ.clear();
        state = Action.IDLE;
    }
    
    private void setPause(){
        state = Action.OFFLOADING;
    }
    
    private boolean pause(){
        return state == Action.OFFLOADING;
    }
    
    private void setGround(){
        state = Action.GROUND;
    }
    
    private void setUp(){
        direction = Direction.UP;
    }
    
    private boolean goingUp(){
        return direction == Direction.UP;
    }
    
    private void setDown(){
        direction = Direction.DOWN;
    }
    
    private boolean goingGround(){
        return state == Action.GROUND;
    }
    
    private boolean goingDown(){
        return direction == Direction.DOWN;
    }
    
    private void setStation(){
        direction = Direction.STATIONARY;
    }
    
    private boolean goingNowhere(){
        return direction == Direction.STATIONARY || state == Action.IDLE;
    }
    
    private void dequeue(){
        q.remove(0);
    }
    
    private Action getAction(){
        return state;
    }
    
    private void setFloor(int floor){
        if(getFloor() <= TOP){
            currentFloor = floor;
            totalFloors += 1;
            panel.levelDisp(printFloor());
        }
    }
    
    public int getFloor(){
        return currentFloor;
    }
    
    private String printFloor(){
        switch(currentFloor){
            case 0:
                return "G";
            default:
                return Integer.toString(currentFloor);
        }
    }
    
    private String printFloor(int floor){
        switch(floor){
            case 0:
                return "G";
            default:
                return Integer.toString(floor);
        }
    }
    
    private boolean hasQueue(){
        return q.size() > 0;
    }
    
    private boolean isCallUp(int floor){
        return floor > getFloor();
    }
    
    private boolean isCallDown(int floor){
        return floor < getFloor();
    }
    
    @Override
    public void run() {
        while(running){
            switch(getAction()){
                case IDLE:  synchronized(this){
                    boolean called = false;
                    if(!hasQueue()){
                        try {
                            System.out.println(new Timestamp(System.currentTimeMillis()) + " | Elevator waiting on Level " + printFloor() + " for requests.");
                            logger("Elevator waiting on Level " + printFloor() + " for requests.", new Timestamp(System.currentTimeMillis()).toString());
                            wait(5000);
                        } catch (IllegalMonitorStateException ex) {
                            System.out.println("Waiting ...");
                        } catch (InterruptedException ex) {
                            System.out.println("Interrupted ...");
                            called = true;
                            setMoving();
                        }

                        if(!called && getFloor() != GROUND && !hasQueue()){
                            setDown();
                            setMoving();
                            setGround();
                        }
                        else{
                            setMoving();
                        }
                    }
                    else{
                        setMoving();
                    }
                    }
                    break;
                
                case OFFLOADING:
                    System.out.println(new Timestamp(System.currentTimeMillis()) + " | Elevator is stopping at Level " + printFloor() + ".");
                    logger("Elevator is stopping at Level " + printFloor() + ".", new Timestamp(System.currentTimeMillis()).toString());
                    door();
                    dequeue();
                    setMoving();
                    break;
                    
                case MOVING:
                    if(hasQueue()){
                        if(getFloor() == q.get(0)){
                            System.out.println(new Timestamp(System.currentTimeMillis()) + " | Elevator reached Level " + printFloor() + ".");
                            logger("Elevator reached Level " + printFloor() + ".", new Timestamp(System.currentTimeMillis()).toString());
                            setPause();
                        }
                        else if(direction == Direction.UP){
                            if(getFloor() == TOP){
                                setDown();
                                setMoving();
                            }
                            else{
                                System.out.println(new Timestamp(System.currentTimeMillis()) + " | Elevator passing Level " + printFloor() + ".");
                                logger("Elevator is passing Level " + printFloor() + ".", new Timestamp(System.currentTimeMillis()).toString());
                                sleep(1);
                                setFloor(getFloor() + 1);
                            }
                        }
                        else if(direction == Direction.DOWN){
                            if(q.get(0) > getFloor()){
                                setUp();
                            }
                            else{
                                System.out.println(new Timestamp(System.currentTimeMillis()) + " | Elevator passing Level " + printFloor() + ".");
                                logger("Elevator passing Level " + printFloor() + ".", new Timestamp(System.currentTimeMillis()).toString());
                                sleep(1);
                                setFloor(getFloor() - 1);
                            }
                        }
                    }
                    else if(!pax.isEmpty()){
                        sort();
                    }
                    else{
                        System.out.println(new Timestamp(System.currentTimeMillis()) + " | Elevator is idle.");
                        logger("Elevator is idle.", new Timestamp(System.currentTimeMillis()).toString());
                        setIdle();
                    }
                    break;
                    
                case GROUND:
                    if(getFloor() - 1 >= 0){
                        sleep(1);
                        setFloor(getFloor() - 1);
                        System.out.println(new Timestamp(System.currentTimeMillis()) + " | Elevator going to Level " + printFloor() + ".");
                        logger("Elevator going to Level " + printFloor() + ".", new Timestamp(System.currentTimeMillis()).toString());
                    }
                    if(!pax.isEmpty()){
                        sort();
                    }
                    else{
                        System.out.println(new Timestamp(System.currentTimeMillis()) + " | Elevator reached Level " + printFloor() + ".");
                        logger("Elevator reached Level " + printFloor() + ".", new Timestamp(System.currentTimeMillis()).toString());
                        setIdle();
                        setStation();
                    }
                    break;
            }
        }
    }
    
    public void queue(int from, int to){
        synchronized(this){
            boolean fit = pax.size() <= CAPACITY || pax.isEmpty();
         
            if(fit){
                
                System.out.println(new Timestamp(System.currentTimeMillis()) + " | Request from Level " + printFloor(from) + " to Level " + printFloor(to));
                logger("Request from Level " + printFloor(from) + " to Level " + printFloor(to), new Timestamp(System.currentTimeMillis()).toString());
                requestID++;
                Order order = new Order(requestID, from, to);
                list.add(order);
                
                if(from == getFloor() && !pause()){
                    setPause();
                    if(!q.isEmpty())
                        q.add(from);
                    else
                        q.add(0, from);
                }
                else{
                    if(goingNowhere() || goingGround() || !hasQueue()){
                        if(isCallUp(from)){
                            setUp();
                            setMoving();
                            if(!q.contains(from))
                                q.add(from);
                        }
                        else if (!isCallUp(from)){
                            setDown();
                            setMoving();
                            if(!q.contains(from))
                                q.add(from);
                        }
                    }
                    if((!goingDown() && isCallUp(from))){
                        setMoving();
                        setUp();
                        if(!q.contains(from))
                            q.add(from);
                    }
                    if(isCallDown(from) && !goingUp()){
                        setMoving();
                        setDown();
                        if(!q.contains(from))
                            q.add(from);
                    }
                    if((goingUp() && isCallUp(from)) || (goingDown() && isCallDown(from))){
                        if(!q.contains(from))
                            q.add(from);
                    }
                    if(isCallDown(from) && goingUp() || !goingDown()){
                        if(!downQ.contains(from))
                            downQ.offer(from);
                    }
                    if(isCallUp(from) && goingDown() || !goingUp()){
                        if(!upQ.contains(from))
                            upQ.offer(from);
                    }
                    if(pause()){
                        if(isCallUp(from))
                            if(!upQ.contains(from))
                                upQ.offer(from);
                        else if(isCallDown(from))
                            if(!downQ.contains(from))
                                downQ.offer(from);
                    }
                    
                }
            sort();
            notifyAll();
            }
        }
    }
    
    private void door(){
        synchronized(this){
            if(!q.isEmpty()){
            if(q.get(0) == getFloor()){
                panel.playSound("ding.wav");
                System.out.println(new Timestamp(System.currentTimeMillis()) + " | Doors are opening ...");
                logger("Doors are opening ...", new Timestamp(System.currentTimeMillis()).toString());
                panel.doorDisp("OPENING");
                sleep(5);
                logger("Doors are open.", new Timestamp(System.currentTimeMillis()).toString());
                panel.doorDisp("OPEN");
                sleep(2);
                logger(leave() + " passenger(s) leaving ... " + board() + " passenger(s) boarding ...", new Timestamp(System.currentTimeMillis()).toString());
                sort();
                sleep(2);
                panel.playSound("ding.wav");
                System.out.println(new Timestamp(System.currentTimeMillis()) + " | Doors are closing ...");
                logger("Doors are closing ...", new Timestamp(System.currentTimeMillis()).toString());
                panel.doorDisp("CLOSING");
                sleep(5);
                System.out.println(new Timestamp(System.currentTimeMillis()) + " | Doors are closed.");
                logger("Doors are closed", new Timestamp(System.currentTimeMillis()).toString());
                panel.doorDisp("CLOSED");
            }
        }
        }
    }
    
    private int leave(){
        int count = 0;
        ArrayList<Integer> temp = new ArrayList<>();
        for (int i = 0; i < pax.size(); i++) {
            if(pax.get(i).to == getFloor()){
                pax.get(i).dropOff();
                paxServed += 1;
                ++count;
                temp.add(i);
            }
        }
        for (int i = temp.size() - 1; i >= 0; i--) {
            int r = temp.get(i);
            pax.remove(r);
        }
        return count;
    }
    
    private int board(){
        int count = 0;
        for (int i = 0; i < list.size(); i++) {
            if(pax.size() < CAPACITY){
                if(!list.get(i).accepted && list.get(i).from == getFloor()){
                pax.add(list.get(i));
                list.get(i).pickUp();
                count++;
            }
            }
        }
        return count;
    }
    
    private void sort(){
        if(getFloor() == TOP)
            setDown();
        if(pax.size() == 1){
            if(pax.get(0).to < getFloor())
                setDown();
            else
                setUp();
        }
        switch(direction){
            case UP:
                if(!pax.isEmpty() || hasQueue()){
                    for (int i = 0; i < pax.size(); i++) {
                        if(pax.get(i).to > getFloor() && !q.contains(pax.get(i).to)){
                            q.add(pax.get(i).to);
                        }
                        else{
                            if(!downQ.contains(pax.get(i).to) && !q.contains(pax.get(i).to))
                                downQ.offer(pax.get(i).to);
                        }
                    }
                }
                while(!upQ.isEmpty()){
                    if(upQ.peek() > getFloor() && !q.contains(upQ.peek())){
                        q.add(upQ.poll());
                    }
                    else{
                        if(!downQ.contains(upQ.peek()) && !q.contains(upQ.peek()))
                            downQ.offer(upQ.poll());
                        else
                            upQ.poll();
                    }
                }
                Collections.sort(q);
                setMoving();
                setUp();
                break;
                
            case DOWN:
                System.out.println("DOWN");
                if(!pax.isEmpty() || hasQueue()){
                    for (int i = 0; i < pax.size(); i++) {
                        if(pax.get(i).to < getFloor() && !q.contains(pax.get(i).to)){
                            q.add(pax.get(i).to);
                        }
                        else{
                            if(!upQ.contains(pax.get(i).to) && !q.contains(pax.get(i).to))
                                upQ.offer(pax.get(i).to);
                        }
                    }
                }
                while(!downQ.isEmpty()){
                    if(downQ.peek() > getFloor() && !q.contains(downQ.peek())){
                        q.add(downQ.poll());
                    }
                    else{
                        if(!upQ.contains(downQ.peek()) && !q.contains(downQ.peek()))
                            upQ.offer(downQ.poll());
                        else
                            downQ.poll();
                    }
                }
                Collections.sort(q);
                Collections.reverse(q);
                setMoving();
                setDown();
                break;
                
            case STATIONARY:
                System.out.println("STATION");
                if(!pax.isEmpty()){
                    for (int i = 0; i < pax.size(); i++) {
                        q.add(pax.get(i).to);
                    }
                    Collections.sort(q);
                    setMoving();
                    if(!(q.get(0) >= getFloor())){
                        setDown();
                    }
                    else{
                        setUp();
                    }
                }
                break;
        }
    }
    
    private void logger(String s, String time){
        log.append(time);
        log.append("\t| \t");
        log.append(s);
        log.append("\n");
    }
    
    private void sleep(int milli){
        int s = 1000 * milli;
        try {
            Thread.sleep(s);
        } catch (InterruptedException ex) {
            System.out.println("InterruptedException");
        }
    }
}
