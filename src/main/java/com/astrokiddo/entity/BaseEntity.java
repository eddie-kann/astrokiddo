package com.astrokiddo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
public abstract class BaseEntity implements Serializable {
    @Id
    protected Long id;

    @Transient
    private String className = getClass().getSimpleName();

    public static boolean equals(BaseEntity e1, BaseEntity e2) {
        if ((null != e1 && null == e2) || (null != e2 && null == e1)) {
            return false;
        }
        if (null == e1) {
            return true;
        }
        return e1.getId().equals(e2.getId());
    }

    @JsonIgnore
    public boolean isPersisted() {
        return null != id && 0 < id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, className);
    }
}
