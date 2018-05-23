# super simple makefile
# call it using 'make NAME=name_of_code_file_without_extension'
# (assumes a .java extension)
NAME = "Main"
SRC = "src"
OUT = "out"

all:
	@echo "Compiling..."
	[ -d $(OUT) ] || mkdir $(OUT)
	javac -cp $(SRC) -d $(OUT) $(SRC)/*.java

run: all
	@echo "Running..."
	java -cp $(OUT) $(NAME)

clean:
	rm -rf $(OUT)/*.class
