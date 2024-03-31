# Author: Jamison Grudem (grude013)
# 
# Using 2 grace days

# Predefined Variables: used for compilation and running
JC = javac
JR = java
BDIR = build
CTARG = src/BankClient
SARG = src/BankServer

# Optional parameters
s?=1# Default server count, 1,3, or 5
t?=24# Default client thread count, any integer
id?=0# Default server id, 0-5
loc=local# Default location, "local" or "remote"

# Compile all dependencies
# 	Ex1: "make"
# 	Ex2: "make all"
all: 
	-make clean
	mkdir log
	mkdir log/html
	cd src && ${JC} -d ../${BDIR} *.java

# Run the client
#	Ex1: "make client t=24" 						[run local client with 24 threads and 3 servers]
#	Ex2: "make client t=10 s=5 loc=remote" 			[run remote client with 10 threads and 5 servers]
# 	Ex3: "make client t=100 s=1 loc=local" 			[run local client with 100 threads and 1 server]
client:
	cd ${BDIR} && ${JR} ${CTARG} $(t) ../config/config$(s)_$(loc).xml

# Run the server
# 	Ex1: "make server id=0" 						[run server with id 0 locally with 3 servers]
#   Ex2: "make server id=3 s=5 loc=remote" 			[run server with id 3 remotely with 5 servers]
# 	BAD Ex3: "make server id=5 s=1 loc=local" 		[run server with id 5 locally with 1 server -- WILL NOT WORK, ONLY 1 SERVER]
server:
	cd ${BDIR} && ${JR} ${SARG} $(id) ../config/config$(s)_$(loc).xml

# Clean all build files and logs
# 	Ex1: "make clean"
clean:
	@echo "Cleaning All Client & Server Binaries"
	-rm -rf ${BDIR}
	-rm -rf log
