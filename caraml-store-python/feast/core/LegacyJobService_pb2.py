# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: feast/core/LegacyJobService.proto
"""Generated protocol buffer code."""
from google.protobuf.internal import builder as _builder
from google.protobuf import descriptor as _descriptor
from google.protobuf import descriptor_pool as _descriptor_pool
from google.protobuf import symbol_database as _symbol_database
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()


from feast_spark.api import JobService_pb2 as feast__spark_dot_api_dot_JobService__pb2


DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(b'\n!feast/core/LegacyJobService.proto\x12\nfeast.core\x1a feast_spark/api/JobService.proto2\xe9\x02\n\nJobService\x12\x97\x01\n StartOfflineToOnlineIngestionJob\x12\x38.feast_spark.api.StartOfflineToOnlineIngestionJobRequest\x1a\x39.feast_spark.api.StartOfflineToOnlineIngestionJobResponse\x12v\n\x15GetHistoricalFeatures\x12-.feast_spark.api.GetHistoricalFeaturesRequest\x1a..feast_spark.api.GetHistoricalFeaturesResponse\x12I\n\x06GetJob\x12\x1e.feast_spark.api.GetJobRequest\x1a\x1f.feast_spark.api.GetJobResponseB3\n dev.caraml.store.protobuf.compatB\x0fJobServiceProtob\x06proto3')

_builder.BuildMessageAndEnumDescriptors(DESCRIPTOR, globals())
_builder.BuildTopDescriptorsAndMessages(DESCRIPTOR, 'feast.core.LegacyJobService_pb2', globals())
if _descriptor._USE_C_DESCRIPTORS == False:

  DESCRIPTOR._options = None
  DESCRIPTOR._serialized_options = b'\n dev.caraml.store.protobuf.compatB\017JobServiceProto'
  _JOBSERVICE._serialized_start=84
  _JOBSERVICE._serialized_end=445
# @@protoc_insertion_point(module_scope)
