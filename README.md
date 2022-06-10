# T-CITY
## Cyber-Physical Attack Graph (CPAG) Analyser for Smart Cities

### Version 0.64.0

## Contents
- [About T-CITY](#about-t-city)
- [Requirements](#requirements)
- [Usage](#usage)
- [CPAG main concepts](#cpag-main-concepts)
- [Execution examples](#execution-examples)
- [Configuration parameters](#configuration-parameters)
- [Licence](#licence)

## About T-CITY
T-CITY stands for **T**rustworthy **C**ritical **I**nfras**T**ructure S**Y**stems. The objective of the T-CITY tool 
is to assist security practitioners in the design and analysis of **Cyber-Physical Attack Graphs (CPAGs)**. A cyber-physical attack graph is a mathematical security tool aimed at modelling the different ways in which an attacker can compromise specific assets in a networked cyber-physical system. More specifically, a CPAG logically encodes the different attack paths that an attacker can follow by exploiting cyber and/or physical weaknesses and moving laterally across the network towards their target. The research work behind T-CITY focuses on the formal specification of individual CPAG units that can be subsequently combined to produce complex CPAG as well as techniques to analyse them. Overall, CPAGs aim at extending the reach of traditional attack graphs by providing a unified approach to analyse multi-stage cyber-physical attacks and their operational impact on smart cities and critical infrastructure systems.

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

## CPAG main concepts

The base building block of a CPAG is called a CPAG unit and is composed of three main parts: `Precondition(s)` -> `Attack` -> `Postcondition(s)`. 
The main idea is that the execution of an atomic attack requires a number of preconditions to be fulfilled by the attacker (e.g. possessing certain security privileges in the target network). If the attack is successful, then the attacker gains one or more new security privileges (postconditions) that can be used as preconditions for further attacks, hence the possibility of conducting multi-stage attacks. 

A CPAG unit is in itself a CPAG. CPAG units can be combined to produce more complex CPAGs. 
In practice, some attacks may require complex combinations of preconditions to be properly executed. CPAGs can express these combainations in the form of  precondition trees that are logically combined and connected to attack actions. 

There are mainly three types of nodes: security privileges, actions, and logical operators. 
* **Security privileges** are represented as ellipses and include cyber privileges, physical privileges, impact nodes, and custom privileges. 
* **Action nodes** (attacks) are represented as rectangles. 
* **Logical operators** include AND and OR gates (rhomboidal shape), and a splitter gate (octagonal shape) that captures multiple postconditions from a single attack action. 

<!-- CPAGs follow a classical requires/provides model for computer attacks in the form of `Precondition(s)` -> `Attack` -> `Postcondition(s)`. -->
Nodes are connected with directed edges that represent the transition between security privilegs and attack actions. 
The T-CITY graphical editor allows designers to easily create graphs using the right mouse button although it can also import existing CPAGs in JSON format. 
CPAG nodes and edges can also have associated values that can be used for further analysis, e.g. Bayesian CPAGs for risk analysis (described later), costs for network hardening (currently under development), among others. 




## Execution examples

### CPAG design and composition
Designing attack graphs for complex scenarios can be a very challenging task. T-CITY aims at assisting CPAG designers to build manageable CPAG units and combine them to produce more complex CPAGs. Here we present an example with four CPAG units and its final composition. 

- Open the T-CITY graphical interface: `$> java -jar tcity.jar -g`
- Go to Menu -> `File` -> `Open CPAG (JSON)` and navigate to the first CPAG unit at `examples/merge/cpag-unit1.json`
- You should see the following CPAG unit: 

<!-- ![Screenshot - CPAG unit1](https://github.com/mbarrere/tcity/blob/main/screenshots/cpag-unit1.png) -->
<p align="center"> <img src="https://github.com/mbarrere/tcity/blob/main/screenshots/cpag-unit1.png" alt="Screenshot - CPAG unit1" width="80%"> </p>

Now go to Menu -> `Operations` -> `Merge CPAGs` and open the second CPAG unit at `examples/merge/cpag-unit2.json`. The following CPAG unit will be merged with the current CPAG: 
<!-- ![Screenshot - CPAG unit2](https://github.com/mbarrere/tcity/blob/main/screenshots/cpag-unit2.png) -->
<p align="center"> <img src="https://github.com/mbarrere/tcity/blob/main/screenshots/cpag-unit2.png" alt="Screenshot - CPAG unit2" width="80%"> </p>

Repeat the merging process with CPAG unit 3 (at `examples/merge/cpag-unit3.json`)
<!-- ![Screenshot - CPAG unit3](https://github.com/mbarrere/tcity/blob/main/screenshots/cpag-unit3.png) -->
<p align="center"> <img src="https://github.com/mbarrere/tcity/blob/main/screenshots/cpag-unit3.png" alt="Screenshot - CPAG unit3" width="80%"> </p>

Repeat the merging process with CPAG unit 4 (at `examples/merge/cpag-unit4.json`)
<!-- ![Screenshot - CPAG unit4](https://github.com/mbarrere/tcity/blob/main/screenshots/cpag-unit4.png) -->
<p align="center"> <img src="https://github.com/mbarrere/tcity/blob/main/screenshots/cpag-unit4.png" alt="Screenshot - CPAG unit4" width="80%"> </p>

After merging the four CPAG units, you should see the following complex CPAG: 
<!-- ![Screenshot - Complex CPAG](https://github.com/mbarrere/tcity/blob/main/screenshots/complex-cpag.png) -->
<p align="center"> <img src="https://github.com/mbarrere/tcity/blob/main/screenshots/complex-cpag.png" alt="Screenshot - Complex CPAG" width="80%"> </p>

#

### Risk analysis with Bayesian CPAGs (graphical interface)
Bayesian CPAGs are CPAGs whose edges have associated conditional probabilities. T-CITY can automatically compute the marginal probabilities of the security privileges, which are understood as the risk (or likelihood) of an attacker reaching specific assets in the network. The following example involves a simplified CPAG designed to analyse cyber-physical attacks in a smart farming context. 

- Open the T-CITY graphical interface: `$> java -jar tcity.jar -g`
- Go to Menu -> `File` -> `Open CPAG (JSON)` and navigate to the smart farming scenario at `examples/smart-farming/farming.json`
- Go to Menu -> `CPAG Metrics` -> `Auto Bayesian risk`
- You should see the following Bayesian CPAG:

<!-- ![Screenshot - T-CITY editor - Smart farming scenario](https://github.com/mbarrere/tcity/blob/main/screenshots/cpag-editor-farming.png) -->
<p align="center"> <img src="https://github.com/mbarrere/tcity/blob/main/screenshots/cpag-editor-farming.png" alt="Screenshot - T-CITY editor - Smart farming scenario" width="100%"> </p>

#

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

<!-- ![Screenshot - Smart farming scenario with Graphviz](https://github.com/mbarrere/tcity/blob/main/screenshots/farming2-bn.png) -->
<p align="center"> <img src="https://github.com/mbarrere/tcity/blob/main/screenshots/farming2-bn.png" alt="Screenshot - Smart farming scenario with Graphviz" width="100%"> </p>


---

## Configuration parameters
The configuration parameters are stored in the file `tcity.conf`.
The tool also accepts a different configuration file as argument [-c configFile] to override the configuration in *tcity.conf*. If the file `tcity.conf` is not present, T-CITY uses the default configuration values.


## Licence
Apache License 2.0
