package com.supermartijn642.core.generator.aggregator;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created 06/05/2023 by SuperMartijn642
 */
public interface ResourceAggregator<S, T> {

    S initialData();

    S combine(S data, T newData);

    void write(OutputStream stream, S data) throws IOException;
}
