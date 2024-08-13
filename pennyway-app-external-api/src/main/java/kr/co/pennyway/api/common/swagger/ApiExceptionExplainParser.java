package kr.co.pennyway.api.common.swagger;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import kr.co.pennyway.api.common.annotation.ApiExceptionExplanation;
import kr.co.pennyway.api.common.annotation.ApiResponseExplanations;
import kr.co.pennyway.api.common.response.ErrorResponse;
import kr.co.pennyway.common.exception.BaseErrorCode;
import lombok.AccessLevel;
import lombok.Builder;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ApiExceptionExplainParser {
    public static void parse(Operation operation, HandlerMethod handlerMethod) {
        ApiResponseExplanations annotation = handlerMethod.getMethodAnnotation(ApiResponseExplanations.class);

        if (annotation != null) {
            generateExceptionResponseDocs(operation, annotation.errors());
        }
    }

    private static void generateExceptionResponseDocs(Operation operation, ApiExceptionExplanation[] exceptions) {
        ApiResponses responses = operation.getResponses();

        Map<Integer, List<ExampleHolder>> holders = Arrays.stream(exceptions)
                .map(ExampleHolder::from)
                .collect(Collectors.groupingBy(ExampleHolder::httpStatus));

        addExamplesToResponses(responses, holders);
    }

    private static void addExamplesToResponses(ApiResponses responses, Map<Integer, List<ExampleHolder>> holders) {
        holders.forEach((httpStatus, exampleHolders) -> {
            Content content = new Content();
            MediaType mediaType = new MediaType();
            ApiResponse response = new ApiResponse();

            exampleHolders.forEach(holder -> mediaType.addExamples(holder.name(), holder.holder()));
            content.addMediaType("application/json", mediaType);
            response.setContent(content);

            responses.addApiResponse(String.valueOf(httpStatus), response);
        });
    }

    @Builder(access = AccessLevel.PRIVATE)
    private record ExampleHolder(int httpStatus, String name, String mediaType, String description, Example holder) {
        static ExampleHolder from(ApiExceptionExplanation annotation) {
            if (annotation instanceof BaseErrorCode errorCode) {
                return ExampleHolder.builder()
                        .httpStatus(errorCode.causedBy().statusCode().getCode())
                        .name(StringUtils.hasText(annotation.name()) ? annotation.name() : errorCode.getExplainError())
                        .mediaType(annotation.mediaType())
                        .description(annotation.description())
                        .holder(createExample(errorCode, annotation.summary(), annotation.description()))
                        .build();
            } else {
                throw new IllegalArgumentException("Annotation must be an instance of BaseErrorCode");
            }
        }

        private static Example createExample(BaseErrorCode errorCode, String summary, String description) {
            ErrorResponse response = ErrorResponse.of(errorCode.causedBy().getCode(), errorCode.getExplainError());

            Example example = new Example();
            example.setValue(response);
            example.setSummary(summary);
            example.setDescription(description);

            return example;
        }
    }
}
