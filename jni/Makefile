JAVA_HOME = /usr/lib/jvm/java-8-openjdk-amd64
CMS_DIR = /home/adam/Programs/cryptominisat/build

CLASS_DIR = ../bin
CLASS = net.ssehub.kernel_haven.cnf.CryptoMiniSatSolver
JNI_HEADER = $(subst .,_,$(CLASS)).h
JAVA_SOURCE = ../src/$(subst .,/,$(CLASS)).java
CPP_SOURCE = $(subst .,_,$(CLASS)).cpp
JNI_LIB = ../res/net/ssehub/kernel_haven/cnf/cryptominisat5-jni.so

$(JNI_LIB): $(JNI_HEADER) $(CPP_SOURCE)
	g++ -shared -fPIC -std=c++11 -I ${JAVA_HOME}/include -I ${JAVA_HOME}/include/linux -I $(CMS_DIR)/include -o $@ $(CPP_SOURCE) $(CMS_DIR)/lib/libcryptominisat5.a

$(JNI_HEADER): $(JAVA_SOURCE)
	javah -classpath $(CLASS_DIR) $(CLASS)

clean:
	rm $(JNI_HEADER) $(JNI_LIB)

.PHONY: clean
