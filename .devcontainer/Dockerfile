FROM eclipse-temurin:11-jdk

ARG USERNAME=mlykes
ARG USER_UID=1000
ARG USER_GID=1000

RUN apt-get update && apt-get install -y curl gnupg git zsh sudo && \
    # Create non-root user matching host UID/GID so file permissions are consistent
    groupadd --gid $USER_GID $USERNAME && \
    useradd --uid $USER_UID --gid $USER_GID -m -s /bin/zsh $USERNAME && \
    echo "$USERNAME ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers.d/$USERNAME && \
    chmod 0440 /etc/sudoers.d/$USERNAME && \
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

# Install oh-my-zsh for the mlykes user
USER $USERNAME
RUN sh -c "$(curl -fsSL https://raw.githubusercontent.com/ohmyzsh/ohmyzsh/master/tools/install.sh)" "" --unattended

USER root
WORKDIR /workspace
RUN chown $USERNAME:$USERNAME /workspace
