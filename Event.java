public class Event {
    enum EventType {VEHICLE_ARRIVAL, TASK_ARRIVAL, UAV_ARRIVAL, VEHICLE_UAV_ENTRANCE, VEHICLE_RSU_ENTRANCE, UAV_LEAVE}

    public static final Event.EventType EventType = null; 

    EventType type;
    double time;
    final long id;
    Vehicle vehicle; 
    UAV uav;

    Event(long id,EventType type, double time) {
        this.type = type;
        this.time = time;
        this.id = id;
        this.vehicle = null;
        // if(type == EventType.VEHICLE_ARRIVAL) {
        //     System.out.println("#Event " + this.id + ": vehicle arrival at time " + this.time);
        // } else if(type == EventType.TASK_ARRIVAL) {
        //     System.out.println("#Event " + this.id + ": task arrival at time " + this.time);
        // } else if(type == EventType.UAV_ARRIVAL) {
        //     System.out.println("#Event " + this.id + ": UAV arrival at time " + this.time);
        // } else if(type == EventType.VEHICLE_UAV_ENTRANCE) {
        //     System.out.println("#Event " + this.id + ": Vehicle " + Simulator.vehicles.get(id).id +  " enters UAV's Coverage Range at time " + this.time);
        // } else if(type == EventType.VEHICLE_RSU_ENTRANCE) {
        //     System.out.println("#Event " + this.id + ": Vehicle " + Simulator.vehicles.get(id).id +  " enters RSU's Coverage Range at time " + this.time);
        // }
    }

     Event(long id, EventType type, double time, Vehicle vehicle) {
        this.id = id;
        this.type = type;
        this.time = time;
        this.vehicle = vehicle;
    }

    Event(long id, EventType type, double time, UAV uav) {
        this.id = id;
        this.type = type;
        this.time = time;
        this.uav = uav;
    }

     Event(long id, EventType type, double time, Vehicle vehicle, UAV uav) {
        this.id = id;
        this.type = type;
        this.time = time;
        this.vehicle = vehicle;
        this.uav = uav;
    }

}