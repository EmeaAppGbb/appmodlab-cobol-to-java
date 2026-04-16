# Makefile for Continental Insurance Group COBOL Application
# Uses GnuCOBOL compiler (cobc)

# Compiler and flags
COBC = cobc
COBCFLAGS = -x -std=mf -I./src/copybooks -fixed
COBCFLAGS_MODULE = -m -std=mf -I./src/copybooks -fixed

# Directories
SRC_DIR = src/cobol
COPYBOOK_DIR = src/copybooks
BIN_DIR = bin
DATA_DIR = data
REPORT_DIR = reports

# Source files
MAIN_PROGRAMS = $(SRC_DIR)/CLMPROC.cbl
SUBPROGRAMS = $(SRC_DIR)/POLYLKUP.cbl \
              $(SRC_DIR)/ADJUDCTN.cbl \
              $(SRC_DIR)/PYMTAUTH.cbl \
              $(SRC_DIR)/RPTGEN.cbl \
              $(SRC_DIR)/ERRHANDL.cbl

# Binaries
MAIN_BIN = $(BIN_DIR)/CLMPROC
SUBPROG_BINS = $(BIN_DIR)/POLYLKUP.so \
               $(BIN_DIR)/ADJUDCTN.so \
               $(BIN_DIR)/PYMTAUTH.so \
               $(BIN_DIR)/RPTGEN.so \
               $(BIN_DIR)/ERRHANDL.so

# Default target
.PHONY: all
all: setup $(SUBPROG_BINS) $(MAIN_BIN)
	@echo "Build complete!"
	@echo "Run: ./bin/CLMPROC or make run"

# Setup directories
.PHONY: setup
setup:
	@mkdir -p $(BIN_DIR)
	@mkdir -p $(REPORT_DIR)

# Compile main program
$(BIN_DIR)/CLMPROC: $(SRC_DIR)/CLMPROC.cbl $(SUBPROG_BINS)
	@echo "Compiling main program: CLMPROC..."
	$(COBC) $(COBCFLAGS) -o $@ $< -L$(BIN_DIR)

# Compile subprograms as modules
$(BIN_DIR)/POLYLKUP.so: $(SRC_DIR)/POLYLKUP.cbl
	@echo "Compiling module: POLYLKUP..."
	$(COBC) $(COBCFLAGS_MODULE) -o $@ $<

$(BIN_DIR)/ADJUDCTN.so: $(SRC_DIR)/ADJUDCTN.cbl
	@echo "Compiling module: ADJUDCTN..."
	$(COBC) $(COBCFLAGS_MODULE) -o $@ $<

$(BIN_DIR)/PYMTAUTH.so: $(SRC_DIR)/PYMTAUTH.cbl
	@echo "Compiling module: PYMTAUTH..."
	$(COBC) $(COBCFLAGS_MODULE) -o $@ $<

$(BIN_DIR)/RPTGEN.so: $(SRC_DIR)/RPTGEN.cbl
	@echo "Compiling module: RPTGEN..."
	$(COBC) $(COBCFLAGS_MODULE) -o $@ $<

$(BIN_DIR)/ERRHANDL.so: $(SRC_DIR)/ERRHANDL.cbl
	@echo "Compiling module: ERRHANDL..."
	$(COBC) $(COBCFLAGS_MODULE) -o $@ $<

# Run the application
.PHONY: run
run: all
	@echo "Running claims processing..."
	@export COB_LIBRARY_PATH=$(BIN_DIR) && $(MAIN_BIN)

# Run nightly batch job
.PHONY: nightrun
nightrun: all
	@echo "Running nightly batch job..."
	@bash src/jcl/NIGHTRUN.sh

# Run month-end job
.PHONY: monthend
monthend: all
	@echo "Running month-end processing..."
	@bash src/jcl/MONTHEND.sh

# Generate reports
.PHONY: report
report: all
	@echo "Generating reports..."
	@bash src/jcl/RPTJOB.sh

# Clean build artifacts
.PHONY: clean
clean:
	@echo "Cleaning build artifacts..."
	@rm -rf $(BIN_DIR)/*
	@rm -f $(REPORT_DIR)/*.txt
	@echo "Clean complete!"

# Clean everything including reports
.PHONY: distclean
distclean: clean
	@rm -rf $(REPORT_DIR)/*
	@echo "Distribution clean complete!"

# Show help
.PHONY: help
help:
	@echo "Continental Insurance Group - COBOL Build System"
	@echo ""
	@echo "Available targets:"
	@echo "  make all        - Compile all programs"
	@echo "  make run        - Compile and run claims processing"
	@echo "  make nightrun   - Run nightly batch job"
	@echo "  make monthend   - Run month-end processing"
	@echo "  make report     - Generate reports"
	@echo "  make clean      - Remove build artifacts"
	@echo "  make distclean  - Remove all generated files"
	@echo "  make help       - Show this help message"
