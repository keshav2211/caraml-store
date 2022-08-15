package dev.caraml.store.sparkjob;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import dev.caraml.store.feature.EntityRepository;
import dev.caraml.store.feature.FeatureTableRepository;
import dev.caraml.store.feature.SpecNotFoundException;
import dev.caraml.store.protobuf.core.FeatureTableProto.FeatureTableSpec;
import dev.caraml.store.protobuf.jobservice.JobServiceProto.Job;
import dev.caraml.store.protobuf.jobservice.JobServiceProto.JobStatus;
import dev.caraml.store.protobuf.jobservice.JobServiceProto.JobType;
import dev.caraml.store.sparkjob.adapter.BatchIngestionArgumentAdapter;
import dev.caraml.store.sparkjob.adapter.StreamIngestionArgumentAdapter;
import dev.caraml.store.sparkjob.crd.SparkApplication;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JobService {

  private final String namespace;
  private final Map<JobType, Map<String, IngestionJobProperties>> ingestionJobsByTypeAndStoreName =
      new HashMap<>();
  private final EntityRepository entityRepository;
  private final FeatureTableRepository tableRepository;

  private final SparkOperatorApi sparkOperatorApi;

  @Autowired
  public JobService(
      JobServiceConfig config,
      EntityRepository entityRepository,
      FeatureTableRepository tableRepository,
      SparkOperatorApi sparkOperatorApi) {
    this.namespace = config.getNamespace();
    this.ingestionJobsByTypeAndStoreName.put(
        JobType.STREAM_INGESTION_JOB,
        config.getStreamIngestion().stream()
            .collect(Collectors.toMap(IngestionJobProperties::store, Function.identity())));
    this.ingestionJobsByTypeAndStoreName.put(
        JobType.BATCH_INGESTION_JOB,
        config.getBatchIngestion().stream()
            .collect(Collectors.toMap(IngestionJobProperties::store, Function.identity())));
    this.entityRepository = entityRepository;
    this.tableRepository = tableRepository;
    this.sparkOperatorApi = sparkOperatorApi;
  }

  static final String LABEL_PREFIX = "caraml.dev/";
  static final String STORE_LABEL = LABEL_PREFIX + "store";
  static final String JOB_TYPE_LABEL = LABEL_PREFIX + "type";
  static final String FEATURE_TABLE_LABEL = LABEL_PREFIX + "table";
  static final String PROJECT_LABEL = LABEL_PREFIX + "project";

  private String getIngestionJobId(JobType jobType, String project, FeatureTableSpec spec) {
    return "caraml-"
        + DigestUtils.md5Hex(
                String.join(
                    "-",
                    jobType.toString(),
                    project,
                    spec.getName(),
                    spec.getOnlineStore().getName()))
            .toLowerCase();
  }

  private Job sparkApplicationToJob(SparkApplication app) {
    Map<String, String> labels = app.getMetadata().getLabels();
    Timestamp startTime =
        Timestamps.fromSeconds(app.getMetadata().getCreationTimestamp().toEpochSecond());

    JobStatus jobStatus = JobStatus.JOB_STATUS_PENDING;
    if(app.getStatus() != null) {
      jobStatus = switch (app.getStatus().getApplicationState().getState()) {
        case "COMPLETED" -> JobStatus.JOB_STATUS_DONE;
        case "FAILED" -> JobStatus.JOB_STATUS_ERROR;
        case "RUNNING" -> JobStatus.JOB_STATUS_RUNNING;
        default -> JobStatus.JOB_STATUS_PENDING;
      };
    }
    String tableName = labels.getOrDefault(FEATURE_TABLE_LABEL, "");

    Job.Builder builder =
        Job.newBuilder()
            .setId(app.getMetadata().getName())
            .setStartTime(startTime)
            .setStatus(jobStatus);
    switch (JobType.valueOf(labels.get(JOB_TYPE_LABEL))) {
      case BATCH_INGESTION_JOB -> builder.setBatchIngestion(
          Job.OfflineToOnlineMeta.newBuilder().setTableName(tableName));
      case STREAM_INGESTION_JOB -> builder.setStreamIngestion(
          Job.StreamToOnlineMeta.newBuilder().setTableName(tableName));
    }

    return builder.build();
  }

  public Job createOrUpdateStreamingIngestionJob(String project, String featureTableName) {
    FeatureTableSpec spec =
        tableRepository
            .findFeatureTableByNameAndProject_NameAndIsDeletedFalse(project, featureTableName)
            .map(ft -> ft.toProto().getSpec())
            .orElseThrow(
                () -> {
                  throw new SpecNotFoundException(
                      String.format(
                          "No such Feature Table: (project: %s, name: %s)",
                          project, featureTableName));
                });
    return createOrUpdateStreamingIngestionJob(project, spec);
  }

  private Map<String, String> getEntityToTypeMap(String project, FeatureTableSpec spec) {
    return spec.getEntitiesList().stream()
        .collect(
            Collectors.toMap(
                Function.identity(),
                e -> entityRepository.findEntityByNameAndProject_Name(e, project).getType()));
  }

  public Job createOrUpdateStreamingIngestionJob(String project, FeatureTableSpec spec) {
    Map<String, String> entityNameToType = getEntityToTypeMap(project, spec);
    List<String> arguments =
        new StreamIngestionArgumentAdapter(project, spec, entityNameToType).getArguments();
    return createOrUpdateIngestionJob(JobType.STREAM_INGESTION_JOB, project, spec, arguments);
  }

  public Job createOrUpdateBatchIngestionJob(
      String project, String featureTableName, Timestamp startTime, Timestamp endTime)
      throws SparkOperatorApiException {
    FeatureTableSpec spec =
        tableRepository
            .findFeatureTableByNameAndProject_NameAndIsDeletedFalse(featureTableName, project)
            .map(ft -> ft.toProto().getSpec())
            .orElseThrow(
                () -> {
                  throw new SpecNotFoundException(
                      String.format(
                          "No such Feature Table: (project: %s, name: %s)",
                          project, featureTableName));
                });
    Map<String, String> entityNameToType = getEntityToTypeMap(project, spec);
    List<String> arguments =
        new BatchIngestionArgumentAdapter(project, spec, entityNameToType, startTime, endTime)
            .getArguments();
    return createOrUpdateIngestionJob(JobType.BATCH_INGESTION_JOB, project, spec, arguments);
  }

  private Job createOrUpdateIngestionJob(
      JobType jobType, String project, FeatureTableSpec spec, List<String> additionalArguments) {

    Map<String, IngestionJobProperties> jobConfigByStoreName =
        ingestionJobsByTypeAndStoreName.get(jobType);
    if (jobConfigByStoreName == null) {
      throw new IllegalArgumentException(
          String.format("Job properties not found for job type: %s", jobType.toString()));
    }
    IngestionJobProperties jobProperties =
        jobConfigByStoreName.get(spec.getOnlineStore().getName());
    if (jobProperties == null) {
      throw new IllegalArgumentException(
          String.format(
              "Job properties not found for store name: %s", spec.getOnlineStore().getName()));
    }

    String ingestionJobId = getIngestionJobId(jobType, project, spec);
    Optional<SparkApplication> existingApplication =
        sparkOperatorApi.get(namespace, ingestionJobId);
    SparkApplication sparkApplication =
        existingApplication.orElseGet(
            () -> {
              SparkApplication app = new SparkApplication();
              app.setMetadata(new V1ObjectMeta());
              app.getMetadata().setName(ingestionJobId);
              app.getMetadata().setNamespace(namespace);
              app.addLabels(
                  Map.of(
                      JOB_TYPE_LABEL,
                      jobType.toString(),
                      STORE_LABEL,
                      spec.getOnlineStore().getName(),
                      PROJECT_LABEL,
                      project,
                      FEATURE_TABLE_LABEL,
                      spec.getName()));
              return app;
            });
    sparkApplication.setSpec(jobProperties.sparkApplicationSpec());
    sparkApplication.getSpec().addArguments(additionalArguments);

    SparkApplication updatedApplication =
        existingApplication.isPresent()
            ? sparkOperatorApi.update(sparkApplication)
            : sparkOperatorApi.create(sparkApplication);
    return sparkApplicationToJob(updatedApplication);
  }

  public List<Job> listJobs(Boolean includeTerminated, String project, String tableName) {
    Stream<String> equalitySelectors =
        Map.of(
                PROJECT_LABEL, project,
                FEATURE_TABLE_LABEL, tableName)
            .entrySet()
            .stream()
            .filter(es -> !es.getValue().isEmpty())
            .map(es -> String.format("%s=%s", es.getKey(), es.getValue()));
    String jobSets =
        Stream.of(JobType.BATCH_INGESTION_JOB, JobType.STREAM_INGESTION_JOB, JobType.RETRIEVAL_JOB)
            .map(Enum::toString)
            .collect(Collectors.joining(","));
    Stream<String> setSelectors = Stream.of(String.format("%s in (%s)", JOB_TYPE_LABEL, jobSets));
    String labelSelectors =
        Stream.concat(equalitySelectors, setSelectors).collect(Collectors.joining(","));
    Stream<Job> jobStream =
        sparkOperatorApi.list(namespace, labelSelectors).stream().map(this::sparkApplicationToJob);
    if (!includeTerminated) {
      jobStream = jobStream.filter(job -> job.getStatus() == JobStatus.JOB_STATUS_RUNNING);
    }
    return jobStream.toList();
  }
}
