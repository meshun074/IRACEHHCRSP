#!/bin/bash
# Read irace parameters
CONFIG="$@"  # Contains --pop-size 100 --mut-rate 0.1 ... AND the instance file

# Extract the instance (last argument)
INSTANCE=${@: -1}  # Assumes instance is the last argument

cd IRACEHHCRSP
# Run Java GA and pass both config + instance
OUTPUT=$(java -cp lib/*:out/artifacts/IRACEHHCRSP_jar/IRACEHHCRSP.jar org.example.Main $CONFIG --instance "$INSTANCE")

# Extract fitness (assuming output format: "Best fitness: 123.45")
FITNESS=$(echo "$OUTPUT" | grep "Best fitness:" | awk '{print $3}')

# Return fitness (irace minimizes by default; use `-${FITNESS}` for maximization)
echo "$FITNESS"