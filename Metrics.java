import java.util.Random;

/* 

Metrics Needed to calculate:
1) Ns, Nq, Ds, Dq
2) Admission Probability (Pa).
3) Deadline Expiry Probability (Pe).
4) HoL Release Probability (Pr).

*/
public class Metrics {
    /**************************** Math Functions ****************************/
    // Exponential Distribution Generator
    public static double exp(Random rng, double rate) {
        if (rate <= 0) throw new IllegalArgumentException("rate must be > 0");
        return -Math.log(1.0 - rng.nextDouble()) / rate;
    }

    // Geometric Distribution Generator
    public static int geometric(double p, Random rng) {
        if (p <= 0.0 || p > 1.0) throw new IllegalArgumentException("p must be in (0,1]");
        if (p == 1.0) return 1;
        double u = rng.nextDouble();
        if (u == 0.0) u = 1e-16;
        return (int) Math.ceil(Math.log(1.0 - u) / Math.log(1.0 - p));
    }

    // Uniform Range [a,b]
    public static int uniform(Random rng, int a, int b) {
        return a + rng.nextInt(b - a + 1);
    }

    /**************************** Scenrio Metrics ****************************/
            /******************** Probabilities ********************/
    // Probability of cooperation (pc) -> Given as input parameter, not generated.

    // Probability of task release (pr) = Number of tasks released / Total number of release opportunities.
        /* release opportunities = Number of tasks in HoL attempted to be released */
    static double probabilityOfTaskRelease(long releasedTasks, long releaseOpportunities) {
        return (double) releasedTasks / releaseOpportunities;
    }

    // Probability of deadline expiry (pe) = Number of expired tasks / Total number of admitted tasks.
    static double probabilityOfDeadlineExpiry(long expiredTasks, long admittedTasks) {
        return (double) expiredTasks / admittedTasks;
    }

    // Probability of admission (pa) = Number of admitted tasks / Total number of task arrivals.
    static double probabilityOfAdmission(long admittedTasks, long totalTaskArrivals) {
        return (double) admittedTasks / totalTaskArrivals;
    }

            /******************** Means ********************/
    // Mean Service Time (Ts) = Total service time for all tasks / Number of released tasks.
        /* Total service time = sum of all service times of released tasks (calculated for each task in system) */
    static double meanServiceTime(double totalServiceTime, long releasedTasks) {
        return totalServiceTime / releasedTasks;
    }

    // Mean Number of Tasks in System (Nt) = area under curve of number of tasks in system over time / total simulation time.
        /* area under curve = sum of (number of tasks in system * time interval) for each event */
    static double meanNumberOfTasksInSystem(double areaUnderCurve, double totalSimulationTime) {
        return areaUnderCurve / totalSimulationTime;
    }

    // Mean Buffering Delay (Qt) = mean number of tasks in system (Nt) / (taskArrivalRate * probability of admission pa).
    static double meanTaskBufferingDelay(double meanNumberOfTasksInSystem, double taskArrivalRate, double probabilityOfAdmission) {
        return meanNumberOfTasksInSystem / (taskArrivalRate * probabilityOfAdmission);
    }

            /******************** Single vs Bulk Release Version Only ********************/
    // Mean Number of Release Tasks(Nb) = total number of released tasks / number of release events.
    static double meanNumberOfReleasedTasks(long releasedTasks, long releaseEvents) {
        return (double) releasedTasks / releaseEvents;
    }

            /******************** Converting Speeds ********************/
    // static double convertToKilometersPerHour(double metersPerSecond) {
    //     return metersPerSecond * 18 / 5;
    // }

    // static double convertToMetersPerSecond(double KilometersPerHour) {
    //     return KilometersPerHour * 5 / 18;
    // }

    static double getXPosition(double arrivalLocation, double arrivalTime, double speed) { // entry position - distance (as it is approaching x = 0)
        return arrivalLocation - ((Simulator.currentTime - arrivalTime) * speed);
    }

    static double contactTime(Vehicle vehicle, UAV uav) {
        double gap = uav.getLocation() - vehicle.getLocation();
        double relativeSpeed = getRelativeSpeed(vehicle, uav);
         double contactDistance = uav.coverageRange / 2; 
        
        if(Math.abs(gap) > uav.coverageRange / 2) {
            if(gap < 0) { // uav is ahead.
                if(relativeSpeed >= 0) return -1; 
                double remainingDistance = Math.abs(gap) - uav.coverageRange / 2;
                double timeNeeded = Math.abs(remainingDistance / relativeSpeed);
                double absoluteTime = Simulator.currentTime + timeNeeded;
                double tVehAtEdge = vehicle.arrivalTime + (vehicle.arrivalLocation - Simulator.rsuCoverageRange / 2) / vehicle.speed;
                if (absoluteTime <= uav.getStopTime() && absoluteTime <= tVehAtEdge) return absoluteTime;
            } else if (gap > 0) { // vehicle is ahead.
                if(relativeSpeed <= 0) return -1;
                double remainingDistance = Math.abs(gap) - uav.coverageRange / 2;
                double timeNeeded = Math.abs(remainingDistance / relativeSpeed);
                double absoluteTime = Simulator.currentTime + timeNeeded;
                double tVehAtEdge = vehicle.arrivalTime + (vehicle.arrivalLocation - Simulator.rsuCoverageRange / 2) / vehicle.speed;
                if (absoluteTime <= uav.getStopTime() && absoluteTime <= tVehAtEdge) return absoluteTime;
            } else { // in contact.
                return Simulator.currentTime;
            }
        } else {
            return Simulator.currentTime;
        }
        return -1;
    }

    // Getting relative speed of Vehicle and UAV.   
        // -ve means UAV is slower than vehicle.
        // +ve means UAV is faster than vehicle.
        // 0 means same speed.
    static double getRelativeSpeed(Vehicle vehicle, UAV uav) {
        return uav.speed - vehicle.speed;
    }
}
