package com.anu.engineeringthoughtrecorder.domainObjects.domainUtils;

public class Thought {
        private final String description;
        private final TimestampRange timestampRange;

        public Thought(String description, TimestampRange timestampRange) {
                if (description == null || description.isBlank()) {
                        throw new IllegalArgumentException(
                                "Thought description cannot be empty");
                }

                if (timestampRange == null) {
                        throw new IllegalArgumentException(
                                "Timestamp range cannot be null");
                }

                this.description = description;
                this.timestampRange = timestampRange;
        }

        public String getDescription() {
                return description;
        }

        public TimestampRange getTimestampRange() {
                return timestampRange;
        }
}
