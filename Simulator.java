import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Simulator {
    static double currentTime;
    static List<Vehicle> activeVehicles = new ArrayList<>();
    static List<UAV> activeUAVs = new ArrayList<>();


    enum toVary{
        PC,
        VEHICLE_ARRIVAL_RATE,
        TASK_ARRIVAL_RATE,
        MAX_TASK_PROCESSING_CAPACITY,
        UAV_ARRIVAL_RATE
    }

    static Random rng = new Random(12345);

    // Vehicle-related data.
    static double minVehicleSpeed = 10.0;
    static double maxVehicleSpeed = 50.0;
    static long minTaskProcessingCapacity = 10_000_000L; // (inst/s)
    static long maxTaskProcessingCapacity; // (inst/s)
    static double vehicleArrivalRate;
    static double avgVehicleSpeed;
    static double stdVehicleSpeed;

    // Task-related data.
    static double minTaskDeadline = 10.0; // (s) // NEED TO GET THIS DATA
    static double maxTaskDeadline = 50.0; // (s)
    static double taskArrivalRate; // (tasks/s)
    static long minTaskSize = 10_000_000L; // (inst)
    static long maxTaskSize = 5_000_000_000L; // (inst)
    // static long maxNumTasks = 1_000_000L; // (tasks)
    static long maxNumTasks = 10; // (tasks)

    // RSU-related data.
    static int rsuBufferCapacity = 50; // (tasks)
    static double rsuCoverageRange = 1000.0; // (m)

    // UAV-related data.
    static double minUAVSpeed = 25; // (m/s)
    static double maxUAVSpeed = 50; // (m/s)
    static double uavCoverageRange = 1000;
    static double UAVArrivalRate;
    static double avgUAVSpeed;
    static double stdUAVSpeed;

    // For metrics collection.
    static long totalTasks = 0;
    static long tasksAdmitted = 0;
    static long tasksOffloaded = 0;
    static long tasksExpired = 0; // UNDONE: need to check what to considered expired.
    static long tasksDropped = 0;
    static double totalServiceTime = 0.0; // update each time
    static double probabilityOfCooperation = 0.5;
    static long totalVehicles = 0;
    static double areaUnderCurveNs = 0;
    static double totalSimulationTime = 0;
    static long releaseOpportunities;
    static long totalUAVs = 0;
    static Set<String> initializedOutputFiles = new HashSet<>();

    static RSU rsu;

    static toVary parameterToVary;

    static double maxPositionVehicle = rsuCoverageRange / 2 + uavCoverageRange;
    static double maxPositionUAV = rsuCoverageRange / 2 + uavCoverageRange / 2;



    void start(double probabilityOfCooperation, double vehicleArrivalRate, double avgVehicleSpeed, double stdVehicleSpeed, double taskArrivalRate, long maxTaskProcessingCapacity, double UAVArrivalRate, double avgUAVSpeed, double stdUAVSpeed, toVary parameterToVary) {
        resetRunState();

        if (maxTaskProcessingCapacity <= minTaskProcessingCapacity) {
            throw new IllegalArgumentException("maxTaskProcessingCapacity must be greater than minTaskProcessingCapacity");
        }

        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        System.out.println("   RSU To Vehicle Offloading Simulator   ");
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        System.out.println("Simulation Parameters:");
        System.out.println("  Max Tasks: " + maxNumTasks);
        System.out.println("  RSU Buffer Capacity: " + rsuBufferCapacity);
        System.out.println("  RSU Coverage Range: " + rsuCoverageRange + " m");
        System.out.println("  Vehicle Arrival Rate: " + vehicleArrivalRate + " vehicles/s");
        System.out.println("  Task Arrival Rate: " + taskArrivalRate + " tasks/s");
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        System.out.println("Simultion Starting...");

        Simulator.probabilityOfCooperation = probabilityOfCooperation;
        Simulator.vehicleArrivalRate = vehicleArrivalRate;
        Simulator.avgVehicleSpeed = avgVehicleSpeed;
        Simulator.stdVehicleSpeed = stdVehicleSpeed;
        Simulator.taskArrivalRate = taskArrivalRate;
        Simulator.maxTaskProcessingCapacity = maxTaskProcessingCapacity;
        Simulator.UAVArrivalRate = UAVArrivalRate;
        Simulator.avgUAVSpeed = avgUAVSpeed;
        Simulator.stdUAVSpeed = stdUAVSpeed;
        Simulator.parameterToVary = parameterToVary;

        EventScheduler eventScheduler = new EventScheduler();
        eventScheduler.initialize();

        Event currentEvent = eventScheduler.processNextEvent();
        double previousEventTime = 0.0;

        rsu = new RSU(rsuBufferCapacity, rsuCoverageRange, RSU.SchedulingPolicy.FCFS, false);

        while(!eventScheduler.isEmpty()) {
            currentTime = currentEvent.time;
            areaUnderCurveNs += rsu.getBufferSize() * (currentEvent.time - previousEventTime); // DOUBLE CHECK THAT!!
            if (currentEvent.type == Event.EventType.TASK_ARRIVAL) {
                double time = currentEvent.time;
                long taskSize = minTaskSize + (long)(rng.nextDouble() * (maxTaskSize - minTaskSize));
                // long taskDeadline = (long) (minTaskDeadline + (long)(rng.nextDouble() * (maxTaskDeadline - minTaskDeadline)));

                double taskDeadlineRate = 1.0 / 25.0; 
                double u = rng.nextDouble();
                double taskDeadline = -Math.log(1.0 - u * (1.0 - Math.exp(-taskDeadlineRate * maxTaskDeadline))) / taskDeadlineRate;

                Task newTask = new Task(time, taskSize, taskDeadline);
                System.out.println("MC = " + String.format("%.3f", currentTime)  + " Task " + newTask.id + " arrived to the System. Deadline = " + newTask.deadline + ". Expiry Time = " + newTask.expiryTime);

                totalTasks++;
                if(!rsu.isBufferFull()) {
                    rsu.admitTask(newTask);
                    tasksAdmitted++;
                } else {
                    System.out.println(String.format("MC = %10.5f: #Task %d dropped due to full buffer", time, newTask.id));
                    tasksDropped++;
                }
            } else if(currentEvent.type == Event.EventType.VEHICLE_ARRIVAL) {
                double time = currentEvent.time;
                long taskProcessingCapacity = minTaskProcessingCapacity + (long)(rng.nextDouble() * (maxTaskProcessingCapacity - minTaskProcessingCapacity));
                boolean isCooperative = (probabilityOfCooperation == 1.0 ? true: rng.nextDouble() < probabilityOfCooperation);

                Vehicle newVehicle = new Vehicle(time, taskProcessingCapacity, isCooperative);
                totalVehicles++;

                activeVehicles.add(newVehicle);

                System.out.println("MC = " + String.format("%.3f", currentTime) + " Vehicle " + newVehicle.id + " arrived on the road.");

                for(UAV uav : activeUAVs) {
                    boolean willContact = (Metrics.contactTime(newVehicle, uav) >= 0);
                    if(!willContact) continue;

                    EventScheduler.eventQueue.add(new Event(EventScheduler.events, Event.EventType.VEHICLE_UAV_ENTRANCE, Metrics.contactTime(newVehicle, uav), newVehicle, uav));
                    EventScheduler.events++;
                }

                double RSU_entrance_time = currentTime + (newVehicle.arrivalLocation -  rsuCoverageRange / 2) / newVehicle.speed;

                EventScheduler.eventQueue.add(new Event(EventScheduler.events, Event.EventType.VEHICLE_RSU_ENTRANCE, RSU_entrance_time, newVehicle));
                EventScheduler.events++;

            } else if(currentEvent.type == Event.EventType.VEHICLE_RSU_ENTRANCE) {
                Vehicle newVehicle = currentEvent.vehicle;
                System.out.println("MC = " + String.format("%.3f", currentTime) + " Vehicle " + newVehicle.id + " enters RSU's Coverage Range now!");

                activeVehicles.remove(newVehicle);
                if(newVehicle.isServed) {
                    previousEventTime = currentEvent.time;
                    currentEvent = eventScheduler.processNextEvent();
                    continue;
                }

                Task taskToProcess = rsu.getHoLTask();

                if(taskToProcess != null) {
                    releaseOpportunities++;
                }

                while(true) {
                    if(taskToProcess != null && newVehicle.isCooperative && newVehicle.canProcessTask(taskToProcess)) {
                        rsu.offloadTaskToVehicle(taskToProcess, newVehicle);
                        tasksOffloaded++;
                        System.out.println(String.format("MC = %.3f RSU offloaded #Task %d to #Vehicle %d, queue time = %.5f, service time = %.5f", 
                            taskToProcess.releaseTime, taskToProcess.id, newVehicle.id, taskToProcess.queueTime, taskToProcess.serviceTime));
                        newVehicle.isServed = true;
                    } else {
                        break;
                    }
                    if(!rsu.isBulkRelease) {
                        break;
                    }
                    taskToProcess = rsu.getHoLTask();
                }
            } else if(currentEvent.type == Event.EventType.UAV_ARRIVAL) {
                double time = currentEvent.time;

                UAV newUAV = new UAV(time, uavCoverageRange);
                totalUAVs++;
                activeUAVs.add(newUAV);

                EventScheduler.eventQueue.add(new Event(EventScheduler.events, Event.EventType.UAV_LEAVE, newUAV.getStopTime(), newUAV));
                EventScheduler.events++;

                System.out.println("MC = " + String.format("%.3f", currentTime)  + " UAV " + newUAV.id + " arrived.");

                for(Vehicle veh : activeVehicles) {
                    if (veh.isServed) continue;
                    boolean willContact = (Metrics.contactTime(veh, newUAV) >= 0);
                    if(!willContact) continue;

                    // if(Metrics.contactTime(veh, newUAV) == Simulator.currentTime) {
                    //     Task taskToProcess = rsu.getHoLTask();
                    //     if (taskToProcess == null) break;              
                    //     releaseOpportunities++;
                    //     if(taskToProcess != null && veh.isCooperative && veh.canProcessTask(taskToProcess)) {
                    //         rsu.offloadTaskToVehicle(taskToProcess, veh);
                    //         tasksOffloaded++;
                    //         System.out.println(String.format("MC = %.3f UAV at Location %.3f offloaded #Task %d to #Vehicle %d at Location %.3f, queue time = %.5f, service time = %.5f", 
                    //             taskToProcess.releaseTime, newUAV.getLocation(), taskToProcess.id, veh.id, veh.getLocation(), taskToProcess.queueTime, taskToProcess.serviceTime));
                    //         veh.isServed = true;
                    //     }
                    //     continue;
                    // }

                    EventScheduler.eventQueue.add(new Event(EventScheduler.events, Event.EventType.VEHICLE_UAV_ENTRANCE, Metrics.contactTime(veh, newUAV), veh, newUAV));
                    EventScheduler.events++;                   
                }

            } else if(currentEvent.type == Event.EventType.UAV_LEAVE) { 
                UAV uav = currentEvent.uav;
                System.out.println(String.format("MC = %.3f UAV %d arrived above RSU at time %.3f", currentTime, uav.id, Simulator.currentTime));
                activeUAVs.remove(uav);
            } else if(currentEvent.type == Event.EventType.VEHICLE_UAV_ENTRANCE) {
                UAV uav = currentEvent.uav;
                boolean inContact = (uav.getLocation() - uav.getRSUcontactLocation() <= 0);
                Vehicle vehicle = currentEvent.vehicle;
                Task taskToProcess = rsu.getHoLTask();

                if(!inContact || vehicle.isServed || taskToProcess == null) {
                    previousEventTime = currentEvent.time;
                    currentEvent = eventScheduler.processNextEvent();
                    continue;
                }

                System.out.println("MC = " + String.format("%.3f", currentTime)  + " UAV " + uav.id + " is connected to RSU.");
                System.out.println("MC = " + String.format("%.3f", currentTime) + " Vehicle " + vehicle.id + " enters UAV " + uav.id + " Coverage Range now!");

                releaseOpportunities++;
                if(taskToProcess != null && vehicle.isCooperative && vehicle.canProcessTask(taskToProcess)) {
                    rsu.offloadTaskToVehicle(taskToProcess, vehicle);
                    tasksOffloaded++;
                    vehicle.isServed = true;
                    System.out.println(String.format("MC = %.3f UAV at Location %.3f offloaded #Task %d to #Vehicle %d at Location %.3f, queue time = %.5f, service time = %.5f", 
                        taskToProcess.releaseTime, uav.getLocation(), taskToProcess.id, vehicle.id, vehicle.getLocation(), taskToProcess.queueTime, taskToProcess.serviceTime));
                }
            }
            previousEventTime = currentEvent.time;
            currentEvent = eventScheduler.processNextEvent();
        }

        Simulator.totalSimulationTime = previousEventTime;

        System.out.println("Simulation Ending.... ");
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        System.out.println("Simulation Metrics:");
        System.out.println("Total simulation time: " + String.format("%10.5f", previousEventTime) + " seconds");
        System.out.println("Total tasks generated: " + totalTasks);
        System.out.println("Total tasks admitted to RSU buffer: " + tasksAdmitted);
        System.out.println("Total tasks offloaded to vehicles: " + tasksOffloaded);
        System.out.println("Total tasks expired: " + tasksExpired);
        System.out.println("Total tasks dropped: " + tasksDropped);
        System.out.println("Total vehicles generated: " + totalVehicles);
        System.out.println("Total release opportunities: " + releaseOpportunities);
        System.out.println("Total UAVs: " + totalUAVs);
        double mu = tasksOffloaded / totalSimulationTime;
        System.out.println("mu = " + mu);

        // System.out.println("Area under the curve (buffer size): " + String.format("%10.5f", Simulator.areaUnderCurveNs) + " task-seconds");

        if(Simulator.parameterToVary == toVary.PC) {
            String fileName = "Results/pc.txt";
            double probabilityOfRelease = Metrics.probabilityOfTaskRelease(tasksOffloaded, releaseOpportunities);
            writeCsvRow(
                fileName,
                "pc,pr",
                String.valueOf(probabilityOfCooperation + "," + probabilityOfRelease)
            );
        } else if(Simulator.parameterToVary == toVary.VEHICLE_ARRIVAL_RATE) {
            double probabilityOfRelease = Metrics.probabilityOfTaskRelease(tasksOffloaded, releaseOpportunities);
            String fileName = "Results/mu.txt";
            double meanServiceTime = Metrics.meanServiceTime(totalServiceTime, tasksOffloaded);
            double probabilityOfExpiry = Metrics.probabilityOfDeadlineExpiry(tasksExpired, tasksAdmitted);
            double probabilityOfAdmission = Metrics.probabilityOfAdmission(tasksAdmitted, totalTasks);

            writeCsvRow(
                fileName,
                "μv,pr,Ts,pa,pe",
                Simulator.vehicleArrivalRate + "," + probabilityOfRelease + "," + meanServiceTime + "," + probabilityOfAdmission + "," + probabilityOfExpiry
            );
        } else if(Simulator.parameterToVary == toVary.TASK_ARRIVAL_RATE) {
            String fileName = "Results/lambda.txt";
            double meanNumberOfTasksInSystem = Metrics.meanNumberOfTasksInSystem(areaUnderCurveNs, totalSimulationTime);
            double probabilityOfAdmission = Metrics.probabilityOfAdmission(tasksAdmitted, totalTasks);
            double meanTaskBufferingDelay = Metrics.meanTaskBufferingDelay(meanNumberOfTasksInSystem, taskArrivalRate, probabilityOfAdmission);
            writeCsvRow(
                fileName,
                "λt,Nt,Qt",
                Simulator.taskArrivalRate + "," + meanNumberOfTasksInSystem + "," + meanTaskBufferingDelay
            );
        } else if(Simulator.parameterToVary == toVary.MAX_TASK_PROCESSING_CAPACITY) {
            String fileName = "Results/c_max.txt";
            double probabilityOfRelease = Metrics.probabilityOfTaskRelease(tasksOffloaded, releaseOpportunities);
            writeCsvRow(
                fileName,
                "c_max,pr",
                Simulator.maxTaskProcessingCapacity + "," + probabilityOfRelease
            );
        }
    }

    private void writeCsvRow(String fileName, String header, String row) {
        File file = new File(fileName);
        boolean isFirstWriteThisRun = initializedOutputFiles.add(fileName);
        boolean writeHeader = isFirstWriteThisRun || !file.exists() || file.length() == 0;

        try (PrintWriter writer = new PrintWriter(new FileWriter(file, !isFirstWriteThisRun))) {
            if (writeHeader) {
                writer.println(header);
            }
            writer.println(row);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write " + fileName, e);
        }
    }

    private void resetRunState() {
        totalTasks = 0;
        tasksAdmitted = 0;
        tasksOffloaded = 0;
        tasksExpired = 0;
        tasksDropped = 0;
        totalServiceTime = 0.0;
        totalVehicles = 0;
        areaUnderCurveNs = 0;
        releaseOpportunities = 0;
        rsu = null;
        Task.count = 0;
        Vehicle.count = 0;
        rng = new Random(12345);
    }
}
