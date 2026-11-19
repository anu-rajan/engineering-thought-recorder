package com.anu.engineeringthoughtrecorder.domainObjects.domainUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DraftSection {

    final DraftSectionType sectionType;
    private List<DraftItem> items;

    public DraftSection(DraftSectionType sectionType) {
        this.sectionType = sectionType;;
        this.items = new ArrayList<>();
    }

    public UUID addItem(String content) {
        DraftItem item = new DraftItem(content);
        items.add(item);
        return item.getId();
    }

    public void addThought(UUID itemId, Thought thought) {
        DraftItem item = findItemById(itemId);
        item.addThought(thought);
    }

    public void addItem(DraftItem item) {
        items.add(item);
    }

    public boolean containsItem(UUID id) {
        return items.stream()
                .anyMatch(item -> item.getId().equals(id));
    }

    public void updateItem(UUID id, String content) {
        DraftItem item = findItemById(id);
        item.updateContent(content);
    }

    public void updateItem(DraftItem item) {
        DraftItem existingItem = findItemById(item.getId());
        existingItem.updateContent(item.getContent());
    }

    private int indexOfItem(UUID id) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(id)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Draft item not found: " + id);
    }


    public void removeItem(UUID id) {
        items.removeIf(item -> item.getId().equals(id));
    }

    private DraftItem findItemById(UUID id) {
        return items.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Draft item not found: " + id));
    }
    public List<DraftItem> getItems() {
        return List.copyOf(items);
    }
}
