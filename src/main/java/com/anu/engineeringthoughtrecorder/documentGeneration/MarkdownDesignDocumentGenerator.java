package com.anu.engineeringthoughtrecorder.documentGeneration;

import com.anu.engineeringthoughtrecorder.domainObjects.DesignSession;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.*;

public class MarkdownDesignDocumentGenerator
        implements DesignDocumentGenerator {

    @Override
    public String generate(DesignSession session) {
        if (session == null) {
            throw new IllegalArgumentException(
                    "Design session cannot be null"
            );
        }

        StringBuilder markdown = new StringBuilder();

        markdown.append("# ")
                .append(session.getTitle())
                .append("\n\n");

        String goal = session.getDesignDraft().getGoal();

        markdown.append("## Design Goal\n\n");

        if (goal != null && !goal.isBlank()) {
            markdown.append(goal).append("\n\n");
        } else {
            markdown.append("Not specified.\n\n");
        }

        markdown.append("## Functional Requirements\n\n");

        for (DraftItem item : session.getDesignDraft()
                .getFunctionalRequirements()
                .getItems()) {

            markdown.append("- ")
                    .append(item.getContent())
                    .append("\n");
        }

        markdown.append("\n");

        markdown.append("## Non-Functional Requirements\n\n");

        for (DraftItem item : session.getDesignDraft()
                .getNonFunctionalRequirements()
                .getItems()) {

            markdown.append("- ")
                    .append(item.getContent())
                    .append("\n");
        }

        markdown.append("\n");

        markdown.append("## Assumptions\n\n");

        for (DraftItem item : session.getDesignDraft()
                .getAssumptions()
                .getItems()) {

            markdown.append("- ")
                    .append(item.getContent())
                    .append("\n");
        }

        markdown.append("\n");
        markdown.append("## Design Decisions\n\n");

        for (DraftItem item : session.getDesignDraft()
                .getDecisions()
                .getItems()) {

            markdown.append("- ")
                    .append(item.getContent())
                    .append("\n");
        }

        markdown.append("\n");
        markdown.append("## Open Questions\n\n");

        for (DraftItem item : session.getDesignDraft()
                .getOpenQuestions()
                .getItems()) {

            markdown.append("- ")
                    .append(item.getContent())
                    .append("\n");
        }

        markdown.append("\n");

        if (session.getSystemSpecification() != null) {

            markdown.append("## System Components\n\n");

            if (session.getSystemSpecification().getComponents() != null) {

                for (DesignComponent component :
                        session.getSystemSpecification().getComponents()) {

                    markdown.append("### ")
                            .append(component.getName())
                            .append("\n\n");

                    markdown.append("**Responsibility:** ")
                            .append(component.getResponsibility())
                            .append("\n\n");

                    markdown.append("**Technology:** ")
                            .append(component.getTechnology())
                            .append("\n\n");
                }
            }
        }

        if (session.getSystemSpecification() != null
                && session.getSystemSpecification().getApis() != null) {

            markdown.append("## API Definitions\n\n");

            for (API api : session.getSystemSpecification().getApis()) {

                markdown.append("### ")
                        .append(api.getName())
                        .append("\n\n");

                markdown.append("**Method:** ")
                        .append(api.getMethod())
                        .append("\n\n");

                markdown.append("**Path:** `")
                        .append(api.getPath())
                        .append("`\n\n");

                markdown.append("**Purpose:** ")
                        .append(api.getPurpose())
                        .append("\n\n");
            }
        }

        if (session.getSystemSpecification() != null
                && session.getSystemSpecification().getPerformanceCapacity() != null
                && session.getSystemSpecification()
                .getPerformanceCapacity().getMetrics() != null) {

            markdown.append("## Performance and Capacity\n\n");

            for (PerformanceMetric metric : session.getSystemSpecification()
                    .getPerformanceCapacity().getMetrics()) {

                markdown.append("- **")
                        .append(metric.getMetric())
                        .append(":** ")
                        .append(metric.getValue())
                        .append(" ")
                        .append(metric.getUnit())
                        .append("\n");
            }

            markdown.append("\n");
        }

        if (session.getSystemSpecification() != null
                && session.getSystemSpecification().getExternalDependencies() != null) {

            markdown.append("## External Dependencies\n\n");

            for (ExternalDependency dependency :
                    session.getSystemSpecification().getExternalDependencies()) {

                markdown.append("### ")
                        .append(dependency.getName())
                        .append("\n\n");

                markdown.append("**Purpose:** ")
                        .append(dependency.getPurpose())
                        .append("\n\n");

                markdown.append("**Requirement:** ")
                        .append(dependency.getRequirement())
                        .append("\n\n");
            }
        }

        return markdown.toString();
    }
}