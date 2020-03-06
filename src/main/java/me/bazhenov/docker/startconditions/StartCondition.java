package me.bazhenov.docker.startconditions;

public @interface StartCondition {

  Class<Condition> condition();
  
  String[] parameters();
  
  int timeout();
  
}
