package com.inso_world.binocular.model;

public class NullKeyAdo extends AbstractDomainObject<Integer, String> {

    private final String key; // Intentionally nullable to trigger Kotlin null-check

    public NullKeyAdo(Integer iid, String key) {
        super(iid);
        this.key = key; // may be null
    }

    @Override
    public String getUniqueKey() {
        return key; // returning null is allowed from Java; Kotlin will assert non-null at callsite
    }
}
