package org.opencds.cqf.ruler.cr.r4.provider;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opencds.cqf.ruler.cql.CqlConfig;
import org.opencds.cqf.ruler.cr.CrConfig;
import org.opencds.cqf.ruler.devtools.DevToolsConfig;
import org.opencds.cqf.ruler.test.RestIntegrationTest;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = { MeasureEvaluateProviderIT.class,
		CrConfig.class, CqlConfig.class, DevToolsConfig.class }, properties = {
				"hapi.fhir.fhir_version=r4"
		})
public class MeasureEvaluateProviderIT extends RestIntegrationTest {


	@Test
	public void testMeasureEvaluate() throws Exception {
		String bundleAsText = stringFromResource( "Exm104FhirR4MeasureBundle.json");
		Bundle bundle = (Bundle)getFhirContext().newJsonParser().parseResource(bundleAsText);
		getClient().transaction().withBundle(bundle).execute();

		Parameters params = new Parameters();
		params.addParameter().setName("periodStart").setValue(new StringType("2019-01-01"));
		params.addParameter().setName("periodEnd").setValue(new StringType("2020-01-01"));
		params.addParameter().setName("reportType").setValue(new StringType("individual"));
		params.addParameter().setName("subject").setValue(new StringType("Patient/numer-EXM104"));
		params.addParameter().setName("lastReceivedOn").setValue(new StringType("2019-12-12"));

		MeasureReport  returnMeasureReport = getClient().operation().onInstance(new IdType("Measure", "measure-EXM104-8.2.000"))
			.named("$evaluate-measure")
			.withParameters(params)
			.returnResourceType(MeasureReport.class)
			.execute();

		assertNotNull(returnMeasureReport);
		//System.out.println("Resource:"+this.getFhirContext().newJsonParser().setPrettyPrint(true).encodeResourceToString(returnMeasureReport));
	}

	@Disabled("$expand not working on server")
	@Test
	public void testMeasureEvaluateWithTerminologyEndpoint() throws Exception {
		String bundleAsText = stringFromResource( "Exm104FhirR4MeasureBundle.json");
		Bundle bundle = (Bundle)getFhirContext().newJsonParser().parseResource(bundleAsText);
		getClient().transaction().withBundle(bundle).execute();

		String terminologyAsText = stringFromResource( "Endpoint.json");
		Endpoint terminologyEndpoint = (Endpoint)getFhirContext().newJsonParser().parseResource(terminologyAsText);

		Parameters params = new Parameters();
		params.addParameter().setName("periodStart").setValue(new StringType("2019-01-01"));
		params.addParameter().setName("periodEnd").setValue(new StringType("2020-01-01"));
		params.addParameter().setName("reportType").setValue(new StringType("individual"));
		params.addParameter().setName("subject").setValue(new StringType("Patient/numer-EXM104"));
		params.addParameter().setName("lastReceivedOn").setValue(new StringType("2019-12-12"));
		params.addParameter().setName("terminologyEndpoint").setResource(terminologyEndpoint);

		MeasureReport  returnMeasureReport = getClient().operation().onInstance(new IdType("Measure", "measure-EXM104-8.2.000"))
			.named("$evaluate-measure")
			.withParameters(params)
			.returnResourceType(MeasureReport.class)
			.execute();

		assertNotNull(returnMeasureReport);
	}

	@Test
	public void testMeasureEvaluateWithAdditionalData() throws Exception {
		String mainBundleAsText = stringFromResource( "Exm104FhirR4MeasurePartBundle.json");
		Bundle bundle = (Bundle)getFhirContext().newJsonParser().parseResource(mainBundleAsText);
		getClient().transaction().withBundle(bundle).execute();

		String additionalBundleAsText = stringFromResource( "Exm104FhirR4MeasureAdditionalData.json");
		Bundle additionalData = (Bundle)getFhirContext().newJsonParser().parseResource(additionalBundleAsText);


		Parameters params = new Parameters();
		params.addParameter().setName("periodStart").setValue(new StringType("2019-01-01"));
		params.addParameter().setName("periodEnd").setValue(new StringType("2020-01-01"));
		params.addParameter().setName("reportType").setValue(new StringType("subject"));
		params.addParameter().setName("subject").setValue(new StringType("Patient/numer-EXM104"));
		params.addParameter().setName("lastReceivedOn").setValue(new StringType("2019-12-12"));
		params.addParameter().setName("additionalData").setResource(additionalData);

		MeasureReport  returnMeasureReport = getClient().operation().onInstance(new IdType("Measure", "measure-EXM104-8.2.000"))
			.named("$evaluate-measure")
			.withParameters(params)
			.returnResourceType(MeasureReport.class)
			.execute();

		assertNotNull(returnMeasureReport);
	}

