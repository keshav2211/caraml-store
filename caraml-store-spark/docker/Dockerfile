FROM apache/spark-py:v3.1.3

ARG GCS_CONNECTOR_VERSION=2.2.5
ARG BQ_CONNECTOR_VERSION=0.18.1

USER root
ADD https://storage.googleapis.com/hadoop-lib/gcs/gcs-connector-hadoop2-${GCS_CONNECTOR_VERSION}.jar /opt/spark/jars
ADD https://repo1.maven.org/maven2/com/google/cloud/spark/spark-bigquery-with-dependencies_2.12/${BQ_CONNECTOR_VERSION}/spark-bigquery-with-dependencies_2.12-${BQ_CONNECTOR_VERSION}.jar /opt/spark/jars

# For logging to /dev/termination-log
RUN mkdir -p /dev

ADD caraml-spark-application-with-dependencies.jar /opt/spark/jars
ADD historical_feature_retrieval_job.py /opt/spark/work-dir

