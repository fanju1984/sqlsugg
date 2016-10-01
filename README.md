# sqlsugg

We will show the steps of building _SQLSugg_ from source in this page

### Dependencies
_SQLSugg_ is developed and tested on Linux-like platforms. Before installing it, please make sure the following dependencies are properly installed and the corresponding environment variables are correctly set.
+ **Java**: the back-end of _SQLSugg_ is developed using java (version >= 1.8).
+ **Gradle**: the back-end is built by using [Gradle] (http://gradle.org/), a light-weight and open-sourced build tool. The installation of Gradle can be referred to [this] (http://gradle.org/gradle-download/).
+ **MySQL**: we store the data in MySQL. The installation of MySQL can be referred to [this] (http://www.mysql.com/).

### Clone from Github
If you want to use the latest code, please clone it from our Github repo using the following commands. Note that the current repo is private, and so make sure you are one of the collaborators. 

    $ git clone https://github.com/fanju1984/sqlsugg
    $ cd etl

Next, we show how to build _SQLSugg_ from the source code. 

### Build _SQLSugg_
First, you can see a configuration file `gradle.build` for gradle build. Using this file, you can configure the dependencies and fat-jars of the backends. If you did not change the code, you can simply use the default `gradle.build` and type the following commands. 

    $ gradle clean build fatjar
    $ cd build/libs

Then, you will see a built jar file, e.g., `sqlsugg-all-0.1.0-alpha.jar`. You may want to copy the jar file into the _bin_ folder.
   
    $ cp build/libs/sqlsugg-all-0.1.0-alpha.jar bin/

Now, we have built the code successfully. 

### Load the example data and register it

We have prepared an example data set for illustration purpose. You can load the data into MySQL by typing the following commands in an MySQL client. 
   
    $ source SQLSUGG_HOME/example/sqlsugg_sample.sql

Note: please make sure to build indices on all the ID attributes of each data table. 

Then, register the dataset by setting the configuration file at `etc/dataset_config.json`.

### Run _SQLSugg_ - 1: Build the keyword-oriented indices

To suggest SQL in an online manner, you first need to build indices at the offline stage. 

(1) Build the indices from keywords to various database elements. 

    $ java -classpath bin/sqlsugg-all-0.1.0-alpha.jar sqlsugg.launcher.KeywordIndexer sample K2V
    $ java -classpath bin/sqlsugg-all-0.1.0-alpha.jar sqlsugg.launcher.KeywordIndexer sample K2R
    $ java -classpath bin/sqlsugg-all-0.1.0-alpha.jar sqlsugg.launcher.KeywordIndexer sample K2M
    $ java -classpath bin/sqlsugg-all-0.1.0-alpha.jar sqlsugg.launcher.KeywordIndexer sample K2F

Note that `sample` is the name of the dataset and `K2*` is the type of index.

(2) Build the index from keywords to records

    $ java -classpath bin/sqlsugg-all-0.1.0-alpha.jar sqlsugg.launcher.InvIndexer sample

(3) Compute the weight of database

    $ java -classpath bin/sqlsugg-all-0.1.0-alpha.jar sqlsugg.launcher.WeightComputation sample

### Run _SQLSugg_ - 2: Run _SQLSugg_ in command line

    $ java -classpath bin/sqlsugg-all-0.1.0-alpha.jar sqlsugg.launcher.SingleSugg sample 2 "data"
    
Note that `sample` is the name of the dataset `2` is the top-k, and `data` is the keyword query.
