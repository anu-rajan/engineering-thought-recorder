package com.anu.engineeringthoughtrecorder.domainObjects.domainUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DraftItem {

    private UUID id;
    private String content;
    private List<Thought> thoughtFlow;

    public DraftItem(String content) {
        this.id = UUID.randomUUID();
        this.content = content;
        this.thoughtFlow = new ArrayList<>();
    }

    public DraftItem(UUID id, String content) {
        this.id = id;
        this.content = content;
        this.thoughtFlow = new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void updateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "Draft item content cannot be null or blank"
            );
        }

        this.content = content;
    }

    public void addThought(Thought thought) {
        if (!thoughtFlow.isEmpty()) {
            Thought previousThought = thoughtFlow.get(thoughtFlow.size() - 1);

            if (thought.getTimestampRange()
                    .getStartTimestamp()
                    .isBefore(
                            previousThought.getTimestampRange()
                                    .getEndTimestamp())) {

                throw new IllegalArgumentException(
                        "Thoughts in a thought flow cannot overlap");
            }
        }

        thoughtFlow.add(thought);
    }

    public List<Thought> getThoughtFlow() {
        return List.copyOf(thoughtFlow);
    }

}
