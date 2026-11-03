import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;


public class EventScheduler {
    static Queue<Event> eventQueue;
    static long events;

    EventScheduler() {
        eventQueue = new PriorityQueue<>(Comparator.comparingDouble((Event e) -> e.time).thenComparingLong(e -> e.id)); // if it happens 2 events @ same time -> take one of them and add a small epsilon.
        this.events = 1;
    }

    void initialize() { 
        double firstVehicleArrival = Metrics.exp(Simulator.vehicleRng, Simulator.vehicleArrivalRate);
        eventQueue.add(new Event(this.events, Event.EventType.VEHICLE_ARRIVAL, firstVehicleArrival));
        this.events++;

        double firstTaskArrival = Metrics.exp(Simulator.taskRng, Simulator.taskArrivalRate);
        eventQueue.add(new Event(this.events, Event.EventType.TASK_ARRIVAL, firstTaskArrival));
        this.events++;

        if(Simulator.useUAV) {
            double firstUAVArrival = Metrics.exp(Simulator.uavRng, Simulator.UAVArrivalRate);
            eventQueue.add(new Event(this.events, Event.EventType.UAV_ARRIVAL, firstUAVArrival));
            this.events++;
        }
    }

    boolean isEmpty() {
        return eventQueue.isEmpty();
    }

    void scheduleNextTaskArrival(Event currentEvent) {
        double nextTaskArrival = currentEvent.time + Metrics.exp(Simulator.taskRng, Simulator.taskArrivalRate);
        eventQueue.add(new Event(this.events, Event.EventType.TASK_ARRIVAL, nextTaskArrival));
        this.events++;
    }

    void scheduleNextVehicleArrival(Event currentEvent) {
        double nextVehicleArrival = currentEvent.time + Metrics.exp(Simulator.vehicleRng, Simulator.vehicleArrivalRate);
        eventQueue.add(new Event(this.events, Event.EventType.VEHICLE_ARRIVAL, nextVehicleArrival));
        this.events++;
    }

    void scheduleNextUAVArrival(Event currentEvent) {
        double nextUAVArrival = currentEvent.time + Metrics.exp(Simulator.uavRng, Simulator.UAVArrivalRate);
        eventQueue.add(new Event(this.events, Event.EventType.UAV_ARRIVAL, nextUAVArrival));
        this.events++;
    }

    Event processNextEvent() {
        Event nextEvent = eventQueue.poll();

        if(nextEvent.type == Event.EventType.TASK_ARRIVAL && (Simulator.totalTasks + 1) < Simulator.maxNumTasks) {
            scheduleNextTaskArrival(nextEvent);
        } else if(nextEvent.type == Event.EventType.VEHICLE_ARRIVAL && ((Simulator.totalTasks + 1) < Simulator.maxNumTasks || !Simulator.rsu.isBufferEmpty())) {
            scheduleNextVehicleArrival(nextEvent);
        } else if (nextEvent.type == Event.EventType.UAV_ARRIVAL && ((Simulator.totalTasks + 1) < Simulator.maxNumTasks || !Simulator.rsu.isBufferEmpty())) {
            scheduleNextUAVArrival(nextEvent);
        } 

        return nextEvent;
    }
}