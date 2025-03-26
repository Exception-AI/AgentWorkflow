package org.dksd.tasks.model;

import java.io.Serializable;
import java.util.UUID;

public interface Identifier extends Serializable {
    UUID getId();
}
