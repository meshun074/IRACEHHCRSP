library(irace)

# Load parameters and scenario
parameters <- readParameters("parameters.txt")
scenario <- list(
  targetRunner = "./tuning.sh",
  instances = readLines("instances-list.txt"),
  maxExperiments = 200,
  parallel = 2,           # Number of parallel runs
  logFile = "irace.Rdata"
)

# Start tuning
irace(scenario, parameters)