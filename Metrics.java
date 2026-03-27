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
}
