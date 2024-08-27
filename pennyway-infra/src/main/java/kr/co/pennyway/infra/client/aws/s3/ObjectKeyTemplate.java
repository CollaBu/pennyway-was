package kr.co.pennyway.infra.client.aws.s3;

import java.util.Map;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ObjectKeyTemplate {
	private String template;

	public String apply(Map<String, String> variables) {
		String result = template;
		for (Map.Entry<String, String> entry : variables.entrySet()) {
			result = result.replace("{" + entry.getKey() + "}", entry.getValue());
		}
		return result;
	}
}