	private void runWithPatient(String measureId, String patientId, int initialPopulationCount, int denominatorCount, int denominatorExclusionCount, int numeratorCount, boolean enrolledDuringParticipationPeriod, String participationPeriod) {
		Parameters params = new Parameters();
		params.addParameter().setName("periodStart").setValue(new StringType("2022-01-01"));
		params.addParameter().setName("periodEnd").setValue(new StringType("2022-12-31"));
		params.addParameter().setName("reportType").setValue(new StringType("individual"));
		params.addParameter().setName("subject").setValue(new StringType(patientId));

		MeasureReport  returnMeasureReport = getClient().operation().onInstance(new IdType("Measure", measureId))
			.named("$evaluate-measure")
			.withParameters(params)
			.returnResourceType(MeasureReport.class)
			.execute();

		assertNotNull(returnMeasureReport);
		//System.out.println("Resource:"+this.getFhirContext().newJsonParser().setPrettyPrint(true).encodeResourceToString(returnMeasureReport));

		for (MeasureReport.MeasureReportGroupPopulationComponent population : returnMeasureReport.getGroupFirstRep().getPopulation()) {
			switch (population.getCode().getCodingFirstRep().getCode()) {
				case "initial-population": assertEquals(initialPopulationCount, population.getCount()); break;
				case "denominator": assertEquals(denominatorCount, population.getCount()); break;
				case "denominator-exclusion": assertEquals(denominatorExclusionCount, population.getCount()); break;
				case "numerator": assertEquals(numeratorCount, population.getCount()); break;
			}
		}

		Observation enrolledDuringParticipationPeriodObs = null;
		Observation participationPeriodObs = null;
		for (Resource r : returnMeasureReport.getContained()) {
			if (r instanceof Observation) {
				Observation o = (Observation)r;
				if (o.getCode().getText().equals("Enrolled During Participation Period")) {
					enrolledDuringParticipationPeriodObs = o;
					continue;
				}
				else if (o.getCode().getText().equals("Participation Period")) {
					participationPeriodObs = o;
					continue;
				}
			}
		}

		assertNotNull(enrolledDuringParticipationPeriodObs);
		assertTrue(enrolledDuringParticipationPeriodObs.getValueCodeableConcept().getCodingFirstRep().getCode().equals(Boolean.toString(enrolledDuringParticipationPeriod).toLowerCase()));

		assertNotNull(participationPeriodObs);
		assertTrue(participationPeriodObs.getValueCodeableConcept().getCodingFirstRep().getCode().equals(participationPeriod));
	}

	@Test
	public void testBCSEHEDISMY2022() throws Exception {
		String bundleAsText = stringFromResource( "BCSEHEDISMY2022-bundle.json");
		Bundle bundle = (Bundle)getFhirContext().newJsonParser().parseResource(bundleAsText);
		getClient().transaction().withBundle(bundle).execute();

		runWithPatient("BCSEHEDISMY2022", "Patient/Patient-5", 0, 0, 0, 0, false, "Interval[2020-10-01T00:00:00.000, 2022-12-31T23:59:59.999]");
		runWithPatient("BCSEHEDISMY2022", "Patient/Patient-7", 1, 1, 0, 0, true, "Interval[2020-10-01T00:00:00.000, 2022-12-31T23:59:59.999]");
		runWithPatient("BCSEHEDISMY2022", "Patient/Patient-9", 0, 0, 0, 0, true, "Interval[2020-10-01T00:00:00.000, 2022-12-31T23:59:59.999]");
		runWithPatient("BCSEHEDISMY2022", "Patient/Patient-21", 1, 0, 1, 0, true, "Interval[2020-10-01T00:00:00.000, 2022-12-31T23:59:59.999]");
		runWithPatient("BCSEHEDISMY2022", "Patient/Patient-23", 1, 1, 0, 0, true, "Interval[2020-10-01T00:00:00.000, 2022-12-31T23:59:59.999]");
		runWithPatient("BCSEHEDISMY2022", "Patient/Patient-65", 1, 1, 0, 1, true, "Interval[2020-10-01T00:00:00.000, 2022-12-31T23:59:59.999]");
	}

	@Test
	public void testClientNonPatientBasedMeasureEvaluate() throws Exception {
		String bundleAsText = stringFromResource( "ClientNonPatientBasedMeasureBundle.json");
		Bundle bundle = (Bundle)getFhirContext().newJsonParser().parseResource(bundleAsText);
		getClient().transaction().withBundle(bundle).execute();

		Measure measure = getClient().read().resource(Measure.class).withId("InitialInpatientPopluation").execute();
		assertNotNull(measure);

		Parameters params = new Parameters();
		params.addParameter().setName("periodStart").setValue(new StringType("2019-01-01"));
		params.addParameter().setName("periodEnd").setValue(new StringType("2020-01-01"));
		params.addParameter().setName("reportType").setValue(new StringType("subject"));
		params.addParameter().setName("subject").setValue(new StringType("Patient/97f27374-8a5c-4aa1-a26f-5a1ab03caa47"));

		MeasureReport  returnMeasureReport = getClient().operation()
			.onInstance(new IdType("Measure", "InitialInpatientPopluation"))
			.named("$evaluate-measure")
			.withParameters(params)
			.returnResourceType(MeasureReport.class)
			.execute();

		assertNotNull(returnMeasureReport);

		String populationName = "initial-population";
		int expectedCount = 2;

		Optional<MeasureReport.MeasureReportGroupPopulationComponent> population = returnMeasureReport.getGroup().get(0).
			getPopulation().stream().filter(x -> x.hasCode() && x.getCode().hasCoding() && x.getCode().getCoding().get(0).getCode().equals(populationName)).findFirst();

		assertTrue(population.isPresent(), String.format("Unable to locate a population with id \"%s\"", populationName));
		assertEquals(population.get().getCount(), expectedCount, String.format("expected count for population \"%s\" did not match", populationName));
	}
}
