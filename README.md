# 🏎️ TrackBench

TrackBench is a benchmarking suite designed specifically to test the performance and efficiency of stream processing systems under diverse workloads.

The suite focuses on evaluating how systems handle varying data structures, volumes, velocities, and complexities. It simulates real-world scenarios with heterogeneous workloads, providing valuable insights into system capabilities for managing high-frequency and high-complexity data streams.

With a flexible and easy-to-configure set of tools, TrackBench is ideal for assessing the limits of advanced stream processing systems.

---

## Key Features
- **Stream Processing Workload Simulation**: Evaluate systems under diverse real-world workload conditions.
- **Customizable Benchmarking Parameters**: Easily tailor benchmarks to specific testing needs.
- **High-Velocity and High-Complexity Testing**: Assess system performance under intense data stream scenarios.
- **Heterogeneous Workload Management**: Simulate and measure the impact of diverse data types and formats.

---

## Getting Started

### Benchmark 
To start the benchmark one has to execute the following command in the `benchmark` folder:

```
./gradlew run
```
to prefix the summary file a prefix can be specified by appending `--p` or `-prefix` followed by the desired prefix.
All other options can be specified in the settings file which is by default located in `benchmark/src/main/resources/settings.properties`.

### Kafka Streams 

To start the Kafka Streams implementation of the 3 default workloads one need to have already a running Kafka instance or
can start a new one by using the `dock-compose.yml` file which is located in `kafka/utils/docker-compose.yml`.
The Kafka configurations might need adjustment, which can be done in the `kafka.properties` file which is locaed in `benchmark/src/main/resources/kafka.properties`.
The Kafka Streams jobs can be started by executing the following command in the `kafka` folder:
```
./gradlew run
```

---

## License

This project is licensed under the [GPL3 License](./LICENSE).  
Feel free to use, modify, and distribute this software as per the terms of the license.


