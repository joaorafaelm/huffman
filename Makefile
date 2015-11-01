# JAVAC VERSÃO: 1.6.0_65
BUILD_DIR = Build/
SOURCE_DIR = Dependencias/

FILES = Huffman.java
		
all: 
	javac -Xlint:unchecked -d $(BUILD_DIR) $(FILES)
	rm -f *.class 
	rm -f $(SOURCE_DIR)*.class 

random:
	base64 /dev/urandom | head -c 10 > "file.txt"
	
clean:
	rm -f *.class 
	rm -f $(BUILD_DIR)*.class 
	rm -f $(BUILD_DIR)$(SOURCE_DIR)*.class 
	rm -f $(SOURCE_DIR)*.class 
