FROM eclipse-temurin:11-jdk

ARG USERNAME=mlykes
ARG USER_UID=1000
ARG USER_GID=1000

RUN apt-get update && apt-get install -y curl gnupg git zsh sudo && \
    # Rename the existing ubuntu user/group (UID/GID 1000) to match the host user.
    # eclipse-temurin is Ubuntu-based and ships with an 'ubuntu' user at 1000.
    groupmod -n $USERNAME $(getent group $USER_GID | cut -d: -f1) && \
    existing_user=$(getent passwd $USER_UID | cut -d: -f1) && \
    usermod -l $USERNAME -d /home/$USERNAME -m -s /bin/zsh $existing_user && \
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
