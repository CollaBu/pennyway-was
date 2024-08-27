module.exports = {
    extends: ["@commitlint/config-conventional"],
    rules: {
        // 스코프는 컨벤션과 맞지 않기에, 사용하지 않는 것으로 한다.
        "scope-empty": [2, "always"],
        // 헤더의 길이는 100자로 제한
        "header-max-length": [2, "always", 100],
        // 본문의 한 줄은 100자로 제한
        "body-max-line-length": [2, "always", 100],
        // 타입은 아래의 태그만 가능
        "type-enum": [
            2,
            "always",
            ["feat", "fix", "docs", "rename", "style", "refactor", "test", "chore", "release"],
        ],
    },
};