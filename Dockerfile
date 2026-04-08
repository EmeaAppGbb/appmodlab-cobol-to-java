# Continental Insurance Group - COBOL Application
# GnuCOBOL Docker Container

FROM ubuntu:22.04

# Set metadata
LABEL maintainer="Continental Insurance Group"
LABEL description="COBOL-85 Claims Processing System with GnuCOBOL"
LABEL version="1.0"

# Prevent interactive prompts during installation
ENV DEBIAN_FRONTEND=noninteractive

# Install GnuCOBOL and dependencies
RUN apt-get update && apt-get install -y \
    gnucobol \
    make \
    bash \
    coreutils \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Set up environment variables for COBOL
ENV COB_LIBRARY_PATH=/app/bin
ENV COBPATH=/app/src/copybooks

# Create application directory
WORKDIR /app

# Copy application files
COPY src/ ./src/
COPY data/ ./data/
COPY Makefile ./

# Create output directories
RUN mkdir -p bin reports

# Compile COBOL programs
RUN make all

# Default command runs the main claims processing program
CMD ["make", "run"]

# Alternative: Use bash as entrypoint for interactive use
# ENTRYPOINT ["/bin/bash"]
