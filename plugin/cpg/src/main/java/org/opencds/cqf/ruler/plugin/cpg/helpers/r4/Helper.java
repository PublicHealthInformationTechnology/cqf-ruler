package org.opencds.cqf.ruler.plugin.cpg.helpers.r4;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.OperationOutcome;

public class Helper {

    public static OperationOutcome createErrorOutcome(String display) {
        Coding code = new Coding().setDisplay(display);
        return new OperationOutcome().addIssue(
                new OperationOutcome.OperationOutcomeIssueComponent()
                        .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                        .setCode(OperationOutcome.IssueType.PROCESSING)
                        .setDetails(new CodeableConcept().addCoding(code))
        );
    }
}
