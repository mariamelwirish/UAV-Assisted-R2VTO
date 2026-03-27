import java.util.Queue;
import java.util.Comparator;
import java.util.PriorityQueue;

class Event {
    enum EventType {VEHICLE_ARRIVAL, TASK_ARRIVAL}

    public static final Event.EventType EventType = null; 

    EventType type;
    double time;
    final long id;

    Event(long id,EventType type, double time) {
        this.type = type;
        this.time = time;
        this.id = id;
        if(type == EventType.VEHICLE_ARRIVAL) {
            // System.out.println("#Event " + this.id + ": vehicle arrival at time " + this.time);
        } else if(type == EventType.TASK_ARRIVAL) {
            // System.out.println("#Event " + this.id + ": task arrival at time " + this.time);
        } 
    }

}

class EventScheduler {
    Queue<Event> eventQueue;
    long events;

    EventScheduler() {
        eventQueue = new PriorityQueue<>(Comparator.comparingDouble((Event e) -> e.time).thenComparingLong(e -> e.id)); // if it happens 2 events @ same time -> take one of them and add a small epsilon.
        this.events = 1;
    }

    void initialize() { 
        double firstVehicleArrival = Metrics.exp(Simulator.rng, Simulator.vehicleArrivalRate); 
        eventQueue.add(new Event(this.events, Event.EventType.VEHICLE_ARRIVAL, firstVehicleArrival));
        this.events++;

        double firstTaskArrival = Metrics.exp(Simulator.rng, Simulator.taskArrivalRate);
        eventQueue.add(new Event(this.events, Event.EventType.TASK_ARRIVAL, firstTaskArrival));
        this.events++;
    }

    boolean isEmpty() {
        return eventQueue.isEmpty();
    }

    void scheduleNextTaskArrival(Event currentEvent) {
        double nextTaskArrival = currentEvent.time + Metrics.exp(Simulator.rng, Simulator.taskArrivalRate);
        eventQueue.add(new Event(this.events, Event.EventType.TASK_ARRIVAL, nextTaskArrival));
        this.events++;
    }

    void scheduleNextVehicleArrival(Event currentEvent) {
        double nextVehicleArrival = currentEvent.time + Metrics.exp(Simulator.rng, Simulator.vehicleArrivalRate);
        eventQueue.add(new Event(this.events, Event.EventType.VEHICLE_ARRIVAL, nextVehicleArrival));
        this.events++;
    }

    Event processNextEvent() {
        Event nextEvent = eventQueue.poll();

        if(nextEvent.type == Event.EventType.TASK_ARRIVAL && (Simulator.totalTasks + 1) < Simulator.maxNumTasks) {
            scheduleNextTaskArrival(nextEvent);
        } else if(nextEvent.type == Event.EventType.VEHICLE_ARRIVAL && ((Simulator.totalTasks + 1) < Simulator.maxNumTasks || !Simulator.rsu.isBufferEmpty())) {
            scheduleNextVehicleArrival(nextEvent);
        }

        return nextEvent;
    }
}