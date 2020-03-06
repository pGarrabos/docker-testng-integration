package me.bazhenov.docker.startconditions;

public @interface StartCondition {

  Class<Condition> contition();
  
  String[] parameters();
  
  int timeout();
  
}
