# ---- Build stage ----
FROM eclipse-temurin:11-jdk AS builder

RUN apt-get update && apt-get install -y curl gnupg && \
    # Node.js (required for Scala.js compilation pipeline)
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash - && \
    apt-get install -y nodejs && \
    # SBT
    curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | gpg --dearmor | tee /usr/share/keyrings/sbt.gpg > /dev/null && \
    echo "deb [signed-by=/usr/share/keyrings/sbt.gpg] https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list && \
    apt-get update && apt-get install -y sbt && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy build metadata first — this layer is cached separately from source changes,
# so dependency downloads are only re-run when build.sbt or project/ changes.
COPY project/build.properties project/plugins.sbt project/
COPY build.sbt .
RUN sbt update

# Copy full source and compile to staged distribution
COPY . .
RUN sbt stage

# ---- Runtime stage ----
FROM eclipse-temurin:11-jre-alpine
RUN apk add --no-cache bash
WORKDIR /app
COPY --from=builder /app/opetopic-play/target/universal/stage .
RUN chmod +x bin/opetopicplay
EXPOSE 9000
# Shell form so environment variables expand at runtime
ENTRYPOINT ["sh", "-c", \
    "exec /app/bin/opetopicplay \
    -Dplay.http.context=/opetopic \
    -Dplay.http.secret.key=$PLAY_SECRET \
    -Dslick.dbs.default.db.properties.url=jdbc:postgresql://opetopic-db:5432/opetopic \
    -Dslick.dbs.default.db.properties.user=opetopic \
    -Dslick.dbs.default.db.properties.password=$DB_PASSWORD \
    -Dplay.evolutions.db.default.autoApply=true \
    -Dhttp.port=9000"]
