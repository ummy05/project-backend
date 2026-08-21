package FYP.project_backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Role {

    ADMIN,
    BUSINESS_OWNER,
    TOURIST,
    SHEHA;

    @JsonCreator
    public static Role fromValue(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        for (Role role : Role.values()) {
            if (role.name().equalsIgnoreCase(value.trim())) {
                return role;
            }
        }

        throw new IllegalArgumentException(
                "Invalid role: " + value
        );
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}