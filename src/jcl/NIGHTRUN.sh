#!/bin/bash
##############################################################################
# NIGHTRUN.sh - Nightly Batch Claims Processing Job
# Simulates mainframe JCL job control
# Runs: Claims Processing -> Policy Lookup -> Adjudication -> Payment Auth
##############################################################################

echo "=========================================="
echo "CONTINENTAL INSURANCE GROUP"
echo "NIGHTLY BATCH PROCESSING JOB"
echo "JOB: NIGHTRUN"
echo "START TIME: $(date)"
echo "=========================================="
echo ""

# Set environment
export COBPATH=./src/copybooks
export COB_LIBRARY_PATH=./bin

# Step 1: Compile all COBOL programs if needed
echo "STEP 1: COMPILE COBOL PROGRAMS"
echo "------------------------------------------"
if [ ! -f "bin/CLMPROC" ] || [ ! -f "bin/POLYLKUP" ] || [ ! -f "bin/ADJUDCTN" ] || [ ! -f "bin/PYMTAUTH" ] || [ ! -f "bin/RPTGEN" ] || [ ! -f "bin/ERRHANDL" ]; then
    echo "Compiling COBOL programs..."
    make all
    if [ $? -ne 0 ]; then
        echo "ERROR: Compilation failed"
        exit 8
    fi
else
    echo "Programs already compiled, skipping..."
fi
echo ""

# Step 2: Run Claims Processing
echo "STEP 2: EXECUTE CLAIMS PROCESSING"
echo "------------------------------------------"
cd "$(dirname "$0")/.." || exit
./bin/CLMPROC
RC=$?

if [ $RC -eq 0 ]; then
    echo "CLAIMS PROCESSING COMPLETED SUCCESSFULLY"
else
    echo "ERROR: CLAIMS PROCESSING FAILED WITH RC=$RC"
    exit $RC
fi
echo ""

# Step 3: Archive reports
echo "STEP 3: ARCHIVE REPORTS"
echo "------------------------------------------"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
if [ -f "reports/summary.txt" ]; then
    cp reports/summary.txt reports/summary_${TIMESTAMP}.txt
    echo "Report archived to: reports/summary_${TIMESTAMP}.txt"
else
    echo "WARNING: No summary report found"
fi
echo ""

echo "=========================================="
echo "JOB NIGHTRUN COMPLETED"
echo "END TIME: $(date)"
echo "RETURN CODE: $RC"
echo "=========================================="

exit $RC
