# Performance Evaluation Metrics
Using 2 grace days  


The servers used to evaluate the performance of the system can be found in the `config` directory. Each remote/local and server count configuration has its own file. Each setup has the file used for its evaluation listed next to it. Please refer to this file to see the hostnames and ports used for the evaluation.

## Remote Evaluation (All Servers On Different Host)
### 1 Server 
* **File Used: `config1_remote.xml`**
* Total Transfers: 4,800 (24 threads, 200 iterations each)
* Average Transfer Time (as observed by Client): ~0.2647s
* Average Transfer Time (as observed by Server0): ~0.2547s
### 3 Servers
* **File Used: `config3_remote.xml`**
* Total Transfers: 4,800 (24 threads, 200 iterations each)
* Average Transfer Time (as observed by Client): ~0.8086s
* Average Transfer Time (as observed by Server0): ~0.181s
* Average Transfer Time (as observed by Server1): ~1.770s
* Average Transfer Time (as observed by Server2): ~0.321s
* Total Average Server Transfer Time: 0.757s
### 5 Servers 
* **File Used: `config5_remote.xml`**
* Total Transfers: 4,800 (24 threads, 200 iterations each)
* Average Transfer Time (as observed by Client): ~1.3992s
* Average Transfer Time (as observed by Server0): ~5.072s
* Average Transfer Time (as observed by Server1): ~0.554s
* Average Transfer Time (as observed by Server2): ~0.314s
* Average Transfer Time (as observed by Server3): ~0.718s
* Average Transfer Time (as observed by Server4): ~0.690s
* Total Average Server Transfer Time: 1.4696s


## Local Evaluation (All Servers On Same Host)
### 1 Server 
* **File Used: `config1_local.xml`**
* Total Transfers: 4,800 (24 threads, 200 iterations each)
* Average Transfer Time (as observed by Client): ~0.1929s
* Average Transfer Time (as observed by Server0): ~0.1925s
### 3 Servers 
* **File Used: `config3_local.xml`**
* Total Transfers: 4,800 (24 threads, 200 iterations each)
* Average Transfer Time (as observed by Client): ~0.0438s
* Average Transfer Time (as observed by Server0): ~0.0115s
* Average Transfer Time (as observed by Server1): ~0.0188s
* Average Transfer Time (as observed by Server2): ~0.1095s
* Total Average Server Transfer Time: 0.0466s
### 5 Servers 
* **File Used: `config5_local.xml`**
* Total Transfers: 4,800 (24 threads, 200 iterations each)
* Average Transfer Time (as observed by Client): ~0.0720s
* Average Transfer Time (as observed by Server0): ~0.0267s
* Average Transfer Time (as observed by Server1): ~0.0192s
* Average Transfer Time (as observed by Server2): ~0.0139s
* Average Transfer Time (as observed by Server3): ~0.0199s
* Average Transfer Time (as observed by Server4): ~0.2573s
* Total Average Server Transfer Time: 0.0674s
