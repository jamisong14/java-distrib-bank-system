# Authors:
#  - Jamison Grudem (grude013)
#  - Manan Mrig (mrig0001)

# Usage
# 	make - Make all binaries
# 	make server - Make server binaries
# 	make client - Make client binaries
# 	make runc - Run client
# 	make runs - Run server

# Predefined Variables: used for compilation and running
JC = javac
JR = java

# Optional parameters
p?=1099# Default port
t?=100# Default client thread count
i?=100# Default client thread iterations
id?=0# Default server id

# Compile all the files
all:
	-make clean
	@echo "Compiling All Client & Server"
	${JC} *.java

# Compile all server files
server:
	@echo "Compiling BankServer Request ClientAccRequest"
	${JC} BankServer.java

# Compile all client files
client:
	@echo "Compiling BankClient"
	${JC} BankClient.java

# Run the client
runc:
	@echo "Running BankClient"
	${JR} BankClient 1 config.xml

# Run the server
runs:
	@echo "Running BankServer"
	${JR} BankServer $(id) config.xml
# Run server 0
r0:
	@echo "Running BankServer 0"
	${JR} BankServer 0 config.xml
# Run server 1
r1:
	@echo "Running BankServer 1"
	${JR} BankServer 1 config.xml
# Run server 2
r2:
	@echo "Running BankServer 2"
	${JR} BankServer 2 config.xml
# Run server 3
r3:
	@echo "Running BankServer 3"
	${JR} BankServer 3 config.xml
# Run server 4
r4:
	@echo "Running BankServer 4"
	${JR} BankServer 4 config.xml

# Clean all compiled files
clean:
	@echo "Cleaning All Client & Server Binaries"
	-rm *.class
	-rm clientOutput
	-rm client.log
	-rm server*.log
	
# Clean just log and output files
log_clean:
	@echo "Cleaning All Log and Output Files"
	-rm client.log
	-rm server*.log
