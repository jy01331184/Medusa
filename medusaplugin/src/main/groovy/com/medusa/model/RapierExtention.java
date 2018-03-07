package com.medusa.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tianyang on 18/2/5.
 */
public class RapierExtention {

    public List<String> staticLink = new ArrayList<>();


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RapierExtention that = (RapierExtention) o;

        return staticLink != null ? staticLink.equals(that.staticLink) : that.staticLink == null;

    }

    @Override
    public int hashCode() {
        return staticLink != null ? staticLink.hashCode() : 0;
    }
}
