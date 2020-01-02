/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Assignment;

import java.sql.Timestamp;

/**
 *
 * @author sarahsyazwina
 */
public class Order {
    
    public int requestID;
    public int from;
    public int to;
    
    public long startTime;
    public long endTime;
    public long waitTime;
    public Timestamp requestTime;
    public Timestamp pickup;
    public Timestamp done;
    
    public boolean delivered;
    public boolean accepted;
    
    public Order(int id, int current, int destination){
        this.accepted = false;
        this.delivered = false;
        this.requestID = id;
        this.from = current;
        this.to = destination;
        startTime = System.nanoTime() / 1000000000;
        requestTime = new Timestamp(System.currentTimeMillis());
    }
    
    public void pickUp(){
        this.accepted = true;
        waitTime = (System.nanoTime() / 1000000000) - startTime;
        pickup = new Timestamp(System.currentTimeMillis());
    }
    
    public void dropOff(){
        this.delivered = true;
        endTime = (System.nanoTime() / 1000000000) - startTime;
        done = new Timestamp(System.currentTimeMillis());
    }
    
    @Override
    public String toString(){
        return "Request ID " + requestID + "\n" + "Order placed:\t" + requestTime + 
                "\n" + "Passenger collected:\t" + pickup + "\n" + "Wait time (s):\t" + 
                waitTime + "\n" + "Passenger dropped off:\t" +
                done + "\n" + "Total time elapsed (s):\t" + endTime + "\n";
    }
}
