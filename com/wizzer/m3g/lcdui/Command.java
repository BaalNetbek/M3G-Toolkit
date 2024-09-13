package com.wizzer.m3g.lcdui;

public class Command {
   public static final int SCREEN = 1;
   public static final int BACK = 2;
   public static final int CANCEL = 3;
   public static final int OK = 4;
   public static final int HELP = 5;
   public static final int STOP = 6;
   public static final int EXIT = 7;
   public static final int ITEM = 8;
   private int type;
   private String label;
   private String longLabel;
   private int priority;

   public Command(String label, int commandType, int priority) {
      this.type = 0;
      this.label = "";
      this.longLabel = "";
      this.priority = 0;
      this.label = label;
      this.type = commandType;
      this.priority = priority;
   }

   public Command(String shortLabel, String longLabel, int commandType, int priority) {
      this(shortLabel, commandType, priority);
      this.longLabel = longLabel;
   }

   public int getCommandType() {
      return this.type;
   }

   public int getPriority() {
      return this.priority;
   }

   public String getLabel() {
      return this.label;
   }

   public String getLongLabel() {
      return this.longLabel;
   }
}
