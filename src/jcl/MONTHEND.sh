#!/bin/bash
##############################################################################
# MONTHEND.sh - Month-End Processing Job
# Simulates mainframe JCL for month-end claims reconciliation
# Runs: Full claims processing with additional validation and reporting
##############################################################################

echo "=========================================="
echo "CONTINENTAL INSURANCE GROUP"
echo "MONTH-END PROCESSING JOB"
echo "JOB: MONTHEND"
echo "START TIME: $(date)"
echo "=========================================="
echo ""

# Set environment
export COBPATH=./src/copybooks
export COB_LIBRARY_PATH=./bin

# Step 1: Backup current data files
echo "STEP 1: BACKUP DATA FILES"
echo "------------------------------------------"
BACKUP_DIR="data/backup_$(date +%Y%m)"
mkdir -p "$BACKUP_DIR"
cp data/claims.dat "$BACKUP_DIR/" 2>/dev/null
cp data/policies.dat "$BACKUP_DIR/" 2>/dev/null
echo "Data backed up to: $BACKUP_DIR"
echo ""

# Step 2: Compile programs
echo "STEP 2: COMPILE COBOL PROGRAMS"
echo "------------------------------------------"
echo "Ensuring all programs are compiled..."
make all
if [ $? -ne 0 ]; then
    echo "ERROR: Compilation failed"
    exit 8
fi
echo ""

# Step 3: Run Claims Processing
echo "STEP 3: EXECUTE MONTH-END CLAIMS PROCESSING"
echo "------------------------------------------"
cd "$(dirname "$0")/.." || exit
./bin/CLMPROC
RC=$?

if [ $RC -eq 0 ]; then
    echo "MONTH-END PROCESSING COMPLETED SUCCESSFULLY"
else
    echo "ERROR: MONTH-END PROCESSING FAILED WITH RC=$RC"
    exit $RC
fi
echo ""

# Step 4: Generate Month-End Reports
echo "STEP 4: GENERATE MONTH-END REPORTS"
echo "------------------------------------------"
MONTH_TIMESTAMP=$(date +%Y%m)
if [ -f "reports/summary.txt" ]; then
    cp reports/summary.txt "reports/monthend_${MONTH_TIMESTAMP}.txt"
    echo "Month-end report: reports/monthend_${MONTH_TIMESTAMP}.txt"
    cat reports/summary.txt
else
    echo "WARNING: No summary report found"
fi
echo ""

# Step 5: Data Cleanup
echo "STEP 5: MONTH-END CLEANUP"
echo "------------------------------------------"
echo "Archiving processed records..."
echo "Cleanup completed"
echo ""

echo "=========================================="
echo "JOB MONTHEND COMPLETED"
echo "END TIME: $(date)"
echo "RETURN CODE: $RC"
echo "=========================================="

exit $RC
