package kr.co.pennyway.infra.client.aws.s3.url.generator;

import kr.co.pennyway.infra.client.aws.s3.ActualIdProvider;
import kr.co.pennyway.infra.client.aws.s3.ObjectKeyType;
import kr.co.pennyway.infra.client.aws.s3.url.properties.PresignedUrlProperty;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UrlGenerator {
    private static final EnumMap<ObjectKeyType, Pattern> DELETE_PATTERNS = new EnumMap<>(ObjectKeyType.class);
    private static final EnumMap<ObjectKeyType, List<String>> VARIABLE_NAMES = new EnumMap<>(ObjectKeyType.class);

    static {
        for (ObjectKeyType type : ObjectKeyType.values()) {
            DELETE_PATTERNS.put(type, createRegexPattern(type.getDeleteTemplate()));
            VARIABLE_NAMES.put(type, extractVariableNames(type.getDeleteTemplate()));
        }
    }

    /**
     * S3에 임시 업로드할 파일의 URL을 생성한다.
     *
     * @param property {@link PresignedUrlProperty}: Presigned URL 생성을 위한 Property
     * @return Presigned URL
     */
    public static String createDeleteUrl(PresignedUrlProperty property) {
        return applyTemplate(property.type().getDeleteTemplate(), property.variables());
    }

    /**
     * 임시 경로에서 실제 경로로 파일을 이동시키기 위한 URL을 생성한다.
     *
     * @param type      {@link ActualIdProvider}
     * @param deleteUrl 임시 경로의 URL
     * @return Presigned URL
     */
    public static String convertDeleteToOriginUrl(ActualIdProvider type, String deleteUrl) {
        Map<String, String> variables = extractVariables(type.getType(), deleteUrl);

        for (String requiredParam : type.getType().getRequiredParams()) {
            if (!type.getActualIds().containsKey(requiredParam)) {
                throw new IllegalArgumentException("Missing required parameter: " + requiredParam);
            }
            variables.put(requiredParam, type.getActualIds().get(requiredParam));
        }

        return applyTemplate(type.getType().getOriginTemplate(), variables);
    }

    private static String applyTemplate(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    private static Map<String, String> extractVariables(ObjectKeyType type, String url) {
        Map<String, String> variables = new HashMap<>();
        Matcher matcher = DELETE_PATTERNS.get(type).matcher(url);

        if (matcher.matches()) {
            List<String> variableNames = VARIABLE_NAMES.get(type);
            for (int i = 0; i < variableNames.size(); i++) {
                variables.put(variableNames.get(i), matcher.group(i + 1));
            }
        } else {
            throw new IllegalArgumentException("URL이 패턴과 일치하지 않습니다. URL: " + url);
        }

        return variables;
    }

    private static Pattern createRegexPattern(String template) {
        String regex = template.replaceAll("\\{[^}]+\\}", "([^/]+)")
                .replace("/", "\\/")
                .replace(".", "\\.");
        return Pattern.compile(regex);
    }

    private static List<String> extractVariableNames(String template) {
        List<String> variableNames = new ArrayList<>();
        Pattern variablePattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = variablePattern.matcher(template);
        while (matcher.find()) {
            variableNames.add(matcher.group(1));
        }
        return variableNames;
    }
}
