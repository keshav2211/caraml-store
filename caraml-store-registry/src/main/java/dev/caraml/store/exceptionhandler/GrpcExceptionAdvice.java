package dev.caraml.store.exceptionhandler;

import dev.caraml.store.feature.SpecNotFoundException;
import io.grpc.Status;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

@GrpcAdvice
public class GrpcExceptionAdvice {

    @GrpcExceptionHandler
    public Status handleInvalidArgument(IllegalArgumentException e) {
        return Status.INVALID_ARGUMENT.withDescription(e.getMessage()).withCause(e);
    }

    @GrpcExceptionHandler
    public Status handleUnsupportedOperation(UnsupportedOperationException e) {
        return Status.UNIMPLEMENTED.withDescription(e.getMessage()).withCause(e);
    }

    @GrpcExceptionHandler
    public Status handleSpecNotFound(SpecNotFoundException e) {
        return Status.NOT_FOUND.withDescription(e.getMessage()).withCause(e);
    }

    @GrpcExceptionHandler
    public Status handleInternalError(Exception e) {
        return Status.INTERNAL.withDescription(e.getMessage()).withCause(e);
    }
}
