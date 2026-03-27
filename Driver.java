public class Driver {

    static double[] vehicleArrivalRates = {0.1, 0.125, 0.15, 0.175, 0.2, 0.225, 0.25, 0.2777};
    static double[] avgVehicleSpeeds = {45, 43.54, 41.95, 40.21, 38.23, 35.89, 32.9, 24.78};
    static double[] stdVehicleSpeeds = {13.5, 13.062, 12.585, 12.063, 11.469, 10.767, 9.87, 7.434};
    static long maxTaskProcessingCapacities; // between [3*10^8, 8*10^8] (inst/s) 
    
    
    public static void main(String[] args) {
      maxTaskProcessingCapacities = 800_000_000L;

        // Varying pc to measure pr
//         for(double pc = 0.01; pc <= 1; pc += 0.1) {
//             Simulator simulator = new Simulator();
//             simulator.start(pc, vehicleArrivalRates[7], avgVehicleSpeeds[7], stdVehicleSpeeds[7], 0.1, maxTaskProcessingCapacities, Simulator.toVary.PC);
//         }

      // Varying vehicle arrival rate to measure pr, Ts
       for(int i = 0; i < vehicleArrivalRates.length; i++) {
            Simulator simulator = new Simulator();
            simulator.start(1, vehicleArrivalRates[i], avgVehicleSpeeds[i], stdVehicleSpeeds[i], 0.3, maxTaskProcessingCapacities, Simulator.toVary.VEHICLE_ARRIVAL_RATE);
       }

         // Varying task arrival rate
        // for(double i = 0.09; i <= 0.2; i += 0.01) {
        //     Simulator simulator = new Simulator();
        //     simulator.start(1, vehicleArrivalRates[6], avgVehicleSpeeds[6], stdVehicleSpeeds[6], i, maxTaskProcessingCapacities, Simulator.toVary.TASK_ARRIVAL_RATE);
        // }

         // Varying c_max to measure pr
//         for(long i = 300_000_000L; i <= 800_000_000; i += 100_000_000L) {
//             Simulator simulator = new Simulator();
//              simulator.start(1, vehicleArrivalRates[7], avgVehicleSpeeds[7], stdVehicleSpeeds[7], 0.3, i, Simulator.toVary.MAX_TASK_PROCESSING_CAPACITY);
//
//         }

    }
} 