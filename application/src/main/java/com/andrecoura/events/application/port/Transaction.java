package com.andrecoura.events.application.port;

import java.util.function.Supplier;

/** Output port: runs the given work atomically (all repository calls inside commit or roll back together). */
public interface Transaction {

    <T> T execute(Supplier<T> work);
}
