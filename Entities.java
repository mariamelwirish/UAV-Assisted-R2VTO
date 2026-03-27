import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

// Entities: Vehicle, Task, RSU.

// MEETING:
// 1. In Bulk Release, how do I decide if the task HoL, or queue.

// Vehicle
class Vehicle {
    static int count = 1;
    final int id ;
    double arrivalTime;
    double speed;
    double residenceTime; // = Road.roadLength / speed
    double departureTime; // = arrivalTime + residenceTime
    double processingCapacity; // (inst/s)
    boolean isCooperative; 
    double remainingProcessingCapacity; // = processingCapacity * residenceTime (inst)

    Vehicle(double arrivalTime, double processingCapacity, boolean isCooperative) {
        this.id = count++;
        this.arrivalTime = arrivalTime;
        this.speed = generateSpeed();
        this.processingCapacity = processingCapacity;
        this.residenceTime = RSU.coverageRange / speed;
        this.departureTime = arrivalTime + residenceTime;
        this.isCooperative = isCooperative; // MEETING: ARE WE THE ONES DOING THE PROBABILITY? OR IS THERE A GIVEN ONE? -> Input paramater, not generated.
        this.remainingProcessingCapacity = processingCapacity * residenceTime;

        System.out.println(String.format("MC = %10.5f: #Vehicle %d%s, processing capacity = %.1e, speed = %.5f, residence time = %f, departure time = %.5f", 
            this.arrivalTime, this.id, (isCooperative ? " cooperative" : " non-cooperative"), 
            this.processingCapacity, this.speed, this.residenceTime, this.departureTime));
    }
    
    double generateSpeed() {
        double speed;
        do {
            speed = Simulator.avgVehicleSpeed + Simulator.stdVehicleSpeed * Simulator.rng.nextGaussian();
        } while (speed < Simulator.minVehicleSpeed || speed > Simulator.maxVehicleSpeed);
        return speed;
    }

    boolean canProcessTask(Task task) {
        return task.size <= remainingProcessingCapacity;
        // return task.size / processingCapacity <= residenceTime;
    }
}

// Task 
// BR = no service time calculated (or consider the tasks in the bulk = service time in HoL).
class Task {
    enum Status { // MEETING: ARE THESE ENOUGH? 
        QUEUED, HoL
    }

    static int count = 1;
    final int id;
    int vehicleId;
    double arrivalTime;
    double HoLTime;
    double releaseTime;
    double deadline;
    double queueTime; // = HoLTime - arrivalTime -> MEETING: DO WE INCLUDE SERVICE OR NOT?
    double serviceTime; // = releaseTime - HoLTime
    double expiryTime; // = arrivalTime + deadline
    long size; 
    boolean isExpired; 
    Status status;

    Task(double arrivalTime, long size, double deadline) {
        this.id = count++;
        this.vehicleId = -1;
        this.arrivalTime = arrivalTime;
        this.size = size;
        this.deadline = deadline;
        this.expiryTime = arrivalTime + deadline;
        this.status = Status.QUEUED;
        this.isExpired = false;

        System.out.println(String.format("MC = %10.5f: #Task %d size = %d, deadline = %.5f, expiry time = %.5f", 
            this.arrivalTime, this.id, this.size, this.deadline, this.expiryTime));
    }
}

// RSU
class RSU {
    enum SchedulingPolicy { FCFS, EDF, STF } 

    int bufferCapacity;
    static double coverageRange;
    final Queue<Task> buffer;
    SchedulingPolicy policy;
    boolean isBulkRelease; 

    RSU(int bufferCapacity, double coverageRange, SchedulingPolicy policy, boolean isBulkRelease) {
        this.bufferCapacity = bufferCapacity;
        this.coverageRange = coverageRange;
        this.policy = policy;
        this.isBulkRelease = isBulkRelease;
        switch(policy) {
            case FCFS:
                this.buffer = new ArrayDeque<>();
                break;
            case EDF:
                this.buffer = new PriorityQueue<>(Comparator.comparingDouble(t -> t.expiryTime));
                break;
            case STF:
                this.buffer = new PriorityQueue<>(Comparator.comparingDouble(t -> t.size));
                break;
            default:
                throw new IllegalArgumentException("Invalid scheduling policy");
        }
    }

    boolean isBufferFull() {
        return buffer.size() >= bufferCapacity;
    }

    boolean isBufferEmpty() {
        return buffer.isEmpty();
    }

    int getBufferSize() {
        return buffer.size();
    }

    void admitTask(Task task) {
        if (isBufferFull()) {
            System.out.println("Buffer is full. Cannot admit task " + task.id);
            return;
        }
        buffer.add(task);
        if(buffer.size() == 1) {
            task.status = Task.Status.HoL;
            task.HoLTime = task.arrivalTime;
        } else {
            task.status = Task.Status.QUEUED;
        }
    }

    void offloadTaskToVehicle(Task task, Vehicle vehicle) {
        buffer.remove(task); 
        task.vehicleId = vehicle.id;
        task.releaseTime = vehicle.arrivalTime;
        task.serviceTime = task.releaseTime - task.HoLTime;
        task.queueTime = task.HoLTime - task.arrivalTime;
        if(task.releaseTime > task.expiryTime && !task.isExpired) {
            task.isExpired = true;
            Simulator.tasksExpired++;
        }
        Simulator.totalServiceTime += task.serviceTime;
        vehicle.remainingProcessingCapacity -= task.size;

        System.out.println(String.format("MC = %10.5f: #Task %d offloaded to #Vehicle %d, queue time = %.5f, service time = %.5f", 
            task.releaseTime, task.id, vehicle.id, task.queueTime, task.serviceTime));
        
        if(!isBufferEmpty()) {
            Task nextTask = buffer.peek();
            nextTask.status = Task.Status.HoL;
            nextTask.HoLTime = task.releaseTime;
            if(nextTask.HoLTime > nextTask.expiryTime && !nextTask.isExpired) {
                nextTask.isExpired = true;
                Simulator.tasksExpired++;
            }
        }
    }

    Task getHoLTask() {
        return buffer.peek();
    }
}

