/**
 * User: Clinton Begin
 * Date: Apr 16, 2003
 * Time: 8:51:04 PM
 */
package com.ibatis.common.util;

import org.apache.commons.logging.*;

import java.util.*;

public class Stopwatch {

  private static final Log log = LogFactory.getLog(Stopwatch.class);

  private Map taskMap = new HashMap();

  private String currentTaskName = null;
  private long currentTaskTime = 0;

  public Iterator getTaskNames() {
    return taskMap.keySet().iterator();
  }

  public long getTaskCount(String taskName) {
    return ((TaskStat) taskMap.get(taskName)).getCount();
  }

  public long getTotalTaskTime(String taskName) {
    return ((TaskStat) taskMap.get(taskName)).getTotal();
  }

  public long getMaxTaskTime(String taskName) {
    return ((TaskStat) taskMap.get(taskName)).getMax();
  }

  public long getMinTaskTime(String taskName) {
    return ((TaskStat) taskMap.get(taskName)).getMin();
  }

  public long getAvgTaskTime(String taskName) {
    return ((TaskStat) taskMap.get(taskName)).getAverage();
  }

  public void start(String taskName) {
    if (log.isDebugEnabled()) {
      log.debug("Starting: " + taskName);
    }
    this.currentTaskName = taskName;
    currentTaskTime = System.currentTimeMillis();
  }

  public void stop() {
    if (log.isDebugEnabled()) {
      log.debug("Stopping: " + currentTaskName);
    }
    currentTaskTime = System.currentTimeMillis() - currentTaskTime;
    appendTaskTime(currentTaskName, currentTaskTime);
  }

  private synchronized void appendTaskTime(String taskName, long taskTime) {
    TaskStat stat = (TaskStat) taskMap.get(taskName);
    if (stat == null) {
      stat = new TaskStat();
      taskMap.put(taskName, stat);
    }
    stat.appendTaskTime(taskTime);
  }

  public void mergeStopwatch(Stopwatch watch) {
    Iterator names = watch.getTaskNames();
    while (names.hasNext()) {
      String name = (String) names.next();
      long taskTime = watch.getTotalTaskTime(name);
      appendTaskTime(name, taskTime);
    }
  }

  public synchronized void reset() {
    taskMap.clear();
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("Task,Count,Total,Max,Min,Avg\n");
    Iterator names = getTaskNames();
    while (names.hasNext()) {
      String name = (String) names.next();
      long taskCount = getTaskCount(name);
      long taskTime = getTotalTaskTime(name);
      long taskMin = getMinTaskTime(name);
      long taskMax = getMaxTaskTime(name);
      long taskAvg = getAvgTaskTime(name);
      buffer.append(name + "," + taskCount + "," + taskTime + "," + taskMax + "," + taskMin + "," + taskAvg + "\n");
    }
    return buffer.toString();
  }

  private class TaskStat {
    private static final long UNSET = -999999;

    private long count = 0;
    private long total = 0;
    private long min = UNSET;
    private long max = UNSET;

    public void appendTaskTime(long taskTime) {
      count++;
      total += taskTime;
      if (max == UNSET || taskTime > max) {
        max = taskTime;
      }
      if (min == UNSET || taskTime < min) {
        min = taskTime;
      }
    }

    public long getTotal() {
      return total;
    }

    public long getMax() {
      return max;
    }

    public long getMin() {
      return min;
    }

    public long getCount() {
      return count;
    }

    public long getAverage() {
      if (count > 0) {
        return Math.round((double) total / (double) count);
      } else {
        return 0;
      }
    }

  }

}



