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
	cd src && ${JC} -d ../build *.java

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
	cd build && ${JR} src/BankClient 10 ../config/config3_local.xml

# Run the client - 1 local server
cl1:
	cd build && ${JR} src/BankClient 24 ../config/config1_local.xml
# Run the client - 3 local servers
cl3:
	cd build && ${JR} src/BankClient 24 ../config/config3_local.xml
# Run the client - 5 local servers
cl5:
	cd build && ${JR} src/BankClient 24 ../config/config5_local.xml
# Run the client - 1 remote server
cr1:
	cd build && ${JR} src/BankClient 24 ../config/config1_remote.xml
# Run the client - 3 remote servers
cr3:
	cd build && ${JR} src/BankClient 24 ../config/config3_remote.xml
# Run the client - 5 remote servers
cr5:
	cd build && ${JR} src/BankClient 24 ../config/config5_remote.xml

# Run the server
runs:
	cd build && ${JR} src/BankServer $(id) ../config/config3_local.xml

# Run the server - 1 local server
sl1:
	cd build && ${JR} src/BankServer 0 ../config/config1_local.xml
# Run the server - 3 local servers
sl3:
	cd build && ${JR} src/BankServer $(id) ../config/config3_local.xml

# Run server 0
r0:
	cd build && ${JR} src/BankServer 0 ../config/config3_local.xml
# Run server 1
r1:
	cd build && ${JR} src/BankServer 1 ../config/config3_local.xml
# Run server 2
r2:
	cd build && ${JR} src/BankServer 2 ../config/config3_local.xml
# Run server 3
r3:
	${JR} BankServer 3 config.xml
# Run server 4
r4:
	${JR} BankServer 4 config.xml

# Clean all compiled files
clean:
	@echo "Cleaning All Client & Server Binaries"
	-rm -rf build
	-rm log/*
	-rm log/html/*
