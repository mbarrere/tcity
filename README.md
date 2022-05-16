# T-CITY
## Cyber-Physical Attack Graph Analyser for Smart Cities

### Version 0.64.0

## Contents
- [Requirements](#requirements)
- [Usage](#usage)
- [Execution examples](#execution-examples)
- [Configuration parameters](#configuration-parameters)
- [Licence](#licence)

## Requirements
* Java 8
* Optional: 
  * Graphviz (`dot` command) is required to produce output graphs in PNG format 
  * Python 3, PuLP, and Gurobi, to enable second MaxSAT solver

## Usage

```java -jar tcity.jar -f inputFile.json [optional args]```  
This command executes the T-CITY tool with an input JSON file that describes the network under analysis. Optional arguments are as follows. 

```
Optional args: 
 -b,--bayesian       Enable Bayesian risk analysis
 -c,--config <arg>   Load custom configuration file
 -d,--debug          Turn on debug mode
 -f,--file <arg>     (Mandatory) Input JSON file
 -h,--hardening      Enable hardening analysis
 -t,--trace          Turn on trace mode
 -x,--display        Enable graph solution display
```



## Execution examples

### Example 1: Risk analysis with Bayesian CPAGs
```
$> java -jar tcity.jar -f examples/smart-farming/farming1.json --bayesian --display
```
```
== TCity-Prototype v0.52 ==
== Started at 2022-05-16 12:33:21.811 ==
Input file: examples/smart-farming/farming1.json
=> Loading problem specification... 
done in 170 ms (0 seconds).
(*) Hardening analysis not enabled (enable with flag -h)
(*) Bayesian enabled
Bayesian. Computing inconditional probabilities for graph G(V=40,E=48)
Bayes => sourceNode: attacker, targetNode: target
[TIME] Bayesian generation took 39 ms (0 seconds)
Unconditional probability for target 'target': 0.61996373508379
Writing Graph to DOT file
== TCity-Prototype ended at 2022-05-16 12:33:24.366 ==
```

#### CPAG display: 
If Graphviz is properly installed and the T-CITY config file has the correct path to the `dot` tool (e.g. `tools.dot = /usr/local/bin/dot`), then you should see the following CPAG: 

![Screenshot - simple example](https://github.com/mbarrere/tcity/blob/main/screenshots/farming2-bn.png)

---

## Configuration parameters
The configuration parameters are stored in the file `tcity.conf`.
The tool also accepts a different configuration file as argument [-c configFile] to override the configuration in *tcity.conf*. If the file `tcity.conf` is not present, T-CITY uses the default configuration values.


## Licence
Apache License 2.0
