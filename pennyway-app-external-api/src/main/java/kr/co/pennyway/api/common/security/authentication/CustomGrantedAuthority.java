package kr.co.pennyway.api.common.security.authentication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.io.Serial;
import java.io.Serializable;

public final class CustomGrantedAuthority implements GrantedAuthority, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String role;

    @JsonCreator
    public CustomGrantedAuthority(@JsonProperty("authority") String role) {
        Assert.hasText(role, "role must not be empty");
        this.role = role;
    }

    @Override
    public String getAuthority() {
        return role;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof CustomGrantedAuthority cga) {
            return this.role.equals(cga.getAuthority());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.role.hashCode();
    }

    @Override
    public String toString() {
        return this.role;
    }
}
