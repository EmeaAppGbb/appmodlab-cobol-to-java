#!/bin/bash
##############################################################################
# RPTJOB.sh - Report Generation Job
# Simulates mainframe JCL for on-demand report generation
# Generates summary reports from existing claims data
##############################################################################

echo "=========================================="
echo "CONTINENTAL INSURANCE GROUP"
echo "REPORT GENERATION JOB"
echo "JOB: RPTJOB"
echo "START TIME: $(date)"
echo "=========================================="
echo ""

# Set environment
export COBPATH=./src/copybooks
export COB_LIBRARY_PATH=./bin

# Step 1: Verify programs are compiled
echo "STEP 1: VERIFY PROGRAMS"
echo "------------------------------------------"
if [ ! -f "bin/CLMPROC" ]; then
    echo "Programs not compiled. Running compilation..."
    make all
    if [ $? -ne 0 ]; then
        echo "ERROR: Compilation failed"
        exit 8
    fi
else
    echo "Programs verified"
fi
echo ""

# Step 2: Run Claims Processing (to generate fresh reports)
echo "STEP 2: GENERATE REPORTS"
echo "------------------------------------------"
cd "$(dirname "$0")/.." || exit
./bin/CLMPROC
RC=$?

if [ $RC -eq 0 ]; then
    echo "REPORT GENERATION COMPLETED SUCCESSFULLY"
else
    echo "ERROR: REPORT GENERATION FAILED WITH RC=$RC"
    exit $RC
fi
echo ""

# Step 3: Display Report
echo "STEP 3: DISPLAY SUMMARY REPORT"
echo "------------------------------------------"
if [ -f "reports/summary.txt" ]; then
    cat reports/summary.txt
else
    echo "ERROR: No report generated"
    exit 12
fi
echo ""

echo "=========================================="
echo "JOB RPTJOB COMPLETED"
echo "END TIME: $(date)"
echo "RETURN CODE: $RC"
echo "=========================================="

exit $RC
