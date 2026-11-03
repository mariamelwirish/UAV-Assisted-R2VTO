public class Driver {

    static double[] vehicleArrivalRates = {0.1, 0.125, 0.15, 0.175, 0.2, 0.225, 0.25, 0.2777};
    static double[] avgVehicleSpeeds = {45, 43.54, 41.95, 40.21, 38.23, 35.89, 32.9, 24.78};
    static double[] stdVehicleSpeeds = {13.5, 13.062, 12.585, 12.063, 11.469, 10.767, 9.87, 7.434};
    static long maxTaskProcessingCapacities; // between [3*10^8, 8*10^8] (inst/s) 
    static double[] UAVArrivalRates = {0.01, 0.02, 0.03, 0.04, 0.05};
    static double[] avgUAVSpeeds = {25, 30, 35, 40, 45, 50}; 
    static double[] stdUAVs = {7.5, 9, 10.5, 12, 13.5, 15};
    static double[] taskArrivalRates = {7.5, 9, 10.5, 12, 13.5, 15};

    static final int REPS = 1;
    static final long BASE_SEED = 1000;

    static void runPair(String experiment, double x,
                        double pc, double muV, double vAvg, double vStd, double lambdaT, long cMax,
                        double uavRate, double uavAvg, double uavStd) {
        for (int r = 0; r < REPS; r++) {
            Simulator.experiment = experiment;
            Simulator.xValue = x;
            Simulator.rep = r;
            Simulator.seed = BASE_SEED + 10L * r;   // same seed for base and UAV in this rep

            // 1) baseline
            Simulator.useUAV = false;
            Simulator.scenario = "base";
            new Simulator().start(pc, muV, vAvg, vStd, lambdaT, cMax, uavRate, uavAvg, uavStd, Simulator.toVary.PC);

            // 2) with UAV, same seed
            Simulator.useUAV = true;
            Simulator.scenario = "uav";
            new Simulator().start(pc, muV, vAvg, vStd, lambdaT, cMax, uavRate, uavAvg, uavStd, Simulator.toVary.PC);
        }
        System.out.printf("done: %s x=%.4f%n", experiment, x);
    }

    public static void main(String[] args) {
        String exp = args.length > 0 ? args[0] : "all";
        boolean all = exp.equals("all");

        if (all || exp.equals("pc"))     expPc();       // Fig 3(a)
//        if (all || exp.equals("muV"))    expMuV();      // Fig 3(b)
//        if (all || exp.equals("cmax"))   expCmax();     // Fig 3(c)
//        if (all || exp.equals("lambda")) expLambda();   // Fig 5(a,b)
//        if (all || exp.equals("muVload"))expMuVLoad();  // Fig 4(c), Fig 6
    }

    // EXP 1: Varying Probability of cooperation.
    public static void expPc() {
        double[] pcValues = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};
        for (int i = 0; i < pcValues.length; i++) {
            runPair("1_pr_vs_pc", pcValues[i],      // sweep name, x value
                    pcValues[i],                      // p_c  <-- varied
                    0.2777, 24.78, 7.434,             // muV and its speed mean/std (paper)
                    0.3,                              // lambda_t
                    800_000_000L,                     // C_max (paper)
                    0.03, 35, 10.5);                  // UAV rate, speed, std (nominal)
        }

    }

    //  EXP 2: Varying Vehicler Arrival Rate.
    public static void expMuV() {

        for (int i = 0; i < vehicleArrivalRates.length; i++) {
            runPair("1_pr_vs_muV", vehicleArrivalRates[i],
                    1,                                                 // p_c (paper)
                    vehicleArrivalRates[i], avgVehicleSpeeds[i], stdVehicleSpeeds[i],  // <-- varied
                    0.3,                                               // lambda_t
                    800_000_000L,                                      // C_max (paper)
                    0.03, 35, 10.5);                                   // UAV
        }

    }

    //  EXP 3: Varying the Maximum Processing Capacity.
    public static void expCmax() {
        long[] cMaxValues = {300_000_000L, 400_000_000L, 500_000_000L,
                600_000_000L, 700_000_000L, 800_000_000L};
        for (int i = 0; i < cMaxValues.length; i++) {
            runPair("1_pr_vs_cmax", cMaxValues[i] / 1e8,   // x in units of 10^8
                    1,                                       // p_c
                    0.2777, 24.78, 7.434,                    // muV and speed
                    0.3,                                     // lambda_t
                    cMaxValues[i],                           // C_max  <-- varied
                    0.03, 35, 10.5);                         // UAV
        }
    }

    // EXP 4: Varying the rate of task arrival.
    public static void expLambda() {
        double[] taskRates = {0.09, 0.1, 0.125, 0.15, 0.175, 0.2,     // paper range (Fig. 5)
                0.25, 0.3, 0.35, 0.4};                  // extension (to show admission)
        long[] cMaxValues  = {300_000_000L, 500_000_000L, 800_000_000L};
        String[] cLabels   = {"C3", "C5", "C8"};

        for (int c = 0; c < cMaxValues.length; c++) {
            for (int i = 0; i < taskRates.length; i++) {
                runPair("1_lambda_" + cLabels[c], taskRates[i],
                        1,                              // p_c
                        0.2777, 24.78, 7.434,           // muV and speed (paper)
                        taskRates[i],                   // lambda_t  <-- varied
                        cMaxValues[c],                  // C_max (3, 5, 8 x10^8)
                        0.03, 35, 10.5);                // UAV
            }
        }
    }

    // EXP 5: Varying vehicles arrival rate.
    public static void expMuVLoad() {
        long[] cMaxValues = {300_000_000L, 500_000_000L, 800_000_000L};
        String[] cLabels  = {"C3", "C5", "C8"};

        for (int c = 0; c < cMaxValues.length; c++) {
            for (int i = 0; i < vehicleArrivalRates.length; i++) {
                runPair("2_muV_" + cLabels[c], vehicleArrivalRates[i],
                        1,                                                  // p_c
                        vehicleArrivalRates[i], avgVehicleSpeeds[i], stdVehicleSpeeds[i],  // <-- varied
                        0.3,                                                // lambda_t (Fig. 6)
                        cMaxValues[c],                                      // C_max
                        0.03, 35, 10.5);                                    // UAV (nominal)
            }
        }
    }
} 