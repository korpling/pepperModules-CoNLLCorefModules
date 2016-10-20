![SaltNPepper project](./gh-site/img/SaltNPepper_logo2010.png)
# CoNLLCorefModules
This repository provides support for bracketed coreference in the CoNLL shared task coreference style, within the Pepper converter framework (see https://u.hu-berlin.de/saltnpepper). So far, only an Exporter module for the format has been developed. In the future, an importer may also be added.

Pepper is a pluggable framework to convert a variety of linguistic formats (like [TigerXML](http://www.ims.uni-stuttgart.de/forschung/ressourcen/werkzeuge/TIGERSearch/doc/html/TigerXML.html), the [EXMARaLDA format](http://www.exmaralda.org/), [PAULA](http://www.sfb632.uni-potsdam.de/paula.html) etc.) into each other. Furthermore Pepper uses Salt (see https://github.com/korpling/salt), the graph-based meta model for linguistic data, which acts as an intermediate model to reduce the number of mappings to be implemented. This means converting data from a format _A_ to a format _B_ consists of two steps. First the data is mapped from format _A_ to Salt and second from Salt to format _B_. This detour reduces the number of Pepper modules from _n<sup>2</sup>-n_ (in the case of a direct mapping) to _2n_ to handle  n formats.

![n:n mappings via SaltNPepper](./gh-site/img/puzzle.png)

In Pepper there are three different types of modules:
* importers (to map a format _A_ to a Salt model)
* manipulators (to map a Salt model to a Salt model, e.g. to add additional annotations, to rename things to merge data etc.)
* exporters (to map a Salt model to a format _B_).

For a simple Pepper workflow you need at least one importer and one exporter. This project currently supplies an importer for WebAnno TSV format, version 3.

## Requirements
Since the module provided here is a plugin for Pepper, you need an instance of the Pepper framework. If you do not already have a running Pepper instance, click on the link below and download the latest stable version (not a SNAPSHOT):

> Note:
> Pepper is a Java based program, therefore you need to have at least Java 7 (JRE or JDK) on your system. You can download Java from https://www.oracle.com/java/index.html or http://openjdk.java.net/ .


## Install module
If this Pepper module is not yet contained in your Pepper distribution, you can easily install it. Just open a command line and enter one of the following program calls:

**Windows**
```
pepperStart.bat 
```

**Linux/Unix**
```
bash pepperStart.sh 
```

Then type in command *is* and the path from where to install the module:
```
pepper> update de.hu_berlin.german.korpling.saltnpepper::pepperModules-CoNLLCorefModules::https://korpling.german.hu-berlin.de/maven2/
```

## Usage
To use this module in your Pepper workflow, put the following lines into the workflow description file. Note the fixed order of xml elements in the workflow description file: &lt;importer/>, &lt;manipulator/>, &lt;exporter/>. The CoNLLCorefExporter is an exporter module, which can be addressed by one of the following alternatives.
A detailed description of the Pepper workflow can be found on the [Pepper project site](https://u.hu-berlin.de/saltnpepper). 

### a) Identify the module by name

```xml
<exporter name="CoNLLCorefExporter" path="PATH_TO_CORPUS"/>
```

### b) Identify the module by formats

```xml
<exporter formatName="ConllCoref" formatVersion="1.0" path="PATH_TO_CORPUS"/>
```

### c) Use properties

```xml
<exporter name="CoNLLCorefExporter" path="PATH_TO_CORPUS">
  <property key="PROPERTY_NAME">PROPERTY_VALUE</property>
</exporter>
```

## Contribute
Since this Pepper module is under a free license, please feel free to fork it from github and improve the module. If you think that others can benefit from your improvements, don't hesitate to make a pull request, so that your changes can be merged.
If you have found any bugs, or have some feature request, please open an issue on github. If you need any help, please write an e-mail to saltnpepper@lists.hu-berlin.de .

## Funders
This project module was developed at Georgetown University. 

## License
  Copyright 2016 Amir Zeldes, Georgetown University.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.


# <a name="details1">CoNLLCorefExporter</a>

## Properties

|name of property			|possible values		|default value|	
|---------------------|-------------------|-------------|
|ConllCoref.Exporter.NodeLayer			    |String           |''|
|ConllCoref.Exporter.EdgeType  |String       |''|
|ConllCoref.Exporter.EdgeAnno  |String       |''|
|ConllCoref.Exporter.RemoveSingletons  |true,false       |false|

### NodeLayer

The name of the Salt layer identifying spans to be exported. Default is "", meaning any span will be exported.

### EdgeType

Name of the edge type identifying two spans as belonging to the same output ID, e.g. 'coref'. Can be supplied as a regular expression matching multiple edge types. Default is '' (any pointing relation detected in data - 
usually not a good idea if your data contains both dependencies and coreference).

### EdgeAnno

A String supplying a single, space separated key-value pair denoting a required edge annotation for including a pointing relation. Both the annotation name and value can be regular expressions, but may not contain the equals sign. For example: `coref_type=ana|appos` will cause only pointing relations with an annotation `coref_type` and value matching `ana` or `appos` to be considered. Default is '' (no annotation is required).

### RemoveSingletons

Boolean, whether to include nodes matching the NodeLayer, even if they are not connected to any other node via a pointing relation (i.e. singletons). Default is false, meaning singletons are included.

## Example output format

The output gives three columns: zero based token index, the token and potentially multiple coreference IDs. Clashes are prevented via pipes (e.g. a line can include a column `(23|48)` to indicate ID 23 opening and 48 closing)

```
# begin document GUM_interview_ants
0	Biologist	(1
1	Nick	_
2	Bos	1)
3	tells	_
4	Wikinews	(2)
5	about	_
6	'	_
7	self-medicating	(3
8	'	_
9	ants	3)
10	Tuesday	(4)
11	,	_
12	September	(4
13	1	_
14	,	_
15	2015	4)
16	Formica	(3
17	fusca	3)
18	,	_
19	from	_
20	file	(5)
21	.	_
22	Image	(6)
23	:	_
24	Mathias	(7
25	Krumbholz	7)
26	.	_
27	Nick	(1
28	Bos	1)
29	,	_
30	of	_
31	the	(8
32	University	_
33	of	_
34	Helsinki	8)
35	,	_
36	studies	_
37	"	_
38	the	(9
39	amazing	_
40	adaptations	9)
41	social	(10
42	insects	10)
43	have	_
44	evolved	_
45	in	_
46	order	_
47	to	_
48	fight	_
49	the	(11
50	extreme	_
51	parasite	(12)
52	pressure	11)
53	they	(10)
54	experience	_
55	"	_
56	.	_
...
# end document

```