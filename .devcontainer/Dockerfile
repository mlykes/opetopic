FROM eclipse-temurin:11-jdk

# eclipse-temurin ships a built-in 'ubuntu' user at UID/GID 1000. Rename it to match
# the host user before common-utils runs, so the feature reuses UID 1000 instead of
# falling back to 1001. Mismatched UIDs cause AccessDeniedException on mounted files.
RUN usermod -l mlykes -d /home/mlykes -m ubuntu \
 && groupmod -n mlykes ubuntu

RUN apt-get update && apt-get install -y curl gnupg git && \
    # Node.js (required for Scala.js compilation pipeline)
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash - && \
    apt-get install -y nodejs && \
    # SBT
    curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | gpg --dearmor | tee /usr/share/keyrings/sbt.gpg > /dev/null && \
    echo "deb [signed-by=/usr/share/keyrings/sbt.gpg] https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list && \
    apt-get update && apt-get install -y sbt && \
    rm -rf /var/lib/apt/lists/*

# Install Claude Code globally (available to all users)
RUN npm install -g @anthropic-ai/claude-code

WORKDIR /workspace
