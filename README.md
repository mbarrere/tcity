# T-CITY
## Cyber-Physical Attack Graph Analyser for Smart Cities

### Version 0.64.0
### Note: this site is under construction. Further documentation, instructions, and examples, will be available soon. 

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

### Graphical front end
```java -jar tcity.jar -g```  
This command executes the T-CITY tool in graphical mode. 

### Command line
```java -jar tcity.jar -f inputFile.json [optional args]```  
This command executes the T-CITY tool with an input JSON file that describes the network under analysis. Optional arguments are as follows. 

```
Optional args: 
 -b,--bayesian       Enable Bayesian risk analysis
 -c,--config <arg>   Load custom configuration file
 -d,--debug          Turn on debug mode
 -f,--file <arg>     (Mandatory) Input JSON file
 -g,--gui            Enable graphical CPAG editor and analyser
 -h,--hardening      Enable hardening analysis
 -t,--trace          Turn on trace mode
 -x,--display        Enable graph solution display
```



## Execution examples

### Risk analysis with Bayesian CPAGs (graphical interface)
```
$> java -jar tcity.jar -g
```
- Go to Menu -> `File` -> `Open CPAG (JSON)` and navigate to the smart farming scenario at `examples/smart-farming/farming.json`
- Go to Menu -> `CPAG Metrics` -> `Auto Bayesian risk`
- You should see the following Bayesian CPAG:

![Screenshot - simple example](https://github.com/mbarrere/tcity/blob/main/screenshots/cpag-editor-farming.png)

### Risk analysis with Bayesian CPAGs (command line)
```
$> java -jar tcity.jar -f examples/smart-farming/farming.json --bayesian --display
```
```
11:46:47 [INFO ] TCity:51 - == T-CITY v0.64 ==
11:46:47 [INFO ] TCity:52 - == Started at 2022-06-10 11:46:47.497 ==
11:46:47 [INFO ] TCity:118 - Input file: examples/smart-farming/farming.json
11:46:47 [INFO ] TCity:120 - => Loading problem specification... 
11:46:47 [INFO ] TCity:124 -  done in 139 ms (0 seconds).
11:46:47 [INFO ] TCity:145 - (*) Hardening analysis not enabled (enable with flag -h)
11:46:47 [INFO ] TCity:150 - (*) Bayesian enabled
11:46:47 [INFO ] BayesianTransformer:245 - Bayesian. Computing inconditional probabilities for graph G(V=40,E=48)
11:46:47 [INFO ] BayesianTransformer:261 - Bayes => sourceNode: attacker, targetNode: target
11:46:47 [INFO ] BayesianTransformer:348 - [TIME] Bayesian generation took 36 ms (0 seconds)
11:46:47 [INFO ] RiskAnalysis:48 - Unconditional probability for target 'target': 0.62050078537184
11:46:47 [INFO ] BayesianAndOrGraphExporter:222 - Writing Graph to DOT file
11:46:49 [INFO ] TCity:160 - == T-CITY ended at 2022-06-10 11:46:49.147 ==
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
