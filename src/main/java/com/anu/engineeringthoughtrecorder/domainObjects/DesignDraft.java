package com.anu.engineeringthoughtrecorder.domainObjects;

import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.DraftItem;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.DraftSection;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.DraftSectionType;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.Thought;

public class DesignDraft {
    private String goal;

    private DraftSection functionalRequirements;
    private DraftSection nonFunctionalRequirements;
    private DraftSection assumptions;
    private DraftSection decisions;
    private DraftSection openQuestions;

    public DesignDraft() {
        this.functionalRequirements = new DraftSection(DraftSectionType.FUNCTIONAL_REQUIREMENTS);
        this.nonFunctionalRequirements = new DraftSection(DraftSectionType.NON_FUNCTIONAL_REQUIREMENTS);
        this.assumptions = new DraftSection(DraftSectionType.ASSUMPTIONS);
        this.decisions = new DraftSection(DraftSectionType.DECISIONS);
        this.openQuestions = new DraftSection(DraftSectionType.OPEN_QUESTIONS);
    }

    public String getGoal() {
        return goal;
    }

    public DraftSection getFunctionalRequirements() {
        return functionalRequirements;
    }

    public DraftSection getNonFunctionalRequirements() {
        return nonFunctionalRequirements;
    }

    public DraftSection getAssumptions() {
        return assumptions;
    }

    public DraftSection getDecisions() {
        return decisions;
    }

    public DraftSection getOpenQuestions() {
        return openQuestions;
    }

    public void updateGoal(String goal) {
        this.goal = goal;
    }

    public void addFunctionalRequirement(String requirement) {
        this.functionalRequirements.addItem(requirement);
    }

    public void addNonFunctionalRequirement(String requirement) {
        this.nonFunctionalRequirements.addItem(requirement);
    }

    public void addAssumption(String assumption) {
        this.assumptions.addItem(assumption);
    }

    public void addDecision(String decision) {
        this.decisions.addItem(decision);
    }

    public void addOpenQuestion(String question) {
        this.openQuestions.addItem(question);
    }

    public void updateDesignDraft(DraftSectionType sectionType, DraftItem item) {
        DraftSection section = getSection(sectionType);

        try {
            section.updateItem(item);
        } catch (IllegalArgumentException e) {
            section.addItem(item);
        }
    }

    private DraftSection getSection(DraftSectionType sectionType) {
        return switch (sectionType) {
            case FUNCTIONAL_REQUIREMENTS -> functionalRequirements;
            case NON_FUNCTIONAL_REQUIREMENTS -> nonFunctionalRequirements;
            case ASSUMPTIONS -> assumptions;
            case DECISIONS -> decisions;
            case OPEN_QUESTIONS -> openQuestions;
        };
    }

    public DraftSection getDraftSection(DraftSectionType sectionType) {
        return getSection(sectionType);
    }
    public void updateDesignDraft(
            DraftSectionType sectionType,
            DraftItem item,
            Thought thought) {

        DraftSection section = getSection(sectionType);

        if (section.containsItem(item.getId())) {
            section.updateItem(item);
        } else {
            section.addItem(item);
        }

        section.addThought(item.getId(), thought);
    }

}
