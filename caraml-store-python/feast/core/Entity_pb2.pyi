"""
@generated by mypy-protobuf.  Do not edit manually!
isort:skip_file
"""
import builtins
import collections.abc
import feast.types.Value_pb2
import google.protobuf.descriptor
import google.protobuf.internal.containers
import google.protobuf.message
import google.protobuf.timestamp_pb2
import sys

if sys.version_info >= (3, 8):
    import typing as typing_extensions
else:
    import typing_extensions

DESCRIPTOR: google.protobuf.descriptor.FileDescriptor

class Entity(google.protobuf.message.Message):
    DESCRIPTOR: google.protobuf.descriptor.Descriptor

    SPEC_FIELD_NUMBER: builtins.int
    META_FIELD_NUMBER: builtins.int
    @property
    def spec(self) -> global___EntitySpec:
        """User-specified specifications of this entity."""
    @property
    def meta(self) -> global___EntityMeta:
        """System-populated metadata for this entity."""
    def __init__(
        self,
        *,
        spec: global___EntitySpec | None = ...,
        meta: global___EntityMeta | None = ...,
    ) -> None: ...
    def HasField(self, field_name: typing_extensions.Literal["meta", b"meta", "spec", b"spec"]) -> builtins.bool: ...
    def ClearField(self, field_name: typing_extensions.Literal["meta", b"meta", "spec", b"spec"]) -> None: ...

global___Entity = Entity

class EntitySpec(google.protobuf.message.Message):
    DESCRIPTOR: google.protobuf.descriptor.Descriptor

    class LabelsEntry(google.protobuf.message.Message):
        DESCRIPTOR: google.protobuf.descriptor.Descriptor

        KEY_FIELD_NUMBER: builtins.int
        VALUE_FIELD_NUMBER: builtins.int
        key: builtins.str
        value: builtins.str
        def __init__(
            self,
            *,
            key: builtins.str = ...,
            value: builtins.str = ...,
        ) -> None: ...
        def ClearField(self, field_name: typing_extensions.Literal["key", b"key", "value", b"value"]) -> None: ...

    NAME_FIELD_NUMBER: builtins.int
    VALUE_TYPE_FIELD_NUMBER: builtins.int
    DESCRIPTION_FIELD_NUMBER: builtins.int
    LABELS_FIELD_NUMBER: builtins.int
    name: builtins.str
    """Name of the entity."""
    value_type: feast.types.Value_pb2.ValueType.Enum.ValueType
    """Type of the entity."""
    description: builtins.str
    """Description of the entity."""
    @property
    def labels(self) -> google.protobuf.internal.containers.ScalarMap[builtins.str, builtins.str]:
        """User defined metadata"""
    def __init__(
        self,
        *,
        name: builtins.str = ...,
        value_type: feast.types.Value_pb2.ValueType.Enum.ValueType = ...,
        description: builtins.str = ...,
        labels: collections.abc.Mapping[builtins.str, builtins.str] | None = ...,
    ) -> None: ...
    def ClearField(self, field_name: typing_extensions.Literal["description", b"description", "labels", b"labels", "name", b"name", "value_type", b"value_type"]) -> None: ...

global___EntitySpec = EntitySpec

class EntityMeta(google.protobuf.message.Message):
    DESCRIPTOR: google.protobuf.descriptor.Descriptor

    CREATED_TIMESTAMP_FIELD_NUMBER: builtins.int
    LAST_UPDATED_TIMESTAMP_FIELD_NUMBER: builtins.int
    @property
    def created_timestamp(self) -> google.protobuf.timestamp_pb2.Timestamp: ...
    @property
    def last_updated_timestamp(self) -> google.protobuf.timestamp_pb2.Timestamp: ...
    def __init__(
        self,
        *,
        created_timestamp: google.protobuf.timestamp_pb2.Timestamp | None = ...,
        last_updated_timestamp: google.protobuf.timestamp_pb2.Timestamp | None = ...,
    ) -> None: ...
    def HasField(self, field_name: typing_extensions.Literal["created_timestamp", b"created_timestamp", "last_updated_timestamp", b"last_updated_timestamp"]) -> builtins.bool: ...
    def ClearField(self, field_name: typing_extensions.Literal["created_timestamp", b"created_timestamp", "last_updated_timestamp", b"last_updated_timestamp"]) -> None: ...

global___EntityMeta = EntityMeta
