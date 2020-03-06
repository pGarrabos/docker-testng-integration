package me.bazhenov.docker.startconditions;

import java.io.IOException;
import java.util.List;

@FunctionalInterface
public interface Condition {

  boolean unsure(String cid, List<String> parametres, int timeout) throws IOException, InterruptedException;
}
