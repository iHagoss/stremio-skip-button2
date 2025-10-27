package com.tvplayer.app;

public class SkipMarkers {
    public TimeRange intro;
    public TimeRange credits;

    public static class TimeRange {
        public long start;
        public long end;
    }
}
