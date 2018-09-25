SRC = ./src/java/assignment
TST = ./src/test/assignment
INCLUDE = ./include
OBJECTS = $(addsuffix .class, $(addprefix assignment/, \
          $(basename $(notdir $(wildcard $(SRC)/*)))))
TST_OBJECTS = $(addsuffix .class, $(addprefix assignment/, \
              $(basename $(notdir $(wildcard $(TST)/*)))))
LIBS = $(addsuffix .jar, $(basename $(wildcard $(INCLUDE)/*)))
BUILD = .
FLAGS = -cp '$(BUILD):$(INCLUDE)/*'



# run the built jar file
run: all
	java -jar out.jar

# run the test file
test: all
	java -jar test.jar

# build everything
all: $(OBJECTS) $(TST_OBJECTS) $(LIBS)
	jar cfm out.jar mainManifest.mf $(OBJECTS) $(LIBS)
	jar cfm test.jar testManifest.mf $(OBJECTS) $(TST_OBJECTS) $(LIBS)

# this tells it that twitter scanner class depends on ParameterStringBuilder class so that
# gets compiled first
assignment/TwitterScanner.class: assignment/ParameterStringBuilder.class
assignment/ParameterStringBuilderTest.class: assignment/ParameterStringBuilder.class

# generic building instructions for the class files
assignment/%.class: $(SRC)/%.java  $(LIBS)
	javac $(FLAGS) -d $(BUILD) $<

# genereic building instructions for the test class files
assignment/%.class: $(TST)/%.java $(LIBS)
	javac $(FLAGS) -d $(BUILD) $<

# remove all junk
clean:
	rm -f assignment/* *.jar testResult.txt
